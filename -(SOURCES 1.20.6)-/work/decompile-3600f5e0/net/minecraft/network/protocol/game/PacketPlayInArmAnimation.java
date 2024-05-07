package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumHand;

public class PacketPlayInArmAnimation implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInArmAnimation> STREAM_CODEC = Packet.codec(PacketPlayInArmAnimation::write, PacketPlayInArmAnimation::new);
    private final EnumHand hand;

    public PacketPlayInArmAnimation(EnumHand enumhand) {
        this.hand = enumhand;
    }

    private PacketPlayInArmAnimation(PacketDataSerializer packetdataserializer) {
        this.hand = (EnumHand) packetdataserializer.readEnum(EnumHand.class);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.hand);
    }

    @Override
    public PacketType<PacketPlayInArmAnimation> type() {
        return GamePacketTypes.SERVERBOUND_SWING;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleAnimate(this);
    }

    public EnumHand getHand() {
        return this.hand;
    }
}
