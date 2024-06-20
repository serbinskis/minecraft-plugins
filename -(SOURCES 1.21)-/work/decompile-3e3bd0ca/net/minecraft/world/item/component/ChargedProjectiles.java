package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ChargedProjectiles {

    public static final ChargedProjectiles EMPTY = new ChargedProjectiles(List.of());
    public static final Codec<ChargedProjectiles> CODEC = ItemStack.CODEC.listOf().xmap(ChargedProjectiles::new, (chargedprojectiles) -> {
        return chargedprojectiles.items;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargedProjectiles> STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ChargedProjectiles::new, (chargedprojectiles) -> {
        return chargedprojectiles.items;
    });
    private final List<ItemStack> items;

    private ChargedProjectiles(List<ItemStack> list) {
        this.items = list;
    }

    public static ChargedProjectiles of(ItemStack itemstack) {
        return new ChargedProjectiles(List.of(itemstack.copy()));
    }

    public static ChargedProjectiles of(List<ItemStack> list) {
        return new ChargedProjectiles(List.copyOf(Lists.transform(list, ItemStack::copy)));
    }

    public boolean contains(Item item) {
        Iterator iterator = this.items.iterator();

        ItemStack itemstack;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            itemstack = (ItemStack) iterator.next();
        } while (!itemstack.is(item));

        return true;
    }

    public List<ItemStack> getItems() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof ChargedProjectiles) {
                ChargedProjectiles chargedprojectiles = (ChargedProjectiles) object;

                if (ItemStack.listMatches(this.items, chargedprojectiles.items)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    public String toString() {
        return "ChargedProjectiles[items=" + String.valueOf(this.items) + "]";
    }
}
