package net.minecraft.world.effect;

import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.World;

class WindChargedMobEffect extends MobEffectList {

    protected WindChargedMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i, Particles.SMALL_GUST);
    }

    @Override
    public void onMobRemoved(EntityLiving entityliving, int i, Entity.RemovalReason entity_removalreason) {
        if (entity_removalreason == Entity.RemovalReason.KILLED) {
            World world = entityliving.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;
                double d0 = entityliving.getX();
                double d1 = entityliving.getY() + (double) (entityliving.getBbHeight() / 2.0F);
                double d2 = entityliving.getZ();
                float f = 3.0F + entityliving.getRandom().nextFloat() * 2.0F;

                worldserver.explode(entityliving, (DamageSource) null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR, d0, d1, d2, f, false, World.a.BLOW, Particles.GUST_EMITTER_SMALL, Particles.GUST_EMITTER_LARGE, SoundEffects.BREEZE_WIND_CHARGE_BURST);
            }
        }

    }
}
