package net.minecraft.data.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorNormal;

public class NoiseData {

    /** @deprecated */
    @Deprecated
    public static final NoiseGeneratorNormal.a DEFAULT_SHIFT = new NoiseGeneratorNormal.a(-3, 1.0D, new double[]{1.0D, 1.0D, 0.0D});

    public NoiseData() {}

    public static void bootstrap(BootstrapContext<NoiseGeneratorNormal.a> bootstrapcontext) {
        registerBiomeNoises(bootstrapcontext, 0, Noises.TEMPERATURE, Noises.VEGETATION, Noises.CONTINENTALNESS, Noises.EROSION);
        registerBiomeNoises(bootstrapcontext, -2, Noises.TEMPERATURE_LARGE, Noises.VEGETATION_LARGE, Noises.CONTINENTALNESS_LARGE, Noises.EROSION_LARGE);
        register(bootstrapcontext, Noises.RIDGE, -7, 1.0D, 2.0D, 1.0D, 0.0D, 0.0D, 0.0D);
        bootstrapcontext.register(Noises.SHIFT, NoiseData.DEFAULT_SHIFT);
        register(bootstrapcontext, Noises.AQUIFER_BARRIER, -3, 1.0D);
        register(bootstrapcontext, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS, -7, 1.0D);
        register(bootstrapcontext, Noises.AQUIFER_LAVA, -1, 1.0D);
        register(bootstrapcontext, Noises.AQUIFER_FLUID_LEVEL_SPREAD, -5, 1.0D);
        register(bootstrapcontext, Noises.PILLAR, -7, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.PILLAR_RARENESS, -8, 1.0D);
        register(bootstrapcontext, Noises.PILLAR_THICKNESS, -8, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_2D, -7, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_2D_ELEVATION, -8, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_2D_MODULATOR, -11, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_2D_THICKNESS, -11, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_3D_1, -7, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_3D_2, -7, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_3D_RARITY, -11, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_3D_THICKNESS, -8, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_ROUGHNESS, -5, 1.0D);
        register(bootstrapcontext, Noises.SPAGHETTI_ROUGHNESS_MODULATOR, -8, 1.0D);
        register(bootstrapcontext, Noises.CAVE_ENTRANCE, -7, 0.4D, 0.5D, 1.0D);
        register(bootstrapcontext, Noises.CAVE_LAYER, -8, 1.0D);
        register(bootstrapcontext, Noises.CAVE_CHEESE, -8, 0.5D, 1.0D, 2.0D, 1.0D, 2.0D, 1.0D, 0.0D, 2.0D, 0.0D);
        register(bootstrapcontext, Noises.ORE_VEININESS, -8, 1.0D);
        register(bootstrapcontext, Noises.ORE_VEIN_A, -7, 1.0D);
        register(bootstrapcontext, Noises.ORE_VEIN_B, -7, 1.0D);
        register(bootstrapcontext, Noises.ORE_GAP, -5, 1.0D);
        register(bootstrapcontext, Noises.NOODLE, -8, 1.0D);
        register(bootstrapcontext, Noises.NOODLE_THICKNESS, -8, 1.0D);
        register(bootstrapcontext, Noises.NOODLE_RIDGE_A, -7, 1.0D);
        register(bootstrapcontext, Noises.NOODLE_RIDGE_B, -7, 1.0D);
        register(bootstrapcontext, Noises.JAGGED, -16, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.SURFACE, -6, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.SURFACE_SECONDARY, -6, 1.0D, 1.0D, 0.0D, 1.0D);
        register(bootstrapcontext, Noises.CLAY_BANDS_OFFSET, -8, 1.0D);
        register(bootstrapcontext, Noises.BADLANDS_PILLAR, -2, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.BADLANDS_PILLAR_ROOF, -8, 1.0D);
        register(bootstrapcontext, Noises.BADLANDS_SURFACE, -6, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.ICEBERG_PILLAR, -6, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.ICEBERG_PILLAR_ROOF, -3, 1.0D);
        register(bootstrapcontext, Noises.ICEBERG_SURFACE, -6, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.SWAMP, -2, 1.0D);
        register(bootstrapcontext, Noises.CALCITE, -9, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.GRAVEL, -8, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.POWDER_SNOW, -6, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.PACKED_ICE, -7, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.ICE, -4, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, Noises.SOUL_SAND_LAYER, -8, 1.0D, 1.0D, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.013333333333333334D);
        register(bootstrapcontext, Noises.GRAVEL_LAYER, -8, 1.0D, 1.0D, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.013333333333333334D);
        register(bootstrapcontext, Noises.PATCH, -5, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.013333333333333334D);
        register(bootstrapcontext, Noises.NETHERRACK, -3, 1.0D, 0.0D, 0.0D, 0.35D);
        register(bootstrapcontext, Noises.NETHER_WART, -3, 1.0D, 0.0D, 0.0D, 0.9D);
        register(bootstrapcontext, Noises.NETHER_STATE_SELECTOR, -4, 1.0D);
    }

    private static void registerBiomeNoises(BootstrapContext<NoiseGeneratorNormal.a> bootstrapcontext, int i, ResourceKey<NoiseGeneratorNormal.a> resourcekey, ResourceKey<NoiseGeneratorNormal.a> resourcekey1, ResourceKey<NoiseGeneratorNormal.a> resourcekey2, ResourceKey<NoiseGeneratorNormal.a> resourcekey3) {
        register(bootstrapcontext, resourcekey, -10 + i, 1.5D, 0.0D, 1.0D, 0.0D, 0.0D, 0.0D);
        register(bootstrapcontext, resourcekey1, -8 + i, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        register(bootstrapcontext, resourcekey2, -9 + i, 1.0D, 1.0D, 2.0D, 2.0D, 2.0D, 1.0D, 1.0D, 1.0D, 1.0D);
        register(bootstrapcontext, resourcekey3, -9 + i, 1.0D, 1.0D, 0.0D, 1.0D, 1.0D);
    }

    private static void register(BootstrapContext<NoiseGeneratorNormal.a> bootstrapcontext, ResourceKey<NoiseGeneratorNormal.a> resourcekey, int i, double d0, double... adouble) {
        bootstrapcontext.register(resourcekey, new NoiseGeneratorNormal.a(i, d0, adouble));
    }
}
