package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTameableAnimal;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;

public class PathfinderGoalOwnerHurtByTarget extends PathfinderGoalTarget {

    private final EntityTameableAnimal tameAnimal;
    private EntityLiving ownerLastHurtBy;
    private int timestamp;

    public PathfinderGoalOwnerHurtByTarget(EntityTameableAnimal entitytameableanimal) {
        super(entitytameableanimal, false);
        this.tameAnimal = entitytameableanimal;
        this.setFlags(EnumSet.of(PathfinderGoal.Type.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.tameAnimal.isTame() && !this.tameAnimal.isOrderedToSit()) {
            EntityLiving entityliving = this.tameAnimal.getOwner();

            if (entityliving == null) {
                return false;
            } else {
                this.ownerLastHurtBy = entityliving.getLastHurtByMob();
                int i = entityliving.getLastHurtByMobTimestamp();

                return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, PathfinderTargetCondition.DEFAULT) && this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, entityliving);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy, org.bukkit.event.entity.EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true); // CraftBukkit - reason
        EntityLiving entityliving = this.tameAnimal.getOwner();

        if (entityliving != null) {
            this.timestamp = entityliving.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
