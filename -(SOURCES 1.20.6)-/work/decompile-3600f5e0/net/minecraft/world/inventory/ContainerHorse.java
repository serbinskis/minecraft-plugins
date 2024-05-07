package net.minecraft.world.inventory;

import net.minecraft.world.IInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.EntityHorseAbstract;
import net.minecraft.world.entity.animal.horse.EntityHorseChestedAbstract;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ContainerHorse extends Container {

    private final IInventory horseContainer;
    private final IInventory armorContainer;
    private final EntityHorseAbstract horse;
    private static final int SLOT_BODY_ARMOR = 1;
    private static final int SLOT_HORSE_INVENTORY_START = 2;

    public ContainerHorse(int i, PlayerInventory playerinventory, IInventory iinventory, final EntityHorseAbstract entityhorseabstract) {
        super((Containers) null, i);
        this.horseContainer = iinventory;
        this.armorContainer = entityhorseabstract.getBodyArmorAccess();
        this.horse = entityhorseabstract;
        boolean flag = true;

        iinventory.startOpen(playerinventory.player);
        boolean flag1 = true;

        this.addSlot(new Slot(this, iinventory, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return itemstack.is(Items.SADDLE) && !this.hasItem() && entityhorseabstract.isSaddleable();
            }

            @Override
            public boolean isActive() {
                return entityhorseabstract.isSaddleable();
            }
        });
        this.addSlot(new Slot(this, this.armorContainer, 0, 8, 36) {
            @Override
            public boolean mayPlace(ItemStack itemstack) {
                return entityhorseabstract.isBodyArmorItem(itemstack);
            }

            @Override
            public boolean isActive() {
                return entityhorseabstract.canWearBodyArmor();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        int j;
        int k;

        if (this.hasChest(entityhorseabstract)) {
            for (j = 0; j < 3; ++j) {
                for (k = 0; k < ((EntityHorseChestedAbstract) entityhorseabstract).getInventoryColumns(); ++k) {
                    this.addSlot(new Slot(iinventory, 1 + k + j * ((EntityHorseChestedAbstract) entityhorseabstract).getInventoryColumns(), 80 + k * 18, 18 + j * 18));
                }
            }
        }

        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerinventory, k + j * 9 + 9, 8 + k * 18, 102 + j * 18 + -18));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerinventory, j, 8 + j * 18, 142));
        }

    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return !this.horse.hasInventoryChanged(this.horseContainer) && this.horseContainer.stillValid(entityhuman) && this.armorContainer.stillValid(entityhuman) && this.horse.isAlive() && entityhuman.canInteractWithEntity((Entity) this.horse, 4.0D);
    }

    private boolean hasChest(EntityHorseAbstract entityhorseabstract) {
        boolean flag;

        if (entityhorseabstract instanceof EntityHorseChestedAbstract entityhorsechestedabstract) {
            if (entityhorsechestedabstract.hasChest()) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            int j = this.horseContainer.getContainerSize() + 1;

            if (i < j) {
                if (!this.moveItemStackTo(itemstack1, j, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (j <= 1 || !this.moveItemStackTo(itemstack1, 2, j, false)) {
                int k = j + 27;
                int l = k + 9;

                if (i >= k && i < l) {
                    if (!this.moveItemStackTo(itemstack1, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= j && i < k) {
                    if (!this.moveItemStackTo(itemstack1, k, l, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, k, k, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(EntityHuman entityhuman) {
        super.removed(entityhuman);
        this.horseContainer.stopOpen(entityhuman);
    }
}
