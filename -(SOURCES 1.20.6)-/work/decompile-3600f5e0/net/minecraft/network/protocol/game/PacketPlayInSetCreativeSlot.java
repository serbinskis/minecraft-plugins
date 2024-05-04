package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public record PacketPlayInSetCreativeSlot(short slotNum, ItemStack itemStack) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayInSetCreativeSlot> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.SHORT, PacketPlayInSetCreativeSlot::slotNum, ItemStack.validatedStreamCodec(ItemStack.OPTIONAL_STREAM_CODEC), PacketPlayInSetCreativeSlot::itemStack, PacketPlayInSetCreativeSlot::new);

    public PacketPlayInSetCreativeSlot(int i, ItemStack itemstack) {
        this((short) i, itemstack);
    }

    @Override
    public PacketType<PacketPlayInSetCreativeSlot> type() {
        return GamePacketTypes.SERVERBOUND_SET_CREATIVE_MODE_SLOT;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetCreativeModeSlot(this);
    }
}
