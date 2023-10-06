package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public record GameEventDebugPayload(ResourceKey<GameEvent> type, Vec3D pos) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/game_event");

    public GameEventDebugPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readResourceKey(Registries.GAME_EVENT), packetdataserializer.readVec3());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceKey(this.type);
        packetdataserializer.writeVec3(this.pos);
    }

    @Override
    public MinecraftKey id() {
        return GameEventDebugPayload.ID;
    }
}
