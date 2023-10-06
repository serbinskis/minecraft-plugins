package net.minecraft.world.effect;

import net.minecraft.world.entity.EntityLiving;

class RegenerationMobEffect extends MobEffectList {

    protected RegenerationMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public void applyEffectTick(EntityLiving entityliving, int i) {
        super.applyEffectTick(entityliving, i);
        if (entityliving.getHealth() < entityliving.getMaxHealth()) {
            entityliving.heal(1.0F);
        }

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        int k = 50 >> j;

        return k > 0 ? i % k == 0 : true;
    }
}
