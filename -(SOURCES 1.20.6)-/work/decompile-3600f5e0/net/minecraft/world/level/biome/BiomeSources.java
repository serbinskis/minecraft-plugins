package net.minecraft.world.level.biome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;

public class BiomeSources {

    public BiomeSources() {}

    public static MapCodec<? extends WorldChunkManager> bootstrap(IRegistry<MapCodec<? extends WorldChunkManager>> iregistry) {
        IRegistry.register(iregistry, "fixed", WorldChunkManagerHell.CODEC);
        IRegistry.register(iregistry, "multi_noise", WorldChunkManagerMultiNoise.CODEC);
        IRegistry.register(iregistry, "checkerboard", WorldChunkManagerCheckerBoard.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "the_end", WorldChunkManagerTheEnd.CODEC);
    }
}
