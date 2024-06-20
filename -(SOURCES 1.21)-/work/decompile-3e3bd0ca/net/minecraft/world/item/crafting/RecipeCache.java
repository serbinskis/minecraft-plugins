package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public class RecipeCache {

    private final RecipeCache.a[] entries;
    private WeakReference<CraftingManager> cachedRecipeManager = new WeakReference((Object) null);

    public RecipeCache(int i) {
        this.entries = new RecipeCache.a[i];
    }

    public Optional<RecipeHolder<RecipeCrafting>> get(World world, CraftingInput craftinginput) {
        if (craftinginput.isEmpty()) {
            return Optional.empty();
        } else {
            this.validateRecipeManager(world);

            for (int i = 0; i < this.entries.length; ++i) {
                RecipeCache.a recipecache_a = this.entries[i];

                if (recipecache_a != null && recipecache_a.matches(craftinginput)) {
                    this.moveEntryToFront(i);
                    return Optional.ofNullable(recipecache_a.value());
                }
            }

            return this.compute(craftinginput, world);
        }
    }

    private void validateRecipeManager(World world) {
        CraftingManager craftingmanager = world.getRecipeManager();

        if (craftingmanager != this.cachedRecipeManager.get()) {
            this.cachedRecipeManager = new WeakReference(craftingmanager);
            Arrays.fill(this.entries, (Object) null);
        }

    }

    private Optional<RecipeHolder<RecipeCrafting>> compute(CraftingInput craftinginput, World world) {
        Optional<RecipeHolder<RecipeCrafting>> optional = world.getRecipeManager().getRecipeFor(Recipes.CRAFTING, craftinginput, world);

        this.insert(craftinginput, (RecipeHolder) optional.orElse((Object) null));
        return optional;
    }

    private void moveEntryToFront(int i) {
        if (i > 0) {
            RecipeCache.a recipecache_a = this.entries[i];

            System.arraycopy(this.entries, 0, this.entries, 1, i);
            this.entries[0] = recipecache_a;
        }

    }

    private void insert(CraftingInput craftinginput, @Nullable RecipeHolder<RecipeCrafting> recipeholder) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(craftinginput.size(), ItemStack.EMPTY);

        for (int i = 0; i < craftinginput.size(); ++i) {
            nonnulllist.set(i, craftinginput.getItem(i).copyWithCount(1));
        }

        System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
        this.entries[0] = new RecipeCache.a(nonnulllist, craftinginput.width(), craftinginput.height(), recipeholder);
    }

    private static record a(NonNullList<ItemStack> key, int width, int height, @Nullable RecipeHolder<RecipeCrafting> value) {

        public boolean matches(CraftingInput craftinginput) {
            if (this.width == craftinginput.width() && this.height == craftinginput.height()) {
                for (int i = 0; i < this.key.size(); ++i) {
                    if (!ItemStack.isSameItemSameComponents((ItemStack) this.key.get(i), craftinginput.getItem(i))) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }
}
