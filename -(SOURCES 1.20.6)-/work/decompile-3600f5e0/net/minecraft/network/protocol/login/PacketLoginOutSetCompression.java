package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class PacketLoginOutSetCompression implements Packet<PacketLoginOutListener> {

    public static final StreamCodec<PacketDataSerializer, PacketLoginOutSetCompression> STREAM_CODEC = Packet.codec(PacketLoginOutSetCompression::write, PacketLoginOutSetCompression::new);
    private final int compressionThreshold;

    public PacketLoginOutSetCompression(int i) {
        this.compressionThreshold = i;
    }

    private PacketLoginOutSetCompression(PacketDataSerializer packetdataserializer) {
        this.compressionThreshold = packetdataserializer.readVarInt();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.compressionThreshold);
    }

    @Override
    public PacketType<PacketLoginOutSetCompression> type() {
        return LoginPacketTypes.CLIENTBOUND_LOGIN_COMPRESSION;
    }

    public void handle(PacketLoginOutListener packetloginoutlistener) {
        packetloginoutlistener.handleCompression(this);
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}
