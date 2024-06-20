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
    private final float yRot;
    private final float xRot;

    public PacketPlayInBlockPlace(EnumHand enumhand, int i, float f, float f1) {
        this.hand = enumhand;
        this.sequence = i;
        this.yRot = f;
        this.xRot = f1;
    }

    private PacketPlayInBlockPlace(PacketDataSerializer packetdataserializer) {
        this.hand = (EnumHand) packetdataserializer.readEnum(EnumHand.class);
        this.sequence = packetdataserializer.readVarInt();
        this.yRot = packetdataserializer.readFloat();
        this.xRot = packetdataserializer.readFloat();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.hand);
        packetdataserializer.writeVarInt(this.sequence);
        packetdataserializer.writeFloat(this.yRot);
        packetdataserializer.writeFloat(this.xRot);
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

    public float getYRot() {
        return this.yRot;
    }

    public float getXRot() {
        return this.xRot;
    }
}
