package net.minecraft.world.effect;

import net.minecraft.world.entity.EntityLiving;

class RegenerationMobEffect extends MobEffectList {

    protected RegenerationMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean applyEffectTick(EntityLiving entityliving, int i) {
        if (entityliving.getHealth() < entityliving.getMaxHealth()) {
            entityliving.heal(1.0F);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        int k = 50 >> j;

        return k > 0 ? i % k == 0 : true;
    }
}
