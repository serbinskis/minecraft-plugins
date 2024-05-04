package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import javax.annotation.Nullable;

public class ItemStackLinkedSet {

    private static final Strategy<? super ItemStack> TYPE_AND_TAG = new Strategy<ItemStack>() {
        public int hashCode(@Nullable ItemStack itemstack) {
            return ItemStack.hashItemAndComponents(itemstack);
        }

        public boolean equals(@Nullable ItemStack itemstack, @Nullable ItemStack itemstack1) {
            return itemstack == itemstack1 || itemstack != null && itemstack1 != null && itemstack.isEmpty() == itemstack1.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, itemstack1);
        }
    };

    public ItemStackLinkedSet() {}

    public static Set<ItemStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet(ItemStackLinkedSet.TYPE_AND_TAG);
    }
}
