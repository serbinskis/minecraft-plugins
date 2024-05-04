package net.minecraft.world.item.crafting;

import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.InventoryCrafting;
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
    public boolean matches(InventoryCrafting inventorycrafting, World world) {
        if (!super.matches(inventorycrafting, world)) {
            return false;
        } else {
            ItemStack itemstack = findFilledMap(inventorycrafting);

            if (itemstack.isEmpty()) {
                return false;
            } else {
                WorldMap worldmap = ItemWorldMap.getSavedData(itemstack, world);

                return worldmap == null ? false : (worldmap.isExplorationMap() ? false : worldmap.scale < 4);
            }
        }
    }

    @Override
    public ItemStack assemble(InventoryCrafting inventorycrafting, HolderLookup.a holderlookup_a) {
        ItemStack itemstack = findFilledMap(inventorycrafting).copyWithCount(1);

        itemstack.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
        return itemstack;
    }

    private static ItemStack findFilledMap(InventoryCrafting inventorycrafting) {
        for (int i = 0; i < inventorycrafting.getContainerSize(); ++i) {
            ItemStack itemstack = inventorycrafting.getItem(i);

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
