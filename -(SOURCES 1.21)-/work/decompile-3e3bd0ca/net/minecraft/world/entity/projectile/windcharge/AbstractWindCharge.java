package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntityFireball;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

public abstract class AbstractWindCharge extends EntityFireball implements ItemSupplier {

    public static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(true, false, Optional.empty(), BuiltInRegistries.BLOCK.getTag(TagsBlock.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()));
    public static final double JUMP_SCALE = 0.25D;

    public AbstractWindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, World world) {
        super(entitytypes, world);
        this.accelerationPower = 0.0D;
    }

    public AbstractWindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, World world, Entity entity, double d0, double d1, double d2) {
        super(entitytypes, d0, d1, d2, world);
        this.setOwner(entity);
        this.accelerationPower = 0.0D;
    }

    AbstractWindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, double d0, double d1, double d2, Vec3D vec3d, World world) {
        super(entitytypes, d0, d1, d2, vec3d, world);
        this.accelerationPower = 0.0D;
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
            Entity entity1 = movingobjectpositionentity.getEntity();

            if (entityliving2 != null) {
                entityliving2.setLastHurtMob(entity1);
            }

            DamageSource damagesource = this.damageSources().windCharge(this, entityliving2);

            if (entity1.hurt(damagesource, 1.0F) && entity1 instanceof EntityLiving) {
                EntityLiving entityliving3 = (EntityLiving) entity1;

                EnchantmentManager.doPostAttackEffects((WorldServer) this.level(), entityliving3, damagesource);
            }

            this.explode(this.position());
        }
    }

    @Override
    public void push(double d0, double d1, double d2) {}

    public abstract void explode(Vec3D vec3d);

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        if (!this.level().isClientSide) {
            BaseBlockPosition baseblockposition = movingobjectpositionblock.getDirection().getNormal();
            Vec3D vec3d = Vec3D.atLowerCornerOf(baseblockposition).multiply(0.25D, 0.25D, 0.25D);
            Vec3D vec3d1 = movingobjectpositionblock.getLocation().add(vec3d);

            this.explode(vec3d1);
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
            this.explode(this.position());
            this.discard();
        } else {
            super.tick();
        }

    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        return false;
    }
}
