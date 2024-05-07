package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayInEnchantItem(int containerId, int buttonId) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayInEnchantItem> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayInEnchantItem::containerId, ByteBufCodecs.VAR_INT, PacketPlayInEnchantItem::buttonId, PacketPlayInEnchantItem::new);

    @Override
    public PacketType<PacketPlayInEnchantItem> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_BUTTON_CLICK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleContainerButtonClick(this);
    }
}
