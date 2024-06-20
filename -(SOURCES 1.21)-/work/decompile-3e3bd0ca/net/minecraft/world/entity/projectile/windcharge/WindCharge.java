package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class WindCharge extends AbstractWindCharge {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.getTag(TagsBlock.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()));
    private static final float RADIUS = 1.2F;
    private int noDeflectTicks = 5;

    public WindCharge(EntityTypes<? extends AbstractWindCharge> entitytypes, World world) {
        super(entitytypes, world);
    }

    public WindCharge(EntityHuman entityhuman, World world, double d0, double d1, double d2) {
        super(EntityTypes.WIND_CHARGE, world, entityhuman, d0, d1, d2);
    }

    public WindCharge(World world, double d0, double d1, double d2, Vec3D vec3d) {
        super(EntityTypes.WIND_CHARGE, d0, d1, d2, vec3d, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.noDeflectTicks > 0) {
            --this.noDeflectTicks;
        }

    }

    @Override
    public boolean deflect(ProjectileDeflection projectiledeflection, @Nullable Entity entity, @Nullable Entity entity1, boolean flag) {
        return this.noDeflectTicks > 0 ? false : super.deflect(projectiledeflection, entity, entity1, flag);
    }

    @Override
    public void explode(Vec3D vec3d) {
        this.level().explode(this, (DamageSource) null, WindCharge.EXPLOSION_DAMAGE_CALCULATOR, vec3d.x(), vec3d.y(), vec3d.z(), 1.2F, false, World.a.TRIGGER, Particles.GUST_EMITTER_SMALL, Particles.GUST_EMITTER_LARGE, SoundEffects.WIND_CHARGE_BURST);
    }
}
