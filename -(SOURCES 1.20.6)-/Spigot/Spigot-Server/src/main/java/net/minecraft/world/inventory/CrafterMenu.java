package net.minecraft.world.inventory;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeCrafting;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.CrafterBlock;

// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafter;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
// CraftBukkit end

public class CrafterMenu extends Container implements ICrafting {

    // CraftBukkit start
    private CraftInventoryView bukkitEntity = null;

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryCrafter inventory = new CraftInventoryCrafter(this.container, this.resultContainer);
        bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);
        return bukkitEntity;
    }
    // CraftBukkit end
    protected static final int SLOT_COUNT = 9;
    private static final int INV_SLOT_START = 9;
    private static final int INV_SLOT_END = 36;
    private static final int USE_ROW_SLOT_START = 36;
    private static final int USE_ROW_SLOT_END = 45;
    private final InventoryCraftResult resultContainer = new InventoryCraftResult();
    private final IContainerProperties containerData;
    private final EntityHuman player;
    private final InventoryCrafting container;

    public CrafterMenu(int i, PlayerInventory playerinventory) {
        super(Containers.CRAFTER_3x3, i);
        this.player = playerinventory.player;
        this.containerData = new ContainerProperties(10);
        this.container = new TransientCraftingContainer(this, 3, 3);
        this.addSlots(playerinventory);
    }

    public CrafterMenu(int i, PlayerInventory playerinventory, InventoryCrafting inventorycrafting, IContainerProperties icontainerproperties) {
        super(Containers.CRAFTER_3x3, i);
        this.player = playerinventory.player;
        this.containerData = icontainerproperties;
        this.container = inventorycrafting;
        checkContainerSize(inventorycrafting, 9);
        inventorycrafting.startOpen(playerinventory.player);
        this.addSlots(playerinventory);
        this.addSlotListener(this);
    }

    private void addSlots(PlayerInventory playerinventory) {
        int i;
        int j;

        for (j = 0; j < 3; ++j) {
            for (i = 0; i < 3; ++i) {
                int k = i + j * 3;

                this.addSlot(new CrafterSlot(this.container, k, 26 + i * 18, 17 + j * 18, this));
            }
        }

        for (j = 0; j < 3; ++j) {
            for (i = 0; i < 9; ++i) {
                this.addSlot(new Slot(playerinventory, i + j * 9 + 9, 8 + i * 18, 84 + j * 18));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerinventory, j, 8 + j * 18, 142));
        }

        this.addSlot(new NonInteractiveResultSlot(this.resultContainer, 0, 134, 35));
        this.addDataSlots(this.containerData);
        this.refreshRecipeResult();
    }

    public void setSlotState(int i, boolean flag) {
        CrafterSlot crafterslot = (CrafterSlot) this.getSlot(i);

        this.containerData.set(crafterslot.index, flag ? 0 : 1);
        this.broadcastChanges();
    }

    public boolean isSlotDisabled(int i) {
        return i > -1 && i < 9 ? this.containerData.get(i) == 1 : false;
    }

    public boolean isPowered() {
        return this.containerData.get(9) == 1;
    }

    @Override
    public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.copy();
            if (i < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(entityhuman, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        if (!this.checkReachable) return true; // CraftBukkit
        return this.container.stillValid(entityhuman);
    }

    private void refreshRecipeResult() {
        EntityHuman entityhuman = this.player;

        if (entityhuman instanceof EntityPlayer entityplayer) {
            World world = entityplayer.level();
            ItemStack itemstack = (ItemStack) CrafterBlock.getPotentialResults(world, this.container).map((recipeholder) -> {
                return ((RecipeCrafting) recipeholder.value()).assemble(this.container, world.registryAccess());
            }).orElse(ItemStack.EMPTY);

            this.resultContainer.setItem(0, itemstack);
        }

    }

    public IInventory getContainer() {
        return this.container;
    }

    @Override
    public void slotChanged(Container container, int i, ItemStack itemstack) {
        this.refreshRecipeResult();
    }

    @Override
    public void dataChanged(Container container, int i, int j) {}
}
