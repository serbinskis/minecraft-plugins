package net.minecraft.world.effect;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;

class HealOrHarmMobEffect extends InstantMobEffect {

    private final boolean isHarm;

    public HealOrHarmMobEffect(MobEffectInfo mobeffectinfo, int i, boolean flag) {
        super(mobeffectinfo, i);
        this.isHarm = flag;
    }

    @Override
    public boolean applyEffectTick(EntityLiving entityliving, int i) {
        if (this.isHarm == entityliving.isInvertedHealAndHarm()) {
            entityliving.heal((float) Math.max(4 << i, 0));
        } else {
            entityliving.hurt(entityliving.damageSources().magic(), (float) (6 << i));
        }

        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity1, EntityLiving entityliving, int i, double d0) {
        int j;

        if (this.isHarm == entityliving.isInvertedHealAndHarm()) {
            j = (int) (d0 * (double) (4 << i) + 0.5D);
            entityliving.heal((float) j);
        } else {
            j = (int) (d0 * (double) (6 << i) + 0.5D);
            if (entity == null) {
                entityliving.hurt(entityliving.damageSources().magic(), (float) j);
            } else {
                entityliving.hurt(entityliving.damageSources().indirectMagic(entity, entity1), (float) j);
            }
        }

    }
}
