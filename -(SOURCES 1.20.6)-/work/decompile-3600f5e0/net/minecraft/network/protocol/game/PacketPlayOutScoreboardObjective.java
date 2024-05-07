package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;

public class PacketPlayOutScoreboardObjective implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutScoreboardObjective> STREAM_CODEC = Packet.codec(PacketPlayOutScoreboardObjective::write, PacketPlayOutScoreboardObjective::new);
    public static final int METHOD_ADD = 0;
    public static final int METHOD_REMOVE = 1;
    public static final int METHOD_CHANGE = 2;
    private final String objectiveName;
    private final IChatBaseComponent displayName;
    private final IScoreboardCriteria.EnumScoreboardHealthDisplay renderType;
    private final Optional<NumberFormat> numberFormat;
    private final int method;

    public PacketPlayOutScoreboardObjective(ScoreboardObjective scoreboardobjective, int i) {
        this.objectiveName = scoreboardobjective.getName();
        this.displayName = scoreboardobjective.getDisplayName();
        this.renderType = scoreboardobjective.getRenderType();
        this.numberFormat = Optional.ofNullable(scoreboardobjective.numberFormat());
        this.method = i;
    }

    private PacketPlayOutScoreboardObjective(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.objectiveName = registryfriendlybytebuf.readUtf();
        this.method = registryfriendlybytebuf.readByte();
        if (this.method != 0 && this.method != 2) {
            this.displayName = CommonComponents.EMPTY;
            this.renderType = IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER;
            this.numberFormat = Optional.empty();
        } else {
            this.displayName = (IChatBaseComponent) ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryfriendlybytebuf);
            this.renderType = (IScoreboardCriteria.EnumScoreboardHealthDisplay) registryfriendlybytebuf.readEnum(IScoreboardCriteria.EnumScoreboardHealthDisplay.class);
            this.numberFormat = (Optional) NumberFormatTypes.OPTIONAL_STREAM_CODEC.decode(registryfriendlybytebuf);
        }

    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeUtf(this.objectiveName);
        registryfriendlybytebuf.writeByte(this.method);
        if (this.method == 0 || this.method == 2) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryfriendlybytebuf, this.displayName);
            registryfriendlybytebuf.writeEnum(this.renderType);
            NumberFormatTypes.OPTIONAL_STREAM_CODEC.encode(registryfriendlybytebuf, this.numberFormat);
        }

    }

    @Override
    public PacketType<PacketPlayOutScoreboardObjective> type() {
        return GamePacketTypes.CLIENTBOUND_SET_OBJECTIVE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleAddObjective(this);
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    public int getMethod() {
        return this.method;
    }

    public IScoreboardCriteria.EnumScoreboardHealthDisplay getRenderType() {
        return this.renderType;
    }

    public Optional<NumberFormat> getNumberFormat() {
        return this.numberFormat;
    }
}
