package net.minecraft.world.effect;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;

class HungerMobEffect extends MobEffectList {

    protected HungerMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public void applyEffectTick(EntityLiving entityliving, int i) {
        super.applyEffectTick(entityliving, i);
        if (entityliving instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) entityliving;

            entityhuman.causeFoodExhaustion(0.005F * (float) (i + 1), org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.HUNGER_EFFECT); // CraftBukkit - EntityExhaustionEvent
        }

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return true;
    }
}
