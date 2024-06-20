package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.inventory.Containers;

public class PacketPlayOutOpenWindow implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutOpenWindow> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayOutOpenWindow::getContainerId, ByteBufCodecs.registry(Registries.MENU), PacketPlayOutOpenWindow::getType, ComponentSerialization.TRUSTED_STREAM_CODEC, PacketPlayOutOpenWindow::getTitle, PacketPlayOutOpenWindow::new);
    private final int containerId;
    private final Containers<?> type;
    private final IChatBaseComponent title;

    public PacketPlayOutOpenWindow(int i, Containers<?> containers, IChatBaseComponent ichatbasecomponent) {
        this.containerId = i;
        this.type = containers;
        this.title = ichatbasecomponent;
    }

    @Override
    public PacketType<PacketPlayOutOpenWindow> type() {
        return GamePacketTypes.CLIENTBOUND_OPEN_SCREEN;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleOpenScreen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public Containers<?> getType() {
        return this.type;
    }

    public IChatBaseComponent getTitle() {
        return this.title;
    }
}
