package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.ChestLock;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.IBlockData;

public abstract class TileEntityContainer extends TileEntity implements IInventory, ITileInventory, INamableTileEntity {

    public ChestLock lockKey;
    @Nullable
    public IChatBaseComponent name;

    protected TileEntityContainer(TileEntityTypes<?> tileentitytypes, BlockPosition blockposition, IBlockData iblockdata) {
        super(tileentitytypes, blockposition, iblockdata);
        this.lockKey = ChestLock.NO_LOCK;
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.lockKey = ChestLock.fromTag(nbttagcompound);
        if (nbttagcompound.contains("CustomName", 8)) {
            this.name = parseCustomNameSafe(nbttagcompound.getString("CustomName"), holderlookup_a);
        }

    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        this.lockKey.addToTag(nbttagcompound);
        if (this.name != null) {
            nbttagcompound.putString("CustomName", IChatBaseComponent.ChatSerializer.toJson(this.name, holderlookup_a));
        }

    }

    @Override
    public IChatBaseComponent getName() {
        return this.name != null ? this.name : this.getDefaultName();
    }

    @Override
    public IChatBaseComponent getDisplayName() {
        return this.getName();
    }

    @Nullable
    @Override
    public IChatBaseComponent getCustomName() {
        return this.name;
    }

    protected abstract IChatBaseComponent getDefaultName();

    public boolean canOpen(EntityHuman entityhuman) {
        return canUnlock(entityhuman, this.lockKey, this.getDisplayName());
    }

    public static boolean canUnlock(EntityHuman entityhuman, ChestLock chestlock, IChatBaseComponent ichatbasecomponent) {
        if (!entityhuman.isSpectator() && !chestlock.unlocksWith(entityhuman.getMainHandItem())) {
            entityhuman.displayClientMessage(IChatBaseComponent.translatable("container.isLocked", ichatbasecomponent), true);
            entityhuman.playNotifySound(SoundEffects.CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return false;
        } else {
            return true;
        }
    }

    protected abstract NonNullList<ItemStack> getItems();

    protected abstract void setItems(NonNullList<ItemStack> nonnulllist);

    @Override
    public boolean isEmpty() {
        Iterator iterator = this.getItems().iterator();

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
        return (ItemStack) this.getItems().get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemstack = ContainerUtil.removeItem(this.getItems(), i, j);

        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerUtil.takeItem(this.getItems(), i);
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.getItems().set(i, itemstack);
        itemstack.limitSize(this.getMaxStackSize(itemstack));
        this.setChanged();
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return IInventory.stillValidBlockEntity(this, entityhuman);
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerinventory, EntityHuman entityhuman) {
        return this.canOpen(entityhuman) ? this.createMenu(i, playerinventory) : null;
    }

    protected abstract Container createMenu(int i, PlayerInventory playerinventory);

    @Override
    protected void applyImplicitComponents(TileEntity.b tileentity_b) {
        super.applyImplicitComponents(tileentity_b);
        this.name = (IChatBaseComponent) tileentity_b.get(DataComponents.CUSTOM_NAME);
        this.lockKey = (ChestLock) tileentity_b.getOrDefault(DataComponents.LOCK, ChestLock.NO_LOCK);
        ((ItemContainerContents) tileentity_b.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyInto(this.getItems());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.CUSTOM_NAME, this.name);
        if (!this.lockKey.equals(ChestLock.NO_LOCK)) {
            datacomponentmap_a.set(DataComponents.LOCK, this.lockKey);
        }

        datacomponentmap_a.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }

    @Override
    public void removeComponentsFromTag(NBTTagCompound nbttagcompound) {
        nbttagcompound.remove("CustomName");
        nbttagcompound.remove("Lock");
        nbttagcompound.remove("Items");
    }
}
