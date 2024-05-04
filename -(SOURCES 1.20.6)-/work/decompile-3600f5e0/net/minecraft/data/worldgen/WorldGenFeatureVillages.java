package net.minecraft.data.worldgen;

import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public class WorldGenFeatureVillages {

    public WorldGenFeatureVillages() {}

    public static void bootstrap(BootstrapContext<WorldGenFeatureDefinedStructurePoolTemplate> bootstrapcontext) {
        WorldGenFeatureVillagePlain.bootstrap(bootstrapcontext);
        WorldGenFeatureVillageSnowy.bootstrap(bootstrapcontext);
        WorldGenFeatureVillageSavanna.bootstrap(bootstrapcontext);
        WorldGenFeatureDesertVillage.bootstrap(bootstrapcontext);
        WorldGenFeatureVillageTaiga.bootstrap(bootstrapcontext);
    }
}
