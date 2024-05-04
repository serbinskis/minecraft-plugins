package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutPlayerListHeaderFooter> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_STREAM_CODEC, PacketPlayOutPlayerListHeaderFooter::header, ComponentSerialization.TRUSTED_STREAM_CODEC, PacketPlayOutPlayerListHeaderFooter::footer, PacketPlayOutPlayerListHeaderFooter::new);

    @Override
    public PacketType<PacketPlayOutPlayerListHeaderFooter> type() {
        return GamePacketTypes.CLIENTBOUND_TAB_LIST;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTabListCustomisation(this);
    }
}
