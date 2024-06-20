package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;

public record ChunkStep(ChunkStatus targetStatus, ChunkDependencies directDependencies, ChunkDependencies accumulatedDependencies, int blockStateWriteRadius, ChunkStatusTask task) {

    public int getAccumulatedRadiusOf(ChunkStatus chunkstatus) {
        return chunkstatus == this.targetStatus ? 0 : this.accumulatedDependencies.getRadiusOf(chunkstatus);
    }

    public CompletableFuture<IChunkAccess> apply(WorldGenContext worldgencontext, StaticCache2D<GenerationChunkHolder> staticcache2d, IChunkAccess ichunkaccess) {
        if (ichunkaccess.getPersistedStatus().isBefore(this.targetStatus)) {
            ProfiledDuration profiledduration = JvmProfiler.INSTANCE.onChunkGenerate(ichunkaccess.getPos(), worldgencontext.level().dimension(), this.targetStatus.getName());

            return this.task.doWork(worldgencontext, this, staticcache2d, ichunkaccess).thenApply((ichunkaccess1) -> {
                return this.completeChunkGeneration(ichunkaccess1, profiledduration);
            });
        } else {
            return this.task.doWork(worldgencontext, this, staticcache2d, ichunkaccess);
        }
    }

    private IChunkAccess completeChunkGeneration(IChunkAccess ichunkaccess, @Nullable ProfiledDuration profiledduration) {
        if (ichunkaccess instanceof ProtoChunk protochunk) {
            if (protochunk.getPersistedStatus().isBefore(this.targetStatus)) {
                protochunk.setPersistedStatus(this.targetStatus);
            }
        }

        if (profiledduration != null) {
            profiledduration.finish();
        }

        return ichunkaccess;
    }

    public static class a {

        private final ChunkStatus status;
        @Nullable
        private final ChunkStep parent;
        private ChunkStatus[] directDependenciesByRadius;
        private int blockStateWriteRadius = -1;
        private ChunkStatusTask task = ChunkStatusTasks::passThrough;

        protected a(ChunkStatus chunkstatus) {
            if (chunkstatus.getParent() != chunkstatus) {
                throw new IllegalArgumentException("Not starting with the first status: " + String.valueOf(chunkstatus));
            } else {
                this.status = chunkstatus;
                this.parent = null;
                this.directDependenciesByRadius = new ChunkStatus[0];
            }
        }

        protected a(ChunkStatus chunkstatus, ChunkStep chunkstep) {
            if (chunkstep.targetStatus.getIndex() != chunkstatus.getIndex() - 1) {
                throw new IllegalArgumentException("Out of order status: " + String.valueOf(chunkstatus));
            } else {
                this.status = chunkstatus;
                this.parent = chunkstep;
                this.directDependenciesByRadius = new ChunkStatus[]{chunkstep.targetStatus};
            }
        }

        public ChunkStep.a addRequirement(ChunkStatus chunkstatus, int i) {
            if (chunkstatus.isOrAfter(this.status)) {
                String s = String.valueOf(chunkstatus);

                throw new IllegalArgumentException("Status " + s + " can not be required by " + String.valueOf(this.status));
            } else {
                ChunkStatus[] achunkstatus = this.directDependenciesByRadius;
                int j = i + 1;

                if (j > achunkstatus.length) {
                    this.directDependenciesByRadius = new ChunkStatus[j];
                    Arrays.fill(this.directDependenciesByRadius, chunkstatus);
                }

                for (int k = 0; k < Math.min(j, achunkstatus.length); ++k) {
                    this.directDependenciesByRadius[k] = ChunkStatus.max(achunkstatus[k], chunkstatus);
                }

                return this;
            }
        }

        public ChunkStep.a blockStateWriteRadius(int i) {
            this.blockStateWriteRadius = i;
            return this;
        }

        public ChunkStep.a setTask(ChunkStatusTask chunkstatustask) {
            this.task = chunkstatustask;
            return this;
        }

        public ChunkStep build() {
            return new ChunkStep(this.status, new ChunkDependencies(ImmutableList.copyOf(this.directDependenciesByRadius)), new ChunkDependencies(ImmutableList.copyOf(this.buildAccumulatedDependencies())), this.blockStateWriteRadius, this.task);
        }

        private ChunkStatus[] buildAccumulatedDependencies() {
            if (this.parent == null) {
                return this.directDependenciesByRadius;
            } else {
                int i = this.getRadiusOfParent(this.parent.targetStatus);
                ChunkDependencies chunkdependencies = this.parent.accumulatedDependencies;
                ChunkStatus[] achunkstatus = new ChunkStatus[Math.max(i + chunkdependencies.size(), this.directDependenciesByRadius.length)];

                for (int j = 0; j < achunkstatus.length; ++j) {
                    int k = j - i;

                    if (k >= 0 && k < chunkdependencies.size()) {
                        if (j >= this.directDependenciesByRadius.length) {
                            achunkstatus[j] = chunkdependencies.get(k);
                        } else {
                            achunkstatus[j] = ChunkStatus.max(this.directDependenciesByRadius[j], chunkdependencies.get(k));
                        }
                    } else {
                        achunkstatus[j] = this.directDependenciesByRadius[j];
                    }
                }

                return achunkstatus;
            }
        }

        private int getRadiusOfParent(ChunkStatus chunkstatus) {
            for (int i = this.directDependenciesByRadius.length - 1; i >= 0; --i) {
                if (this.directDependenciesByRadius[i].isOrAfter(chunkstatus)) {
                    return i;
                }
            }

            return 0;
        }
    }
}
