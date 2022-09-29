package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.raid.EntityRaider;

public class PathfinderGoalNearestAttackableTargetWitch<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {

    private boolean canAttack = true;

    public PathfinderGoalNearestAttackableTargetWitch(EntityRaider entityraider, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<EntityLiving> predicate) {
        super(entityraider, oclass, i, flag, flag1, predicate);
    }

    public void setCanAttack(boolean flag) {
        this.canAttack = flag;
    }

    @Override
    public boolean canUse() {
        return this.canAttack && super.canUse();
    }
}
