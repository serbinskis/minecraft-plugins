package net.minecraft.world.level.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public class MultiNoiseBiomeSourceParameterLists {

    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> NETHER = register("nether");
    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> OVERWORLD = register("overworld");

    public MultiNoiseBiomeSourceParameterLists() {}

    public static void bootstrap(BootstrapContext<MultiNoiseBiomeSourceParameterList> bootstrapcontext) {
        HolderGetter<BiomeBase> holdergetter = bootstrapcontext.lookup(Registries.BIOME);

        bootstrapcontext.register(MultiNoiseBiomeSourceParameterLists.NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.a.NETHER, holdergetter));
        bootstrapcontext.register(MultiNoiseBiomeSourceParameterLists.OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.a.OVERWORLD, holdergetter));
    }

    private static ResourceKey<MultiNoiseBiomeSourceParameterList> register(String s) {
        return ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, new MinecraftKey(s));
    }
}
