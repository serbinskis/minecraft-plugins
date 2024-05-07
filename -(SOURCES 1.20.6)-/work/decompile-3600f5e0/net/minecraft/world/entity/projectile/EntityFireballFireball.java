package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;

public abstract class EntityFireballFireball extends EntityFireball implements ItemSupplier {

    private static final DataWatcherObject<ItemStack> DATA_ITEM_STACK = DataWatcher.defineId(EntityFireballFireball.class, DataWatcherRegistry.ITEM_STACK);

    public EntityFireballFireball(EntityTypes<? extends EntityFireballFireball> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityFireballFireball(EntityTypes<? extends EntityFireballFireball> entitytypes, double d0, double d1, double d2, double d3, double d4, double d5, World world) {
        super(entitytypes, d0, d1, d2, d3, d4, d5, world);
    }

    public EntityFireballFireball(EntityTypes<? extends EntityFireballFireball> entitytypes, EntityLiving entityliving, double d0, double d1, double d2, World world) {
        super(entitytypes, entityliving, d0, d1, d2, world);
    }

    public void setItem(ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            this.getEntityData().set(EntityFireballFireball.DATA_ITEM_STACK, this.getDefaultItem());
        } else {
            this.getEntityData().set(EntityFireballFireball.DATA_ITEM_STACK, itemstack.copyWithCount(1));
        }

    }

    @Override
    public ItemStack getItem() {
        return (ItemStack) this.getEntityData().get(EntityFireballFireball.DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityFireballFireball.DATA_ITEM_STACK, this.getDefaultItem());
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
            this.setItem((ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound.getCompound("Item")).orElse(this.getDefaultItem()));
        } else {
            this.setItem(this.getDefaultItem());
        }

    }

    private ItemStack getDefaultItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    public SlotAccess getSlot(int i) {
        return i == 0 ? SlotAccess.of(this::getItem, this::setItem) : super.getSlot(i);
    }
}
