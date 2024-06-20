package net.minecraft.world.item.crafting;

import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemWorldMap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.World;
import net.minecraft.world.level.saveddata.maps.WorldMap;

public class RecipeMapExtend extends ShapedRecipes {

    public RecipeMapExtend(CraftingBookCategory craftingbookcategory) {
        super("", craftingbookcategory, ShapedRecipePattern.of(Map.of('#', RecipeItemStack.of(Items.PAPER), 'x', RecipeItemStack.of(Items.FILLED_MAP)), "###", "#x#", "###"), new ItemStack(Items.MAP));
    }

    @Override
    public boolean matches(CraftingInput craftinginput, World world) {
        if (!super.matches(craftinginput, world)) {
            return false;
        } else {
            ItemStack itemstack = findFilledMap(craftinginput);

            if (itemstack.isEmpty()) {
                return false;
            } else {
                WorldMap worldmap = ItemWorldMap.getSavedData(itemstack, world);

                return worldmap == null ? false : (worldmap.isExplorationMap() ? false : worldmap.scale < 4);
            }
        }
    }

    @Override
    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        ItemStack itemstack = findFilledMap(craftinginput).copyWithCount(1);

        itemstack.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
        return itemstack;
    }

    private static ItemStack findFilledMap(CraftingInput craftinginput) {
        for (int i = 0; i < craftinginput.size(); ++i) {
            ItemStack itemstack = craftinginput.getItem(i);

            if (itemstack.is(Items.FILLED_MAP)) {
                return itemstack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_EXTENDING;
    }
}
