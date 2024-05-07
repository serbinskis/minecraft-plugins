package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import org.slf4j.Logger;

public class GameTestHarnessRunner {

    public static final int DEFAULT_TESTS_PER_ROW = 8;
    private static final Logger LOGGER = LogUtils.getLogger();
    final WorldServer level;
    private final GameTestHarnessTicker testTicker;
    private final List<GameTestHarnessInfo> allTestInfos;
    private ImmutableList<GameTestHarnessBatch> batches;
    final List<GameTestBatchListener> batchListeners = Lists.newArrayList();
    private final List<GameTestHarnessInfo> scheduledForRerun = Lists.newArrayList();
    private final GameTestHarnessRunner.b testBatcher;
    private boolean stopped = true;
    @Nullable
    GameTestHarnessBatch currentBatch;
    private final GameTestHarnessRunner.c existingStructureSpawner;
    private final GameTestHarnessRunner.c newStructureSpawner;

    protected GameTestHarnessRunner(GameTestHarnessRunner.b gametestharnessrunner_b, Collection<GameTestHarnessBatch> collection, WorldServer worldserver, GameTestHarnessTicker gametestharnessticker, GameTestHarnessRunner.c gametestharnessrunner_c, GameTestHarnessRunner.c gametestharnessrunner_c1) {
        this.level = worldserver;
        this.testTicker = gametestharnessticker;
        this.testBatcher = gametestharnessrunner_b;
        this.existingStructureSpawner = gametestharnessrunner_c;
        this.newStructureSpawner = gametestharnessrunner_c1;
        this.batches = ImmutableList.copyOf(collection);
        this.allTestInfos = (List) this.batches.stream().flatMap((gametestharnessbatch) -> {
            return gametestharnessbatch.gameTestInfos().stream();
        }).collect(SystemUtils.toMutableList());
        gametestharnessticker.setRunner(this);
        this.allTestInfos.forEach((gametestharnessinfo) -> {
            gametestharnessinfo.addListener(new ReportGameListener());
        });
    }

    public List<GameTestHarnessInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.stopped = false;
        this.runBatch(0);
    }

    public void stop() {
        this.stopped = true;
        if (this.currentBatch != null) {
            this.currentBatch.afterBatchFunction().accept(this.level);
        }

    }

    public void rerunTest(GameTestHarnessInfo gametestharnessinfo) {
        GameTestHarnessInfo gametestharnessinfo1 = gametestharnessinfo.copyReset();

        gametestharnessinfo.getListeners().forEach((gametestharnesslistener) -> {
            gametestharnesslistener.testAddedForRerun(gametestharnessinfo, gametestharnessinfo1, this);
        });
        this.allTestInfos.add(gametestharnessinfo1);
        this.scheduledForRerun.add(gametestharnessinfo1);
        if (this.stopped) {
            this.runScheduledRerunTests();
        }

    }

    void runBatch(final int i) {
        if (i >= this.batches.size()) {
            this.runScheduledRerunTests();
        } else {
            this.currentBatch = (GameTestHarnessBatch) this.batches.get(i);
            Collection<GameTestHarnessInfo> collection = this.createStructuresForBatch(this.currentBatch.gameTestInfos());
            String s = this.currentBatch.name();

            GameTestHarnessRunner.LOGGER.info("Running test batch '{}' ({} tests)...", s, collection.size());
            this.currentBatch.beforeBatchFunction().accept(this.level);
            this.batchListeners.forEach((gametestbatchlistener) -> {
                gametestbatchlistener.testBatchStarting(this.currentBatch);
            });
            final GameTestHarnessCollector gametestharnesscollector = new GameTestHarnessCollector();

            Objects.requireNonNull(gametestharnesscollector);
            collection.forEach(gametestharnesscollector::addTestToTrack);
            gametestharnesscollector.addListener(new GameTestHarnessListener() {
                private void testCompleted() {
                    if (gametestharnesscollector.isDone()) {
                        GameTestHarnessRunner.this.currentBatch.afterBatchFunction().accept(GameTestHarnessRunner.this.level);
                        GameTestHarnessRunner.this.batchListeners.forEach((gametestbatchlistener) -> {
                            gametestbatchlistener.testBatchFinished(GameTestHarnessRunner.this.currentBatch);
                        });
                        LongArraySet longarrayset = new LongArraySet(GameTestHarnessRunner.this.level.getForcedChunks());

                        longarrayset.forEach((j) -> {
                            GameTestHarnessRunner.this.level.setChunkForced(ChunkCoordIntPair.getX(j), ChunkCoordIntPair.getZ(j), false);
                        });
                        GameTestHarnessRunner.this.runBatch(i + 1);
                    }

                }

                @Override
                public void testStructureLoaded(GameTestHarnessInfo gametestharnessinfo) {}

                @Override
                public void testPassed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
                    this.testCompleted();
                }

                @Override
                public void testFailed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
                    this.testCompleted();
                }

                @Override
                public void testAddedForRerun(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessInfo gametestharnessinfo1, GameTestHarnessRunner gametestharnessrunner) {}
            });
            GameTestHarnessTicker gametestharnessticker = this.testTicker;

            Objects.requireNonNull(this.testTicker);
            collection.forEach(gametestharnessticker::add);
        }
    }

    private void runScheduledRerunTests() {
        if (!this.scheduledForRerun.isEmpty()) {
            GameTestHarnessRunner.LOGGER.info("Starting re-run of tests: {}", this.scheduledForRerun.stream().map((gametestharnessinfo) -> {
                return gametestharnessinfo.getTestFunction().testName();
            }).collect(Collectors.joining(", ")));
            this.batches = ImmutableList.copyOf(this.testBatcher.batch(this.scheduledForRerun));
            this.scheduledForRerun.clear();
            this.stopped = false;
            this.runBatch(0);
        } else {
            this.batches = ImmutableList.of();
            this.stopped = true;
        }

    }

    public void addListener(GameTestBatchListener gametestbatchlistener) {
        this.batchListeners.add(gametestbatchlistener);
    }

    private Collection<GameTestHarnessInfo> createStructuresForBatch(Collection<GameTestHarnessInfo> collection) {
        return collection.stream().map(this::spawn).flatMap(Optional::stream).toList();
    }

    private Optional<GameTestHarnessInfo> spawn(GameTestHarnessInfo gametestharnessinfo) {
        return gametestharnessinfo.getStructureBlockPos() == null ? this.newStructureSpawner.spawnStructure(gametestharnessinfo) : this.existingStructureSpawner.spawnStructure(gametestharnessinfo);
    }

    public static void clearMarkers(WorldServer worldserver) {
        PacketDebug.sendGameTestClearPacket(worldserver);
    }

    public interface b {

        Collection<GameTestHarnessBatch> batch(Collection<GameTestHarnessInfo> collection);
    }

    public interface c {

        GameTestHarnessRunner.c IN_PLACE = (gametestharnessinfo) -> {
            return Optional.of(gametestharnessinfo.prepareTestStructure().placeStructure().startExecution(1));
        };
        GameTestHarnessRunner.c NOT_SET = (gametestharnessinfo) -> {
            return Optional.empty();
        };

        Optional<GameTestHarnessInfo> spawnStructure(GameTestHarnessInfo gametestharnessinfo);
    }

    public static class a {

        private final WorldServer level;
        private final GameTestHarnessTicker testTicker;
        private final GameTestHarnessRunner.b batcher;
        private final GameTestHarnessRunner.c existingStructureSpawner;
        private GameTestHarnessRunner.c newStructureSpawner;
        private final Collection<GameTestHarnessBatch> batches;

        private a(Collection<GameTestHarnessBatch> collection, WorldServer worldserver) {
            this.testTicker = GameTestHarnessTicker.SINGLETON;
            this.batcher = GameTestBatchFactory.fromGameTestInfo();
            this.existingStructureSpawner = GameTestHarnessRunner.c.IN_PLACE;
            this.newStructureSpawner = GameTestHarnessRunner.c.NOT_SET;
            this.batches = collection;
            this.level = worldserver;
        }

        public static GameTestHarnessRunner.a fromBatches(Collection<GameTestHarnessBatch> collection, WorldServer worldserver) {
            return new GameTestHarnessRunner.a(collection, worldserver);
        }

        public static GameTestHarnessRunner.a fromInfo(Collection<GameTestHarnessInfo> collection, WorldServer worldserver) {
            return fromBatches(GameTestBatchFactory.fromGameTestInfo().batch(collection), worldserver);
        }

        public GameTestHarnessRunner.a newStructureSpawner(GameTestHarnessRunner.c gametestharnessrunner_c) {
            this.newStructureSpawner = gametestharnessrunner_c;
            return this;
        }

        public GameTestHarnessRunner build() {
            return new GameTestHarnessRunner(this.batcher, this.batches, this.level, this.testTicker, this.existingStructureSpawner, this.newStructureSpawner);
        }
    }
}
