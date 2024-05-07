package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.phys.Vec3D;

public class Shoot extends Behavior<Breeze> {

    private static final int ATTACK_RANGE_MIN_SQRT = 4;
    private static final int ATTACK_RANGE_MAX_SQRT = 256;
    private static final int UNCERTAINTY_BASE = 5;
    private static final int UNCERTAINTY_MULTIPLIER = 4;
    private static final float PROJECTILE_MOVEMENT_SCALE = 0.7F;
    private static final int SHOOT_INITIAL_DELAY_TICKS = Math.round(15.0F);
    private static final int SHOOT_RECOVER_DELAY_TICKS = Math.round(4.0F);
    private static final int SHOOT_COOLDOWN_TICKS = Math.round(10.0F);

    @VisibleForTesting
    public Shoot() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREEZE_SHOOT_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT_CHARGING, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT_RECOVERING, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_ABSENT), Shoot.SHOOT_INITIAL_DELAY_TICKS + 1 + Shoot.SHOOT_RECOVER_DELAY_TICKS);
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, Breeze breeze) {
        return breeze.getPose() != EntityPose.STANDING ? false : (Boolean) breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map((entityliving) -> {
            return isTargetWithinRange(breeze, entityliving);
        }).map((obool) -> {
            if (!obool) {
                breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
            }

            return obool;
        }).orElse(false);
    }

    protected boolean canStillUse(WorldServer worldserver, Breeze breeze, long i) {
        return breeze.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && breeze.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_SHOOT);
    }

    protected void start(WorldServer worldserver, Breeze breeze, long i) {
        breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((entityliving) -> {
            breeze.setPose(EntityPose.SHOOTING);
        });
        breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, (long) Shoot.SHOOT_INITIAL_DELAY_TICKS);
        breeze.playSound(SoundEffects.BREEZE_INHALE, 1.0F, 1.0F);
    }

    protected void stop(WorldServer worldserver, Breeze breeze, long i) {
        if (breeze.getPose() == EntityPose.SHOOTING) {
            breeze.setPose(EntityPose.STANDING);
        }

        breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, (long) Shoot.SHOOT_COOLDOWN_TICKS);
        breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
    }

    protected void tick(WorldServer worldserver, Breeze breeze, long i) {
        BehaviorController<Breeze> behaviorcontroller = breeze.getBrain();
        EntityLiving entityliving = (EntityLiving) behaviorcontroller.getMemory(MemoryModuleType.ATTACK_TARGET).orElse((Object) null);

        if (entityliving != null) {
            breeze.lookAt(ArgumentAnchor.Anchor.EYES, entityliving.position());
            if (!behaviorcontroller.getMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() && !behaviorcontroller.getMemory(MemoryModuleType.BREEZE_SHOOT_RECOVERING).isPresent()) {
                behaviorcontroller.setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_RECOVERING, Unit.INSTANCE, (long) Shoot.SHOOT_RECOVER_DELAY_TICKS);
                if (isFacingTarget(breeze, entityliving)) {
                    double d0 = entityliving.getX() - breeze.getX();
                    double d1 = entityliving.getY(0.3D) - breeze.getY(0.5D);
                    double d2 = entityliving.getZ() - breeze.getZ();
                    BreezeWindCharge breezewindcharge = new BreezeWindCharge(breeze, worldserver);

                    breeze.playSound(SoundEffects.BREEZE_SHOOT, 1.5F, 1.0F);
                    breezewindcharge.shoot(d0, d1, d2, 0.7F, (float) (5 - worldserver.getDifficulty().getId() * 4));
                    worldserver.addFreshEntity(breezewindcharge);
                }

            }
        }
    }

    @VisibleForTesting
    public static boolean isFacingTarget(Breeze breeze, EntityLiving entityliving) {
        Vec3D vec3d = breeze.getViewVector(1.0F);
        Vec3D vec3d1 = entityliving.position().subtract(breeze.position()).normalize();

        return vec3d.dot(vec3d1) > 0.5D;
    }

    private static boolean isTargetWithinRange(Breeze breeze, EntityLiving entityliving) {
        double d0 = breeze.position().distanceToSqr(entityliving.position());

        return d0 > 4.0D && d0 < 256.0D;
    }
}
