// mc-dev import
package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumHand;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class PacketPlayInUseItem implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInUseItem> STREAM_CODEC = Packet.codec(PacketPlayInUseItem::write, PacketPlayInUseItem::new);
    private final MovingObjectPositionBlock blockHit;
    private final EnumHand hand;
    private final int sequence;

    public PacketPlayInUseItem(EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock, int i) {
        this.hand = enumhand;
        this.blockHit = movingobjectpositionblock;
        this.sequence = i;
    }

    private PacketPlayInUseItem(PacketDataSerializer packetdataserializer) {
        this.hand = (EnumHand) packetdataserializer.readEnum(EnumHand.class);
        this.blockHit = packetdataserializer.readBlockHitResult();
        this.sequence = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeEnum(this.hand);
        packetdataserializer.writeBlockHitResult(this.blockHit);
        packetdataserializer.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<PacketPlayInUseItem> type() {
        return GamePacketTypes.SERVERBOUND_USE_ITEM_ON;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleUseItemOn(this);
    }

    public EnumHand getHand() {
        return this.hand;
    }

    public MovingObjectPositionBlock getHitResult() {
        return this.blockHit;
    }

    public int getSequence() {
        return this.sequence;
    }
}
