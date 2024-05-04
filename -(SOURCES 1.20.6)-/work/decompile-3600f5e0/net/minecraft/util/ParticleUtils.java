package net.minecraft.util;

import java.util.function.Supplier;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public class ParticleUtils {

    public ParticleUtils() {}

    public static void spawnParticlesOnBlockFaces(World world, BlockPosition blockposition, ParticleParam particleparam, IntProvider intprovider) {
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];

            spawnParticlesOnBlockFace(world, blockposition, particleparam, intprovider, enumdirection, () -> {
                return getRandomSpeedRanges(world.random);
            }, 0.55D);
        }

    }

    public static void spawnParticlesOnBlockFace(World world, BlockPosition blockposition, ParticleParam particleparam, IntProvider intprovider, EnumDirection enumdirection, Supplier<Vec3D> supplier, double d0) {
        int i = intprovider.sample(world.random);

        for (int j = 0; j < i; ++j) {
            spawnParticleOnFace(world, blockposition, enumdirection, particleparam, (Vec3D) supplier.get(), d0);
        }

    }

    private static Vec3D getRandomSpeedRanges(RandomSource randomsource) {
        return new Vec3D(MathHelper.nextDouble(randomsource, -0.5D, 0.5D), MathHelper.nextDouble(randomsource, -0.5D, 0.5D), MathHelper.nextDouble(randomsource, -0.5D, 0.5D));
    }

    public static void spawnParticlesAlongAxis(EnumDirection.EnumAxis enumdirection_enumaxis, World world, BlockPosition blockposition, double d0, ParticleParam particleparam, UniformInt uniformint) {
        Vec3D vec3d = Vec3D.atCenterOf(blockposition);
        boolean flag = enumdirection_enumaxis == EnumDirection.EnumAxis.X;
        boolean flag1 = enumdirection_enumaxis == EnumDirection.EnumAxis.Y;
        boolean flag2 = enumdirection_enumaxis == EnumDirection.EnumAxis.Z;
        int i = uniformint.sample(world.random);

        for (int j = 0; j < i; ++j) {
            double d1 = vec3d.x + MathHelper.nextDouble(world.random, -1.0D, 1.0D) * (flag ? 0.5D : d0);
            double d2 = vec3d.y + MathHelper.nextDouble(world.random, -1.0D, 1.0D) * (flag1 ? 0.5D : d0);
            double d3 = vec3d.z + MathHelper.nextDouble(world.random, -1.0D, 1.0D) * (flag2 ? 0.5D : d0);
            double d4 = flag ? MathHelper.nextDouble(world.random, -1.0D, 1.0D) : 0.0D;
            double d5 = flag1 ? MathHelper.nextDouble(world.random, -1.0D, 1.0D) : 0.0D;
            double d6 = flag2 ? MathHelper.nextDouble(world.random, -1.0D, 1.0D) : 0.0D;

            world.addParticle(particleparam, d1, d2, d3, d4, d5, d6);
        }

    }

    public static void spawnParticleOnFace(World world, BlockPosition blockposition, EnumDirection enumdirection, ParticleParam particleparam, Vec3D vec3d, double d0) {
        Vec3D vec3d1 = Vec3D.atCenterOf(blockposition);
        int i = enumdirection.getStepX();
        int j = enumdirection.getStepY();
        int k = enumdirection.getStepZ();
        double d1 = vec3d1.x + (i == 0 ? MathHelper.nextDouble(world.random, -0.5D, 0.5D) : (double) i * d0);
        double d2 = vec3d1.y + (j == 0 ? MathHelper.nextDouble(world.random, -0.5D, 0.5D) : (double) j * d0);
        double d3 = vec3d1.z + (k == 0 ? MathHelper.nextDouble(world.random, -0.5D, 0.5D) : (double) k * d0);
        double d4 = i == 0 ? vec3d.x() : 0.0D;
        double d5 = j == 0 ? vec3d.y() : 0.0D;
        double d6 = k == 0 ? vec3d.z() : 0.0D;

        world.addParticle(particleparam, d1, d2, d3, d4, d5, d6);
    }

    public static void spawnParticleBelow(World world, BlockPosition blockposition, RandomSource randomsource, ParticleParam particleparam) {
        double d0 = (double) blockposition.getX() + randomsource.nextDouble();
        double d1 = (double) blockposition.getY() - 0.05D;
        double d2 = (double) blockposition.getZ() + randomsource.nextDouble();

        world.addParticle(particleparam, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnParticleInBlock(GeneratorAccess generatoraccess, BlockPosition blockposition, int i, ParticleParam particleparam) {
        double d0 = 0.5D;
        IBlockData iblockdata = generatoraccess.getBlockState(blockposition);
        double d1 = iblockdata.isAir() ? 1.0D : iblockdata.getShape(generatoraccess, blockposition).max(EnumDirection.EnumAxis.Y);

        spawnParticles(generatoraccess, blockposition, i, 0.5D, d1, true, particleparam);
    }

    public static void spawnParticles(GeneratorAccess generatoraccess, BlockPosition blockposition, int i, double d0, double d1, boolean flag, ParticleParam particleparam) {
        RandomSource randomsource = generatoraccess.getRandom();

        for (int j = 0; j < i; ++j) {
            double d2 = randomsource.nextGaussian() * 0.02D;
            double d3 = randomsource.nextGaussian() * 0.02D;
            double d4 = randomsource.nextGaussian() * 0.02D;
            double d5 = 0.5D - d0;
            double d6 = (double) blockposition.getX() + d5 + randomsource.nextDouble() * d0 * 2.0D;
            double d7 = (double) blockposition.getY() + randomsource.nextDouble() * d1;
            double d8 = (double) blockposition.getZ() + d5 + randomsource.nextDouble() * d0 * 2.0D;

            if (flag || !generatoraccess.getBlockState(BlockPosition.containing(d6, d7, d8).below()).isAir()) {
                generatoraccess.addParticle(particleparam, d6, d7, d8, d2, d3, d4);
            }
        }

    }

    public static void spawnSmashAttackParticles(GeneratorAccess generatoraccess, BlockPosition blockposition, int i) {
        Vec3D vec3d = blockposition.getCenter().add(0.0D, 0.5D, 0.0D);
        ParticleParamBlock particleparamblock = new ParticleParamBlock(Particles.DUST_PILLAR, generatoraccess.getBlockState(blockposition));

        double d0;
        double d1;
        double d2;
        double d3;
        double d4;
        double d5;
        int j;

        for (j = 0; (float) j < (float) i / 3.0F; ++j) {
            d0 = vec3d.x + generatoraccess.getRandom().nextGaussian() / 2.0D;
            d1 = vec3d.y;
            d2 = vec3d.z + generatoraccess.getRandom().nextGaussian() / 2.0D;
            d3 = generatoraccess.getRandom().nextGaussian() * 0.20000000298023224D;
            d4 = generatoraccess.getRandom().nextGaussian() * 0.20000000298023224D;
            d5 = generatoraccess.getRandom().nextGaussian() * 0.20000000298023224D;
            generatoraccess.addParticle(particleparamblock, d0, d1, d2, d3, d4, d5);
        }

        for (j = 0; (float) j < (float) i / 1.5F; ++j) {
            d0 = vec3d.x + 3.5D * Math.cos((double) j) + generatoraccess.getRandom().nextGaussian() / 2.0D;
            d1 = vec3d.y;
            d2 = vec3d.z + 3.5D * Math.sin((double) j) + generatoraccess.getRandom().nextGaussian() / 2.0D;
            d3 = generatoraccess.getRandom().nextGaussian() * 0.05000000074505806D;
            d4 = generatoraccess.getRandom().nextGaussian() * 0.05000000074505806D;
            d5 = generatoraccess.getRandom().nextGaussian() * 0.05000000074505806D;
            generatoraccess.addParticle(particleparamblock, d0, d1, d2, d3, d4, d5);
        }

    }
}
