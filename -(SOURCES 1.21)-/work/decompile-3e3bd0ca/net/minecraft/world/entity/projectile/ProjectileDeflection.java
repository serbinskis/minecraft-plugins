package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

@FunctionalInterface
public interface ProjectileDeflection {

    ProjectileDeflection NONE = (iprojectile, entity, randomsource) -> {
    };
    ProjectileDeflection REVERSE = (iprojectile, entity, randomsource) -> {
        float f = 170.0F + randomsource.nextFloat() * 20.0F;

        iprojectile.setDeltaMovement(iprojectile.getDeltaMovement().scale(-0.5D));
        iprojectile.setYRot(iprojectile.getYRot() + f);
        iprojectile.yRotO += f;
        iprojectile.hasImpulse = true;
    };
    ProjectileDeflection AIM_DEFLECT = (iprojectile, entity, randomsource) -> {
        if (entity != null) {
            Vec3D vec3d = entity.getLookAngle().normalize();

            iprojectile.setDeltaMovement(vec3d);
            iprojectile.hasImpulse = true;
        }

    };
    ProjectileDeflection MOMENTUM_DEFLECT = (iprojectile, entity, randomsource) -> {
        if (entity != null) {
            Vec3D vec3d = entity.getDeltaMovement().normalize();

            iprojectile.setDeltaMovement(vec3d);
            iprojectile.hasImpulse = true;
        }

    };

    void deflect(IProjectile iprojectile, @Nullable Entity entity, RandomSource randomsource);
}
