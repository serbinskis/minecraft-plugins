package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public class PacketPlayOutWindowItems implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutWindowItems> STREAM_CODEC = Packet.codec(PacketPlayOutWindowItems::write, PacketPlayOutWindowItems::new);
    private final int containerId;
    private final int stateId;
    private final List<ItemStack> items;
    private final ItemStack carriedItem;

    public PacketPlayOutWindowItems(int i, int j, NonNullList<ItemStack> nonnulllist, ItemStack itemstack) {
        this.containerId = i;
        this.stateId = j;
        this.items = NonNullList.withSize(nonnulllist.size(), ItemStack.EMPTY);

        for (int k = 0; k < nonnulllist.size(); ++k) {
            this.items.set(k, ((ItemStack) nonnulllist.get(k)).copy());
        }

        this.carriedItem = itemstack.copy();
    }

    private PacketPlayOutWindowItems(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.containerId = registryfriendlybytebuf.readUnsignedByte();
        this.stateId = registryfriendlybytebuf.readVarInt();
        this.items = (List) ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(registryfriendlybytebuf);
        this.carriedItem = (ItemStack) ItemStack.OPTIONAL_STREAM_CODEC.decode(registryfriendlybytebuf);
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeByte(this.containerId);
        registryfriendlybytebuf.writeVarInt(this.stateId);
        ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(registryfriendlybytebuf, this.items);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryfriendlybytebuf, this.carriedItem);
    }

    @Override
    public PacketType<PacketPlayOutWindowItems> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_CONTENT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleContainerContent(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public int getStateId() {
        return this.stateId;
    }
}
