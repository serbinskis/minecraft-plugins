package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkGenerationTask {

    private final GeneratingChunkMap chunkMap;
    private final ChunkCoordIntPair pos;
    @Nullable
    private ChunkStatus scheduledStatus = null;
    public final ChunkStatus targetStatus;
    private volatile boolean markedForCancellation;
    private final List<CompletableFuture<ChunkResult<IChunkAccess>>> scheduledLayer = new ArrayList();
    private final StaticCache2D<GenerationChunkHolder> cache;
    private boolean needsGeneration;

    private ChunkGenerationTask(GeneratingChunkMap generatingchunkmap, ChunkStatus chunkstatus, ChunkCoordIntPair chunkcoordintpair, StaticCache2D<GenerationChunkHolder> staticcache2d) {
        this.chunkMap = generatingchunkmap;
        this.targetStatus = chunkstatus;
        this.pos = chunkcoordintpair;
        this.cache = staticcache2d;
    }

    public static ChunkGenerationTask create(GeneratingChunkMap generatingchunkmap, ChunkStatus chunkstatus, ChunkCoordIntPair chunkcoordintpair) {
        int i = ChunkPyramid.GENERATION_PYRAMID.getStepTo(chunkstatus).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
        StaticCache2D<GenerationChunkHolder> staticcache2d = StaticCache2D.create(chunkcoordintpair.x, chunkcoordintpair.z, i, (j, k) -> {
            return generatingchunkmap.acquireGeneration(ChunkCoordIntPair.asLong(j, k));
        });

        return new ChunkGenerationTask(generatingchunkmap, chunkstatus, chunkcoordintpair, staticcache2d);
    }

    @Nullable
    public CompletableFuture<?> runUntilWait() {
        while (true) {
            CompletableFuture<?> completablefuture = this.waitForScheduledLayer();

            if (completablefuture != null) {
                return completablefuture;
            }

            if (this.markedForCancellation || this.scheduledStatus == this.targetStatus) {
                this.releaseClaim();
                return null;
            }

            this.scheduleNextLayer();
        }
    }

    private void scheduleNextLayer() {
        ChunkStatus chunkstatus;

        if (this.scheduledStatus == null) {
            chunkstatus = ChunkStatus.EMPTY;
        } else if (!this.needsGeneration && this.scheduledStatus == ChunkStatus.EMPTY && !this.canLoadWithoutGeneration()) {
            this.needsGeneration = true;
            chunkstatus = ChunkStatus.EMPTY;
        } else {
            chunkstatus = (ChunkStatus) ChunkStatus.getStatusList().get(this.scheduledStatus.getIndex() + 1);
        }

        this.scheduleLayer(chunkstatus, this.needsGeneration);
        this.scheduledStatus = chunkstatus;
    }

    public void markForCancellation() {
        this.markedForCancellation = true;
    }

    private void releaseClaim() {
        GenerationChunkHolder generationchunkholder = (GenerationChunkHolder) this.cache.get(this.pos.x, this.pos.z);

        generationchunkholder.removeTask(this);
        StaticCache2D staticcache2d = this.cache;
        GeneratingChunkMap generatingchunkmap = this.chunkMap;

        Objects.requireNonNull(this.chunkMap);
        staticcache2d.forEach(generatingchunkmap::releaseGeneration);
    }

    private boolean canLoadWithoutGeneration() {
        if (this.targetStatus == ChunkStatus.EMPTY) {
            return true;
        } else {
            ChunkStatus chunkstatus = ((GenerationChunkHolder) this.cache.get(this.pos.x, this.pos.z)).getPersistedStatus();

            if (chunkstatus != null && !chunkstatus.isBefore(this.targetStatus)) {
                ChunkDependencies chunkdependencies = ChunkPyramid.LOADING_PYRAMID.getStepTo(this.targetStatus).accumulatedDependencies();
                int i = chunkdependencies.getRadius();

                for (int j = this.pos.x - i; j <= this.pos.x + i; ++j) {
                    for (int k = this.pos.z - i; k <= this.pos.z + i; ++k) {
                        int l = this.pos.getChessboardDistance(j, k);
                        ChunkStatus chunkstatus1 = chunkdependencies.get(l);
                        ChunkStatus chunkstatus2 = ((GenerationChunkHolder) this.cache.get(j, k)).getPersistedStatus();

                        if (chunkstatus2 == null || chunkstatus2.isBefore(chunkstatus1)) {
                            return false;
                        }
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public GenerationChunkHolder getCenter() {
        return (GenerationChunkHolder) this.cache.get(this.pos.x, this.pos.z);
    }

    private void scheduleLayer(ChunkStatus chunkstatus, boolean flag) {
        int i = this.getRadiusForLayer(chunkstatus, flag);

        for (int j = this.pos.x - i; j <= this.pos.x + i; ++j) {
            for (int k = this.pos.z - i; k <= this.pos.z + i; ++k) {
                GenerationChunkHolder generationchunkholder = (GenerationChunkHolder) this.cache.get(j, k);

                if (this.markedForCancellation || !this.scheduleChunkInLayer(chunkstatus, flag, generationchunkholder)) {
                    return;
                }
            }
        }

    }

    private int getRadiusForLayer(ChunkStatus chunkstatus, boolean flag) {
        ChunkPyramid chunkpyramid = flag ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;

        return chunkpyramid.getStepTo(this.targetStatus).getAccumulatedRadiusOf(chunkstatus);
    }

    private boolean scheduleChunkInLayer(ChunkStatus chunkstatus, boolean flag, GenerationChunkHolder generationchunkholder) {
        ChunkStatus chunkstatus1 = generationchunkholder.getPersistedStatus();
        boolean flag1 = chunkstatus1 != null && chunkstatus.isAfter(chunkstatus1);
        ChunkPyramid chunkpyramid = flag1 ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;

        if (flag1 && !flag) {
            throw new IllegalStateException("Can't load chunk, but didn't expect to need to generate");
        } else {
            CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = generationchunkholder.applyStep(chunkpyramid.getStepTo(chunkstatus), this.chunkMap, this.cache);
            ChunkResult<IChunkAccess> chunkresult = (ChunkResult) completablefuture.getNow((Object) null);

            if (chunkresult == null) {
                this.scheduledLayer.add(completablefuture);
                return true;
            } else if (chunkresult.isSuccess()) {
                return true;
            } else {
                this.markForCancellation();
                return false;
            }
        }
    }

    @Nullable
    private CompletableFuture<?> waitForScheduledLayer() {
        while (!this.scheduledLayer.isEmpty()) {
            CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = (CompletableFuture) this.scheduledLayer.getLast();
            ChunkResult<IChunkAccess> chunkresult = (ChunkResult) completablefuture.getNow((Object) null);

            if (chunkresult == null) {
                return completablefuture;
            }

            this.scheduledLayer.removeLast();
            if (!chunkresult.isSuccess()) {
                this.markForCancellation();
            }
        }

        return null;
    }
}
