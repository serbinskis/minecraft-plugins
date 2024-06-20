package net.minecraft.world.entity.animal.armadillo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.TimeRange;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorMakeLoveAnimal;
import net.minecraft.world.entity.ai.behavior.BehaviorNop;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorSwim;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomLookAround;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

public class ArmadilloAi {

    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0F;
    private static final double DEFAULT_CLOSE_ENOUGH_DIST = 2.0D;
    private static final double BABY_CLOSE_ENOUGH_DIST = 1.0D;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super Armadillo>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.ARMADILLO_TEMPTATIONS, SensorType.NEAREST_ADULT, SensorType.ARMADILLO_SCARE_DETECTED);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.DANGER_DETECTED_RECENTLY});
    private static final OneShot<Armadillo> ARMADILLO_ROLLING_OUT = BehaviorBuilder.create((behaviorbuilder_b) -> {
        return behaviorbuilder_b.group(behaviorbuilder_b.absent(MemoryModuleType.DANGER_DETECTED_RECENTLY)).apply(behaviorbuilder_b, (memoryaccessor) -> {
            return (worldserver, armadillo, i) -> {
                if (armadillo.isScared()) {
                    armadillo.rollOut();
                    return true;
                } else {
                    return false;
                }
            };
        });
    });

    public ArmadilloAi() {}

    public static BehaviorController.b<Armadillo> brainProvider() {
        return BehaviorController.provider(ArmadilloAi.MEMORY_TYPES, ArmadilloAi.SENSOR_TYPES);
    }

    protected static BehaviorController<?> makeBrain(BehaviorController<Armadillo> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initScaredActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(Set.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    private static void initCoreActivity(BehaviorController<Armadillo> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorSwim(0.8F), new ArmadilloAi.b(2.0F), new BehaviorLook(45, 90), new BehavorMove() {
            @Override
            protected boolean checkExtraStartConditions(WorldServer worldserver, EntityInsentient entityinsentient) {
                if (entityinsentient instanceof Armadillo armadillo) {
                    if (armadillo.isScared()) {
                        return false;
                    }
                }

                return super.checkExtraStartConditions(worldserver, entityinsentient);
            }
        }, new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS), ArmadilloAi.ARMADILLO_ROLLING_OUT));
    }

    private static void initIdleActivity(BehaviorController<Armadillo> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0, SetEntityLookTargetSometimes.create(EntityTypes.PLAYER, 6.0F, UniformInt.of(30, 60))), Pair.of(1, new BehaviorMakeLoveAnimal(EntityTypes.ARMADILLO, 1.0F, 1)), Pair.of(2, new BehaviorGateSingle<>(ImmutableList.of(Pair.of(new FollowTemptation((entityliving) -> {
            return 1.25F;
        }, (entityliving) -> {
            return entityliving.isBaby() ? 1.0D : 2.0D;
        }), 1), Pair.of(BehaviorFollowAdult.create(ArmadilloAi.ADULT_FOLLOW_RANGE, 1.25F), 1)))), Pair.of(3, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)), Pair.of(4, new BehaviorGateSingle<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(1.0F), 1), Pair.of(BehaviorLookWalk.create(1.0F, 3), 1), Pair.of(new BehaviorNop(30, 60), 1))))));
    }

    private static void initScaredActivity(BehaviorController<Armadillo> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.PANIC, ImmutableList.of(Pair.of(0, new ArmadilloAi.a())), Set.of(Pair.of(MemoryModuleType.DANGER_DETECTED_RECENTLY, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT)));
    }

    public static void updateActivity(Armadillo armadillo) {
        armadillo.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.PANIC, Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return (itemstack) -> {
            return itemstack.is(TagsItem.ARMADILLO_FOOD);
        };
    }

    public static class b extends AnimalPanic<Armadillo> {

        public b(float f) {
            super(f, (entitycreature) -> {
                return DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES;
            });
        }

        protected void start(WorldServer worldserver, Armadillo armadillo, long i) {
            armadillo.rollOut();
            super.start(worldserver, (EntityCreature) armadillo, i);
        }
    }

    public static class a extends Behavior<Armadillo> {

        static final int BALL_UP_STAY_IN_STATE = 5 * TimeRange.SECONDS_PER_MINUTE * 20;
        static final int TICKS_DELAY_TO_DETERMINE_IF_DANGER_IS_STILL_AROUND = 5;
        static final int DANGER_DETECTED_RECENTLY_DANGER_THRESHOLD = 75;
        int nextPeekTimer = 0;
        boolean dangerWasAround;

        public a() {
            super(Map.of(), ArmadilloAi.a.BALL_UP_STAY_IN_STATE);
        }

        protected void tick(WorldServer worldserver, Armadillo armadillo, long i) {
            super.tick(worldserver, armadillo, i);
            if (this.nextPeekTimer > 0) {
                --this.nextPeekTimer;
            }

            if (armadillo.shouldSwitchToScaredState()) {
                armadillo.switchToState(Armadillo.a.SCARED);
                if (armadillo.onGround()) {
                    armadillo.playSound(SoundEffects.ARMADILLO_LAND);
                }

            } else {
                Armadillo.a armadillo_a = armadillo.getState();
                long j = armadillo.getBrain().getTimeUntilExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY);
                boolean flag = j > 75L;

                if (flag != this.dangerWasAround) {
                    this.nextPeekTimer = this.pickNextPeekTimer(armadillo);
                }

                this.dangerWasAround = flag;
                if (armadillo_a == Armadillo.a.SCARED) {
                    if (this.nextPeekTimer == 0 && armadillo.onGround() && flag) {
                        worldserver.broadcastEntityEvent(armadillo, (byte) 64);
                        this.nextPeekTimer = this.pickNextPeekTimer(armadillo);
                    }

                    if (j < (long) Armadillo.a.UNROLLING.animationDuration()) {
                        armadillo.playSound(SoundEffects.ARMADILLO_UNROLL_START);
                        armadillo.switchToState(Armadillo.a.UNROLLING);
                    }
                } else if (armadillo_a == Armadillo.a.UNROLLING && j > (long) Armadillo.a.UNROLLING.animationDuration()) {
                    armadillo.switchToState(Armadillo.a.SCARED);
                }

            }
        }

        private int pickNextPeekTimer(Armadillo armadillo) {
            return Armadillo.a.SCARED.animationDuration() + armadillo.getRandom().nextIntBetweenInclusive(100, 400);
        }

        protected boolean checkExtraStartConditions(WorldServer worldserver, Armadillo armadillo) {
            return armadillo.onGround();
        }

        protected boolean canStillUse(WorldServer worldserver, Armadillo armadillo, long i) {
            return armadillo.getState().isThreatened();
        }

        protected void start(WorldServer worldserver, Armadillo armadillo, long i) {
            armadillo.rollUp();
        }

        protected void stop(WorldServer worldserver, Armadillo armadillo, long i) {
            if (!armadillo.canStayRolledUp()) {
                armadillo.rollOut();
            }

        }
    }
}
