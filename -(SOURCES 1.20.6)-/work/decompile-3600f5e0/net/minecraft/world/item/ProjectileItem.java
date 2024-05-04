package net.minecraft.world.item;

import java.util.OptionalInt;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.core.dispenser.SourceBlock;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.phys.Vec3D;

public interface ProjectileItem {

    IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection);

    default ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.DEFAULT;
    }

    default void shoot(IProjectile iprojectile, double d0, double d1, double d2, float f, float f1) {
        iprojectile.shoot(d0, d1, d2, f, f1);
    }

    public static record a(ProjectileItem.b positionFunction, float uncertainty, float power, OptionalInt overrideDispenseEvent) {

        public static final ProjectileItem.a DEFAULT = builder().build();

        public static ProjectileItem.a.a builder() {
            return new ProjectileItem.a.a();
        }

        public static class a {

            private ProjectileItem.b positionFunction = (sourceblock, enumdirection) -> {
                return BlockDispenser.getDispensePosition(sourceblock, 0.7D, new Vec3D(0.0D, 0.1D, 0.0D));
            };
            private float uncertainty = 6.0F;
            private float power = 1.1F;
            private OptionalInt overrideDispenseEvent = OptionalInt.empty();

            public a() {}

            public ProjectileItem.a.a positionFunction(ProjectileItem.b projectileitem_b) {
                this.positionFunction = projectileitem_b;
                return this;
            }

            public ProjectileItem.a.a uncertainty(float f) {
                this.uncertainty = f;
                return this;
            }

            public ProjectileItem.a.a power(float f) {
                this.power = f;
                return this;
            }

            public ProjectileItem.a.a overrideDispenseEvent(int i) {
                this.overrideDispenseEvent = OptionalInt.of(i);
                return this;
            }

            public ProjectileItem.a build() {
                return new ProjectileItem.a(this.positionFunction, this.uncertainty, this.power, this.overrideDispenseEvent);
            }
        }
    }

    @FunctionalInterface
    public interface b {

        IPosition getDispensePosition(SourceBlock sourceblock, EnumDirection enumdirection);
    }
}
