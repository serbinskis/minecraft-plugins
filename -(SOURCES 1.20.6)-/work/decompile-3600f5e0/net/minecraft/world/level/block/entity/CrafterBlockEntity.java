package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.IContainerProperties;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.state.IBlockData;

public class CrafterBlockEntity extends TileEntityLootable implements InventoryCrafting {

    public static final int CONTAINER_WIDTH = 3;
    public static final int CONTAINER_HEIGHT = 3;
    public static final int CONTAINER_SIZE = 9;
    public static final int SLOT_DISABLED = 1;
    public static final int SLOT_ENABLED = 0;
    public static final int DATA_TRIGGERED = 9;
    public static final int NUM_DATA = 10;
    private NonNullList<ItemStack> items;
    public int craftingTicksRemaining;
    protected final IContainerProperties containerData;

    public CrafterBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.CRAFTER, blockposition, iblockdata);
        this.items = NonNullList.withSize(9, ItemStack.EMPTY);
        this.craftingTicksRemaining = 0;
        this.containerData = new IContainerProperties(this) {
            private final int[] slotStates = new int[9];
            private int triggered = 0;

            @Override
            public int get(int i) {
                return i == 9 ? this.triggered : this.slotStates[i];
            }

            @Override
            public void set(int i, int j) {
                if (i == 9) {
                    this.triggered = j;
                } else {
                    this.slotStates[i] = j;
                }

            }

            @Override
            public int getCount() {
                return 10;
            }
        };
    }

    @Override
    protected IChatBaseComponent getDefaultName() {
        return IChatBaseComponent.translatable("container.crafter");
    }

    @Override
    protected Container createMenu(int i, PlayerInventory playerinventory) {
        return new CrafterMenu(i, playerinventory, this, this.containerData);
    }

    public void setSlotState(int i, boolean flag) {
        if (this.slotCanBeDisabled(i)) {
            this.containerData.set(i, flag ? 0 : 1);
            this.setChanged();
        }
    }

    public boolean isSlotDisabled(int i) {
        return i >= 0 && i < 9 ? this.containerData.get(i) == 1 : false;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemstack) {
        if (this.containerData.get(i) == 1) {
            return false;
        } else {
            ItemStack itemstack1 = (ItemStack) this.items.get(i);
            int j = itemstack1.getCount();

            return j >= itemstack1.getMaxStackSize() ? false : (itemstack1.isEmpty() ? true : !this.smallerStackExist(j, itemstack1, i));
        }
    }

    private boolean smallerStackExist(int i, ItemStack itemstack, int j) {
        for (int k = j + 1; k < 9; ++k) {
            if (!this.isSlotDisabled(k)) {
                ItemStack itemstack1 = this.getItem(k);

                if (itemstack1.isEmpty() || itemstack1.getCount() < i && ItemStack.isSameItemSameComponents(itemstack1, itemstack)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.craftingTicksRemaining = nbttagcompound.getInt("crafting_ticks_remaining");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(nbttagcompound)) {
            ContainerUtil.loadAllItems(nbttagcompound, this.items, holderlookup_a);
        }

        int[] aint = nbttagcompound.getIntArray("disabled_slots");

        for (int i = 0; i < 9; ++i) {
            this.containerData.set(i, 0);
        }

        int[] aint1 = aint;
        int j = aint.length;

        for (int k = 0; k < j; ++k) {
            int l = aint1[k];

            if (this.slotCanBeDisabled(l)) {
                this.containerData.set(l, 1);
            }
        }

        this.containerData.set(9, nbttagcompound.getInt("triggered"));
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        nbttagcompound.putInt("crafting_ticks_remaining", this.craftingTicksRemaining);
        if (!this.trySaveLootTable(nbttagcompound)) {
            ContainerUtil.saveAllItems(nbttagcompound, this.items, holderlookup_a);
        }

        this.addDisabledSlots(nbttagcompound);
        this.addTriggered(nbttagcompound);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        Iterator iterator = this.items.iterator();

        ItemStack itemstack;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            itemstack = (ItemStack) iterator.next();
        } while (itemstack.isEmpty());

        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return (ItemStack) this.items.get(i);
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        if (this.isSlotDisabled(i)) {
            this.setSlotState(i, true);
        }

        super.setItem(i, itemstack);
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return IInventory.stillValidBlockEntity(this, entityhuman);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonnulllist) {
        this.items = nonnulllist;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public void fillStackedContents(AutoRecipeStackManager autorecipestackmanager) {
        Iterator iterator = this.items.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            autorecipestackmanager.accountSimpleStack(itemstack);
        }

    }

    private void addDisabledSlots(NBTTagCompound nbttagcompound) {
        IntArrayList intarraylist = new IntArrayList();

        for (int i = 0; i < 9; ++i) {
            if (this.isSlotDisabled(i)) {
                intarraylist.add(i);
            }
        }

        nbttagcompound.putIntArray("disabled_slots", (List) intarraylist);
    }

    private void addTriggered(NBTTagCompound nbttagcompound) {
        nbttagcompound.putInt("triggered", this.containerData.get(9));
    }

    public void setTriggered(boolean flag) {
        this.containerData.set(9, flag ? 1 : 0);
    }

    @VisibleForTesting
    public boolean isTriggered() {
        return this.containerData.get(9) == 1;
    }

    public static void serverTick(World world, BlockPosition blockposition, IBlockData iblockdata, CrafterBlockEntity crafterblockentity) {
        int i = crafterblockentity.craftingTicksRemaining - 1;

        if (i >= 0) {
            crafterblockentity.craftingTicksRemaining = i;
            if (i == 0) {
                world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CrafterBlock.CRAFTING, false), 3);
            }

        }
    }

    public void setCraftingTicksRemaining(int i) {
        this.craftingTicksRemaining = i;
    }

    public int getRedstoneSignal() {
        int i = 0;

        for (int j = 0; j < this.getContainerSize(); ++j) {
            ItemStack itemstack = this.getItem(j);

            if (!itemstack.isEmpty() || this.isSlotDisabled(j)) {
                ++i;
            }
        }

        return i;
    }

    private boolean slotCanBeDisabled(int i) {
        return i > -1 && i < 9 && ((ItemStack) this.items.get(i)).isEmpty();
    }
}
