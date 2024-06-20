package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public record GameEventDebugPayload(ResourceKey<GameEvent> gameEventType, Vec3D pos) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, GameEventDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(GameEventDebugPayload::write, GameEventDebugPayload::new);
    public static final CustomPacketPayload.b<GameEventDebugPayload> TYPE = CustomPacketPayload.createType("debug/game_event");

    private GameEventDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readResourceKey(Registries.GAME_EVENT), packetdataserializer.readVec3());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceKey(this.gameEventType);
        packetdataserializer.writeVec3(this.pos);
    }

    @Override
    public CustomPacketPayload.b<GameEventDebugPayload> type() {
        return GameEventDebugPayload.TYPE;
    }
}
