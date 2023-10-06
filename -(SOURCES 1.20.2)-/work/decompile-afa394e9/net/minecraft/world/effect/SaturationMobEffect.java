package net.minecraft.world.effect;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;

class SaturationMobEffect extends InstantMobEffect {

    protected SaturationMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public void applyEffectTick(EntityLiving entityliving, int i) {
        super.applyEffectTick(entityliving, i);
        if (!entityliving.level().isClientSide && entityliving instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) entityliving;

            entityhuman.getFoodData().eat(i + 1, 1.0F);
        }

    }
}
