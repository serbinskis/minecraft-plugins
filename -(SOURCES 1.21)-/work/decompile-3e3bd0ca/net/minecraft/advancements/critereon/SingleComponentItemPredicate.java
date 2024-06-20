package net.minecraft.advancements.critereon;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface SingleComponentItemPredicate<T> extends ItemSubPredicate {

    @Override
    default boolean matches(ItemStack itemstack) {
        T t0 = itemstack.get(this.componentType());

        return t0 != null && this.matches(itemstack, t0);
    }

    DataComponentType<T> componentType();

    boolean matches(ItemStack itemstack, T t0);
}
