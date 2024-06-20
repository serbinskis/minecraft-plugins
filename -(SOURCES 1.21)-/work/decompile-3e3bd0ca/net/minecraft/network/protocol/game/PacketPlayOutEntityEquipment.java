package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;

public class PacketPlayOutEntityEquipment implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutEntityEquipment> STREAM_CODEC = Packet.codec(PacketPlayOutEntityEquipment::write, PacketPlayOutEntityEquipment::new);
    private static final byte CONTINUE_MASK = Byte.MIN_VALUE;
    private final int entity;
    private final List<Pair<EnumItemSlot, ItemStack>> slots;

    public PacketPlayOutEntityEquipment(int i, List<Pair<EnumItemSlot, ItemStack>> list) {
        this.entity = i;
        this.slots = list;
    }

    private PacketPlayOutEntityEquipment(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.entity = registryfriendlybytebuf.readVarInt();
        EnumItemSlot[] aenumitemslot = EnumItemSlot.values();

        this.slots = Lists.newArrayList();

        byte b0;

        do {
            b0 = registryfriendlybytebuf.readByte();
            EnumItemSlot enumitemslot = aenumitemslot[b0 & 127];
            ItemStack itemstack = (ItemStack) ItemStack.OPTIONAL_STREAM_CODEC.decode(registryfriendlybytebuf);

            this.slots.add(Pair.of(enumitemslot, itemstack));
        } while ((b0 & Byte.MIN_VALUE) != 0);

    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeVarInt(this.entity);
        int i = this.slots.size();

        for (int j = 0; j < i; ++j) {
            Pair<EnumItemSlot, ItemStack> pair = (Pair) this.slots.get(j);
            EnumItemSlot enumitemslot = (EnumItemSlot) pair.getFirst();
            boolean flag = j != i - 1;
            int k = enumitemslot.ordinal();

            registryfriendlybytebuf.writeByte(flag ? k | Byte.MIN_VALUE : k);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(registryfriendlybytebuf, (ItemStack) pair.getSecond());
        }

    }

    @Override
    public PacketType<PacketPlayOutEntityEquipment> type() {
        return GamePacketTypes.CLIENTBOUND_SET_EQUIPMENT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetEquipment(this);
    }

    public int getEntity() {
        return this.entity;
    }

    public List<Pair<EnumItemSlot, ItemStack>> getSlots() {
        return this.slots;
    }
}
