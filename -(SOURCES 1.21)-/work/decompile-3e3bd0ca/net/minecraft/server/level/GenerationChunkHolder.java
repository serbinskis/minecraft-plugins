package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunkExtension;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public abstract class GenerationChunkHolder {

    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private static final ChunkResult<IChunkAccess> NOT_DONE_YET = ChunkResult.error("Not done yet");
    public static final ChunkResult<IChunkAccess> UNLOADED_CHUNK = ChunkResult.error("Unloaded chunk");
    public static final CompletableFuture<ChunkResult<IChunkAccess>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(GenerationChunkHolder.UNLOADED_CHUNK);
    protected final ChunkCoordIntPair pos;
    @Nullable
    private volatile ChunkStatus highestAllowedStatus;
    private final AtomicReference<ChunkStatus> startedWork = new AtomicReference();
    private final AtomicReferenceArray<CompletableFuture<ChunkResult<IChunkAccess>>> futures;
    private final AtomicReference<ChunkGenerationTask> task;
    private final AtomicInteger generationRefCount;

    public GenerationChunkHolder(ChunkCoordIntPair chunkcoordintpair) {
        this.futures = new AtomicReferenceArray(GenerationChunkHolder.CHUNK_STATUSES.size());
        this.task = new AtomicReference();
        this.generationRefCount = new AtomicInteger();
        this.pos = chunkcoordintpair;
    }

    public CompletableFuture<ChunkResult<IChunkAccess>> scheduleChunkGenerationTask(ChunkStatus chunkstatus, PlayerChunkMap playerchunkmap) {
        if (this.isStatusDisallowed(chunkstatus)) {
            return GenerationChunkHolder.UNLOADED_CHUNK_FUTURE;
        } else {
            CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = this.getOrCreateFuture(chunkstatus);

            if (completablefuture.isDone()) {
                return completablefuture;
            } else {
                ChunkGenerationTask chunkgenerationtask = (ChunkGenerationTask) this.task.get();

                if (chunkgenerationtask == null || chunkstatus.isAfter(chunkgenerationtask.targetStatus)) {
                    this.rescheduleChunkTask(playerchunkmap, chunkstatus);
                }

                return completablefuture;
            }
        }
    }

    CompletableFuture<ChunkResult<IChunkAccess>> applyStep(ChunkStep chunkstep, GeneratingChunkMap generatingchunkmap, StaticCache2D<GenerationChunkHolder> staticcache2d) {
        return this.isStatusDisallowed(chunkstep.targetStatus()) ? GenerationChunkHolder.UNLOADED_CHUNK_FUTURE : (this.acquireStatusBump(chunkstep.targetStatus()) ? generatingchunkmap.applyStep(this, chunkstep, staticcache2d).handle((ichunkaccess, throwable) -> {
            if (throwable != null) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception chunk generation/loading");

                MinecraftServer.setFatalException(new ReportedException(crashreport));
            } else {
                this.completeFuture(chunkstep.targetStatus(), ichunkaccess);
            }

            return ChunkResult.of(ichunkaccess);
        }) : this.getOrCreateFuture(chunkstep.targetStatus()));
    }

    protected void updateHighestAllowedStatus(PlayerChunkMap playerchunkmap) {
        ChunkStatus chunkstatus = this.highestAllowedStatus;
        ChunkStatus chunkstatus1 = ChunkLevel.generationStatus(this.getTicketLevel());

        this.highestAllowedStatus = chunkstatus1;
        boolean flag = chunkstatus != null && (chunkstatus1 == null || chunkstatus1.isBefore(chunkstatus));

        if (flag) {
            this.failAndClearPendingFuturesBetween(chunkstatus1, chunkstatus);
            if (this.task.get() != null) {
                this.rescheduleChunkTask(playerchunkmap, this.findHighestStatusWithPendingFuture(chunkstatus1));
            }
        }

    }

    public void replaceProtoChunk(ProtoChunkExtension protochunkextension) {
        CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = CompletableFuture.completedFuture(ChunkResult.of(protochunkextension));

        for (int i = 0; i < this.futures.length() - 1; ++i) {
            CompletableFuture<ChunkResult<IChunkAccess>> completablefuture1 = (CompletableFuture) this.futures.get(i);

            Objects.requireNonNull(completablefuture1);
            IChunkAccess ichunkaccess = (IChunkAccess) ((ChunkResult) completablefuture1.getNow(GenerationChunkHolder.NOT_DONE_YET)).orElse((Object) null);

            if (!(ichunkaccess instanceof ProtoChunk)) {
                throw new IllegalStateException("Trying to replace a ProtoChunk, but found " + String.valueOf(ichunkaccess));
            }

            if (!this.futures.compareAndSet(i, completablefuture1, completablefuture)) {
                throw new IllegalStateException("Future changed by other thread while trying to replace it");
            }
        }

    }

    void removeTask(ChunkGenerationTask chunkgenerationtask) {
        this.task.compareAndSet(chunkgenerationtask, (Object) null);
    }

    private void rescheduleChunkTask(PlayerChunkMap playerchunkmap, @Nullable ChunkStatus chunkstatus) {
        ChunkGenerationTask chunkgenerationtask;

        if (chunkstatus != null) {
            chunkgenerationtask = playerchunkmap.scheduleGenerationTask(chunkstatus, this.getPos());
        } else {
            chunkgenerationtask = null;
        }

        ChunkGenerationTask chunkgenerationtask1 = (ChunkGenerationTask) this.task.getAndSet(chunkgenerationtask);

        if (chunkgenerationtask1 != null) {
            chunkgenerationtask1.markForCancellation();
        }

    }

    private CompletableFuture<ChunkResult<IChunkAccess>> getOrCreateFuture(ChunkStatus chunkstatus) {
        if (this.isStatusDisallowed(chunkstatus)) {
            return GenerationChunkHolder.UNLOADED_CHUNK_FUTURE;
        } else {
            int i = chunkstatus.getIndex();
            CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = (CompletableFuture) this.futures.get(i);

            CompletableFuture completablefuture1;

            do {
                if (completablefuture != null) {
                    return completablefuture;
                }

                completablefuture1 = new CompletableFuture();
                completablefuture = (CompletableFuture) this.futures.compareAndExchange(i, (Object) null, completablefuture1);
            } while (completablefuture != null);

            if (this.isStatusDisallowed(chunkstatus)) {
                this.failAndClearPendingFuture(i, completablefuture1);
                return GenerationChunkHolder.UNLOADED_CHUNK_FUTURE;
            } else {
                return completablefuture1;
            }
        }
    }

    private void failAndClearPendingFuturesBetween(@Nullable ChunkStatus chunkstatus, ChunkStatus chunkstatus1) {
        int i = chunkstatus == null ? 0 : chunkstatus.getIndex() + 1;
        int j = chunkstatus1.getIndex();

        for (int k = i; k <= j; ++k) {
            CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = (CompletableFuture) this.futures.get(k);

            if (completablefuture != null) {
                this.failAndClearPendingFuture(k, completablefuture);
            }
        }

    }

    private void failAndClearPendingFuture(int i, CompletableFuture<ChunkResult<IChunkAccess>> completablefuture) {
        if (completablefuture.complete(GenerationChunkHolder.UNLOADED_CHUNK) && !this.futures.compareAndSet(i, completablefuture, (Object) null)) {
            throw new IllegalStateException("Nothing else should replace the future here");
        }
    }

    private void completeFuture(ChunkStatus chunkstatus, IChunkAccess ichunkaccess) {
        ChunkResult<IChunkAccess> chunkresult = ChunkResult.of(ichunkaccess);
        int i = chunkstatus.getIndex();

        do {
            while (true) {
                CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = (CompletableFuture) this.futures.get(i);

                if (completablefuture == null) {
                    break;
                }

                if (completablefuture.complete(chunkresult)) {
                    return;
                }

                if (((ChunkResult) completablefuture.getNow(GenerationChunkHolder.NOT_DONE_YET)).isSuccess()) {
                    throw new IllegalStateException("Trying to complete a future but found it to be completed successfully already");
                }

                Thread.yield();
            }
        } while (!this.futures.compareAndSet(i, (Object) null, CompletableFuture.completedFuture(chunkresult)));

    }

    @Nullable
    private ChunkStatus findHighestStatusWithPendingFuture(@Nullable ChunkStatus chunkstatus) {
        if (chunkstatus == null) {
            return null;
        } else {
            ChunkStatus chunkstatus1 = chunkstatus;

            for (ChunkStatus chunkstatus2 = (ChunkStatus) this.startedWork.get(); chunkstatus2 == null || chunkstatus1.isAfter(chunkstatus2); chunkstatus1 = chunkstatus1.getParent()) {
                if (this.futures.get(chunkstatus1.getIndex()) != null) {
                    return chunkstatus1;
                }

                if (chunkstatus1 == ChunkStatus.EMPTY) {
                    break;
                }
            }

            return null;
        }
    }

    private boolean acquireStatusBump(ChunkStatus chunkstatus) {
        ChunkStatus chunkstatus1 = chunkstatus == ChunkStatus.EMPTY ? null : chunkstatus.getParent();
        ChunkStatus chunkstatus2 = (ChunkStatus) this.startedWork.compareAndExchange(chunkstatus1, chunkstatus);

        if (chunkstatus2 == chunkstatus1) {
            return true;
        } else if (chunkstatus2 != null && !chunkstatus.isAfter(chunkstatus2)) {
            return false;
        } else {
            String s = String.valueOf(chunkstatus2);

            throw new IllegalStateException("Unexpected last startedWork status: " + s + " while trying to start: " + String.valueOf(chunkstatus));
        }
    }

    private boolean isStatusDisallowed(ChunkStatus chunkstatus) {
        ChunkStatus chunkstatus1 = this.highestAllowedStatus;

        return chunkstatus1 == null || chunkstatus.isAfter(chunkstatus1);
    }

    public void increaseGenerationRefCount() {
        this.generationRefCount.incrementAndGet();
    }

    public void decreaseGenerationRefCount() {
        int i = this.generationRefCount.decrementAndGet();

        if (i < 0) {
            throw new IllegalStateException("More releases than claims. Count: " + i);
        }
    }

    public int getGenerationRefCount() {
        return this.generationRefCount.get();
    }

    @Nullable
    public IChunkAccess getChunkIfPresentUnchecked(ChunkStatus chunkstatus) {
        CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = (CompletableFuture) this.futures.get(chunkstatus.getIndex());

        return completablefuture == null ? null : (IChunkAccess) ((ChunkResult) completablefuture.getNow(GenerationChunkHolder.NOT_DONE_YET)).orElse((Object) null);
    }

    @Nullable
    public IChunkAccess getChunkIfPresent(ChunkStatus chunkstatus) {
        return this.isStatusDisallowed(chunkstatus) ? null : this.getChunkIfPresentUnchecked(chunkstatus);
    }

    @Nullable
    public IChunkAccess getLatestChunk() {
        ChunkStatus chunkstatus = (ChunkStatus) this.startedWork.get();

        if (chunkstatus == null) {
            return null;
        } else {
            IChunkAccess ichunkaccess = this.getChunkIfPresentUnchecked(chunkstatus);

            return ichunkaccess != null ? ichunkaccess : this.getChunkIfPresentUnchecked(chunkstatus.getParent());
        }
    }

    @Nullable
    public ChunkStatus getPersistedStatus() {
        CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = (CompletableFuture) this.futures.get(ChunkStatus.EMPTY.getIndex());
        IChunkAccess ichunkaccess = completablefuture == null ? null : (IChunkAccess) ((ChunkResult) completablefuture.getNow(GenerationChunkHolder.NOT_DONE_YET)).orElse((Object) null);

        return ichunkaccess == null ? null : ichunkaccess.getPersistedStatus();
    }

    public ChunkCoordIntPair getPos() {
        return this.pos;
    }

    public FullChunkStatus getFullStatus() {
        return ChunkLevel.fullStatus(this.getTicketLevel());
    }

    public abstract int getTicketLevel();

    public abstract int getQueueLevel();

    @VisibleForDebug
    public List<Pair<ChunkStatus, CompletableFuture<ChunkResult<IChunkAccess>>>> getAllFutures() {
        List<Pair<ChunkStatus, CompletableFuture<ChunkResult<IChunkAccess>>>> list = new ArrayList();

        for (int i = 0; i < GenerationChunkHolder.CHUNK_STATUSES.size(); ++i) {
            list.add(Pair.of((ChunkStatus) GenerationChunkHolder.CHUNK_STATUSES.get(i), (CompletableFuture) this.futures.get(i)));
        }

        return list;
    }

    @Nullable
    @VisibleForDebug
    public ChunkStatus getLatestStatus() {
        for (int i = GenerationChunkHolder.CHUNK_STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus chunkstatus = (ChunkStatus) GenerationChunkHolder.CHUNK_STATUSES.get(i);
            IChunkAccess ichunkaccess = this.getChunkIfPresentUnchecked(chunkstatus);

            if (ichunkaccess != null) {
                return chunkstatus;
            }
        }

        return null;
    }
}
