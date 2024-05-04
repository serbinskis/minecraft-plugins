package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;

public class PacketPlayInWindowClick implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayInWindowClick> STREAM_CODEC = Packet.codec(PacketPlayInWindowClick::write, PacketPlayInWindowClick::new);
    private static final int MAX_SLOT_COUNT = 128;
    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<ItemStack>> SLOTS_STREAM_CODEC = ByteBufCodecs.map(Int2ObjectOpenHashMap::new, ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue), ItemStack.OPTIONAL_STREAM_CODEC, 128);
    private final int containerId;
    private final int stateId;
    private final int slotNum;
    private final int buttonNum;
    private final InventoryClickType clickType;
    private final ItemStack carriedItem;
    private final Int2ObjectMap<ItemStack> changedSlots;

    public PacketPlayInWindowClick(int i, int j, int k, int l, InventoryClickType inventoryclicktype, ItemStack itemstack, Int2ObjectMap<ItemStack> int2objectmap) {
        this.containerId = i;
        this.stateId = j;
        this.slotNum = k;
        this.buttonNum = l;
        this.clickType = inventoryclicktype;
        this.carriedItem = itemstack;
        this.changedSlots = Int2ObjectMaps.unmodifiable(int2objectmap);
    }

    private PacketPlayInWindowClick(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.containerId = registryfriendlybytebuf.readByte();
        this.stateId = registryfriendlybytebuf.readVarInt();
        this.slotNum = registryfriendlybytebuf.readShort();
        this.buttonNum = registryfriendlybytebuf.readByte();
        this.clickType = (InventoryClickType) registryfriendlybytebuf.readEnum(InventoryClickType.class);
        this.changedSlots = Int2ObjectMaps.unmodifiable((Int2ObjectMap) PacketPlayInWindowClick.SLOTS_STREAM_CODEC.decode(registryfriendlybytebuf));
        this.carriedItem = (ItemStack) ItemStack.OPTIONAL_STREAM_CODEC.decode(registryfriendlybytebuf);
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeByte(this.containerId);
        registryfriendlybytebuf.writeVarInt(this.stateId);
        registryfriendlybytebuf.writeShort(this.slotNum);
        registryfriendlybytebuf.writeByte(this.buttonNum);
        registryfriendlybytebuf.writeEnum(this.clickType);
        PacketPlayInWindowClick.SLOTS_STREAM_CODEC.encode(registryfriendlybytebuf, this.changedSlots);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryfriendlybytebuf, this.carriedItem);
    }

    @Override
    public PacketType<PacketPlayInWindowClick> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_CLICK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleContainerClick(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public int getButtonNum() {
        return this.buttonNum;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public Int2ObjectMap<ItemStack> getChangedSlots() {
        return this.changedSlots;
    }

    public InventoryClickType getClickType() {
        return this.clickType;
    }

    public int getStateId() {
        return this.stateId;
    }
}
