package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntityFireball;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;

public abstract class AbstractWindCharge extends EntityFireball implements ItemSupplier {

    public static final AbstractWindCharge.a EXPLOSION_DAMAGE_CALCULATOR = new AbstractWindCharge.a();

    public AbstractWindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, World world) {
        super(entitytypes, world);
    }

    public AbstractWindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, World world, Entity entity, double d0, double d1, double d2) {
        super(entitytypes, d0, d1, d2, world);
        this.setOwner(entity);
    }

    AbstractWindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, double d0, double d1, double d2, double d3, double d4, double d5, World world) {
        super(entitytypes, d0, d1, d2, d3, d4, d5, world);
    }

    @Override
    protected AxisAlignedBB makeBoundingBox() {
        float f = this.getType().getDimensions().width() / 2.0F;
        float f1 = this.getType().getDimensions().height();
        float f2 = 0.15F;

        return new AxisAlignedBB(this.position().x - (double) f, this.position().y - 0.15000000596046448D, this.position().z - (double) f, this.position().x + (double) f, this.position().y - 0.15000000596046448D + (double) f1, this.position().z + (double) f);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return entity instanceof AbstractWindCharge ? false : super.canCollideWith(entity);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof AbstractWindCharge ? false : (entity.getType() == EntityTypes.END_CRYSTAL ? false : super.canHitEntity(entity));
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        if (!this.level().isClientSide) {
            Entity entity = this.getOwner();
            EntityLiving entityliving;

            if (entity instanceof EntityLiving) {
                EntityLiving entityliving1 = (EntityLiving) entity;

                entityliving = entityliving1;
            } else {
                entityliving = null;
            }

            EntityLiving entityliving2 = entityliving;
            Entity entity1 = (Entity) movingobjectpositionentity.getEntity().getPassengerClosestTo(movingobjectpositionentity.getLocation()).orElse(movingobjectpositionentity.getEntity());

            if (entityliving2 != null) {
                entityliving2.setLastHurtMob(entity1);
            }

            entity1.hurt(this.damageSources().windCharge(this, entityliving2), 1.0F);
            this.explode();
        }
    }

    @Override
    public void push(double d0, double d1, double d2) {}

    public abstract void explode();

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        if (!this.level().isClientSide) {
            this.explode();
            this.discard();
        }

    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            this.discard();
        }

    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected float getLiquidInertia() {
        return this.getInertia();
    }

    @Nullable
    @Override
    protected ParticleParam getTrailParticle() {
        return null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.getBlockY() > this.level().getMaxBuildHeight() + 30) {
            this.explode();
            this.discard();
        } else {
            super.tick();
        }

    }

    public static class a extends ExplosionDamageCalculator {

        public a() {}

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
            return iblockdata.is(TagsBlock.BLOCKS_WIND_CHARGE_EXPLOSIONS) ? Optional.of(3600000.0F) : Optional.empty();
        }
    }
}
