package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.EnumGamemode;

public class ClientboundPlayerInfoUpdatePacket implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerInfoUpdatePacket> STREAM_CODEC = Packet.codec(ClientboundPlayerInfoUpdatePacket::write, ClientboundPlayerInfoUpdatePacket::new);
    private final EnumSet<ClientboundPlayerInfoUpdatePacket.a> actions;
    private final List<ClientboundPlayerInfoUpdatePacket.b> entries;

    public ClientboundPlayerInfoUpdatePacket(EnumSet<ClientboundPlayerInfoUpdatePacket.a> enumset, Collection<EntityPlayer> collection) {
        this.actions = enumset;
        this.entries = collection.stream().map(ClientboundPlayerInfoUpdatePacket.b::new).toList();
    }

    public ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a clientboundplayerinfoupdatepacket_a, EntityPlayer entityplayer) {
        this.actions = EnumSet.of(clientboundplayerinfoupdatepacket_a);
        this.entries = List.of(new ClientboundPlayerInfoUpdatePacket.b(entityplayer));
    }

    public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<EntityPlayer> collection) {
        EnumSet<ClientboundPlayerInfoUpdatePacket.a> enumset = EnumSet.of(ClientboundPlayerInfoUpdatePacket.a.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.a.INITIALIZE_CHAT, ClientboundPlayerInfoUpdatePacket.a.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.a.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.a.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.a.UPDATE_DISPLAY_NAME);

        return new ClientboundPlayerInfoUpdatePacket(enumset, collection);
    }

    private ClientboundPlayerInfoUpdatePacket(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.actions = registryfriendlybytebuf.readEnumSet(ClientboundPlayerInfoUpdatePacket.a.class);
        this.entries = registryfriendlybytebuf.readList((packetdataserializer) -> {
            ClientboundPlayerInfoUpdatePacket.c clientboundplayerinfoupdatepacket_c = new ClientboundPlayerInfoUpdatePacket.c(packetdataserializer.readUUID());
            Iterator iterator = this.actions.iterator();

            while (iterator.hasNext()) {
                ClientboundPlayerInfoUpdatePacket.a clientboundplayerinfoupdatepacket_a = (ClientboundPlayerInfoUpdatePacket.a) iterator.next();

                clientboundplayerinfoupdatepacket_a.reader.read(clientboundplayerinfoupdatepacket_c, (RegistryFriendlyByteBuf) packetdataserializer);
            }

            return clientboundplayerinfoupdatepacket_c.build();
        });
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeEnumSet(this.actions, ClientboundPlayerInfoUpdatePacket.a.class);
        registryfriendlybytebuf.writeCollection(this.entries, (packetdataserializer, clientboundplayerinfoupdatepacket_b) -> {
            packetdataserializer.writeUUID(clientboundplayerinfoupdatepacket_b.profileId());
            Iterator iterator = this.actions.iterator();

            while (iterator.hasNext()) {
                ClientboundPlayerInfoUpdatePacket.a clientboundplayerinfoupdatepacket_a = (ClientboundPlayerInfoUpdatePacket.a) iterator.next();

                clientboundplayerinfoupdatepacket_a.writer.write((RegistryFriendlyByteBuf) packetdataserializer, clientboundplayerinfoupdatepacket_b);
            }

        });
    }

    @Override
    public PacketType<ClientboundPlayerInfoUpdatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handlePlayerInfoUpdate(this);
    }

    public EnumSet<ClientboundPlayerInfoUpdatePacket.a> actions() {
        return this.actions;
    }

    public List<ClientboundPlayerInfoUpdatePacket.b> entries() {
        return this.entries;
    }

    public List<ClientboundPlayerInfoUpdatePacket.b> newEntries() {
        return this.actions.contains(ClientboundPlayerInfoUpdatePacket.a.ADD_PLAYER) ? this.entries : List.of();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public static record b(UUID profileId, @Nullable GameProfile profile, boolean listed, int latency, EnumGamemode gameMode, @Nullable IChatBaseComponent displayName, @Nullable RemoteChatSession.a chatSession) {

        b(EntityPlayer entityplayer) {
            this(entityplayer.getUUID(), entityplayer.getGameProfile(), true, entityplayer.connection.latency(), entityplayer.gameMode.getGameModeForPlayer(), entityplayer.getTabListDisplayName(), (RemoteChatSession.a) Optionull.map(entityplayer.getChatSession(), RemoteChatSession::asData));
        }
    }

    public static enum a {

        ADD_PLAYER((clientboundplayerinfoupdatepacket_c, registryfriendlybytebuf) -> {
            GameProfile gameprofile = new GameProfile(clientboundplayerinfoupdatepacket_c.profileId, registryfriendlybytebuf.readUtf(16));

            gameprofile.getProperties().putAll((Multimap) ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(registryfriendlybytebuf));
            clientboundplayerinfoupdatepacket_c.profile = gameprofile;
        }, (registryfriendlybytebuf, clientboundplayerinfoupdatepacket_b) -> {
            GameProfile gameprofile = (GameProfile) Objects.requireNonNull(clientboundplayerinfoupdatepacket_b.profile());

            registryfriendlybytebuf.writeUtf(gameprofile.getName(), 16);
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(registryfriendlybytebuf, gameprofile.getProperties());
        }), INITIALIZE_CHAT((clientboundplayerinfoupdatepacket_c, registryfriendlybytebuf) -> {
            clientboundplayerinfoupdatepacket_c.chatSession = (RemoteChatSession.a) registryfriendlybytebuf.readNullable(RemoteChatSession.a::read);
        }, (registryfriendlybytebuf, clientboundplayerinfoupdatepacket_b) -> {
            registryfriendlybytebuf.writeNullable(clientboundplayerinfoupdatepacket_b.chatSession, RemoteChatSession.a::write);
        }), UPDATE_GAME_MODE((clientboundplayerinfoupdatepacket_c, registryfriendlybytebuf) -> {
            clientboundplayerinfoupdatepacket_c.gameMode = EnumGamemode.byId(registryfriendlybytebuf.readVarInt());
        }, (registryfriendlybytebuf, clientboundplayerinfoupdatepacket_b) -> {
            registryfriendlybytebuf.writeVarInt(clientboundplayerinfoupdatepacket_b.gameMode().getId());
        }), UPDATE_LISTED((clientboundplayerinfoupdatepacket_c, registryfriendlybytebuf) -> {
            clientboundplayerinfoupdatepacket_c.listed = registryfriendlybytebuf.readBoolean();
        }, (registryfriendlybytebuf, clientboundplayerinfoupdatepacket_b) -> {
            registryfriendlybytebuf.writeBoolean(clientboundplayerinfoupdatepacket_b.listed());
        }), UPDATE_LATENCY((clientboundplayerinfoupdatepacket_c, registryfriendlybytebuf) -> {
            clientboundplayerinfoupdatepacket_c.latency = registryfriendlybytebuf.readVarInt();
        }, (registryfriendlybytebuf, clientboundplayerinfoupdatepacket_b) -> {
            registryfriendlybytebuf.writeVarInt(clientboundplayerinfoupdatepacket_b.latency());
        }), UPDATE_DISPLAY_NAME((clientboundplayerinfoupdatepacket_c, registryfriendlybytebuf) -> {
            clientboundplayerinfoupdatepacket_c.displayName = (IChatBaseComponent) PacketDataSerializer.readNullable(registryfriendlybytebuf, ComponentSerialization.TRUSTED_STREAM_CODEC);
        }, (registryfriendlybytebuf, clientboundplayerinfoupdatepacket_b) -> {
            PacketDataSerializer.writeNullable(registryfriendlybytebuf, clientboundplayerinfoupdatepacket_b.displayName(), ComponentSerialization.TRUSTED_STREAM_CODEC);
        });

        final ClientboundPlayerInfoUpdatePacket.a.a reader;
        final ClientboundPlayerInfoUpdatePacket.a.b writer;

        private a(final ClientboundPlayerInfoUpdatePacket.a.a clientboundplayerinfoupdatepacket_a_a, final ClientboundPlayerInfoUpdatePacket.a.b clientboundplayerinfoupdatepacket_a_b) {
            this.reader = clientboundplayerinfoupdatepacket_a_a;
            this.writer = clientboundplayerinfoupdatepacket_a_b;
        }

        public interface a {

            void read(ClientboundPlayerInfoUpdatePacket.c clientboundplayerinfoupdatepacket_c, RegistryFriendlyByteBuf registryfriendlybytebuf);
        }

        public interface b {

            void write(RegistryFriendlyByteBuf registryfriendlybytebuf, ClientboundPlayerInfoUpdatePacket.b clientboundplayerinfoupdatepacket_b);
        }
    }

    private static class c {

        final UUID profileId;
        @Nullable
        GameProfile profile;
        boolean listed;
        int latency;
        EnumGamemode gameMode;
        @Nullable
        IChatBaseComponent displayName;
        @Nullable
        RemoteChatSession.a chatSession;

        c(UUID uuid) {
            this.gameMode = EnumGamemode.DEFAULT_MODE;
            this.profileId = uuid;
        }

        ClientboundPlayerInfoUpdatePacket.b build() {
            return new ClientboundPlayerInfoUpdatePacket.b(this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession);
        }
    }
}
