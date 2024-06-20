package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.item.ItemStack;

public class CraftingInput implements RecipeInput {

    public static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());
    private final int width;
    private final int height;
    private final List<ItemStack> items;
    private final AutoRecipeStackManager stackedContents = new AutoRecipeStackManager();
    private final int ingredientCount;

    private CraftingInput(int i, int j, List<ItemStack> list) {
        this.width = i;
        this.height = j;
        this.items = list;
        int k = 0;
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (!itemstack.isEmpty()) {
                ++k;
                this.stackedContents.accountStack(itemstack, 1);
            }
        }

        this.ingredientCount = k;
    }

    public static CraftingInput of(int i, int j, List<ItemStack> list) {
        return ofPositioned(i, j, list).input();
    }

    public static CraftingInput.a ofPositioned(int i, int j, List<ItemStack> list) {
        if (i != 0 && j != 0) {
            int k = i - 1;
            int l = 0;
            int i1 = j - 1;
            int j1 = 0;

            int k1;

            for (k1 = 0; k1 < j; ++k1) {
                boolean flag = true;

                for (int l1 = 0; l1 < i; ++l1) {
                    ItemStack itemstack = (ItemStack) list.get(l1 + k1 * i);

                    if (!itemstack.isEmpty()) {
                        k = Math.min(k, l1);
                        l = Math.max(l, l1);
                        flag = false;
                    }
                }

                if (!flag) {
                    i1 = Math.min(i1, k1);
                    j1 = Math.max(j1, k1);
                }
            }

            k1 = l - k + 1;
            int i2 = j1 - i1 + 1;

            if (k1 > 0 && i2 > 0) {
                if (k1 == i && i2 == j) {
                    return new CraftingInput.a(new CraftingInput(i, j, list), k, i1);
                } else {
                    List<ItemStack> list1 = new ArrayList(k1 * i2);

                    for (int j2 = 0; j2 < i2; ++j2) {
                        for (int k2 = 0; k2 < k1; ++k2) {
                            int l2 = k2 + k + (j2 + i1) * i;

                            list1.add((ItemStack) list.get(l2));
                        }
                    }

                    return new CraftingInput.a(new CraftingInput(k1, i2, list1), k, i1);
                }
            } else {
                return CraftingInput.a.EMPTY;
            }
        } else {
            return CraftingInput.a.EMPTY;
        }
    }

    @Override
    public ItemStack getItem(int i) {
        return (ItemStack) this.items.get(i);
    }

    public ItemStack getItem(int i, int j) {
        return (ItemStack) this.items.get(i + j * this.width);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.ingredientCount == 0;
    }

    public AutoRecipeStackManager stackedContents() {
        return this.stackedContents;
    }

    public List<ItemStack> items() {
        return this.items;
    }

    public int ingredientCount() {
        return this.ingredientCount;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (!(object instanceof CraftingInput)) {
            return false;
        } else {
            CraftingInput craftinginput = (CraftingInput) object;

            return this.width == craftinginput.width && this.height == craftinginput.height && this.ingredientCount == craftinginput.ingredientCount && ItemStack.listMatches(this.items, craftinginput.items);
        }
    }

    public int hashCode() {
        int i = ItemStack.hashStackList(this.items);

        i = 31 * i + this.width;
        i = 31 * i + this.height;
        return i;
    }

    public static record a(CraftingInput input, int left, int top) {

        public static final CraftingInput.a EMPTY = new CraftingInput.a(CraftingInput.EMPTY, 0, 0);
    }
}
