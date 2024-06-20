package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetForget;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetSet;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorNop;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorSwim;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class BreezeAi {

    public static final float SPEED_MULTIPLIER_WHEN_SLIDING = 0.6F;
    public static final float JUMP_CIRCLE_INNER_RADIUS = 4.0F;
    public static final float JUMP_CIRCLE_MIDDLE_RADIUS = 8.0F;
    public static final float JUMP_CIRCLE_OUTER_RADIUS = 20.0F;
    static final List<SensorType<? extends Sensor<? super Breeze>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.BREEZE_ATTACK_ENTITY_SENSOR);
    static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryModuleType.BREEZE_SHOOT, MemoryModuleType.BREEZE_SHOOT_CHARGING, MemoryModuleType.BREEZE_SHOOT_RECOVERING, MemoryModuleType.BREEZE_SHOOT_COOLDOWN, new MemoryModuleType[]{MemoryModuleType.BREEZE_JUMP_TARGET, MemoryModuleType.BREEZE_LEAVING_WATER, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PATH});

    public BreezeAi() {}

    protected static BehaviorController<?> makeBrain(Breeze breeze, BehaviorController<Breeze> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initFightActivity(breeze, behaviorcontroller);
        behaviorcontroller.setCoreActivities(Set.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.FIGHT);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    private static void initCoreActivity(BehaviorController<Breeze> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorSwim(0.8F), new BehaviorLook(45, 90)));
    }

    private static void initIdleActivity(BehaviorController<Breeze> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0, BehaviorAttackTargetSet.create((breeze) -> {
            return breeze.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        })), Pair.of(1, BehaviorAttackTargetSet.create(Breeze::getHurtBy)), Pair.of(2, new BreezeAi.a(20, 40)), Pair.of(3, new BehaviorGateSingle<>(ImmutableList.of(Pair.of(new BehaviorNop(20, 100), 1), Pair.of(BehaviorStrollRandomUnconstrained.stroll(0.6F), 2))))));
    }

    private static void initFightActivity(Breeze breeze, BehaviorController<Breeze> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.FIGHT, ImmutableList.of(Pair.of(0, BehaviorAttackTargetForget.create((entityliving) -> {
            return !Sensor.isEntityAttackable(breeze, entityliving);
        })), Pair.of(1, new Shoot()), Pair.of(2, new LongJump()), Pair.of(3, new ShootWhenStuck()), Pair.of(4, new Slide())), ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT)));
    }

    static void updateActivity(Breeze breeze) {
        breeze.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }

    public static class a extends BehavorMove {

        @VisibleForTesting
        public a(int i, int j) {
            super(i, j);
        }

        @Override
        protected void start(WorldServer worldserver, EntityInsentient entityinsentient, long i) {
            super.start(worldserver, entityinsentient, i);
            entityinsentient.playSound(SoundEffects.BREEZE_SLIDE);
            entityinsentient.setPose(EntityPose.SLIDING);
        }

        @Override
        protected void stop(WorldServer worldserver, EntityInsentient entityinsentient, long i) {
            super.stop(worldserver, entityinsentient, i);
            entityinsentient.setPose(EntityPose.STANDING);
            if (entityinsentient.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                entityinsentient.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 60L);
            }

        }
    }
}
