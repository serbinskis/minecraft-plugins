package net.minecraft.world.effect;

import net.minecraft.world.entity.EntityLiving;

class WitherMobEffect extends MobEffectList {

    protected WitherMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public void applyEffectTick(EntityLiving entityliving, int i) {
        super.applyEffectTick(entityliving, i);
        entityliving.hurt(entityliving.damageSources().wither(), 1.0F);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        int k = 40 >> j;

        return k > 0 ? i % k == 0 : true;
    }
}
