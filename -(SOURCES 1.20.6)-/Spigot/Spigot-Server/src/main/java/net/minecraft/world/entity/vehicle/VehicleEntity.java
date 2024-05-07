package net.minecraft.world.entity.vehicle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

// CraftBukkit start
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
// CraftBukkit end

public abstract class VehicleEntity extends Entity {

    protected static final DataWatcherObject<Integer> DATA_ID_HURT = DataWatcher.defineId(VehicleEntity.class, DataWatcherRegistry.INT);
    protected static final DataWatcherObject<Integer> DATA_ID_HURTDIR = DataWatcher.defineId(VehicleEntity.class, DataWatcherRegistry.INT);
    protected static final DataWatcherObject<Float> DATA_ID_DAMAGE = DataWatcher.defineId(VehicleEntity.class, DataWatcherRegistry.FLOAT);

    public VehicleEntity(EntityTypes<?> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            if (this.isInvulnerableTo(damagesource)) {
                return false;
            } else {
                // CraftBukkit start
                Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                org.bukkit.entity.Entity attacker = (damagesource.getEntity() == null) ? null : damagesource.getEntity().getBukkitEntity();

                VehicleDamageEvent event = new VehicleDamageEvent(vehicle, attacker, (double) f);
                this.level().getCraftServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return false;
                }
                f = (float) event.getDamage();
                // CraftBukkit end
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.markHurt();
                this.setDamage(this.getDamage() + f * 10.0F);
                this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
                boolean flag = damagesource.getEntity() instanceof EntityHuman && ((EntityHuman) damagesource.getEntity()).getAbilities().instabuild;

                if ((flag || this.getDamage() <= 40.0F) && !this.shouldSourceDestroy(damagesource)) {
                    if (flag) {
                        // CraftBukkit start
                        VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, attacker);
                        this.level().getCraftServer().getPluginManager().callEvent(destroyEvent);

                        if (destroyEvent.isCancelled()) {
                            this.setDamage(40.0F); // Maximize damage so this doesn't get triggered again right away
                            return true;
                        }
                        // CraftBukkit end
                        this.discard(EntityRemoveEvent.Cause.DEATH); // CraftBukkit - add Bukkit remove cause
                    }
                } else {
                    // CraftBukkit start
                    VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, attacker);
                    this.level().getCraftServer().getPluginManager().callEvent(destroyEvent);

                    if (destroyEvent.isCancelled()) {
                        this.setDamage(40.0F); // Maximize damage so this doesn't get triggered again right away
                        return true;
                    }
                    // CraftBukkit end
                    this.destroy(damagesource);
                }

                return true;
            }
        } else {
            return true;
        }
    }

    boolean shouldSourceDestroy(DamageSource damagesource) {
        return false;
    }

    public void destroy(Item item) {
        this.kill();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemstack = new ItemStack(item);

            itemstack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
            this.spawnAtLocation(itemstack);
        }
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(VehicleEntity.DATA_ID_HURT, 0);
        datawatcher_a.define(VehicleEntity.DATA_ID_HURTDIR, 1);
        datawatcher_a.define(VehicleEntity.DATA_ID_DAMAGE, 0.0F);
    }

    public void setHurtTime(int i) {
        this.entityData.set(VehicleEntity.DATA_ID_HURT, i);
    }

    public void setHurtDir(int i) {
        this.entityData.set(VehicleEntity.DATA_ID_HURTDIR, i);
    }

    public void setDamage(float f) {
        this.entityData.set(VehicleEntity.DATA_ID_DAMAGE, f);
    }

    public float getDamage() {
        return (Float) this.entityData.get(VehicleEntity.DATA_ID_DAMAGE);
    }

    public int getHurtTime() {
        return (Integer) this.entityData.get(VehicleEntity.DATA_ID_HURT);
    }

    public int getHurtDir() {
        return (Integer) this.entityData.get(VehicleEntity.DATA_ID_HURTDIR);
    }

    protected void destroy(DamageSource damagesource) {
        this.destroy(this.getDropItem());
    }

    abstract Item getDropItem();
}
