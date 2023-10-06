package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTameableAnimal;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;

public class PathfinderGoalOwnerHurtTarget extends PathfinderGoalTarget {

    private final EntityTameableAnimal tameAnimal;
    private EntityLiving ownerLastHurt;
    private int timestamp;

    public PathfinderGoalOwnerHurtTarget(EntityTameableAnimal entitytameableanimal) {
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
                this.ownerLastHurt = entityliving.getLastHurtMob();
                int i = entityliving.getLastHurtMobTimestamp();

                return i != this.timestamp && this.canAttack(this.ownerLastHurt, PathfinderTargetCondition.DEFAULT) && this.tameAnimal.wantsToAttack(this.ownerLastHurt, entityliving);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt, org.bukkit.event.entity.EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true); // CraftBukkit - reason
        EntityLiving entityliving = this.tameAnimal.getOwner();

        if (entityliving != null) {
            this.timestamp = entityliving.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
