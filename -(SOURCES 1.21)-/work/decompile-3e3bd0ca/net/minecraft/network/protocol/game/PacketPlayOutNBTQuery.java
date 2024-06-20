package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketPlayOutNBTQuery implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutNBTQuery> STREAM_CODEC = Packet.codec(PacketPlayOutNBTQuery::write, PacketPlayOutNBTQuery::new);
    private final int transactionId;
    @Nullable
    private final NBTTagCompound tag;

    public PacketPlayOutNBTQuery(int i, @Nullable NBTTagCompound nbttagcompound) {
        this.transactionId = i;
        this.tag = nbttagcompound;
    }

    private PacketPlayOutNBTQuery(PacketDataSerializer packetdataserializer) {
        this.transactionId = packetdataserializer.readVarInt();
        this.tag = packetdataserializer.readNbt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.transactionId);
        packetdataserializer.writeNbt(this.tag);
    }

    @Override
    public PacketType<PacketPlayOutNBTQuery> type() {
        return GamePacketTypes.CLIENTBOUND_TAG_QUERY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTagQueryPacket(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    public NBTTagCompound getTag() {
        return this.tag;
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}
