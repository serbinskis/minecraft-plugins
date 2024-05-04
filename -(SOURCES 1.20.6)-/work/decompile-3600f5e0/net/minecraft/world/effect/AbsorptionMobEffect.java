package net.minecraft.world.effect;

import net.minecraft.world.entity.EntityLiving;

class AbsorptionMobEffect extends MobEffectList {

    protected AbsorptionMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean applyEffectTick(EntityLiving entityliving, int i) {
        return entityliving.getAbsorptionAmount() > 0.0F || entityliving.level().isClientSide;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return true;
    }

    @Override
    public void onEffectStarted(EntityLiving entityliving, int i) {
        super.onEffectStarted(entityliving, i);
        entityliving.setAbsorptionAmount(Math.max(entityliving.getAbsorptionAmount(), (float) (4 * (1 + i))));
    }
}
