package net.minecraft.world.level.chunk.status;

import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.IChunkAccess;

@FunctionalInterface
public interface ChunkStatusTask {

    CompletableFuture<IChunkAccess> doWork(WorldGenContext worldgencontext, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess);
}
