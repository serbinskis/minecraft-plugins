package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.ScoreboardObjective;

public class PacketPlayOutScoreboardDisplayObjective implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutScoreboardDisplayObjective> STREAM_CODEC = Packet.codec(PacketPlayOutScoreboardDisplayObjective::write, PacketPlayOutScoreboardDisplayObjective::new);
    private final DisplaySlot slot;
    private final String objectiveName;

    public PacketPlayOutScoreboardDisplayObjective(DisplaySlot displayslot, @Nullable ScoreboardObjective scoreboardobjective) {
        this.slot = displayslot;
        if (scoreboardobjective == null) {
            this.objectiveName = "";
        } else {
            this.objectiveName = scoreboardobjective.getName();
        }

    }

    private PacketPlayOutScoreboardDisplayObjective(PacketDataSerializer packetdataserializer) {
        this.slot = (DisplaySlot) packetdataserializer.readById(DisplaySlot.BY_ID);
        this.objectiveName = packetdataserializer.readUtf();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeById(DisplaySlot::id, this.slot);
        packetdataserializer.writeUtf(this.objectiveName);
    }

    @Override
    public PacketType<PacketPlayOutScoreboardDisplayObjective> type() {
        return GamePacketTypes.CLIENTBOUND_SET_DISPLAY_OBJECTIVE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetDisplayObjective(this);
    }

    public DisplaySlot getSlot() {
        return this.slot;
    }

    @Nullable
    public String getObjectiveName() {
        return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
    }
}
