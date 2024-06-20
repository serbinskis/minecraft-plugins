package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumHand;

public class PacketPlayOutOpenBook implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutOpenBook> STREAM_CODEC = Packet.codec(PacketPlayOutOpenBook::write, PacketPlayOutOpenBook::new);
    private final EnumHand hand;

    public PacketPlayOutOpenBook(EnumHand enumhand) {
        this.hand = enumhand;
    }

    private PacketPlayOutOpenBook(PacketDataSerializer packetdataserializer) {
        this.hand = (EnumHand) packetdataserializer.readEnum(EnumHand.class);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.hand);
    }

    @Override
    public PacketType<PacketPlayOutOpenBook> type() {
        return GamePacketTypes.CLIENTBOUND_OPEN_BOOK;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleOpenBook(this);
    }

    public EnumHand getHand() {
        return this.hand;
    }
}
