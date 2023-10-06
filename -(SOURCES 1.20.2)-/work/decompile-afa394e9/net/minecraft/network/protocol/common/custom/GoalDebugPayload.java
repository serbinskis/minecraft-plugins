package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record GoalDebugPayload(int entityId, BlockPosition pos, List<GoalDebugPayload.a> goals) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/goal_selector");

    public GoalDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readInt(), packetdataserializer.readBlockPos(), packetdataserializer.readList(GoalDebugPayload.a::new));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeInt(this.entityId);
        packetdataserializer.writeBlockPos(this.pos);
        packetdataserializer.writeCollection(this.goals, (packetdataserializer1, goaldebugpayload_a) -> {
            goaldebugpayload_a.write(packetdataserializer1);
        });
    }

    @Override
    public MinecraftKey id() {
        return GoalDebugPayload.ID;
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
