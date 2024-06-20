package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public class Explosion {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private final boolean fire;
    private final Explosion.Effect blockInteraction;
    private final RandomSource random;
    private final World level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    public final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ParticleParam smallExplosionParticles;
    private final ParticleParam largeExplosionParticles;
    private final Holder<SoundEffect> explosionSound;
    private final ObjectArrayList<BlockPosition> toBlow;
    private final Map<EntityHuman, Vec3D> hitPlayers;

    public static DamageSource getDefaultDamageSource(World world, @Nullable Entity entity) {
        return world.damageSources().explosion(entity, getIndirectSourceEntityInternal(entity));
    }

    public Explosion(World world, @Nullable Entity entity, double d0, double d1, double d2, float f, List<BlockPosition> list, Explosion.Effect explosion_effect, ParticleParam particleparam, ParticleParam particleparam1, Holder<SoundEffect> holder) {
        this(world, entity, getDefaultDamageSource(world, entity), (ExplosionDamageCalculator) null, d0, d1, d2, f, false, explosion_effect, particleparam, particleparam1, holder);
        this.toBlow.addAll(list);
    }

    public Explosion(World world, @Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect, List<BlockPosition> list) {
        this(world, entity, d0, d1, d2, f, flag, explosion_effect);
        this.toBlow.addAll(list);
    }

    public Explosion(World world, @Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect) {
        this(world, entity, getDefaultDamageSource(world, entity), (ExplosionDamageCalculator) null, d0, d1, d2, f, flag, explosion_effect, Particles.EXPLOSION, Particles.EXPLOSION_EMITTER, SoundEffects.GENERIC_EXPLODE);
    }

    public Explosion(World world, @Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect, ParticleParam particleparam, ParticleParam particleparam1, Holder<SoundEffect> holder) {
        this.random = RandomSource.create();
        this.toBlow = new ObjectArrayList();
        this.hitPlayers = Maps.newHashMap();
        this.level = world;
        this.source = entity;
        this.radius = f;
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.fire = flag;
        this.blockInteraction = explosion_effect;
        this.damageSource = damagesource == null ? world.damageSources().explosion(this) : damagesource;
        this.damageCalculator = explosiondamagecalculator == null ? this.makeDamageCalculator(entity) : explosiondamagecalculator;
        this.smallExplosionParticles = particleparam;
        this.largeExplosionParticles = particleparam1;
        this.explosionSound = holder;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return (ExplosionDamageCalculator) (entity == null ? Explosion.EXPLOSION_DAMAGE_CALCULATOR : new ExplosionDamageCalculatorEntity(entity));
    }

    public static float getSeenPercent(Vec3D vec3d, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            int i = 0;
            int j = 0;

            for (double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
                for (double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
                    for (double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
                        double d8 = MathHelper.lerp(d5, axisalignedbb.minX, axisalignedbb.maxX);
                        double d9 = MathHelper.lerp(d6, axisalignedbb.minY, axisalignedbb.maxY);
                        double d10 = MathHelper.lerp(d7, axisalignedbb.minZ, axisalignedbb.maxZ);
                        Vec3D vec3d1 = new Vec3D(d8 + d3, d9, d10 + d4);

                        if (entity.level().clip(new RayTrace(vec3d1, vec3d, RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, entity)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float) i / (float) j;
        } else {
            return 0.0F;
        }
    }

    public float radius() {
        return this.radius;
    }

    public Vec3D center() {
        return new Vec3D(this.x, this.y, this.z);
    }

    public void explode() {
        this.level.gameEvent(this.source, (Holder) GameEvent.EXPLODE, new Vec3D(this.x, this.y, this.z));
        Set<BlockPosition> set = Sets.newHashSet();
        boolean flag = true;

        int i;
        int j;

        for (int k = 0; k < 16; ++k) {
            for (i = 0; i < 16; ++i) {
                for (j = 0; j < 16; ++j) {
                    if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                        double d0 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) i / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d5 = this.y;
                        double d6 = this.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPosition blockposition = BlockPosition.containing(d4, d5, d6);
                            IBlockData iblockdata = this.level.getBlockState(blockposition);
                            Fluid fluid = this.level.getFluidState(blockposition);

                            if (!this.level.isInWorldBounds(blockposition)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockposition, iblockdata, fluid);

                            if (optional.isPresent()) {
                                f -= ((Float) optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockposition, iblockdata, f)) {
                                set.add(blockposition);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d5 += d1 * 0.30000001192092896D;
                            d6 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);
        float f2 = this.radius * 2.0F;

        i = MathHelper.floor(this.x - (double) f2 - 1.0D);
        j = MathHelper.floor(this.x + (double) f2 + 1.0D);
        int l = MathHelper.floor(this.y - (double) f2 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double) f2 + 1.0D);
        int j1 = MathHelper.floor(this.z - (double) f2 - 1.0D);
        int k1 = MathHelper.floor(this.z + (double) f2 + 1.0D);
        List<Entity> list = this.level.getEntities(this.source, new AxisAlignedBB((double) i, (double) l, (double) j1, (double) j, (double) i1, (double) k1));
        Vec3D vec3d = new Vec3D(this.x, this.y, this.z);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (!entity.ignoreExplosion(this)) {
                double d7 = Math.sqrt(entity.distanceToSqr(vec3d)) / (double) f2;

                if (d7 <= 1.0D) {
                    double d8 = entity.getX() - this.x;
                    double d9 = (entity instanceof EntityTNTPrimed ? entity.getY() : entity.getEyeY()) - this.y;
                    double d10 = entity.getZ() - this.z;
                    double d11 = Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10);

                    if (d11 != 0.0D) {
                        d8 /= d11;
                        d9 /= d11;
                        d10 /= d11;
                        if (this.damageCalculator.shouldDamageEntity(this, entity)) {
                            entity.hurt(this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity));
                        }

                        double d12 = (1.0D - d7) * (double) getSeenPercent(vec3d, entity) * (double) this.damageCalculator.getKnockbackMultiplier(entity);
                        double d13;

                        if (entity instanceof EntityLiving) {
                            EntityLiving entityliving = (EntityLiving) entity;

                            d13 = d12 * (1.0D - entityliving.getAttributeValue(GenericAttributes.EXPLOSION_KNOCKBACK_RESISTANCE));
                        } else {
                            d13 = d12;
                        }

                        d8 *= d13;
                        d9 *= d13;
                        d10 *= d13;
                        Vec3D vec3d1 = new Vec3D(d8, d9, d10);

                        entity.setDeltaMovement(entity.getDeltaMovement().add(vec3d1));
                        if (entity instanceof EntityHuman) {
                            EntityHuman entityhuman = (EntityHuman) entity;

                            if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.getAbilities().flying)) {
                                this.hitPlayers.put(entityhuman, vec3d1);
                            }
                        }

                        entity.onExplosionHit(this.source);
                    }
                }
            }
        }

    }

    public void finalizeExplosion(boolean flag) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, (SoundEffect) this.explosionSound.value(), SoundCategory.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean flag1 = this.interactsWithBlocks();

        if (flag) {
            ParticleParam particleparam;

            if (this.radius >= 2.0F && flag1) {
                particleparam = this.largeExplosionParticles;
            } else {
                particleparam = this.smallExplosionParticles;
            }

            this.level.addParticle(particleparam, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        }

        if (flag1) {
            this.level.getProfiler().push("explosion_blocks");
            List<Pair<ItemStack, BlockPosition>> list = new ArrayList();

            SystemUtils.shuffle(this.toBlow, this.level.random);
            ObjectListIterator objectlistiterator = this.toBlow.iterator();

            while (objectlistiterator.hasNext()) {
                BlockPosition blockposition = (BlockPosition) objectlistiterator.next();

                this.level.getBlockState(blockposition).onExplosionHit(this.level, blockposition, this, (itemstack, blockposition1) -> {
                    addOrAppendStack(list, itemstack, blockposition1);
                });
            }

            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Pair<ItemStack, BlockPosition> pair = (Pair) iterator.next();

                Block.popResource(this.level, (BlockPosition) pair.getSecond(), (ItemStack) pair.getFirst());
            }

            this.level.getProfiler().pop();
        }

        if (this.fire) {
            ObjectListIterator objectlistiterator1 = this.toBlow.iterator();

            while (objectlistiterator1.hasNext()) {
                BlockPosition blockposition1 = (BlockPosition) objectlistiterator1.next();

                if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockposition1).isAir() && this.level.getBlockState(blockposition1.below()).isSolidRender(this.level, blockposition1.below())) {
                    this.level.setBlockAndUpdate(blockposition1, BlockFireAbstract.getState(this.level, blockposition1));
                }
            }
        }

    }

    private static void addOrAppendStack(List<Pair<ItemStack, BlockPosition>> list, ItemStack itemstack, BlockPosition blockposition) {
        for (int i = 0; i < list.size(); ++i) {
            Pair<ItemStack, BlockPosition> pair = (Pair) list.get(i);
            ItemStack itemstack1 = (ItemStack) pair.getFirst();

            if (EntityItem.areMergable(itemstack1, itemstack)) {
                list.set(i, Pair.of(EntityItem.merge(itemstack1, itemstack, 16), (BlockPosition) pair.getSecond()));
                if (itemstack.isEmpty()) {
                    return;
                }
            }
        }

        list.add(Pair.of(itemstack, blockposition));
    }

    public boolean interactsWithBlocks() {
        return this.blockInteraction != Explosion.Effect.KEEP;
    }

    public Map<EntityHuman, Vec3D> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    private static EntityLiving getIndirectSourceEntityInternal(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        } else if (entity instanceof EntityTNTPrimed) {
            EntityTNTPrimed entitytntprimed = (EntityTNTPrimed) entity;

            return entitytntprimed.getOwner();
        } else if (entity instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) entity;

            return entityliving;
        } else {
            if (entity instanceof IProjectile) {
                IProjectile iprojectile = (IProjectile) entity;
                Entity entity1 = iprojectile.getOwner();

                if (entity1 instanceof EntityLiving) {
                    EntityLiving entityliving1 = (EntityLiving) entity1;

                    return entityliving1;
                }
            }

            return null;
        }
    }

    @Nullable
    public EntityLiving getIndirectSourceEntity() {
        return getIndirectSourceEntityInternal(this.source);
    }

    @Nullable
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPosition> getToBlow() {
        return this.toBlow;
    }

    public Explosion.Effect getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleParam getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleParam getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public Holder<SoundEffect> getExplosionSound() {
        return this.explosionSound;
    }

    public boolean canTriggerBlocks() {
        return this.blockInteraction == Explosion.Effect.TRIGGER_BLOCK && !this.level.isClientSide() ? (this.source != null && this.source.getType() == EntityTypes.BREEZE_WIND_CHARGE ? this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) : true) : false;
    }

    public static enum Effect {

        KEEP, DESTROY, DESTROY_WITH_DECAY, TRIGGER_BLOCK;

        private Effect() {}
    }
}
