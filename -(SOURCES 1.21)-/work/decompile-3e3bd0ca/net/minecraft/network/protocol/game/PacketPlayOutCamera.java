package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.World;

public class PacketPlayOutCamera implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutCamera> STREAM_CODEC = Packet.codec(PacketPlayOutCamera::write, PacketPlayOutCamera::new);
    private final int cameraId;

    public PacketPlayOutCamera(Entity entity) {
        this.cameraId = entity.getId();
    }

    private PacketPlayOutCamera(PacketDataSerializer packetdataserializer) {
        this.cameraId = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.cameraId);
    }

    @Override
    public PacketType<PacketPlayOutCamera> type() {
        return GamePacketTypes.CLIENTBOUND_SET_CAMERA;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetCamera(this);
    }

    @Nullable
    public Entity getEntity(World world) {
        return world.getEntity(this.cameraId);
    }
}
