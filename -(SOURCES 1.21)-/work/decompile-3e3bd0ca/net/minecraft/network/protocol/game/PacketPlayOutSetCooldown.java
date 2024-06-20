package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.Item;

public record PacketPlayOutSetCooldown(Item item, int duration) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutSetCooldown> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.registry(Registries.ITEM), PacketPlayOutSetCooldown::item, ByteBufCodecs.VAR_INT, PacketPlayOutSetCooldown::duration, PacketPlayOutSetCooldown::new);

    @Override
    public PacketType<PacketPlayOutSetCooldown> type() {
        return GamePacketTypes.CLIENTBOUND_COOLDOWN;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleItemCooldown(this);
    }
}
