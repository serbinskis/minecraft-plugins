package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public class EntitySmallFireball extends EntityFireballFireball {

    public EntitySmallFireball(EntityTypes<? extends EntitySmallFireball> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntitySmallFireball(World world, EntityLiving entityliving, Vec3D vec3d) {
        super(EntityTypes.SMALL_FIREBALL, entityliving, vec3d, world);
        // CraftBukkit start
        if (this.getOwner() != null && this.getOwner() instanceof EntityInsentient) {
            isIncendiary = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
        // CraftBukkit end
    }

    public EntitySmallFireball(World world, double d0, double d1, double d2, Vec3D vec3d) {
        super(EntityTypes.SMALL_FIREBALL, d0, d1, d2, vec3d, world);
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            Entity entity = movingobjectpositionentity.getEntity();
            Entity entity1 = this.getOwner();
            int i = entity.getRemainingFireTicks();

            // CraftBukkit start - Entity damage by entity event + combust event
            EntityCombustByEntityEvent event = new EntityCombustByEntityEvent((org.bukkit.entity.Projectile) this.getBukkitEntity(), entity.getBukkitEntity(), 5.0F);
            entity.level().getCraftServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                entity.igniteForSeconds(event.getDuration(), false);
            }
            // CraftBukkit end
            DamageSource damagesource = this.damageSources().fireball(this, entity1);

            if (!entity.hurt(damagesource, 5.0F)) {
                entity.setRemainingFireTicks(i);
            } else {
                EnchantmentManager.doPostAttackEffects(worldserver, entity, damagesource);
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
