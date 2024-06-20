package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.World;

public class RecipeFireworksFade extends IRecipeComplex {

    private static final RecipeItemStack STAR_INGREDIENT = RecipeItemStack.of(Items.FIREWORK_STAR);

    public RecipeFireworksFade(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        boolean flag = false;
        boolean flag1 = false;

        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack = craftinginput.getItem(i);

            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof ItemDye) {
                    flag = true;
                } else {
                    if (!RecipeFireworksFade.STAR_INGREDIENT.test(itemstack)) {
                        return false;
                    }

                    if (flag1) {
                        return false;
                    }

                    flag1 = true;
                }
            }
        }

        return flag1 && flag;
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        IntArrayList intarraylist = new IntArrayList();
        ItemStack itemstack = null;

        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack1 = craftinginput.getItem(i);
            Item item = itemstack1.getItem();

            if (item instanceof ItemDye) {
                intarraylist.add(((ItemDye) item).getDyeColor().getFireworkColor());
            } else if (RecipeFireworksFade.STAR_INGREDIENT.test(itemstack1)) {
                itemstack = itemstack1.copyWithCount(1);
            }
        }

        if (itemstack != null && !intarraylist.isEmpty()) {
            itemstack.update(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT, intarraylist, FireworkExplosion::withFadeColors);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR_FADE;
    }
}
