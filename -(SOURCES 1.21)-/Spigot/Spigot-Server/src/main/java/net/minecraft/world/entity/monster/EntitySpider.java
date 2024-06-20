package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalAvoidTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLeapAtTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.ai.navigation.NavigationSpider;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public class EntitySpider extends EntityMonster {

    private static final DataWatcherObject<Byte> DATA_FLAGS_ID = DataWatcher.defineId(EntitySpider.class, DataWatcherRegistry.BYTE);
    private static final float SPIDER_SPECIAL_EFFECT_CHANCE = 0.1F;

    public EntitySpider(EntityTypes<? extends EntitySpider> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(2, new PathfinderGoalAvoidTarget<>(this, Armadillo.class, 6.0F, 1.0D, 1.2D, (entityliving) -> {
            return !((Armadillo) entityliving).isScared();
        }));
        this.goalSelector.addGoal(3, new PathfinderGoalLeapAtTarget(this, 0.4F));
        this.goalSelector.addGoal(4, new EntitySpider.PathfinderGoalSpiderMeleeAttack(this));
        this.goalSelector.addGoal(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
        this.goalSelector.addGoal(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.addGoal(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.addGoal(1, new PathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.addGoal(2, new EntitySpider.PathfinderGoalSpiderNearestAttackableTarget<>(this, EntityHuman.class));
        this.targetSelector.addGoal(3, new EntitySpider.PathfinderGoalSpiderNearestAttackableTarget<>(this, EntityIronGolem.class));
    }

    @Override
    protected NavigationAbstract createNavigation(World world) {
        return new NavigationSpider(this, world);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntitySpider.DATA_FLAGS_ID, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.setClimbing(this.horizontalCollision);
        }

    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityMonster.createMonsterAttributes().add(GenericAttributes.MAX_HEALTH, 16.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.30000001192092896D);
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.SPIDER_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.SPIDER_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public void makeStuckInBlock(IBlockData iblockdata, Vec3D vec3d) {
        if (!iblockdata.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(iblockdata, vec3d);
        }

    }

    @Override
    public boolean canBeAffected(MobEffect mobeffect) {
        return mobeffect.is(MobEffects.POISON) ? false : super.canBeAffected(mobeffect);
    }

    public boolean isClimbing() {
        return ((Byte) this.entityData.get(EntitySpider.DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean flag) {
        byte b0 = (Byte) this.entityData.get(EntitySpider.DATA_FLAGS_ID);

        if (flag) {
            b0 = (byte) (b0 | 1);
        } else {
            b0 &= -2;
        }

        this.entityData.set(EntitySpider.DATA_FLAGS_ID, b0);
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        Object object = super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, groupdataentity);
        RandomSource randomsource = worldaccess.getRandom();

        if (randomsource.nextInt(100) == 0) {
            EntitySkeleton entityskeleton = (EntitySkeleton) EntityTypes.SKELETON.create(this.level());

            if (entityskeleton != null) {
                entityskeleton.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                entityskeleton.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, (GroupDataEntity) null);
                entityskeleton.startRiding(this);
            }
        }

        if (object == null) {
            object = new EntitySpider.GroupDataSpider();
            if (worldaccess.getDifficulty() == EnumDifficulty.HARD && randomsource.nextFloat() < 0.1F * difficultydamagescaler.getSpecialMultiplier()) {
                ((EntitySpider.GroupDataSpider) object).setRandomEffect(randomsource);
            }
        }

        if (object instanceof EntitySpider.GroupDataSpider entityspider_groupdataspider) {
            Holder<MobEffectList> holder = entityspider_groupdataspider.effect;

            if (holder != null) {
                this.addEffect(new MobEffect(holder, -1), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.SPIDER_SPAWN); // CraftBukkit
            }
        }

        return (GroupDataEntity) object;
    }

    @Override
    public Vec3D getVehicleAttachmentPoint(Entity entity) {
        return entity.getBbWidth() <= this.getBbWidth() ? new Vec3D(0.0D, 0.3125D * (double) this.getScale(), 0.0D) : super.getVehicleAttachmentPoint(entity);
    }

    private static class PathfinderGoalSpiderMeleeAttack extends PathfinderGoalMeleeAttack {

        public PathfinderGoalSpiderMeleeAttack(EntitySpider entityspider) {
            super(entityspider, 1.0D, true);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        @Override
        public boolean canContinueToUse() {
            float f = this.mob.getLightLevelDependentMagicValue();

            if (f >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget((EntityLiving) null);
                return false;
            } else {
                return super.canContinueToUse();
            }
        }
    }

    private static class PathfinderGoalSpiderNearestAttackableTarget<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {

        public PathfinderGoalSpiderNearestAttackableTarget(EntitySpider entityspider, Class<T> oclass) {
            super(entityspider, oclass, true);
        }

        @Override
        public boolean canUse() {
            float f = this.mob.getLightLevelDependentMagicValue();

            return f >= 0.5F ? false : super.canUse();
        }
    }

    public static class GroupDataSpider implements GroupDataEntity {

        @Nullable
        public Holder<MobEffectList> effect;

        public GroupDataSpider() {}

        public void setRandomEffect(RandomSource randomsource) {
            int i = randomsource.nextInt(5);

            if (i <= 1) {
                this.effect = MobEffects.MOVEMENT_SPEED;
            } else if (i <= 2) {
                this.effect = MobEffects.DAMAGE_BOOST;
            } else if (i <= 3) {
                this.effect = MobEffects.REGENERATION;
            } else if (i <= 4) {
                this.effect = MobEffects.INVISIBILITY;
            }

        }
    }
}
