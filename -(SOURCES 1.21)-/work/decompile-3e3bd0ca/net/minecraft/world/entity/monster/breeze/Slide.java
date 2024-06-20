package net.minecraft.world.entity.monster.breeze;

import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3D;

public class Slide extends Behavior<Breeze> {

    public Slide() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_ABSENT));
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, Breeze breeze) {
        return breeze.onGround() && !breeze.isInWater() && breeze.getPose() == EntityPose.STANDING;
    }

    protected void start(WorldServer worldserver, Breeze breeze, long i) {
        EntityLiving entityliving = (EntityLiving) breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse((Object) null);

        if (entityliving != null) {
            boolean flag = breeze.withinInnerCircleRange(entityliving.position());
            Vec3D vec3d = null;

            if (flag) {
                Vec3D vec3d1 = DefaultRandomPos.getPosAway(breeze, 5, 5, entityliving.position());

                if (vec3d1 != null && BreezeUtil.hasLineOfSight(breeze, vec3d1) && entityliving.distanceToSqr(vec3d1.x, vec3d1.y, vec3d1.z) > entityliving.distanceToSqr((Entity) breeze)) {
                    vec3d = vec3d1;
                }
            }

            if (vec3d == null) {
                vec3d = breeze.getRandom().nextBoolean() ? BreezeUtil.randomPointBehindTarget(entityliving, breeze.getRandom()) : randomPointInMiddleCircle(breeze, entityliving);
            }

            breeze.getBrain().setMemory(MemoryModuleType.WALK_TARGET, (Object) (new MemoryTarget(BlockPosition.containing(vec3d), 0.6F, 1)));
        }
    }

    private static Vec3D randomPointInMiddleCircle(Breeze breeze, EntityLiving entityliving) {
        Vec3D vec3d = entityliving.position().subtract(breeze.position());
        double d0 = vec3d.length() - MathHelper.lerp(breeze.getRandom().nextDouble(), 8.0D, 4.0D);
        Vec3D vec3d1 = vec3d.normalize().multiply(d0, d0, d0);

        return breeze.position().add(vec3d1);
    }
}
