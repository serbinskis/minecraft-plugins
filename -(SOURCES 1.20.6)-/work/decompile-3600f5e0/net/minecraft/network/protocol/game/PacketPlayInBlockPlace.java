package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumHand;

public class PacketPlayInBlockPlace implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInBlockPlace> STREAM_CODEC = Packet.codec(PacketPlayInBlockPlace::write, PacketPlayInBlockPlace::new);
    private final EnumHand hand;
    private final int sequence;

    public PacketPlayInBlockPlace(EnumHand enumhand, int i) {
        this.hand = enumhand;
        this.sequence = i;
    }

    private PacketPlayInBlockPlace(PacketDataSerializer packetdataserializer) {
        this.hand = (EnumHand) packetdataserializer.readEnum(EnumHand.class);
        this.sequence = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.hand);
        packetdataserializer.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<PacketPlayInBlockPlace> type() {
        return GamePacketTypes.SERVERBOUND_USE_ITEM;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleUseItem(this);
    }

    public EnumHand getHand() {
        return this.hand;
    }

    public int getSequence() {
        return this.sequence;
    }
}
