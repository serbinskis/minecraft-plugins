package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

public record GameEventListenerDebugPayload(PositionSource listenerPos, int listenerRange) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/game_event_listeners");

    public GameEventListenerDebugPayload(PacketDataSerializer packetdataserializer) {
        this(PositionSourceType.fromNetwork(packetdataserializer), packetdataserializer.readVarInt());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        PositionSourceType.toNetwork(this.listenerPos, packetdataserializer);
        packetdataserializer.writeVarInt(this.listenerRange);
    }

    @Override
    public MinecraftKey id() {
        return GameEventListenerDebugPayload.ID;
    }
}
