package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundProjectilePowerPacket implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundProjectilePowerPacket> STREAM_CODEC = Packet.codec(ClientboundProjectilePowerPacket::write, ClientboundProjectilePowerPacket::new);
    private final int id;
    private final double xPower;
    private final double yPower;
    private final double zPower;

    public ClientboundProjectilePowerPacket(int i, double d0, double d1, double d2) {
        this.id = i;
        this.xPower = d0;
        this.yPower = d1;
        this.zPower = d2;
    }

    private ClientboundProjectilePowerPacket(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readVarInt();
        this.xPower = packetdataserializer.readDouble();
        this.yPower = packetdataserializer.readDouble();
        this.zPower = packetdataserializer.readDouble();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.id);
        packetdataserializer.writeDouble(this.xPower);
        packetdataserializer.writeDouble(this.yPower);
        packetdataserializer.writeDouble(this.zPower);
    }

    @Override
    public PacketType<ClientboundProjectilePowerPacket> type() {
        return GamePacketTypes.CLIENTBOUND_PROJECTILE_POWER;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleProjectilePowerPacket(this);
    }

    public int getId() {
        return this.id;
    }

    public double getXPower() {
        return this.xPower;
    }

    public double getYPower() {
        return this.yPower;
    }

    public double getZPower() {
        return this.zPower;
    }
}
