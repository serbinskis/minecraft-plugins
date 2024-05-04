package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.PacketLoginInEncryptionBegin;
import net.minecraft.network.protocol.login.PacketLoginInListener;
import net.minecraft.network.protocol.login.PacketLoginInStart;
import net.minecraft.network.protocol.login.PacketLoginOutDisconnect;
import net.minecraft.network.protocol.login.PacketLoginOutEncryptionBegin;
import net.minecraft.network.protocol.login.PacketLoginOutSetCompression;
import net.minecraft.network.protocol.login.PacketLoginOutSuccess;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.util.RandomSource;
import net.minecraft.util.UtilColor;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class LoginListener implements PacketLoginInListener, TickablePacketListener {

    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TICKS_BEFORE_LOGIN = 600;
    private final byte[] challenge;
    final MinecraftServer server;
    public final NetworkManager connection;
    private volatile LoginListener.EnumProtocolState state;
    private int tick;
    @Nullable
    String requestedUsername;
    @Nullable
    private GameProfile authenticatedProfile;
    private final String serverId;
    private final boolean transferred;

    public LoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager, boolean flag) {
        this.state = LoginListener.EnumProtocolState.HELLO;
        this.serverId = "";
        this.server = minecraftserver;
        this.connection = networkmanager;
        this.challenge = Ints.toByteArray(RandomSource.create().nextInt());
        this.transferred = flag;
    }

    @Override
    public void tick() {
        if (this.state == LoginListener.EnumProtocolState.VERIFYING) {
            this.verifyLoginAndFinishConnectionSetup((GameProfile) Objects.requireNonNull(this.authenticatedProfile));
        }

        if (this.state == LoginListener.EnumProtocolState.WAITING_FOR_DUPE_DISCONNECT && !this.isPlayerAlreadyInWorld((GameProfile) Objects.requireNonNull(this.authenticatedProfile))) {
            this.finishLoginAndWaitForClient(this.authenticatedProfile);
        }

        if (this.tick++ == 600) {
            this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.slow_login"));
        }

    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void disconnect(IChatBaseComponent ichatbasecomponent) {
        try {
            LoginListener.LOGGER.info("Disconnecting {}: {}", this.getUserName(), ichatbasecomponent.getString());
            this.connection.send(new PacketLoginOutDisconnect(ichatbasecomponent));
            this.connection.disconnect(ichatbasecomponent);
        } catch (Exception exception) {
            LoginListener.LOGGER.error("Error whilst disconnecting player", exception);
        }

    }

    private boolean isPlayerAlreadyInWorld(GameProfile gameprofile) {
        return this.server.getPlayerList().getPlayer(gameprofile.getId()) != null;
    }

    @Override
    public void onDisconnect(IChatBaseComponent ichatbasecomponent) {
        LoginListener.LOGGER.info("{} lost connection: {}", this.getUserName(), ichatbasecomponent.getString());
    }

    public String getUserName() {
        String s = this.connection.getLoggableAddress(this.server.logIPs());

        return this.requestedUsername != null ? this.requestedUsername + " (" + s + ")" : s;
    }

    @Override
    public void handleHello(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.state == LoginListener.EnumProtocolState.HELLO, "Unexpected hello packet", new Object[0]);
        Validate.validState(UtilColor.isValidPlayerName(packetlogininstart.name()), "Invalid characters in username", new Object[0]);
        this.requestedUsername = packetlogininstart.name();
        GameProfile gameprofile = this.server.getSingleplayerProfile();

        if (gameprofile != null && this.requestedUsername.equalsIgnoreCase(gameprofile.getName())) {
            this.startClientVerification(gameprofile);
        } else {
            if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
                this.state = LoginListener.EnumProtocolState.KEY;
                this.connection.send(new PacketLoginOutEncryptionBegin("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge, true));
            } else {
                this.startClientVerification(UUIDUtil.createOfflineProfile(this.requestedUsername));
            }

        }
    }

    void startClientVerification(GameProfile gameprofile) {
        this.authenticatedProfile = gameprofile;
        this.state = LoginListener.EnumProtocolState.VERIFYING;
    }

    private void verifyLoginAndFinishConnectionSetup(GameProfile gameprofile) {
        PlayerList playerlist = this.server.getPlayerList();
        IChatBaseComponent ichatbasecomponent = playerlist.canPlayerLogin(this.connection.getRemoteAddress(), gameprofile);

        if (ichatbasecomponent != null) {
            this.disconnect(ichatbasecomponent);
        } else {
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection.send(new PacketLoginOutSetCompression(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> {
                    this.connection.setupCompression(this.server.getCompressionThreshold(), true);
                }));
            }

            boolean flag = playerlist.disconnectAllPlayersWithProfile(gameprofile);

            if (flag) {
                this.state = LoginListener.EnumProtocolState.WAITING_FOR_DUPE_DISCONNECT;
            } else {
                this.finishLoginAndWaitForClient(gameprofile);
            }
        }

    }

    private void finishLoginAndWaitForClient(GameProfile gameprofile) {
        this.state = LoginListener.EnumProtocolState.PROTOCOL_SWITCHING;
        this.connection.send(new PacketLoginOutSuccess(gameprofile, true));
    }

    @Override
    public void handleKey(PacketLoginInEncryptionBegin packetlogininencryptionbegin) {
        Validate.validState(this.state == LoginListener.EnumProtocolState.KEY, "Unexpected key packet", new Object[0]);

        final String s;

        try {
            PrivateKey privatekey = this.server.getKeyPair().getPrivate();

            if (!packetlogininencryptionbegin.isChallengeValid(this.challenge, privatekey)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretkey = packetlogininencryptionbegin.getSecretKey(privatekey);
            Cipher cipher = MinecraftEncryption.getCipher(2, secretkey);
            Cipher cipher1 = MinecraftEncryption.getCipher(1, secretkey);

            s = (new BigInteger(MinecraftEncryption.digestData("", this.server.getKeyPair().getPublic(), secretkey))).toString(16);
            this.state = LoginListener.EnumProtocolState.AUTHENTICATING;
            this.connection.setEncryptionKey(cipher, cipher1);
        } catch (CryptographyException cryptographyexception) {
            throw new IllegalStateException("Protocol error", cryptographyexception);
        }

        Thread thread = new Thread("User Authenticator #" + LoginListener.UNIQUE_THREAD_ID.incrementAndGet()) {
            public void run() {
                String s1 = (String) Objects.requireNonNull(LoginListener.this.requestedUsername, "Player name not initialized");

                try {
                    ProfileResult profileresult = LoginListener.this.server.getSessionService().hasJoinedServer(s1, s, this.getAddress());

                    if (profileresult != null) {
                        GameProfile gameprofile = profileresult.profile();

                        LoginListener.LOGGER.info("UUID of player {} is {}", gameprofile.getName(), gameprofile.getId());
                        LoginListener.this.startClientVerification(gameprofile);
                    } else if (LoginListener.this.server.isSingleplayer()) {
                        LoginListener.LOGGER.warn("Failed to verify username but will let them in anyway!");
                        LoginListener.this.startClientVerification(UUIDUtil.createOfflineProfile(s1));
                    } else {
                        LoginListener.this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.unverified_username"));
                        LoginListener.LOGGER.error("Username '{}' tried to join with an invalid session", s1);
                    }
                } catch (AuthenticationUnavailableException authenticationunavailableexception) {
                    if (LoginListener.this.server.isSingleplayer()) {
                        LoginListener.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        LoginListener.this.startClientVerification(UUIDUtil.createOfflineProfile(s1));
                    } else {
                        LoginListener.this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.authservers_down"));
                        LoginListener.LOGGER.error("Couldn't verify username because servers are unavailable");
                    }
                }

            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketaddress = LoginListener.this.connection.getRemoteAddress();

                return LoginListener.this.server.getPreventProxyConnections() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress() : null;
            }
        };

        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LoginListener.LOGGER));
        thread.start();
    }

    @Override
    public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket serverboundcustomqueryanswerpacket) {
        this.disconnect(ServerCommonPacketListenerImpl.DISCONNECT_UNEXPECTED_QUERY);
    }

    @Override
    public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket serverboundloginacknowledgedpacket) {
        Validate.validState(this.state == LoginListener.EnumProtocolState.PROTOCOL_SWITCHING, "Unexpected login acknowledgement packet", new Object[0]);
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
        CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial((GameProfile) Objects.requireNonNull(this.authenticatedProfile), this.transferred);
        ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl = new ServerConfigurationPacketListenerImpl(this.server, this.connection, commonlistenercookie);

        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, serverconfigurationpacketlistenerimpl);
        serverconfigurationpacketlistenerimpl.startConfiguration();
        this.state = LoginListener.EnumProtocolState.ACCEPTED;
    }

    @Override
    public void fillListenerSpecificCrashDetails(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.setDetail("Login phase", () -> {
            return this.state.toString();
        });
    }

    @Override
    public void handleCookieResponse(ServerboundCookieResponsePacket serverboundcookieresponsepacket) {
        this.disconnect(ServerCommonPacketListenerImpl.DISCONNECT_UNEXPECTED_QUERY);
    }

    private static enum EnumProtocolState {

        HELLO, KEY, AUTHENTICATING, NEGOTIATING, VERIFYING, WAITING_FOR_DUPE_DISCONNECT, PROTOCOL_SWITCHING, ACCEPTED;

        private EnumProtocolState() {}
    }
}
