package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.scores.ScoreboardTeam;

// CraftBukkit start
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public abstract class PathfinderGoalTarget extends PathfinderGoal {

    private static final int EMPTY_REACH_CACHE = 0;
    private static final int CAN_REACH_CACHE = 1;
    private static final int CANT_REACH_CACHE = 2;
    protected final EntityInsentient mob;
    protected final boolean mustSee;
    private final boolean mustReach;
    private int reachCache;
    private int reachCacheTime;
    private int unseenTicks;
    @Nullable
    protected EntityLiving targetMob;
    protected int unseenMemoryTicks;

    public PathfinderGoalTarget(EntityInsentient entityinsentient, boolean flag) {
        this(entityinsentient, flag, false);
    }

    public PathfinderGoalTarget(EntityInsentient entityinsentient, boolean flag, boolean flag1) {
        this.unseenMemoryTicks = 60;
        this.mob = entityinsentient;
        this.mustSee = flag;
        this.mustReach = flag1;
    }

    @Override
    public boolean canContinueToUse() {
        EntityLiving entityliving = this.mob.getTarget();

        if (entityliving == null) {
            entityliving = this.targetMob;
        }

        if (entityliving == null) {
            return false;
        } else if (!this.mob.canAttack(entityliving)) {
            return false;
        } else {
            ScoreboardTeam scoreboardteam = this.mob.getTeam();
            ScoreboardTeam scoreboardteam1 = entityliving.getTeam();

            if (scoreboardteam != null && scoreboardteam1 == scoreboardteam) {
                return false;
            } else {
                double d0 = this.getFollowDistance();

                if (this.mob.distanceToSqr((Entity) entityliving) > d0 * d0) {
                    return false;
                } else {
                    if (this.mustSee) {
                        if (this.mob.getSensing().hasLineOfSight(entityliving)) {
                            this.unseenTicks = 0;
                        } else if (++this.unseenTicks > reducedTickDelay(this.unseenMemoryTicks)) {
                            return false;
                        }
                    }

                    this.mob.setTarget(entityliving, EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true); // CraftBukkit
                    return true;
                }
            }
        }
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(GenericAttributes.FOLLOW_RANGE);
    }

    @Override
    public void start() {
        this.reachCache = 0;
        this.reachCacheTime = 0;
        this.unseenTicks = 0;
    }

    @Override
    public void stop() {
        this.mob.setTarget((EntityLiving) null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true); // CraftBukkit
        this.targetMob = null;
    }

    protected boolean canAttack(@Nullable EntityLiving entityliving, PathfinderTargetCondition pathfindertargetcondition) {
        if (entityliving == null) {
            return false;
        } else if (!pathfindertargetcondition.test(this.mob, entityliving)) {
            return false;
        } else if (!this.mob.isWithinRestriction(entityliving.blockPosition())) {
            return false;
        } else {
            if (this.mustReach) {
                if (--this.reachCacheTime <= 0) {
                    this.reachCache = 0;
                }

                if (this.reachCache == 0) {
                    this.reachCache = this.canReach(entityliving) ? 1 : 2;
                }

                if (this.reachCache == 2) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean canReach(EntityLiving entityliving) {
        this.reachCacheTime = reducedTickDelay(10 + this.mob.getRandom().nextInt(5));
        PathEntity pathentity = this.mob.getNavigation().createPath((Entity) entityliving, 0);

        if (pathentity == null) {
            return false;
        } else {
            PathPoint pathpoint = pathentity.getEndNode();

            if (pathpoint == null) {
                return false;
            } else {
                int i = pathpoint.x - entityliving.getBlockX();
                int j = pathpoint.z - entityliving.getBlockZ();

                return (double) (i * i + j * j) <= 2.25D;
            }
        }
    }

    public PathfinderGoalTarget setUnseenMemoryTicks(int i) {
        this.unseenMemoryTicks = i;
        return this;
    }
}
