package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public class RecipeCache {

    private final RecipeCache.a[] entries;
    private WeakReference<CraftingManager> cachedRecipeManager = new WeakReference((Object) null);

    public RecipeCache(int i) {
        this.entries = new RecipeCache.a[i];
    }

    public Optional<RecipeHolder<RecipeCrafting>> get(World world, InventoryCrafting inventorycrafting) {
        if (inventorycrafting.isEmpty()) {
            return Optional.empty();
        } else {
            this.validateRecipeManager(world);

            for (int i = 0; i < this.entries.length; ++i) {
                RecipeCache.a recipecache_a = this.entries[i];

                if (recipecache_a != null && recipecache_a.matches(inventorycrafting.getItems())) {
                    this.moveEntryToFront(i);
                    return Optional.ofNullable(recipecache_a.value());
                }
            }

            return this.compute(inventorycrafting, world);
        }
    }

    private void validateRecipeManager(World world) {
        CraftingManager craftingmanager = world.getRecipeManager();

        if (craftingmanager != this.cachedRecipeManager.get()) {
            this.cachedRecipeManager = new WeakReference(craftingmanager);
            Arrays.fill(this.entries, (Object) null);
        }

    }

    private Optional<RecipeHolder<RecipeCrafting>> compute(InventoryCrafting inventorycrafting, World world) {
        Optional<RecipeHolder<RecipeCrafting>> optional = world.getRecipeManager().getRecipeFor(Recipes.CRAFTING, inventorycrafting, world);

        this.insert(inventorycrafting.getItems(), (RecipeHolder) optional.orElse((Object) null));
        return optional;
    }

    private void moveEntryToFront(int i) {
        if (i > 0) {
            RecipeCache.a recipecache_a = this.entries[i];

            System.arraycopy(this.entries, 0, this.entries, 1, i);
            this.entries[0] = recipecache_a;
        }

    }

    private void insert(List<ItemStack> list, @Nullable RecipeHolder<RecipeCrafting> recipeholder) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(list.size(), ItemStack.EMPTY);

        for (int i = 0; i < list.size(); ++i) {
            nonnulllist.set(i, ((ItemStack) list.get(i)).copyWithCount(1));
        }

        System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
        this.entries[0] = new RecipeCache.a(nonnulllist, recipeholder);
    }

    private static record a(NonNullList<ItemStack> key, @Nullable RecipeHolder<RecipeCrafting> value) {

        public boolean matches(List<ItemStack> list) {
            if (this.key.size() != list.size()) {
                return false;
            } else {
                for (int i = 0; i < this.key.size(); ++i) {
                    if (!ItemStack.isSameItemSameComponents((ItemStack) this.key.get(i), (ItemStack) list.get(i))) {
                        return false;
                    }
                }

                return true;
            }
        }
    }
}
