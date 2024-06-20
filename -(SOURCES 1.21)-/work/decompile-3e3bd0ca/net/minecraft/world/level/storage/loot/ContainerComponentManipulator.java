package net.minecraft.world.level.storage.loot;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface ContainerComponentManipulator<T> {

    DataComponentType<T> type();

    T empty();

    T setContents(T t0, Stream<ItemStack> stream);

    Stream<ItemStack> getContents(T t0);

    default void setContents(ItemStack itemstack, T t0, Stream<ItemStack> stream) {
        T t1 = itemstack.getOrDefault(this.type(), t0);
        T t2 = this.setContents(t1, stream);

        itemstack.set(this.type(), t2);
    }

    default void setContents(ItemStack itemstack, Stream<ItemStack> stream) {
        this.setContents(itemstack, this.empty(), stream);
    }

    default void modifyItems(ItemStack itemstack, UnaryOperator<ItemStack> unaryoperator) {
        T t0 = itemstack.get(this.type());

        if (t0 != null) {
            UnaryOperator<ItemStack> unaryoperator1 = (itemstack1) -> {
                if (itemstack1.isEmpty()) {
                    return itemstack1;
                } else {
                    ItemStack itemstack2 = (ItemStack) unaryoperator.apply(itemstack1);

                    itemstack2.limitSize(itemstack2.getMaxStackSize());
                    return itemstack2;
                }
            };

            this.setContents(itemstack, this.getContents(t0).map(unaryoperator1));
        }

    }
}
