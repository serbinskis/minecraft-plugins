package net.minecraft.world.entity.animal.camel;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.IJumpable;
import net.minecraft.world.entity.ISaddleable;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerLook;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.entity.ai.control.EntityAIBodyControl;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.animal.horse.EntityHorseAbstract;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;

public class Camel extends EntityHorseAbstract implements IJumpable, ISaddleable {

    public static final float BABY_SCALE = 0.45F;
    public static final int DASH_COOLDOWN_TICKS = 55;
    public static final int MAX_HEAD_Y_ROT = 30;
    private static final float RUNNING_SPEED_BONUS = 0.1F;
    private static final float DASH_VERTICAL_MOMENTUM = 1.4285F;
    private static final float DASH_HORIZONTAL_MOMENTUM = 22.2222F;
    private static final int DASH_MINIMUM_DURATION_TICKS = 5;
    private static final int SITDOWN_DURATION_TICKS = 40;
    private static final int STANDUP_DURATION_TICKS = 52;
    private static final int IDLE_MINIMAL_DURATION_TICKS = 80;
    private static final float SITTING_HEIGHT_DIFFERENCE = 1.43F;
    public static final DataWatcherObject<Boolean> DASH = DataWatcher.defineId(Camel.class, DataWatcherRegistry.BOOLEAN);
    public static final DataWatcherObject<Long> LAST_POSE_CHANGE_TICK = DataWatcher.defineId(Camel.class, DataWatcherRegistry.LONG);
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState sitPoseAnimationState = new AnimationState();
    public final AnimationState sitUpAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();
    private static final EntitySize SITTING_DIMENSIONS = EntitySize.scalable(EntityTypes.CAMEL.getWidth(), EntityTypes.CAMEL.getHeight() - 1.43F).withEyeHeight(0.845F);
    private int dashCooldown = 0;
    private int idleAnimationTimeout = 0;

    public Camel(EntityTypes<? extends Camel> entitytypes, World world) {
        super(entitytypes, world);
        this.moveControl = new Camel.c();
        this.lookControl = new Camel.b();
        Navigation navigation = (Navigation) this.getNavigation();

        navigation.setCanFloat(true);
        navigation.setCanWalkOverFences(true);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putLong("LastPoseTick", (Long) this.entityData.get(Camel.LAST_POSE_CHANGE_TICK));
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        long i = nbttagcompound.getLong("LastPoseTick");

        if (i < 0L) {
            this.setPose(EntityPose.SITTING);
        }

        this.resetLastPoseChangeTick(i);
    }

    public static AttributeProvider.Builder createAttributes() {
        return createBaseHorseAttributes().add(GenericAttributes.MAX_HEALTH, 32.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.09000000357627869D).add(GenericAttributes.JUMP_STRENGTH, 0.41999998688697815D).add(GenericAttributes.STEP_HEIGHT, 1.5D);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(Camel.DASH, false);
        datawatcher_a.define(Camel.LAST_POSE_CHANGE_TICK, 0L);
    }

    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        CamelAi.initMemories(this, worldaccess.getRandom());
        this.resetLastPoseChangeTickToFullStand(worldaccess.getLevel().getGameTime());
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, groupdataentity);
    }

    @Override
    protected BehaviorController.b<Camel> brainProvider() {
        return CamelAi.brainProvider();
    }

    @Override
    protected void registerGoals() {}

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return CamelAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public EntitySize getDefaultDimensions(EntityPose entitypose) {
        return entitypose == EntityPose.SITTING ? Camel.SITTING_DIMENSIONS.scale(this.getAgeScale()) : super.getDefaultDimensions(entitypose);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("camelBrain");
        BehaviorController<?> behaviorcontroller = this.getBrain();

        behaviorcontroller.tick((WorldServer) this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("camelActivityUpdate");
        CamelAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isDashing() && this.dashCooldown < 50 && (this.onGround() || this.isInLiquid() || this.isPassenger())) {
            this.setDashing(false);
        }

        if (this.dashCooldown > 0) {
            --this.dashCooldown;
            if (this.dashCooldown == 0) {
                this.level().playSound((EntityHuman) null, this.blockPosition(), SoundEffects.CAMEL_DASH_READY, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }

        if (this.refuseToMove()) {
            this.clampHeadRotationToBody();
        }

        if (this.isCamelSitting() && this.isInWater()) {
            this.standUpInstantly();
        }

    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        if (this.isCamelVisuallySitting()) {
            this.sitUpAnimationState.stop();
            this.dashAnimationState.stop();
            if (this.isVisuallySittingDown()) {
                this.sitAnimationState.startIfStopped(this.tickCount);
                this.sitPoseAnimationState.stop();
            } else {
                this.sitAnimationState.stop();
                this.sitPoseAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.sitAnimationState.stop();
            this.sitPoseAnimationState.stop();
            this.dashAnimationState.animateWhen(this.isDashing(), this.tickCount);
            this.sitUpAnimationState.animateWhen(this.isInPoseTransition() && this.getPoseTime() >= 0L, this.tickCount);
        }

    }

    @Override
    protected void updateWalkAnimation(float f) {
        float f1;

        if (this.getPose() == EntityPose.STANDING && !this.dashAnimationState.isStarted()) {
            f1 = Math.min(f * 6.0F, 1.0F);
        } else {
            f1 = 0.0F;
        }

        this.walkAnimation.update(f1, 0.2F);
    }

    @Override
    public void travel(Vec3D vec3d) {
        if (this.refuseToMove() && this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            vec3d = vec3d.multiply(0.0D, 1.0D, 0.0D);
        }

        super.travel(vec3d);
    }

    @Override
    protected void tickRidden(EntityHuman entityhuman, Vec3D vec3d) {
        super.tickRidden(entityhuman, vec3d);
        if (entityhuman.zza > 0.0F && this.isCamelSitting() && !this.isInPoseTransition()) {
            this.standUp();
        }

    }

    public boolean refuseToMove() {
        return this.isCamelSitting() || this.isInPoseTransition();
    }

    @Override
    protected float getRiddenSpeed(EntityHuman entityhuman) {
        float f = entityhuman.isSprinting() && this.getJumpCooldown() == 0 ? 0.1F : 0.0F;

        return (float) this.getAttributeValue(GenericAttributes.MOVEMENT_SPEED) + f;
    }

    @Override
    protected Vec2F getRiddenRotation(EntityLiving entityliving) {
        return this.refuseToMove() ? new Vec2F(this.getXRot(), this.getYRot()) : super.getRiddenRotation(entityliving);
    }

    @Override
    protected Vec3D getRiddenInput(EntityHuman entityhuman, Vec3D vec3d) {
        return this.refuseToMove() ? Vec3D.ZERO : super.getRiddenInput(entityhuman, vec3d);
    }

    @Override
    public boolean canJump() {
        return !this.refuseToMove() && super.canJump();
    }

    @Override
    public void onPlayerJump(int i) {
        if (this.isSaddled() && this.dashCooldown <= 0 && this.onGround()) {
            super.onPlayerJump(i);
        }
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected void executeRidersJump(float f, Vec3D vec3d) {
        double d0 = (double) this.getJumpPower();

        this.addDeltaMovement(this.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) (22.2222F * f) * this.getAttributeValue(GenericAttributes.MOVEMENT_SPEED) * (double) this.getBlockSpeedFactor()).add(0.0D, (double) (1.4285F * f) * d0, 0.0D));
        this.dashCooldown = 55;
        this.setDashing(true);
        this.hasImpulse = true;
    }

    public boolean isDashing() {
        return (Boolean) this.entityData.get(Camel.DASH);
    }

    public void setDashing(boolean flag) {
        this.entityData.set(Camel.DASH, flag);
    }

    @Override
    public void handleStartJump(int i) {
        this.makeSound(SoundEffects.CAMEL_DASH);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.setDashing(true);
    }

    @Override
    public void handleStopJump() {}

    @Override
    public int getJumpCooldown() {
        return this.dashCooldown;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.CAMEL_AMBIENT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.CAMEL_DEATH;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.CAMEL_HURT;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        if (iblockdata.is(TagsBlock.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
            this.playSound(SoundEffects.CAMEL_STEP_SAND, 1.0F, 1.0F);
        } else {
            this.playSound(SoundEffects.CAMEL_STEP, 1.0F, 1.0F);
        }

    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.CAMEL_FOOD);
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (entityhuman.isSecondaryUseActive() && !this.isBaby()) {
            this.openCustomInventoryScreen(entityhuman);
            return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            EnumInteractionResult enuminteractionresult = itemstack.interactLivingEntity(entityhuman, this, enumhand);

            if (enuminteractionresult.consumesAction()) {
                return enuminteractionresult;
            } else if (this.isFood(itemstack)) {
                return this.fedFood(entityhuman, itemstack);
            } else {
                if (this.getPassengers().size() < 2 && !this.isBaby()) {
                    this.doPlayerRide(entityhuman);
                }

                return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
    }

    @Override
    public boolean handleLeashAtDistance(Entity entity, float f) {
        if (f > 6.0F && this.isCamelSitting() && !this.isInPoseTransition() && this.canCamelChangePose()) {
            this.standUp();
        }

        return true;
    }

    public boolean canCamelChangePose() {
        return this.wouldNotSuffocateAtTargetPose(this.isCamelSitting() ? EntityPose.STANDING : EntityPose.SITTING);
    }

    @Override
    protected boolean handleEating(EntityHuman entityhuman, ItemStack itemstack) {
        if (!this.isFood(itemstack)) {
            return false;
        } else {
            boolean flag = this.getHealth() < this.getMaxHealth();

            if (flag) {
                this.heal(2.0F);
            }

            boolean flag1 = this.isTamed() && this.getAge() == 0 && this.canFallInLove();

            if (flag1) {
                this.setInLove(entityhuman);
            }

            boolean flag2 = this.isBaby();

            if (flag2) {
                this.level().addParticle(Particles.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
                if (!this.level().isClientSide) {
                    this.ageUp(10);
                }
            }

            if (!flag && !flag1 && !flag2) {
                return false;
            } else {
                if (!this.isSilent()) {
                    SoundEffect soundeffect = this.getEatingSound();

                    if (soundeffect != null) {
                        this.level().playSound((EntityHuman) null, this.getX(), this.getY(), this.getZ(), soundeffect, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                    }
                }

                this.gameEvent(GameEvent.EAT);
                return true;
            }
        }
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    @Override
    public boolean canMate(EntityAnimal entityanimal) {
        boolean flag;

        if (entityanimal != this && entityanimal instanceof Camel camel) {
            if (this.canParent() && camel.canParent()) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    @Nullable
    @Override
    public Camel getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        return (Camel) EntityTypes.CAMEL.create(worldserver);
    }

    @Nullable
    @Override
    protected SoundEffect getEatingSound() {
        return SoundEffects.CAMEL_EAT;
    }

    @Override
    protected void actuallyHurt(DamageSource damagesource, float f) {
        this.standUpInstantly();
        super.actuallyHurt(damagesource, f);
    }

    @Override
    protected Vec3D getPassengerAttachmentPoint(Entity entity, EntitySize entitysize, float f) {
        int i = Math.max(this.getPassengers().indexOf(entity), 0);
        boolean flag = i == 0;
        float f1 = 0.5F;
        float f2 = (float) (this.isRemoved() ? 0.009999999776482582D : this.getBodyAnchorAnimationYOffset(flag, 0.0F, entitysize, f));

        if (this.getPassengers().size() > 1) {
            if (!flag) {
                f1 = -0.7F;
            }

            if (entity instanceof EntityAnimal) {
                f1 += 0.2F;
            }
        }

        return (new Vec3D(0.0D, (double) f2, (double) (f1 * f))).yRot(-this.getYRot() * 0.017453292F);
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.45F : 1.0F;
    }

    private double getBodyAnchorAnimationYOffset(boolean flag, float f, EntitySize entitysize, float f1) {
        double d0 = (double) (entitysize.height() - 0.375F * f1);
        float f2 = f1 * 1.43F;
        float f3 = f2 - f1 * 0.2F;
        float f4 = f2 - f3;
        boolean flag1 = this.isInPoseTransition();
        boolean flag2 = this.isCamelSitting();

        if (flag1) {
            int i = flag2 ? 40 : 52;
            int j;
            float f5;

            if (flag2) {
                j = 28;
                f5 = flag ? 0.5F : 0.1F;
            } else {
                j = flag ? 24 : 32;
                f5 = flag ? 0.6F : 0.35F;
            }

            float f6 = MathHelper.clamp((float) this.getPoseTime() + f, 0.0F, (float) i);
            boolean flag3 = f6 < (float) j;
            float f7 = flag3 ? f6 / (float) j : (f6 - (float) j) / (float) (i - j);
            float f8 = f2 - f5 * f3;

            d0 += flag2 ? (double) MathHelper.lerp(f7, flag3 ? f2 : f8, flag3 ? f8 : f4) : (double) MathHelper.lerp(f7, flag3 ? f4 - f2 : f4 - f8, flag3 ? f4 - f8 : 0.0F);
        }

        if (flag2 && !flag1) {
            d0 += (double) f4;
        }

        return d0;
    }

    @Override
    public Vec3D getLeashOffset(float f) {
        EntitySize entitysize = this.getDimensions(this.getPose());
        float f1 = this.getAgeScale();

        return new Vec3D(0.0D, this.getBodyAnchorAnimationYOffset(true, f, entitysize, f1) - (double) (0.2F * f1), (double) (entitysize.width() * 0.56F));
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() <= 2;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
    }

    public boolean isCamelSitting() {
        return (Long) this.entityData.get(Camel.LAST_POSE_CHANGE_TICK) < 0L;
    }

    public boolean isCamelVisuallySitting() {
        return this.getPoseTime() < 0L != this.isCamelSitting();
    }

    public boolean isInPoseTransition() {
        long i = this.getPoseTime();

        return i < (long) (this.isCamelSitting() ? 40 : 52);
    }

    private boolean isVisuallySittingDown() {
        return this.isCamelSitting() && this.getPoseTime() < 40L && this.getPoseTime() >= 0L;
    }

    public void sitDown() {
        if (!this.isCamelSitting()) {
            this.makeSound(SoundEffects.CAMEL_SIT);
            this.setPose(EntityPose.SITTING);
            this.gameEvent(GameEvent.ENTITY_ACTION);
            this.resetLastPoseChangeTick(-this.level().getGameTime());
        }
    }

    public void standUp() {
        if (this.isCamelSitting()) {
            this.makeSound(SoundEffects.CAMEL_STAND);
            this.setPose(EntityPose.STANDING);
            this.gameEvent(GameEvent.ENTITY_ACTION);
            this.resetLastPoseChangeTick(this.level().getGameTime());
        }
    }

    public void standUpInstantly() {
        this.setPose(EntityPose.STANDING);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.resetLastPoseChangeTickToFullStand(this.level().getGameTime());
    }

    @VisibleForTesting
    public void resetLastPoseChangeTick(long i) {
        this.entityData.set(Camel.LAST_POSE_CHANGE_TICK, i);
    }

    private void resetLastPoseChangeTickToFullStand(long i) {
        this.resetLastPoseChangeTick(Math.max(0L, i - 52L - 1L));
    }

    public long getPoseTime() {
        return this.level().getGameTime() - Math.abs((Long) this.entityData.get(Camel.LAST_POSE_CHANGE_TICK));
    }

    @Override
    public SoundEffect getSaddleSoundEvent() {
        return SoundEffects.CAMEL_SADDLE;
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (!this.firstTick && Camel.DASH.equals(datawatcherobject)) {
            this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
        }

        super.onSyncedDataUpdated(datawatcherobject);
    }

    @Override
    public boolean isTamed() {
        return true;
    }

    @Override
    public void openCustomInventoryScreen(EntityHuman entityhuman) {
        if (!this.level().isClientSide) {
            entityhuman.openHorseInventory(this, this.inventory);
        }

    }

    @Override
    protected EntityAIBodyControl createBodyControl() {
        return new Camel.a(this);
    }

    private class c extends ControllerMove {

        public c() {
            super(Camel.this);
        }

        @Override
        public void tick() {
            if (this.operation == ControllerMove.Operation.MOVE_TO && !Camel.this.isLeashed() && Camel.this.isCamelSitting() && !Camel.this.isInPoseTransition() && Camel.this.canCamelChangePose()) {
                Camel.this.standUp();
            }

            super.tick();
        }
    }

    private class b extends ControllerLook {

        b() {
            super(Camel.this);
        }

        @Override
        public void tick() {
            if (!Camel.this.hasControllingPassenger()) {
                super.tick();
            }

        }
    }

    private class a extends EntityAIBodyControl {

        public a(final Camel camel) {
            super(camel);
        }

        @Override
        public void clientTick() {
            if (!Camel.this.refuseToMove()) {
                super.clientTick();
            }

        }
    }
}
