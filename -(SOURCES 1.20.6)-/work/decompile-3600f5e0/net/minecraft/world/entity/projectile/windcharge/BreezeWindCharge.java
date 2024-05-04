package net.minecraft.world.entity.projectile.windcharge;

import net.minecraft.core.particles.Particles;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.World;

public class BreezeWindCharge extends AbstractWindCharge {

    private static final float RADIUS = 3.0F;

    public BreezeWindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, World world) {
        super(entitytypes, world);
    }

    public BreezeWindCharge(Breeze breeze, World world) {
        super(EntityTypes.BREEZE_WIND_CHARGE, world, breeze, breeze.getX(), breeze.getSnoutYPosition(), breeze.getZ());
    }

    @Override
    public void explode() {
        this.level().explode(this, (DamageSource) null, BreezeWindCharge.EXPLOSION_DAMAGE_CALCULATOR, this.getX(), this.getY(), this.getZ(), 3.0F, false, World.a.BLOW, Particles.GUST_EMITTER_SMALL, Particles.GUST_EMITTER_LARGE, SoundEffects.BREEZE_WIND_CHARGE_BURST);
    }
}
