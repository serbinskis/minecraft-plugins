package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.phys.Vec3D;

public final class LongJumpUtil {

    public LongJumpUtil() {}

    public static Optional<Vec3D> calculateJumpVectorForAngle(EntityInsentient entityinsentient, Vec3D vec3d, float f, int i, boolean flag) {
        Vec3D vec3d1 = entityinsentient.position();
        Vec3D vec3d2 = (new Vec3D(vec3d.x - vec3d1.x, 0.0D, vec3d.z - vec3d1.z)).normalize().scale(0.5D);
        Vec3D vec3d3 = vec3d.subtract(vec3d2);
        Vec3D vec3d4 = vec3d3.subtract(vec3d1);
        float f1 = (float) i * 3.1415927F / 180.0F;
        double d0 = Math.atan2(vec3d4.z, vec3d4.x);
        double d1 = vec3d4.subtract(0.0D, vec3d4.y, 0.0D).lengthSqr();
        double d2 = Math.sqrt(d1);
        double d3 = vec3d4.y;
        double d4 = entityinsentient.getGravity();
        double d5 = Math.sin((double) (2.0F * f1));
        double d6 = Math.pow(Math.cos((double) f1), 2.0D);
        double d7 = Math.sin((double) f1);
        double d8 = Math.cos((double) f1);
        double d9 = Math.sin(d0);
        double d10 = Math.cos(d0);
        double d11 = d1 * d4 / (d2 * d5 - 2.0D * d3 * d6);

        if (d11 < 0.0D) {
            return Optional.empty();
        } else {
            double d12 = Math.sqrt(d11);

            if (d12 > (double) f) {
                return Optional.empty();
            } else {
                double d13 = d12 * d8;
                double d14 = d12 * d7;

                if (flag) {
                    int j = MathHelper.ceil(d2 / d13) * 2;
                    double d15 = 0.0D;
                    Vec3D vec3d5 = null;
                    EntitySize entitysize = entityinsentient.getDimensions(EntityPose.LONG_JUMPING);

                    for (int k = 0; k < j - 1; ++k) {
                        d15 += d2 / (double) j;
                        double d16 = d7 / d8 * d15 - Math.pow(d15, 2.0D) * d4 / (2.0D * d11 * Math.pow(d8, 2.0D));
                        double d17 = d15 * d10;
                        double d18 = d15 * d9;
                        Vec3D vec3d6 = new Vec3D(vec3d1.x + d17, vec3d1.y + d16, vec3d1.z + d18);

                        if (vec3d5 != null && !isClearTransition(entityinsentient, entitysize, vec3d5, vec3d6)) {
                            return Optional.empty();
                        }

                        vec3d5 = vec3d6;
                    }
                }

                return Optional.of((new Vec3D(d13 * d10, d14, d13 * d9)).scale(0.949999988079071D));
            }
        }
    }

    private static boolean isClearTransition(EntityInsentient entityinsentient, EntitySize entitysize, Vec3D vec3d, Vec3D vec3d1) {
        Vec3D vec3d2 = vec3d1.subtract(vec3d);
        double d0 = (double) Math.min(entitysize.width(), entitysize.height());
        int i = MathHelper.ceil(vec3d2.length() / d0);
        Vec3D vec3d3 = vec3d2.normalize();
        Vec3D vec3d4 = vec3d;

        for (int j = 0; j < i; ++j) {
            vec3d4 = j == i - 1 ? vec3d1 : vec3d4.add(vec3d3.scale(d0 * 0.8999999761581421D));
            if (!entityinsentient.level().noCollision(entityinsentient, entitysize.makeBoundingBox(vec3d4))) {
                return false;
            }
        }

        return true;
    }
}
