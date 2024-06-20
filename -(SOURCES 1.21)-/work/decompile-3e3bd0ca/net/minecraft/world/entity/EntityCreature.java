package net.minecraft.world.entity;

import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalWrapped;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public abstract class EntityCreature extends EntityInsentient {

    protected static final float DEFAULT_WALK_TARGET_VALUE = 0.0F;

    protected EntityCreature(EntityTypes<? extends EntityCreature> entitytypes, World world) {
        super(entitytypes, world);
    }

    public float getWalkTargetValue(BlockPosition blockposition) {
        return this.getWalkTargetValue(blockposition, this.level());
    }

    public float getWalkTargetValue(BlockPosition blockposition, IWorldReader iworldreader) {
        return 0.0F;
    }

    @Override
    public boolean checkSpawnRules(GeneratorAccess generatoraccess, EnumMobSpawn enummobspawn) {
        return this.getWalkTargetValue(this.blockPosition(), generatoraccess) >= 0.0F;
    }

    public boolean isPathFinding() {
        return !this.getNavigation().isDone();
    }

    public boolean isPanicking() {
        if (this.brain.hasMemoryValue(MemoryModuleType.IS_PANICKING)) {
            return this.brain.getMemory(MemoryModuleType.IS_PANICKING).isPresent();
        } else {
            Iterator iterator = this.goalSelector.getAvailableGoals().iterator();

            PathfinderGoalWrapped pathfindergoalwrapped;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                pathfindergoalwrapped = (PathfinderGoalWrapped) iterator.next();
            } while (!pathfindergoalwrapped.isRunning() || !(pathfindergoalwrapped.getGoal() instanceof PathfinderGoalPanic));

            return true;
        }
    }

    protected boolean shouldStayCloseToLeashHolder() {
        return true;
    }

    @Override
    public void closeRangeLeashBehaviour(Entity entity) {
        super.closeRangeLeashBehaviour(entity);
        if (this.shouldStayCloseToLeashHolder() && !this.isPanicking()) {
            this.goalSelector.enableControlFlag(PathfinderGoal.Type.MOVE);
            float f = 2.0F;
            float f1 = this.distanceTo(entity);
            Vec3D vec3d = (new Vec3D(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ())).normalize().scale((double) Math.max(f1 - 2.0F, 0.0F));

            this.getNavigation().moveTo(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z, this.followLeashSpeed());
        }

    }

    @Override
    public boolean handleLeashAtDistance(Entity entity, float f) {
        this.restrictTo(entity.blockPosition(), 5);
        return true;
    }

    protected double followLeashSpeed() {
        return 1.0D;
    }
}
