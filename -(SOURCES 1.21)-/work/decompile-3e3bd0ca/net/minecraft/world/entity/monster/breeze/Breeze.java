package net.minecraft.world.entity.monster.breeze;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.EnumRenderType;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3D;

public class Breeze extends EntityMonster {

    private static final int SLIDE_PARTICLES_AMOUNT = 20;
    private static final int IDLE_PARTICLES_AMOUNT = 1;
    private static final int JUMP_DUST_PARTICLES_AMOUNT = 20;
    private static final int JUMP_TRAIL_PARTICLES_AMOUNT = 3;
    private static final int JUMP_TRAIL_DURATION_TICKS = 5;
    private static final int JUMP_CIRCLE_DISTANCE_Y = 10;
    private static final float FALL_DISTANCE_SOUND_TRIGGER_THRESHOLD = 3.0F;
    private static final int WHIRL_SOUND_FREQUENCY_MIN = 1;
    private static final int WHIRL_SOUND_FREQUENCY_MAX = 80;
    public AnimationState idle = new AnimationState();
    public AnimationState slide = new AnimationState();
    public AnimationState slideBack = new AnimationState();
    public AnimationState longJump = new AnimationState();
    public AnimationState shoot = new AnimationState();
    public AnimationState inhale = new AnimationState();
    private int jumpTrailStartedTick = 0;
    private int soundTick = 0;
    private static final ProjectileDeflection PROJECTILE_DEFLECTION = (iprojectile, entity, randomsource) -> {
        entity.level().playSound((EntityHuman) null, entity, SoundEffects.BREEZE_DEFLECT, entity.getSoundSource(), 1.0F, 1.0F);
        ProjectileDeflection.REVERSE.deflect(iprojectile, entity, randomsource);
    };

    public static AttributeProvider.Builder createAttributes() {
        return EntityInsentient.createMobAttributes().add(GenericAttributes.MOVEMENT_SPEED, 0.6299999952316284D).add(GenericAttributes.MAX_HEALTH, 30.0D).add(GenericAttributes.FOLLOW_RANGE, 24.0D).add(GenericAttributes.ATTACK_DAMAGE, 3.0D);
    }

    public Breeze(EntityTypes<? extends EntityMonster> entitytypes, World world) {
        super(entitytypes, world);
        this.setPathfindingMalus(PathType.DANGER_TRAPDOOR, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.xpReward = 10;
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return BreezeAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public BehaviorController<Breeze> getBrain() {
        return super.getBrain();
    }

    @Override
    protected BehaviorController.b<Breeze> brainProvider() {
        return BehaviorController.provider(BreezeAi.MEMORY_TYPES, BreezeAi.SENSOR_TYPES);
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (this.level().isClientSide() && Breeze.DATA_POSE.equals(datawatcherobject)) {
            this.resetAnimations();
            EntityPose entitypose = this.getPose();

            switch (entitypose) {
                case SHOOTING:
                    this.shoot.startIfStopped(this.tickCount);
                    break;
                case INHALING:
                    this.longJump.startIfStopped(this.tickCount);
                    break;
                case SLIDING:
                    this.slide.startIfStopped(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(datawatcherobject);
    }

    private void resetAnimations() {
        this.shoot.stop();
        this.idle.stop();
        this.inhale.stop();
        this.longJump.stop();
    }

    @Override
    public void tick() {
        EntityPose entitypose = this.getPose();

        switch (entitypose) {
            case SHOOTING:
            case INHALING:
            case STANDING:
                this.resetJumpTrail().emitGroundParticles(1 + this.getRandom().nextInt(1));
                break;
            case SLIDING:
                this.emitGroundParticles(20);
                break;
            case LONG_JUMPING:
                this.emitJumpTrailParticles();
        }

        if (entitypose != EntityPose.SLIDING && this.slide.isStarted()) {
            this.slideBack.start(this.tickCount);
            this.slide.stop();
        }

        this.soundTick = this.soundTick == 0 ? this.random.nextIntBetweenInclusive(1, 80) : this.soundTick - 1;
        if (this.soundTick == 0) {
            this.playWhirlSound();
        }

        super.tick();
    }

    public Breeze resetJumpTrail() {
        this.jumpTrailStartedTick = 0;
        return this;
    }

    public void emitJumpTrailParticles() {
        if (++this.jumpTrailStartedTick <= 5) {
            IBlockData iblockdata = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
            Vec3D vec3d = this.getDeltaMovement();
            Vec3D vec3d1 = this.position().add(vec3d).add(0.0D, 0.10000000149011612D, 0.0D);

            for (int i = 0; i < 3; ++i) {
                this.level().addParticle(new ParticleParamBlock(Particles.BLOCK, iblockdata), vec3d1.x, vec3d1.y, vec3d1.z, 0.0D, 0.0D, 0.0D);
            }

        }
    }

    public void emitGroundParticles(int i) {
        if (!this.isPassenger()) {
            Vec3D vec3d = this.getBoundingBox().getCenter();
            Vec3D vec3d1 = new Vec3D(vec3d.x, this.position().y, vec3d.z);
            IBlockData iblockdata = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();

            if (iblockdata.getRenderShape() != EnumRenderType.INVISIBLE) {
                for (int j = 0; j < i; ++j) {
                    this.level().addParticle(new ParticleParamBlock(Particles.BLOCK, iblockdata), vec3d1.x, vec3d1.y, vec3d1.z, 0.0D, 0.0D, 0.0D);
                }

            }
        }
    }

    @Override
    public void playAmbientSound() {
        if (this.getTarget() == null || !this.onGround()) {
            this.level().playLocalSound(this, this.getAmbientSound(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    public void playWhirlSound() {
        float f = 0.7F + 0.4F * this.random.nextFloat();
        float f1 = 0.8F + 0.2F * this.random.nextFloat();

        this.level().playLocalSound(this, SoundEffects.BREEZE_WHIRL, this.getSoundSource(), f1, f);
    }

    @Override
    public ProjectileDeflection deflection(IProjectile iprojectile) {
        return iprojectile.getType() != EntityTypes.BREEZE_WIND_CHARGE && iprojectile.getType() != EntityTypes.WIND_CHARGE ? (this.getType().is(TagsEntity.DEFLECTS_PROJECTILES) ? Breeze.PROJECTILE_DEFLECTION : ProjectileDeflection.NONE) : ProjectileDeflection.NONE;
    }

    @Override
    public SoundCategory getSoundSource() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.BREEZE_DEATH;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.BREEZE_HURT;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return this.onGround() ? SoundEffects.BREEZE_IDLE_GROUND : SoundEffects.BREEZE_IDLE_AIR;
    }

    public Optional<EntityLiving> getHurtBy() {
        return this.getBrain().getMemory(MemoryModuleType.HURT_BY).map(DamageSource::getEntity).filter((entity) -> {
            return entity instanceof EntityLiving;
        }).map((entity) -> {
            return (EntityLiving) entity;
        });
    }

    public boolean withinInnerCircleRange(Vec3D vec3d) {
        Vec3D vec3d1 = this.blockPosition().getCenter();

        return vec3d.closerThan(vec3d1, 4.0D, 10.0D);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("breezeBrain");
        this.getBrain().tick((WorldServer) this.level(), this);
        this.level().getProfiler().popPush("breezeActivityUpdate");
        BreezeAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
        PacketDebug.sendBreezeInfo(this);
    }

    @Override
    public boolean canAttackType(EntityTypes<?> entitytypes) {
        return entitytypes == EntityTypes.PLAYER || entitytypes == EntityTypes.IRON_GOLEM;
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    public int getHeadRotSpeed() {
        return 25;
    }

    public double getSnoutYPosition() {
        return this.getEyeY() - 0.4D;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damagesource) {
        return damagesource.getEntity() instanceof Breeze || super.isInvulnerableTo(damagesource);
    }

    @Override
    public double getFluidJumpThreshold() {
        return (double) this.getEyeHeight();
    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
        if (f > 3.0F) {
            this.playSound(SoundEffects.BREEZE_LAND, 1.0F, 1.0F);
        }

        return super.causeFallDamage(f, f1, damagesource);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Nullable
    @Override
    public EntityLiving getTarget() {
        return this.getTargetFromBrain();
    }
}
