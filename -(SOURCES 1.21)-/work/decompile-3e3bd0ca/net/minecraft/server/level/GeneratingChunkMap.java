package net.minecraft.server.level;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public interface GeneratingChunkMap {

    GenerationChunkHolder acquireGeneration(long i);

    void releaseGeneration(GenerationChunkHolder generationchunkholder);

    CompletableFuture<IChunkAccess> applyStep(GenerationChunkHolder generationchunkholder, ChunkStep chunkstep, StaticCache2D<GenerationChunkHolder> staticcache2d);

    ChunkGenerationTask scheduleGenerationTask(ChunkStatus chunkstatus, ChunkCoordIntPair chunkcoordintpair);

    void runGenerationTasks();
}
