package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntityEndermite;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

public class EntityEnderPearl extends EntityProjectileThrowable {

    public EntityEnderPearl(EntityTypes<? extends EntityEnderPearl> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityEnderPearl(World world, EntityLiving entityliving) {
        super(EntityTypes.ENDER_PEARL, entityliving, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        movingobjectpositionentity.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);

        for (int i = 0; i < 32; ++i) {
            this.level().addParticle(Particles.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0D, this.getZ(), this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
        }

        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (!this.isRemoved()) {
                Entity entity = this.getOwner();

                if (entity != null && isAllowedToTeleportOwner(entity, worldserver)) {
                    if (entity.isPassenger()) {
                        entity.unRide();
                    }

                    if (entity instanceof EntityPlayer) {
                        EntityPlayer entityplayer = (EntityPlayer) entity;

                        if (entityplayer.connection.isAcceptingMessages()) {
                            if (this.random.nextFloat() < 0.05F && worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                                EntityEndermite entityendermite = (EntityEndermite) EntityTypes.ENDERMITE.create(worldserver);

                                if (entityendermite != null) {
                                    entityendermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                                    worldserver.addFreshEntity(entityendermite);
                                }
                            }

                            entity.changeDimension(new DimensionTransition(worldserver, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
                            entity.resetFallDistance();
                            entityplayer.resetCurrentImpulseContext();
                            entity.hurt(this.damageSources().fall(), 5.0F);
                            this.playSound(worldserver, this.position());
                        }
                    } else {
                        entity.changeDimension(new DimensionTransition(worldserver, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
                        entity.resetFallDistance();
                        this.playSound(worldserver, this.position());
                    }

                    this.discard();
                    return;
                }

                this.discard();
                return;
            }
        }

    }

    private static boolean isAllowedToTeleportOwner(Entity entity, World world) {
        if (entity.level().dimension() == world.dimension()) {
            if (!(entity instanceof EntityLiving)) {
                return entity.isAlive();
            } else {
                EntityLiving entityliving = (EntityLiving) entity;

                return entityliving.isAlive() && !entityliving.isSleeping();
            }
        } else {
            return entity.canUsePortal(true);
        }
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();

        if (entity instanceof EntityPlayer && !entity.isAlive() && this.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else {
            super.tick();
        }

    }

    private void playSound(World world, Vec3D vec3d) {
        world.playSound((EntityHuman) null, vec3d.x, vec3d.y, vec3d.z, SoundEffects.PLAYER_TELEPORT, SoundCategory.PLAYERS);
    }

    @Override
    public boolean canChangeDimensions(World world, World world1) {
        if (world.dimension() == World.END) {
            Entity entity = this.getOwner();

            if (entity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entity;

                return super.canChangeDimensions(world, world1) && entityplayer.seenCredits;
            }
        }

        return super.canChangeDimensions(world, world1);
    }

    @Override
    protected void onInsideBlock(IBlockData iblockdata) {
        super.onInsideBlock(iblockdata);
        if (iblockdata.is(Blocks.END_GATEWAY)) {
            Entity entity = this.getOwner();

            if (entity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entity;

                entityplayer.onInsideBlock(iblockdata);
            }
        }

    }
}
