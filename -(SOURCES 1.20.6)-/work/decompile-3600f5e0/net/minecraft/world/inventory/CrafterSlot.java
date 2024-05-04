package net.minecraft.world.inventory;

import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;

public class CrafterSlot extends Slot {

    private final CrafterMenu menu;

    public CrafterSlot(IInventory iinventory, int i, int j, int k, CrafterMenu craftermenu) {
        super(iinventory, i, j, k);
        this.menu = craftermenu;
    }

    @Override
    public boolean mayPlace(ItemStack itemstack) {
        return !this.menu.isSlotDisabled(this.index) && super.mayPlace(itemstack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.menu.slotsChanged(this.container);
    }
}
