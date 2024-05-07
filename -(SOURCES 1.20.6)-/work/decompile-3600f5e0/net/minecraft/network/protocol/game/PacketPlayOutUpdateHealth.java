package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutUpdateHealth implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutUpdateHealth> STREAM_CODEC = Packet.codec(PacketPlayOutUpdateHealth::write, PacketPlayOutUpdateHealth::new);
    private final float health;
    private final int food;
    private final float saturation;

    public PacketPlayOutUpdateHealth(float f, int i, float f1) {
        this.health = f;
        this.food = i;
        this.saturation = f1;
    }

    private PacketPlayOutUpdateHealth(PacketDataSerializer packetdataserializer) {
        this.health = packetdataserializer.readFloat();
        this.food = packetdataserializer.readVarInt();
        this.saturation = packetdataserializer.readFloat();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeFloat(this.health);
        packetdataserializer.writeVarInt(this.food);
        packetdataserializer.writeFloat(this.saturation);
    }

    @Override
    public PacketType<PacketPlayOutUpdateHealth> type() {
        return GamePacketTypes.CLIENTBOUND_SET_HEALTH;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetHealth(this);
    }

    public float getHealth() {
        return this.health;
    }

    public int getFood() {
        return this.food;
    }

    public float getSaturation() {
        return this.saturation;
    }
}
