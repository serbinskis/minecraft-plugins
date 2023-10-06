package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionEntity;

import org.bukkit.event.entity.ExplosionPrimeEvent; // CraftBukkit

public class EntityLargeFireball extends EntityFireballFireball {

    public int explosionPower = 1;

    public EntityLargeFireball(EntityTypes<? extends EntityLargeFireball> entitytypes, World world) {
        super(entitytypes, world);
        isIncendiary = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING); // CraftBukkit
    }

    public EntityLargeFireball(World world, EntityLiving entityliving, double d0, double d1, double d2, int i) {
        super(EntityTypes.FIREBALL, entityliving, d0, d1, d2, world);
        this.explosionPower = i;
        isIncendiary = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING); // CraftBukkit
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);

            // CraftBukkit start - fire ExplosionPrimeEvent
            ExplosionPrimeEvent event = new ExplosionPrimeEvent((org.bukkit.entity.Explosive) this.getBukkitEntity());
            this.level().getCraftServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                // give 'this' instead of (Entity) null so we know what causes the damage
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), event.getRadius(), event.getFire(), World.a.MOB);
            }
            // CraftBukkit end
            this.discard();
        }

    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        if (!this.level().isClientSide) {
            Entity entity = movingobjectpositionentity.getEntity();
            Entity entity1 = this.getOwner();

            entity.hurt(this.damageSources().fireball(this, entity1), 6.0F);
            if (entity1 instanceof EntityLiving) {
                this.doEnchantDamageEffects((EntityLiving) entity1, entity);
            }

        }
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putByte("ExplosionPower", (byte) this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        if (nbttagcompound.contains("ExplosionPower", 99)) {
            // CraftBukkit - set bukkitYield when setting explosionpower
            bukkitYield = this.explosionPower = nbttagcompound.getByte("ExplosionPower");
        }

    }
}
