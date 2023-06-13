package net.minecraft.world.entity.ai.goal.target;

import java.util.List;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntityAngerable;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AxisAlignedBB;

public class PathfinderGoalUniversalAngerReset<T extends EntityInsentient & IEntityAngerable> extends PathfinderGoal {

    private static final int ALERT_RANGE_Y = 10;
    private final T mob;
    private final boolean alertOthersOfSameType;
    private int lastHurtByPlayerTimestamp;

    public PathfinderGoalUniversalAngerReset(T t0, boolean flag) {
        this.mob = t0;
        this.alertOthersOfSameType = flag;
    }

    @Override
    public boolean canUse() {
        return this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.wasHurtByPlayer();
    }

    private boolean wasHurtByPlayer() {
        return this.mob.getLastHurtByMob() != null && this.mob.getLastHurtByMob().getType() == EntityTypes.PLAYER && this.mob.getLastHurtByMobTimestamp() > this.lastHurtByPlayerTimestamp;
    }

    @Override
    public void start() {
        this.lastHurtByPlayerTimestamp = this.mob.getLastHurtByMobTimestamp();
        ((IEntityAngerable) this.mob).forgetCurrentTargetAndRefreshUniversalAnger();
        if (this.alertOthersOfSameType) {
            this.getNearbyMobsOfSameType().stream().filter((entityinsentient) -> {
                return entityinsentient != this.mob;
            }).map((entityinsentient) -> {
                return (IEntityAngerable) entityinsentient;
            }).forEach(IEntityAngerable::forgetCurrentTargetAndRefreshUniversalAnger);
        }

        super.start();
    }

    private List<? extends EntityInsentient> getNearbyMobsOfSameType() {
        double d0 = this.mob.getAttributeValue(GenericAttributes.FOLLOW_RANGE);
        AxisAlignedBB axisalignedbb = AxisAlignedBB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0D, d0);

        return this.mob.level().getEntitiesOfClass(this.mob.getClass(), axisalignedbb, IEntitySelector.NO_SPECTATORS);
    }
}
