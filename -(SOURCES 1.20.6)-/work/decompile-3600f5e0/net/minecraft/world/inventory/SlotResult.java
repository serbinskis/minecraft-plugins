package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipes;

public class SlotResult extends Slot {

    private final InventoryCrafting craftSlots;
    private final EntityHuman player;
    private int removeCount;

    public SlotResult(EntityHuman entityhuman, InventoryCrafting inventorycrafting, IInventory iinventory, int i, int j, int k) {
        super(iinventory, i, j, k);
        this.player = entityhuman;
        this.craftSlots = inventorycrafting;
    }

    @Override
    public boolean mayPlace(ItemStack itemstack) {
        return false;
    }

    @Override
    public ItemStack remove(int i) {
        if (this.hasItem()) {
            this.removeCount += Math.min(i, this.getItem().getCount());
        }

        return super.remove(i);
    }

    @Override
    protected void onQuickCraft(ItemStack itemstack, int i) {
        this.removeCount += i;
        this.checkTakeAchievements(itemstack);
    }

    @Override
    protected void onSwapCraft(int i) {
        this.removeCount += i;
    }

    @Override
    protected void checkTakeAchievements(ItemStack itemstack) {
        if (this.removeCount > 0) {
            itemstack.onCraftedBy(this.player.level(), this.player, this.removeCount);
        }

        IInventory iinventory = this.container;

        if (iinventory instanceof RecipeCraftingHolder recipecraftingholder) {
            recipecraftingholder.awardUsedRecipes(this.player, this.craftSlots.getItems());
        }

        this.removeCount = 0;
    }

    @Override
    public void onTake(EntityHuman entityhuman, ItemStack itemstack) {
        this.checkTakeAchievements(itemstack);
        NonNullList<ItemStack> nonnulllist = entityhuman.level().getRecipeManager().getRemainingItemsFor(Recipes.CRAFTING, this.craftSlots, entityhuman.level());

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack1 = this.craftSlots.getItem(i);
            ItemStack itemstack2 = (ItemStack) nonnulllist.get(i);

            if (!itemstack1.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                itemstack1 = this.craftSlots.getItem(i);
            }

            if (!itemstack2.isEmpty()) {
                if (itemstack1.isEmpty()) {
                    this.craftSlots.setItem(i, itemstack2);
                } else if (ItemStack.isSameItemSameComponents(itemstack1, itemstack2)) {
                    itemstack2.grow(itemstack1.getCount());
                    this.craftSlots.setItem(i, itemstack2);
                } else if (!this.player.getInventory().add(itemstack2)) {
                    this.player.drop(itemstack2, false);
                }
            }
        }

    }

    @Override
    public boolean isFake() {
        return true;
    }
}
