package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public class PacketPlayOutSetSlot implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutSetSlot> STREAM_CODEC = Packet.codec(PacketPlayOutSetSlot::write, PacketPlayOutSetSlot::new);
    public static final int CARRIED_ITEM = -1;
    public static final int PLAYER_INVENTORY = -2;
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public PacketPlayOutSetSlot(int i, int j, int k, ItemStack itemstack) {
        this.containerId = i;
        this.stateId = j;
        this.slot = k;
        this.itemStack = itemstack.copy();
    }

    private PacketPlayOutSetSlot(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.containerId = registryfriendlybytebuf.readByte();
        this.stateId = registryfriendlybytebuf.readVarInt();
        this.slot = registryfriendlybytebuf.readShort();
        this.itemStack = (ItemStack) ItemStack.OPTIONAL_STREAM_CODEC.decode(registryfriendlybytebuf);
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeByte(this.containerId);
        registryfriendlybytebuf.writeVarInt(this.stateId);
        registryfriendlybytebuf.writeShort(this.slot);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryfriendlybytebuf, this.itemStack);
    }

    @Override
    public PacketType<PacketPlayOutSetSlot> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_SLOT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleContainerSetSlot(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public int getStateId() {
        return this.stateId;
    }
}
