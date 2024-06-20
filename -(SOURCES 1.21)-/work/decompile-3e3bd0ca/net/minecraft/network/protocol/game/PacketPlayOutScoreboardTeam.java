package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.scores.ScoreboardTeam;

public class PacketPlayOutScoreboardTeam implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutScoreboardTeam> STREAM_CODEC = Packet.codec(PacketPlayOutScoreboardTeam::write, PacketPlayOutScoreboardTeam::new);
    private static final int METHOD_ADD = 0;
    private static final int METHOD_REMOVE = 1;
    private static final int METHOD_CHANGE = 2;
    private static final int METHOD_JOIN = 3;
    private static final int METHOD_LEAVE = 4;
    private static final int MAX_VISIBILITY_LENGTH = 40;
    private static final int MAX_COLLISION_LENGTH = 40;
    private final int method;
    private final String name;
    private final Collection<String> players;
    private final Optional<PacketPlayOutScoreboardTeam.b> parameters;

    private PacketPlayOutScoreboardTeam(String s, int i, Optional<PacketPlayOutScoreboardTeam.b> optional, Collection<String> collection) {
        this.name = s;
        this.method = i;
        this.parameters = optional;
        this.players = ImmutableList.copyOf(collection);
    }

    public static PacketPlayOutScoreboardTeam createAddOrModifyPacket(ScoreboardTeam scoreboardteam, boolean flag) {
        return new PacketPlayOutScoreboardTeam(scoreboardteam.getName(), flag ? 0 : 2, Optional.of(new PacketPlayOutScoreboardTeam.b(scoreboardteam)), (Collection) (flag ? scoreboardteam.getPlayers() : ImmutableList.of()));
    }

    public static PacketPlayOutScoreboardTeam createRemovePacket(ScoreboardTeam scoreboardteam) {
        return new PacketPlayOutScoreboardTeam(scoreboardteam.getName(), 1, Optional.empty(), ImmutableList.of());
    }

    public static PacketPlayOutScoreboardTeam createPlayerPacket(ScoreboardTeam scoreboardteam, String s, PacketPlayOutScoreboardTeam.a packetplayoutscoreboardteam_a) {
        return new PacketPlayOutScoreboardTeam(scoreboardteam.getName(), packetplayoutscoreboardteam_a == PacketPlayOutScoreboardTeam.a.ADD ? 3 : 4, Optional.empty(), ImmutableList.of(s));
    }

    private PacketPlayOutScoreboardTeam(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.name = registryfriendlybytebuf.readUtf();
        this.method = registryfriendlybytebuf.readByte();
        if (shouldHaveParameters(this.method)) {
            this.parameters = Optional.of(new PacketPlayOutScoreboardTeam.b(registryfriendlybytebuf));
        } else {
            this.parameters = Optional.empty();
        }

        if (shouldHavePlayerList(this.method)) {
            this.players = registryfriendlybytebuf.readList(PacketDataSerializer::readUtf);
        } else {
            this.players = ImmutableList.of();
        }

    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeUtf(this.name);
        registryfriendlybytebuf.writeByte(this.method);
        if (shouldHaveParameters(this.method)) {
            ((PacketPlayOutScoreboardTeam.b) this.parameters.orElseThrow(() -> {
                return new IllegalStateException("Parameters not present, but method is" + this.method);
            })).write(registryfriendlybytebuf);
        }

        if (shouldHavePlayerList(this.method)) {
            registryfriendlybytebuf.writeCollection(this.players, PacketDataSerializer::writeUtf);
        }

    }

    private static boolean shouldHavePlayerList(int i) {
        return i == 0 || i == 3 || i == 4;
    }

    private static boolean shouldHaveParameters(int i) {
        return i == 0 || i == 2;
    }

    @Nullable
    public PacketPlayOutScoreboardTeam.a getPlayerAction() {
        PacketPlayOutScoreboardTeam.a packetplayoutscoreboardteam_a;

        switch (this.method) {
            case 0:
            case 3:
                packetplayoutscoreboardteam_a = PacketPlayOutScoreboardTeam.a.ADD;
                break;
            case 1:
            case 2:
            default:
                packetplayoutscoreboardteam_a = null;
                break;
            case 4:
                packetplayoutscoreboardteam_a = PacketPlayOutScoreboardTeam.a.REMOVE;
        }

        return packetplayoutscoreboardteam_a;
    }

    @Nullable
    public PacketPlayOutScoreboardTeam.a getTeamAction() {
        PacketPlayOutScoreboardTeam.a packetplayoutscoreboardteam_a;

        switch (this.method) {
            case 0:
                packetplayoutscoreboardteam_a = PacketPlayOutScoreboardTeam.a.ADD;
                break;
            case 1:
                packetplayoutscoreboardteam_a = PacketPlayOutScoreboardTeam.a.REMOVE;
                break;
            default:
                packetplayoutscoreboardteam_a = null;
        }

        return packetplayoutscoreboardteam_a;
    }

    @Override
    public PacketType<PacketPlayOutScoreboardTeam> type() {
        return GamePacketTypes.CLIENTBOUND_SET_PLAYER_TEAM;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetPlayerTeamPacket(this);
    }

    public String getName() {
        return this.name;
    }

    public Collection<String> getPlayers() {
        return this.players;
    }

    public Optional<PacketPlayOutScoreboardTeam.b> getParameters() {
        return this.parameters;
    }

    public static class b {

        private final IChatBaseComponent displayName;
        private final IChatBaseComponent playerPrefix;
        private final IChatBaseComponent playerSuffix;
        private final String nametagVisibility;
        private final String collisionRule;
        private final EnumChatFormat color;
        private final int options;

        public b(ScoreboardTeam scoreboardteam) {
            this.displayName = scoreboardteam.getDisplayName();
            this.options = scoreboardteam.packOptions();
            this.nametagVisibility = scoreboardteam.getNameTagVisibility().name;
            this.collisionRule = scoreboardteam.getCollisionRule().name;
            this.color = scoreboardteam.getColor();
            this.playerPrefix = scoreboardteam.getPlayerPrefix();
            this.playerSuffix = scoreboardteam.getPlayerSuffix();
        }

        public b(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            this.displayName = (IChatBaseComponent) ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryfriendlybytebuf);
            this.options = registryfriendlybytebuf.readByte();
            this.nametagVisibility = registryfriendlybytebuf.readUtf(40);
            this.collisionRule = registryfriendlybytebuf.readUtf(40);
            this.color = (EnumChatFormat) registryfriendlybytebuf.readEnum(EnumChatFormat.class);
            this.playerPrefix = (IChatBaseComponent) ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryfriendlybytebuf);
            this.playerSuffix = (IChatBaseComponent) ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryfriendlybytebuf);
        }

        public IChatBaseComponent getDisplayName() {
            return this.displayName;
        }

        public int getOptions() {
            return this.options;
        }

        public EnumChatFormat getColor() {
            return this.color;
        }

        public String getNametagVisibility() {
            return this.nametagVisibility;
        }

        public String getCollisionRule() {
            return this.collisionRule;
        }

        public IChatBaseComponent getPlayerPrefix() {
            return this.playerPrefix;
        }

        public IChatBaseComponent getPlayerSuffix() {
            return this.playerSuffix;
        }

        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryfriendlybytebuf, this.displayName);
            registryfriendlybytebuf.writeByte(this.options);
            registryfriendlybytebuf.writeUtf(this.nametagVisibility);
            registryfriendlybytebuf.writeUtf(this.collisionRule);
            registryfriendlybytebuf.writeEnum(this.color);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryfriendlybytebuf, this.playerPrefix);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryfriendlybytebuf, this.playerSuffix);
        }
    }

    public static enum a {

        ADD, REMOVE;

        private a() {}
    }
}
