package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTameableAnimal;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AxisAlignedBB;

public class PathfinderGoalHurtByTarget extends PathfinderGoalTarget {

    private static final PathfinderTargetCondition HURT_BY_TARGETING = PathfinderTargetCondition.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    @Nullable
    private Class<?>[] toIgnoreAlert;

    public PathfinderGoalHurtByTarget(EntityCreature entitycreature, Class<?>... aclass) {
        super(entitycreature, true);
        this.toIgnoreDamage = aclass;
        this.setFlags(EnumSet.of(PathfinderGoal.Type.TARGET));
    }

    @Override
    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        EntityLiving entityliving = this.mob.getLastHurtByMob();

        if (i != this.timestamp && entityliving != null) {
            if (entityliving.getType() == EntityTypes.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                Class[] aclass = this.toIgnoreDamage;
                int j = aclass.length;

                for (int k = 0; k < j; ++k) {
                    Class<?> oclass = aclass[k];

                    if (oclass.isAssignableFrom(entityliving.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(entityliving, PathfinderGoalHurtByTarget.HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public PathfinderGoalHurtByTarget setAlertOthers(Class<?>... aclass) {
        this.alertSameType = true;
        this.toIgnoreAlert = aclass;
        return this;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob(), org.bukkit.event.entity.EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true); // CraftBukkit - reason
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.start();
    }

    protected void alertOthers() {
        double d0 = this.getFollowDistance();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0D, d0);
        List<? extends EntityInsentient> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), axisalignedbb, IEntitySelector.NO_SPECTATORS);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityInsentient entityinsentient = (EntityInsentient) iterator.next();

            if (this.mob != entityinsentient && entityinsentient.getTarget() == null && (!(this.mob instanceof EntityTameableAnimal) || ((EntityTameableAnimal) this.mob).getOwner() == ((EntityTameableAnimal) entityinsentient).getOwner()) && !entityinsentient.isAlliedTo((Entity) this.mob.getLastHurtByMob())) {
                if (this.toIgnoreAlert != null) {
                    boolean flag = false;
                    Class[] aclass = this.toIgnoreAlert;
                    int i = aclass.length;

                    for (int j = 0; j < i; ++j) {
                        Class<?> oclass = aclass[j];

                        if (entityinsentient.getClass() == oclass) {
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        continue;
                    }
                }

                this.alertOther(entityinsentient, this.mob.getLastHurtByMob());
            }
        }

    }

    protected void alertOther(EntityInsentient entityinsentient, EntityLiving entityliving) {
        entityinsentient.setTarget(entityliving, org.bukkit.event.entity.EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true); // CraftBukkit - reason
    }
}
