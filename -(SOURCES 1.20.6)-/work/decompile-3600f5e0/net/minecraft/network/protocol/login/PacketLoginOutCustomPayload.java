package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryPayload;
import net.minecraft.resources.MinecraftKey;

public record PacketLoginOutCustomPayload(int transactionId, CustomQueryPayload payload) implements Packet<PacketLoginOutListener> {

    public static final StreamCodec<PacketDataSerializer, PacketLoginOutCustomPayload> STREAM_CODEC = Packet.codec(PacketLoginOutCustomPayload::write, PacketLoginOutCustomPayload::new);
    private static final int MAX_PAYLOAD_SIZE = 1048576;

    private PacketLoginOutCustomPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt(), readPayload(packetdataserializer.readResourceLocation(), packetdataserializer));
    }

    private static CustomQueryPayload readPayload(MinecraftKey minecraftkey, PacketDataSerializer packetdataserializer) {
        return readUnknownPayload(minecraftkey, packetdataserializer);
    }

    private static DiscardedQueryPayload readUnknownPayload(MinecraftKey minecraftkey, PacketDataSerializer packetdataserializer) {
        int i = packetdataserializer.readableBytes();

        if (i >= 0 && i <= 1048576) {
            packetdataserializer.skipBytes(i);
            return new DiscardedQueryPayload(minecraftkey);
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.transactionId);
        packetdataserializer.writeResourceLocation(this.payload.id());
        this.payload.write(packetdataserializer);
    }

    @Override
    public PacketType<PacketLoginOutCustomPayload> type() {
        return LoginPacketTypes.CLIENTBOUND_CUSTOM_QUERY;
    }

    public void handle(PacketLoginOutListener packetloginoutlistener) {
        packetloginoutlistener.handleCustomQuery(this);
    }
}
