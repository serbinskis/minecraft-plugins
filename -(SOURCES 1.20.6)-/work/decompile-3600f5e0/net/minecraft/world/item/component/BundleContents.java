package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import org.apache.commons.lang3.math.Fraction;

public final class BundleContents implements TooltipComponent {

    public static final BundleContents EMPTY = new BundleContents(List.of());
    public static final Codec<BundleContents> CODEC = ItemStack.CODEC.listOf().xmap(BundleContents::new, (bundlecontents) -> {
        return bundlecontents.items;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(BundleContents::new, (bundlecontents) -> {
        return bundlecontents.items;
    });
    private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction(1, 16);
    private static final int NO_STACK_INDEX = -1;
    final List<ItemStack> items;
    final Fraction weight;

    BundleContents(List<ItemStack> list, Fraction fraction) {
        this.items = list;
        this.weight = fraction;
    }

    public BundleContents(List<ItemStack> list) {
        this(list, computeContentWeight(list));
    }

    private static Fraction computeContentWeight(List<ItemStack> list) {
        Fraction fraction = Fraction.ZERO;

        ItemStack itemstack;

        for (Iterator iterator = list.iterator(); iterator.hasNext(); fraction = fraction.add(getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)))) {
            itemstack = (ItemStack) iterator.next();
        }

        return fraction;
    }

    static Fraction getWeight(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null) {
            return BundleContents.BUNDLE_IN_BUNDLE_WEIGHT.add(bundlecontents.weight());
        } else {
            List<TileEntityBeehive.c> list = (List) itemstack.getOrDefault(DataComponents.BEES, List.of());

            return !list.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, itemstack.getMaxStackSize());
        }
    }

    public ItemStack getItemUnsafe(int i) {
        return (ItemStack) this.items.get(i);
    }

    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Iterable<ItemStack> items() {
        return this.items;
    }

    public Iterable<ItemStack> itemsCopy() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public int size() {
        return this.items.size();
    }

    public Fraction weight() {
        return this.weight;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof BundleContents)) {
            return false;
        } else {
            BundleContents bundlecontents = (BundleContents) object;

            return this.weight.equals(bundlecontents.weight) && ItemStack.listMatches(this.items, bundlecontents.items);
        }
    }

    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    public String toString() {
        return "BundleContents" + String.valueOf(this.items);
    }

    public static class a {

        private final List<ItemStack> items;
        private Fraction weight;

        public a(BundleContents bundlecontents) {
            this.items = new ArrayList(bundlecontents.items);
            this.weight = bundlecontents.weight;
        }

        public BundleContents.a clearItems() {
            this.items.clear();
            this.weight = Fraction.ZERO;
            return this;
        }

        private int findStackIndex(ItemStack itemstack) {
            if (!itemstack.isStackable()) {
                return -1;
            } else {
                for (int i = 0; i < this.items.size(); ++i) {
                    if (ItemStack.isSameItemSameComponents((ItemStack) this.items.get(i), itemstack)) {
                        return i;
                    }
                }

                return -1;
            }
        }

        private int getMaxAmountToAdd(ItemStack itemstack) {
            Fraction fraction = Fraction.ONE.subtract(this.weight);

            return Math.max(fraction.divideBy(BundleContents.getWeight(itemstack)).intValue(), 0);
        }

        public int tryInsert(ItemStack itemstack) {
            if (!itemstack.isEmpty() && itemstack.getItem().canFitInsideContainerItems()) {
                int i = Math.min(itemstack.getCount(), this.getMaxAmountToAdd(itemstack));

                if (i == 0) {
                    return 0;
                } else {
                    this.weight = this.weight.add(BundleContents.getWeight(itemstack).multiplyBy(Fraction.getFraction(i, 1)));
                    int j = this.findStackIndex(itemstack);

                    if (j != -1) {
                        ItemStack itemstack1 = (ItemStack) this.items.remove(j);
                        ItemStack itemstack2 = itemstack1.copyWithCount(itemstack1.getCount() + i);

                        itemstack.shrink(i);
                        this.items.add(0, itemstack2);
                    } else {
                        this.items.add(0, itemstack.split(i));
                    }

                    return i;
                }
            } else {
                return 0;
            }
        }

        public int tryTransfer(Slot slot, EntityHuman entityhuman) {
            ItemStack itemstack = slot.getItem();
            int i = this.getMaxAmountToAdd(itemstack);

            return this.tryInsert(slot.safeTake(itemstack.getCount(), i, entityhuman));
        }

        @Nullable
        public ItemStack removeOne() {
            if (this.items.isEmpty()) {
                return null;
            } else {
                ItemStack itemstack = ((ItemStack) this.items.remove(0)).copy();

                this.weight = this.weight.subtract(BundleContents.getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
                return itemstack;
            }
        }

        public Fraction weight() {
            return this.weight;
        }

        public BundleContents toImmutable() {
            return new BundleContents(List.copyOf(this.items), this.weight);
        }
    }
}
