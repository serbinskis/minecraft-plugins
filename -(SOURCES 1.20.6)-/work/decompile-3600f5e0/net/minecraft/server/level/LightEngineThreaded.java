package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.thread.Mailbox;
import net.minecraft.util.thread.ThreadedMailbox;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.NibbleArray;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class LightEngineThreaded extends LevelLightEngine implements AutoCloseable {

    public static final int DEFAULT_BATCH_SIZE = 1000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ThreadedMailbox<Runnable> taskMailbox;
    private final ObjectList<Pair<LightEngineThreaded.Update, Runnable>> lightTasks = new ObjectArrayList();
    private final PlayerChunkMap chunkMap;
    private final Mailbox<ChunkTaskQueueSorter.a<Runnable>> sorterMailbox;
    private final int taskPerBatch = 1000;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    public LightEngineThreaded(ILightAccess ilightaccess, PlayerChunkMap playerchunkmap, boolean flag, ThreadedMailbox<Runnable> threadedmailbox, Mailbox<ChunkTaskQueueSorter.a<Runnable>> mailbox) {
        super(ilightaccess, true, flag);
        this.chunkMap = playerchunkmap;
        this.sorterMailbox = mailbox;
        this.taskMailbox = threadedmailbox;
    }

    public void close() {}

    @Override
    public int runLightUpdates() {
        throw (UnsupportedOperationException) SystemUtils.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
    }

    @Override
    public void checkBlock(BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.immutable();

        this.addTask(SectionPosition.blockToSectionCoord(blockposition.getX()), SectionPosition.blockToSectionCoord(blockposition.getZ()), LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            super.checkBlock(blockposition1);
        }, () -> {
            return "checkBlock " + String.valueOf(blockposition1);
        }));
    }

    protected void updateChunkStatus(ChunkCoordIntPair chunkcoordintpair) {
        this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            super.retainData(chunkcoordintpair, false);
            super.setLightEnabled(chunkcoordintpair, false);

            int i;

            for (i = this.getMinLightSection(); i < this.getMaxLightSection(); ++i) {
                super.queueSectionData(EnumSkyBlock.BLOCK, SectionPosition.of(chunkcoordintpair, i), (NibbleArray) null);
                super.queueSectionData(EnumSkyBlock.SKY, SectionPosition.of(chunkcoordintpair, i), (NibbleArray) null);
            }

            for (i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                super.updateSectionStatus(SectionPosition.of(chunkcoordintpair, i), true);
            }

        }, () -> {
            return "updateChunkStatus " + String.valueOf(chunkcoordintpair) + " true";
        }));
    }

    @Override
    public void updateSectionStatus(SectionPosition sectionposition, boolean flag) {
        this.addTask(sectionposition.x(), sectionposition.z(), () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            super.updateSectionStatus(sectionposition, flag);
        }, () -> {
            String s = String.valueOf(sectionposition);

            return "updateSectionStatus " + s + " " + flag;
        }));
    }

    @Override
    public void propagateLightSources(ChunkCoordIntPair chunkcoordintpair) {
        this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            super.propagateLightSources(chunkcoordintpair);
        }, () -> {
            return "propagateLight " + String.valueOf(chunkcoordintpair);
        }));
    }

    @Override
    public void setLightEnabled(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            super.setLightEnabled(chunkcoordintpair, flag);
        }, () -> {
            String s = String.valueOf(chunkcoordintpair);

            return "enableLight " + s + " " + flag;
        }));
    }

    @Override
    public void queueSectionData(EnumSkyBlock enumskyblock, SectionPosition sectionposition, @Nullable NibbleArray nibblearray) {
        this.addTask(sectionposition.x(), sectionposition.z(), () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            super.queueSectionData(enumskyblock, sectionposition, nibblearray);
        }, () -> {
            return "queueData " + String.valueOf(sectionposition);
        }));
    }

    private void addTask(int i, int j, LightEngineThreaded.Update lightenginethreaded_update, Runnable runnable) {
        this.addTask(i, j, this.chunkMap.getChunkQueueLevel(ChunkCoordIntPair.asLong(i, j)), lightenginethreaded_update, runnable);
    }

    private void addTask(int i, int j, IntSupplier intsupplier, LightEngineThreaded.Update lightenginethreaded_update, Runnable runnable) {
        this.sorterMailbox.tell(ChunkTaskQueueSorter.message(() -> {
            this.lightTasks.add(Pair.of(lightenginethreaded_update, runnable));
            if (this.lightTasks.size() >= 1000) {
                this.runUpdate();
            }

        }, ChunkCoordIntPair.asLong(i, j), intsupplier));
    }

    @Override
    public void retainData(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            super.retainData(chunkcoordintpair, flag);
        }, () -> {
            return "retainData " + String.valueOf(chunkcoordintpair);
        }));
    }

    public CompletableFuture<IChunkAccess> initializeLight(IChunkAccess ichunkaccess, boolean flag) {
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();

        this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            ChunkSection[] achunksection = ichunkaccess.getSections();

            for (int i = 0; i < ichunkaccess.getSectionsCount(); ++i) {
                ChunkSection chunksection = achunksection[i];

                if (!chunksection.hasOnlyAir()) {
                    int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);

                    super.updateSectionStatus(SectionPosition.of(chunkcoordintpair, j), false);
                }
            }

        }, () -> {
            return "initializeLight: " + String.valueOf(chunkcoordintpair);
        }));
        return CompletableFuture.supplyAsync(() -> {
            super.setLightEnabled(chunkcoordintpair, flag);
            super.retainData(chunkcoordintpair, false);
            return ichunkaccess;
        }, (runnable) -> {
            this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, LightEngineThreaded.Update.POST_UPDATE, runnable);
        });
    }

    public CompletableFuture<IChunkAccess> lightChunk(IChunkAccess ichunkaccess, boolean flag) {
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();

        ichunkaccess.setLightCorrect(false);
        this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.name(() -> {
            if (!flag) {
                super.propagateLightSources(chunkcoordintpair);
            }

        }, () -> {
            String s = String.valueOf(chunkcoordintpair);

            return "lightChunk " + s + " " + flag;
        }));
        return CompletableFuture.supplyAsync(() -> {
            ichunkaccess.setLightCorrect(true);
            this.chunkMap.releaseLightTicket(chunkcoordintpair);
            return ichunkaccess;
        }, (runnable) -> {
            this.addTask(chunkcoordintpair.x, chunkcoordintpair.z, LightEngineThreaded.Update.POST_UPDATE, runnable);
        });
    }

    public void tryScheduleUpdate() {
        if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
            this.taskMailbox.tell(() -> {
                this.runUpdate();
                this.scheduled.set(false);
            });
        }

    }

    private void runUpdate() {
        int i = Math.min(this.lightTasks.size(), 1000);
        ObjectListIterator<Pair<LightEngineThreaded.Update, Runnable>> objectlistiterator = this.lightTasks.iterator();

        Pair pair;
        int j;

        for (j = 0; objectlistiterator.hasNext() && j < i; ++j) {
            pair = (Pair) objectlistiterator.next();
            if (pair.getFirst() == LightEngineThreaded.Update.PRE_UPDATE) {
                ((Runnable) pair.getSecond()).run();
            }
        }

        objectlistiterator.back(j);
        super.runLightUpdates();

        for (j = 0; objectlistiterator.hasNext() && j < i; ++j) {
            pair = (Pair) objectlistiterator.next();
            if (pair.getFirst() == LightEngineThreaded.Update.POST_UPDATE) {
                ((Runnable) pair.getSecond()).run();
            }

            objectlistiterator.remove();
        }

    }

    public CompletableFuture<?> waitForPendingTasks(int i, int j) {
        return CompletableFuture.runAsync(() -> {
        }, (runnable) -> {
            this.addTask(i, j, LightEngineThreaded.Update.POST_UPDATE, runnable);
        });
    }

    private static enum Update {

        PRE_UPDATE, POST_UPDATE;

        private Update() {}
    }
}
