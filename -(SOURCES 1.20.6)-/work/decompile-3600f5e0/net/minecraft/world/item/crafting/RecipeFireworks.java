package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.World;

public class RecipeFireworks extends IRecipeComplex {

    private static final RecipeItemStack PAPER_INGREDIENT = RecipeItemStack.of(Items.PAPER);
    private static final RecipeItemStack GUNPOWDER_INGREDIENT = RecipeItemStack.of(Items.GUNPOWDER);
    private static final RecipeItemStack STAR_INGREDIENT = RecipeItemStack.of(Items.FIREWORK_STAR);

    public RecipeFireworks(CraftingBookCategory craftingbookcategory) {
        super(craftingbookcategory);
    }

    public boolean matches(InventoryCrafting inventorycrafting, World world) {
        boolean flag = false;
        int i = 0;

        for (int j = 0; j < inventorycrafting.getContainerSize(); ++j) {
            ItemStack itemstack = inventorycrafting.getItem(j);

            if (!itemstack.isEmpty()) {
                if (RecipeFireworks.PAPER_INGREDIENT.test(itemstack)) {
                    if (flag) {
                        return false;
                    }

                    flag = true;
                } else if (RecipeFireworks.GUNPOWDER_INGREDIENT.test(itemstack)) {
                    ++i;
                    if (i > 3) {
                        return false;
                    }
                } else if (!RecipeFireworks.STAR_INGREDIENT.test(itemstack)) {
                    return false;
                }
            }
        }

        return flag && i >= 1;
    }

    public ItemStack assemble(InventoryCrafting inventorycrafting, HolderLookup.a holderlookup_a) {
        List<FireworkExplosion> list = new ArrayList();
        int i = 0;

        for (int j = 0; j < inventorycrafting.getContainerSize(); ++j) {
            ItemStack itemstack = inventorycrafting.getItem(j);

            if (!itemstack.isEmpty()) {
                if (RecipeFireworks.GUNPOWDER_INGREDIENT.test(itemstack)) {
                    ++i;
                } else if (RecipeFireworks.STAR_INGREDIENT.test(itemstack)) {
                    FireworkExplosion fireworkexplosion = (FireworkExplosion) itemstack.get(DataComponents.FIREWORK_EXPLOSION);

                    if (fireworkexplosion != null) {
                        list.add(fireworkexplosion);
                    }
                }
            }
        }

        ItemStack itemstack1 = new ItemStack(Items.FIREWORK_ROCKET, 3);

        itemstack1.set(DataComponents.FIREWORKS, new Fireworks(i, list));
        return itemstack1;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.a holderlookup_a) {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}
