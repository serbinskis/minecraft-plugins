package net.minecraft.world.entity;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.EnumPistonReaction;
import net.minecraft.world.phys.Vec3D;

public class OminousItemSpawner extends Entity {

    private static final int SPAWN_ITEM_DELAY_MIN = 60;
    private static final int SPAWN_ITEM_DELAY_MAX = 120;
    private static final String TAG_SPAWN_ITEM_AFTER_TICKS = "spawn_item_after_ticks";
    private static final String TAG_ITEM = "item";
    private static final DataWatcherObject<ItemStack> DATA_ITEM = DataWatcher.defineId(OminousItemSpawner.class, DataWatcherRegistry.ITEM_STACK);
    public static final int TICKS_BEFORE_ABOUT_TO_SPAWN_SOUND = 36;
    public long spawnItemAfterTicks;

    public OminousItemSpawner(EntityTypes<? extends OminousItemSpawner> entitytypes, World world) {
        super(entitytypes, world);
        this.noPhysics = true;
    }

    public static OminousItemSpawner create(World world, ItemStack itemstack) {
        OminousItemSpawner ominousitemspawner = new OminousItemSpawner(EntityTypes.OMINOUS_ITEM_SPAWNER, world);

        ominousitemspawner.spawnItemAfterTicks = (long) world.random.nextIntBetweenInclusive(60, 120);
        ominousitemspawner.setItem(itemstack);
        return ominousitemspawner;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.tickClient();
        } else {
            this.tickServer();
        }
    }

    private void tickServer() {
        if ((long) this.tickCount == this.spawnItemAfterTicks - 36L) {
            this.level().playSound((EntityHuman) null, this.blockPosition(), SoundEffects.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundCategory.NEUTRAL);
        }

        if ((long) this.tickCount >= this.spawnItemAfterTicks) {
            this.spawnItem();
            this.kill();
        }

    }

    private void tickClient() {
        if (this.level().getGameTime() % 5L == 0L) {
            this.addParticles();
        }

    }

    private void spawnItem() {
        World world = this.level();
        ItemStack itemstack = this.getItem();

        if (!itemstack.isEmpty()) {
            Item item = itemstack.getItem();
            Object object;

            if (item instanceof ProjectileItem) {
                ProjectileItem projectileitem = (ProjectileItem) item;
                EnumDirection enumdirection = EnumDirection.DOWN;
                IProjectile iprojectile = projectileitem.asProjectile(world, this.position(), itemstack, enumdirection);
                ProjectileItem.a projectileitem_a = projectileitem.createDispenseConfig();

                projectileitem.shoot(iprojectile, (double) enumdirection.getStepX(), (double) enumdirection.getStepY(), (double) enumdirection.getStepZ(), projectileitem_a.power(), projectileitem_a.uncertainty());
                projectileitem_a.overrideDispenseEvent().ifPresent((i) -> {
                    world.levelEvent(i, this.blockPosition(), 0);
                });
                object = iprojectile;
            } else {
                object = new EntityItem(world, this.getX(), this.getY(), this.getZ(), itemstack);
            }

            world.addFreshEntity((Entity) object);
            world.levelEvent(3021, this.blockPosition(), 1);
            world.gameEvent((Entity) object, (Holder) GameEvent.ENTITY_PLACE, this.position());
            this.setItem(ItemStack.EMPTY);
        }
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(OminousItemSpawner.DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        ItemStack itemstack = nbttagcompound.contains("item", 10) ? (ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound.getCompound("item")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;

        this.setItem(itemstack);
        nbttagcompound.getLong("spawn_item_after_ticks");
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        if (!this.getItem().isEmpty()) {
            nbttagcompound.put("item", this.getItem().save(this.registryAccess()).copy());
        }

        nbttagcompound.putLong("spawn_item_after_ticks", this.spawnItemAfterTicks);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity entity) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public EnumPistonReaction getPistonPushReaction() {
        return EnumPistonReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public void addParticles() {
        Vec3D vec3d = this.position();
        int i = this.random.nextIntBetweenInclusive(1, 3);

        for (int j = 0; j < i; ++j) {
            double d0 = 0.4D;
            Vec3D vec3d1 = new Vec3D(this.getX() + 0.4D * (this.random.nextGaussian() - this.random.nextGaussian()), this.getY() + 0.4D * (this.random.nextGaussian() - this.random.nextGaussian()), this.getZ() + 0.4D * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3D vec3d2 = vec3d.vectorTo(vec3d1);

            this.level().addParticle(Particles.OMINOUS_SPAWNING, vec3d.x(), vec3d.y(), vec3d.z(), vec3d2.x(), vec3d2.y(), vec3d2.z());
        }

    }

    public ItemStack getItem() {
        return (ItemStack) this.getEntityData().get(OminousItemSpawner.DATA_ITEM);
    }

    public void setItem(ItemStack itemstack) {
        this.getEntityData().set(OminousItemSpawner.DATA_ITEM, itemstack);
    }
}
