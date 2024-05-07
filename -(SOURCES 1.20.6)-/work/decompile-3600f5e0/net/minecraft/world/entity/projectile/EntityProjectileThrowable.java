package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public abstract class EntityProjectileThrowable extends EntityProjectile implements ItemSupplier {

    private static final DataWatcherObject<ItemStack> DATA_ITEM_STACK = DataWatcher.defineId(EntityProjectileThrowable.class, DataWatcherRegistry.ITEM_STACK);

    public EntityProjectileThrowable(EntityTypes<? extends EntityProjectileThrowable> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityProjectileThrowable(EntityTypes<? extends EntityProjectileThrowable> entitytypes, double d0, double d1, double d2, World world) {
        super(entitytypes, d0, d1, d2, world);
    }

    public EntityProjectileThrowable(EntityTypes<? extends EntityProjectileThrowable> entitytypes, EntityLiving entityliving, World world) {
        super(entitytypes, entityliving, world);
    }

    public void setItem(ItemStack itemstack) {
        this.getEntityData().set(EntityProjectileThrowable.DATA_ITEM_STACK, itemstack.copyWithCount(1));
    }

    protected abstract Item getDefaultItem();

    @Override
    public ItemStack getItem() {
        return (ItemStack) this.getEntityData().get(EntityProjectileThrowable.DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityProjectileThrowable.DATA_ITEM_STACK, new ItemStack(this.getDefaultItem()));
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.put("Item", this.getItem().save(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        if (nbttagcompound.contains("Item", 10)) {
            this.setItem((ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound.getCompound("Item")).orElseGet(() -> {
                return new ItemStack(this.getDefaultItem());
            }));
        } else {
            this.setItem(new ItemStack(this.getDefaultItem()));
        }

    }
}
