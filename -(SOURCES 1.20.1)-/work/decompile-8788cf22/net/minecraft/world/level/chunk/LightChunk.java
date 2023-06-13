package net.minecraft.world.level.chunk;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;

public interface LightChunk extends IBlockAccess {

    void findBlockLightSources(BiConsumer<BlockPosition, IBlockData> biconsumer);

    ChunkSkyLightSources getSkyLightSources();
}
