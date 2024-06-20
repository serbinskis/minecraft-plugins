package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public record BrandPayload(String brand) implements CustomPacketPayload {

    public static final StreamCodec<PacketDataSerializer, BrandPayload> STREAM_CODEC = CustomPacketPayload.codec(BrandPayload::write, BrandPayload::new);
    public static final CustomPacketPayload.b<BrandPayload> TYPE = CustomPacketPayload.createType("brand");

    private BrandPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.brand);
    }

    @Override
    public CustomPacketPayload.b<BrandPayload> type() {
        return BrandPayload.TYPE;
    }
}
