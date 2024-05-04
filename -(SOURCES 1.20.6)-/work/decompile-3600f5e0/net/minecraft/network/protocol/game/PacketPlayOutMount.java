package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class PacketPlayOutMount implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutMount> STREAM_CODEC = Packet.codec(PacketPlayOutMount::write, PacketPlayOutMount::new);
    private final int vehicle;
    private final int[] passengers;

    public PacketPlayOutMount(Entity entity) {
        this.vehicle = entity.getId();
        List<Entity> list = entity.getPassengers();

        this.passengers = new int[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            this.passengers[i] = ((Entity) list.get(i)).getId();
        }

    }

    private PacketPlayOutMount(PacketDataSerializer packetdataserializer) {
        this.vehicle = packetdataserializer.readVarInt();
        this.passengers = packetdataserializer.readVarIntArray();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.vehicle);
        packetdataserializer.writeVarIntArray(this.passengers);
    }

    @Override
    public PacketType<PacketPlayOutMount> type() {
        return GamePacketTypes.CLIENTBOUND_SET_PASSENGERS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetEntityPassengersPacket(this);
    }

    public int[] getPassengers() {
        return this.passengers;
    }

    public int getVehicle() {
        return this.vehicle;
    }
}
