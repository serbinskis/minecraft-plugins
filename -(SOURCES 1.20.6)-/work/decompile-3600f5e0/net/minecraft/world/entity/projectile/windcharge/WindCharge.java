package net.minecraft.world.entity.projectile.windcharge;

import net.minecraft.core.particles.Particles;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;

public class WindCharge extends AbstractWindCharge {

    private static final WindCharge.a EXPLOSION_DAMAGE_CALCULATOR = new WindCharge.a();
    private static final float RADIUS = 1.2F;

    public WindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, World world) {
        super(entitytypes, world);
    }

    public WindCharge(EntityHuman entityhuman, World world, double d0, double d1, double d2) {
        super(EntityTypes.WIND_CHARGE, world, entityhuman, d0, d1, d2);
    }

    public WindCharge(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
        super(EntityTypes.WIND_CHARGE, d0, d1, d2, d3, d4, d5, world);
    }

    @Override
    public void explode() {
        this.level().explode(this, (DamageSource) null, WindCharge.EXPLOSION_DAMAGE_CALCULATOR, this.getX(), this.getY(), this.getZ(), 1.2F, false, World.a.BLOW, Particles.GUST_EMITTER_SMALL, Particles.GUST_EMITTER_LARGE, SoundEffects.WIND_CHARGE_BURST);
    }

    public static final class a extends AbstractWindCharge.a {

        public a() {}

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 1.1F;
        }
    }
}
