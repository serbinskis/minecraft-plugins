package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public record GeneratorSettingBase(NoiseSettings noiseSettings, IBlockData defaultBlock, IBlockData defaultFluid, NoiseRouter noiseRouter, SurfaceRules.o surfaceRule, List<Climate.d> spawnTarget, int seaLevel, boolean disableMobGeneration, boolean aquifersEnabled, boolean oreVeinsEnabled, boolean useLegacyRandomSource) {

    public static final Codec<GeneratorSettingBase> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(NoiseSettings.CODEC.fieldOf("noise").forGetter(GeneratorSettingBase::noiseSettings), IBlockData.CODEC.fieldOf("default_block").forGetter(GeneratorSettingBase::defaultBlock), IBlockData.CODEC.fieldOf("default_fluid").forGetter(GeneratorSettingBase::defaultFluid), NoiseRouter.CODEC.fieldOf("noise_router").forGetter(GeneratorSettingBase::noiseRouter), SurfaceRules.o.CODEC.fieldOf("surface_rule").forGetter(GeneratorSettingBase::surfaceRule), Climate.d.CODEC.listOf().fieldOf("spawn_target").forGetter(GeneratorSettingBase::spawnTarget), Codec.INT.fieldOf("sea_level").forGetter(GeneratorSettingBase::seaLevel), Codec.BOOL.fieldOf("disable_mob_generation").forGetter(GeneratorSettingBase::disableMobGeneration), Codec.BOOL.fieldOf("aquifers_enabled").forGetter(GeneratorSettingBase::isAquifersEnabled), Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(GeneratorSettingBase::oreVeinsEnabled), Codec.BOOL.fieldOf("legacy_random_source").forGetter(GeneratorSettingBase::useLegacyRandomSource)).apply(instance, GeneratorSettingBase::new);
    });
    public static final Codec<Holder<GeneratorSettingBase>> CODEC = RegistryFileCodec.create(Registries.NOISE_SETTINGS, GeneratorSettingBase.DIRECT_CODEC);
    public static final ResourceKey<GeneratorSettingBase> OVERWORLD = ResourceKey.create(Registries.NOISE_SETTINGS, new MinecraftKey("overworld"));
    public static final ResourceKey<GeneratorSettingBase> LARGE_BIOMES = ResourceKey.create(Registries.NOISE_SETTINGS, new MinecraftKey("large_biomes"));
    public static final ResourceKey<GeneratorSettingBase> AMPLIFIED = ResourceKey.create(Registries.NOISE_SETTINGS, new MinecraftKey("amplified"));
    public static final ResourceKey<GeneratorSettingBase> NETHER = ResourceKey.create(Registries.NOISE_SETTINGS, new MinecraftKey("nether"));
    public static final ResourceKey<GeneratorSettingBase> END = ResourceKey.create(Registries.NOISE_SETTINGS, new MinecraftKey("end"));
    public static final ResourceKey<GeneratorSettingBase> CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, new MinecraftKey("caves"));
    public static final ResourceKey<GeneratorSettingBase> FLOATING_ISLANDS = ResourceKey.create(Registries.NOISE_SETTINGS, new MinecraftKey("floating_islands"));

    public boolean isAquifersEnabled() {
        return this.aquifersEnabled;
    }

    public SeededRandom.a getRandomSource() {
        return this.useLegacyRandomSource ? SeededRandom.a.LEGACY : SeededRandom.a.XOROSHIRO;
    }

    public static void bootstrap(BootstrapContext<GeneratorSettingBase> bootstrapcontext) {
        bootstrapcontext.register(GeneratorSettingBase.OVERWORLD, overworld(bootstrapcontext, false, false));
        bootstrapcontext.register(GeneratorSettingBase.LARGE_BIOMES, overworld(bootstrapcontext, false, true));
        bootstrapcontext.register(GeneratorSettingBase.AMPLIFIED, overworld(bootstrapcontext, true, false));
        bootstrapcontext.register(GeneratorSettingBase.NETHER, nether(bootstrapcontext));
        bootstrapcontext.register(GeneratorSettingBase.END, end(bootstrapcontext));
        bootstrapcontext.register(GeneratorSettingBase.CAVES, caves(bootstrapcontext));
        bootstrapcontext.register(GeneratorSettingBase.FLOATING_ISLANDS, floatingIslands(bootstrapcontext));
    }

    private static GeneratorSettingBase end(BootstrapContext<?> bootstrapcontext) {
        return new GeneratorSettingBase(NoiseSettings.END_NOISE_SETTINGS, Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), NoiseRouterData.end(bootstrapcontext.lookup(Registries.DENSITY_FUNCTION)), SurfaceRuleData.end(), List.of(), 0, true, false, false, true);
    }

    private static GeneratorSettingBase nether(BootstrapContext<?> bootstrapcontext) {
        return new GeneratorSettingBase(NoiseSettings.NETHER_NOISE_SETTINGS, Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), NoiseRouterData.nether(bootstrapcontext.lookup(Registries.DENSITY_FUNCTION), bootstrapcontext.lookup(Registries.NOISE)), SurfaceRuleData.nether(), List.of(), 32, false, false, false, true);
    }

    private static GeneratorSettingBase overworld(BootstrapContext<?> bootstrapcontext, boolean flag, boolean flag1) {
        return new GeneratorSettingBase(NoiseSettings.OVERWORLD_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworld(bootstrapcontext.lookup(Registries.DENSITY_FUNCTION), bootstrapcontext.lookup(Registries.NOISE), flag1, flag), SurfaceRuleData.overworld(), (new OverworldBiomeBuilder()).spawnTarget(), 63, false, true, true, false);
    }

    private static GeneratorSettingBase caves(BootstrapContext<?> bootstrapcontext) {
        return new GeneratorSettingBase(NoiseSettings.CAVES_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.caves(bootstrapcontext.lookup(Registries.DENSITY_FUNCTION), bootstrapcontext.lookup(Registries.NOISE)), SurfaceRuleData.overworldLike(false, true, true), List.of(), 32, false, false, false, true);
    }

    private static GeneratorSettingBase floatingIslands(BootstrapContext<?> bootstrapcontext) {
        return new GeneratorSettingBase(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.floatingIslands(bootstrapcontext.lookup(Registries.DENSITY_FUNCTION), bootstrapcontext.lookup(Registries.NOISE)), SurfaceRuleData.overworldLike(false, false, false), List.of(), -64, false, false, false, true);
    }

    public static GeneratorSettingBase dummy() {
        return new GeneratorSettingBase(NoiseSettings.OVERWORLD_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), NoiseRouterData.none(), SurfaceRuleData.air(), List.of(), 63, true, false, false, false);
    }
}
