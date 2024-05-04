package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;

// CraftBukkit start
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public class EntitySmallFireball extends EntityFireballFireball {

    public EntitySmallFireball(EntityTypes<? extends EntitySmallFireball> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntitySmallFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(EntityTypes.SMALL_FIREBALL, entityliving, d0, d1, d2, world);
        // CraftBukkit start
        if (this.getOwner() != null && this.getOwner() instanceof EntityInsentient) {
            isIncendiary = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
        // CraftBukkit end
    }

    public EntitySmallFireball(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
        super(EntityTypes.SMALL_FIREBALL, d0, d1, d2, d3, d4, d5, world);
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        if (!this.level().isClientSide) {
            Entity entity = movingobjectpositionentity.getEntity();
            Entity entity1 = this.getOwner();
            int i = entity.getRemainingFireTicks();

            // CraftBukkit start - Entity damage by entity event + combust event
            EntityCombustByEntityEvent event = new EntityCombustByEntityEvent((org.bukkit.entity.Projectile) this.getBukkitEntity(), entity.getBukkitEntity(), 5);
            entity.level().getCraftServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                entity.igniteForSeconds(event.getDuration(), false);
            }
            // CraftBukkit end
            if (!entity.hurt(this.damageSources().fireball(this, entity1), 5.0F)) {
                entity.setRemainingFireTicks(i);
            } else if (entity1 instanceof EntityLiving) {
                this.doEnchantDamageEffects((EntityLiving) entity1, entity);
            }

        }
    }

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        if (!this.level().isClientSide) {
            Entity entity = this.getOwner();

            if (isIncendiary) { // CraftBukkit
                BlockPosition blockposition = movingobjectpositionblock.getBlockPos().relative(movingobjectpositionblock.getDirection());

                if (this.level().isEmptyBlock(blockposition) && !org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(this.level(), blockposition, this).isCancelled()) { // CraftBukkit
                    this.level().setBlockAndUpdate(blockposition, BlockFireAbstract.getState(this.level(), blockposition));
                }
            }

        }
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            this.discard(EntityRemoveEvent.Cause.HIT); // CraftBukkit - add Bukkit remove cause
        }

    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        return false;
    }
}
