package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record GoalDebugPayload(int entityId, BlockPosition pos, List<GoalDebugPayload.a> goals) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, GoalDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(GoalDebugPayload::write, GoalDebugPayload::new);
    public static final CustomPacketPayload.b<GoalDebugPayload> TYPE = CustomPacketPayload.createType("debug/goal_selector");

    private GoalDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readInt(), packetdataserializer.readBlockPos(), packetdataserializer.readList(GoalDebugPayload.a::new));
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.entityId);
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeCollection(this.goals, (packetdataserializer1, goaldebugpayload_a) -> {
            goaldebugpayload_a.write(packetdataserializer1);
        });
    }

    @Override
    public CustomPacketPayload.b<GoalDebugPayload> type() {
        return GoalDebugPayload.TYPE;
    }

    public static record a(int priority, boolean isRunning, String name) {

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readInt(), packetdataserializer.readBoolean(), packetdataserializer.readUtf(255));
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeInt(this.priority);
            packetdataserializer.writeBoolean(this.isRunning);
            packetdataserializer.writeUtf(this.name);
        }
    }
}
