package net.minecraft.world.level.levelgen.presets;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.biome.WorldChunkManagerHell;
import net.minecraft.world.level.biome.WorldChunkManagerMultiNoise;
import net.minecraft.world.level.biome.WorldChunkManagerTheEnd;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract;
import net.minecraft.world.level.levelgen.ChunkProviderDebug;
import net.minecraft.world.level.levelgen.ChunkProviderFlat;
import net.minecraft.world.level.levelgen.GeneratorSettingBase;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.GeneratorSettingsFlat;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class WorldPresets {

    public static final ResourceKey<WorldPreset> NORMAL = register("normal");
    public static final ResourceKey<WorldPreset> FLAT = register("flat");
    public static final ResourceKey<WorldPreset> LARGE_BIOMES = register("large_biomes");
    public static final ResourceKey<WorldPreset> AMPLIFIED = register("amplified");
    public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = register("single_biome_surface");
    public static final ResourceKey<WorldPreset> DEBUG = register("debug_all_block_states");

    public WorldPresets() {}

    public static void bootstrap(BootstrapContext<WorldPreset> bootstrapcontext) {
        (new WorldPresets.a(bootstrapcontext)).bootstrap();
    }

    private static ResourceKey<WorldPreset> register(String s) {
        return ResourceKey.create(Registries.WORLD_PRESET, MinecraftKey.withDefaultNamespace(s));
    }

    public static Optional<ResourceKey<WorldPreset>> fromSettings(WorldDimensions worlddimensions) {
        return worlddimensions.get(WorldDimension.OVERWORLD).flatMap((worlddimension) -> {
            ChunkGenerator chunkgenerator = worlddimension.generator();

            Objects.requireNonNull(chunkgenerator);
            ChunkGenerator chunkgenerator1 = chunkgenerator;
            byte b0 = 0;
            Optional optional;

            //$FF: b0->value
            //0->net/minecraft/world/level/levelgen/ChunkProviderFlat
            //1->net/minecraft/world/level/levelgen/ChunkProviderDebug
            //2->net/minecraft/world/level/levelgen/ChunkGeneratorAbstract
            switch (chunkgenerator1.typeSwitch<invokedynamic>(chunkgenerator1, b0)) {
                case 0:
                    ChunkProviderFlat chunkproviderflat = (ChunkProviderFlat)chunkgenerator1;

                    optional = Optional.of(WorldPresets.FLAT);
                    break;
                case 1:
                    ChunkProviderDebug chunkproviderdebug = (ChunkProviderDebug)chunkgenerator1;

                    optional = Optional.of(WorldPresets.DEBUG);
                    break;
                case 2:
                    ChunkGeneratorAbstract chunkgeneratorabstract = (ChunkGeneratorAbstract)chunkgenerator1;

                    optional = Optional.of(WorldPresets.NORMAL);
                    break;
                default:
                    optional = Optional.empty();
            }

            return optional;
        });
    }

    public static WorldDimensions createNormalWorldDimensions(IRegistryCustom iregistrycustom) {
        return ((WorldPreset) iregistrycustom.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.NORMAL).value()).createWorldDimensions();
    }

    public static WorldDimension getNormalOverworld(IRegistryCustom iregistrycustom) {
        return (WorldDimension) ((WorldPreset) iregistrycustom.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.NORMAL).value()).overworld().orElseThrow();
    }

    private static class a {

        private final BootstrapContext<WorldPreset> context;
        private final HolderGetter<GeneratorSettingBase> noiseSettings;
        private final HolderGetter<BiomeBase> biomes;
        private final HolderGetter<PlacedFeature> placedFeatures;
        private final HolderGetter<StructureSet> structureSets;
        private final HolderGetter<MultiNoiseBiomeSourceParameterList> multiNoiseBiomeSourceParameterLists;
        private final Holder<DimensionManager> overworldDimensionType;
        private final WorldDimension netherStem;
        private final WorldDimension endStem;

        a(BootstrapContext<WorldPreset> bootstrapcontext) {
            this.context = bootstrapcontext;
            HolderGetter<DimensionManager> holdergetter = bootstrapcontext.lookup(Registries.DIMENSION_TYPE);

            this.noiseSettings = bootstrapcontext.lookup(Registries.NOISE_SETTINGS);
            this.biomes = bootstrapcontext.lookup(Registries.BIOME);
            this.placedFeatures = bootstrapcontext.lookup(Registries.PLACED_FEATURE);
            this.structureSets = bootstrapcontext.lookup(Registries.STRUCTURE_SET);
            this.multiNoiseBiomeSourceParameterLists = bootstrapcontext.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            this.overworldDimensionType = holdergetter.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
            Holder<DimensionManager> holder = holdergetter.getOrThrow(BuiltinDimensionTypes.NETHER);
            Holder<GeneratorSettingBase> holder1 = this.noiseSettings.getOrThrow(GeneratorSettingBase.NETHER);
            Holder.c<MultiNoiseBiomeSourceParameterList> holder_c = this.multiNoiseBiomeSourceParameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);

            this.netherStem = new WorldDimension(holder, new ChunkGeneratorAbstract(WorldChunkManagerMultiNoise.createFromPreset(holder_c), holder1));
            Holder<DimensionManager> holder2 = holdergetter.getOrThrow(BuiltinDimensionTypes.END);
            Holder<GeneratorSettingBase> holder3 = this.noiseSettings.getOrThrow(GeneratorSettingBase.END);

            this.endStem = new WorldDimension(holder2, new ChunkGeneratorAbstract(WorldChunkManagerTheEnd.create(this.biomes), holder3));
        }

        private WorldDimension makeOverworld(ChunkGenerator chunkgenerator) {
            return new WorldDimension(this.overworldDimensionType, chunkgenerator);
        }

        private WorldDimension makeNoiseBasedOverworld(WorldChunkManager worldchunkmanager, Holder<GeneratorSettingBase> holder) {
            return this.makeOverworld(new ChunkGeneratorAbstract(worldchunkmanager, holder));
        }

        private WorldPreset createPresetWithCustomOverworld(WorldDimension worlddimension) {
            return new WorldPreset(Map.of(WorldDimension.OVERWORLD, worlddimension, WorldDimension.NETHER, this.netherStem, WorldDimension.END, this.endStem));
        }

        private void registerCustomOverworldPreset(ResourceKey<WorldPreset> resourcekey, WorldDimension worlddimension) {
            this.context.register(resourcekey, this.createPresetWithCustomOverworld(worlddimension));
        }

        private void registerOverworlds(WorldChunkManager worldchunkmanager) {
            Holder<GeneratorSettingBase> holder = this.noiseSettings.getOrThrow(GeneratorSettingBase.OVERWORLD);

            this.registerCustomOverworldPreset(WorldPresets.NORMAL, this.makeNoiseBasedOverworld(worldchunkmanager, holder));
            Holder<GeneratorSettingBase> holder1 = this.noiseSettings.getOrThrow(GeneratorSettingBase.LARGE_BIOMES);

            this.registerCustomOverworldPreset(WorldPresets.LARGE_BIOMES, this.makeNoiseBasedOverworld(worldchunkmanager, holder1));
            Holder<GeneratorSettingBase> holder2 = this.noiseSettings.getOrThrow(GeneratorSettingBase.AMPLIFIED);

            this.registerCustomOverworldPreset(WorldPresets.AMPLIFIED, this.makeNoiseBasedOverworld(worldchunkmanager, holder2));
        }

        public void bootstrap() {
            Holder.c<MultiNoiseBiomeSourceParameterList> holder_c = this.multiNoiseBiomeSourceParameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

            this.registerOverworlds(WorldChunkManagerMultiNoise.createFromPreset(holder_c));
            Holder<GeneratorSettingBase> holder = this.noiseSettings.getOrThrow(GeneratorSettingBase.OVERWORLD);
            Holder.c<BiomeBase> holder_c1 = this.biomes.getOrThrow(Biomes.PLAINS);

            this.registerCustomOverworldPreset(WorldPresets.SINGLE_BIOME_SURFACE, this.makeNoiseBasedOverworld(new WorldChunkManagerHell(holder_c1), holder));
            this.registerCustomOverworldPreset(WorldPresets.FLAT, this.makeOverworld(new ChunkProviderFlat(GeneratorSettingsFlat.getDefault(this.biomes, this.structureSets, this.placedFeatures))));
            this.registerCustomOverworldPreset(WorldPresets.DEBUG, this.makeOverworld(new ChunkProviderDebug(holder_c1)));
        }
    }
}
