package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.resources.MinecraftKey;

public record BrandPayload(String brand) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("brand");

    public BrandPayload(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.brand);
    }

    @Override
    public MinecraftKey id() {
        return BrandPayload.ID;
    }
}
