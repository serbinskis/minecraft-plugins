package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.phys.AxisAlignedBB;

public class GameTestHarnessInfo {

    private final GameTestHarnessTestFunction testFunction;
    @Nullable
    private BlockPosition structureBlockPos;
    @Nullable
    private BlockPosition northWestCorner;
    private final WorldServer level;
    private final Collection<GameTestHarnessListener> listeners = Lists.newArrayList();
    private final int timeoutTicks;
    private final Collection<GameTestHarnessSequence> sequences = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap();
    private long startTick;
    private int ticksToWaitForChunkLoading = 20;
    private boolean placedStructure;
    private boolean chunksLoaded;
    private long tickCount;
    private boolean started;
    private final RetryOptions retryOptions;
    private final Stopwatch timer = Stopwatch.createUnstarted();
    private boolean done;
    private final EnumBlockRotation rotation;
    @Nullable
    private Throwable error;
    @Nullable
    private TileEntityStructure structureBlockEntity;

    public GameTestHarnessInfo(GameTestHarnessTestFunction gametestharnesstestfunction, EnumBlockRotation enumblockrotation, WorldServer worldserver, RetryOptions retryoptions) {
        this.testFunction = gametestharnesstestfunction;
        this.level = worldserver;
        this.retryOptions = retryoptions;
        this.timeoutTicks = gametestharnesstestfunction.maxTicks();
        this.rotation = gametestharnesstestfunction.rotation().getRotated(enumblockrotation);
    }

    void setStructureBlockPos(BlockPosition blockposition) {
        this.structureBlockPos = blockposition;
    }

    public GameTestHarnessInfo startExecution(int i) {
        this.startTick = this.level.getGameTime() + this.testFunction.setupTicks() + (long) i;
        this.timer.start();
        return this;
    }

    public GameTestHarnessInfo placeStructure() {
        if (this.placedStructure) {
            return this;
        } else {
            this.ticksToWaitForChunkLoading = 0;
            this.placedStructure = true;
            TileEntityStructure tileentitystructure = this.getStructureBlockEntity();

            tileentitystructure.placeStructure(this.level);
            StructureBoundingBox structureboundingbox = GameTestHarnessStructures.getStructureBoundingBox(tileentitystructure);

            this.level.getBlockTicks().clearArea(structureboundingbox);
            this.level.clearBlockEvents(structureboundingbox);
            return this;
        }
    }

    private boolean ensureStructureIsPlaced() {
        if (this.placedStructure) {
            return true;
        } else if (this.ticksToWaitForChunkLoading > 0) {
            --this.ticksToWaitForChunkLoading;
            return false;
        } else {
            this.placeStructure().startExecution(0);
            return true;
        }
    }

    public void tick(GameTestHarnessRunner gametestharnessrunner) {
        if (!this.isDone()) {
            if (this.structureBlockEntity == null) {
                this.fail(new IllegalStateException("Running test without structure block entity"));
            }

            if (this.chunksLoaded || GameTestHarnessStructures.getStructureBoundingBox(this.structureBlockEntity).intersectingChunks().allMatch((chunkcoordintpair) -> {
                return this.level.isPositionEntityTicking(chunkcoordintpair.getWorldPosition());
            })) {
                this.chunksLoaded = true;
                if (this.ensureStructureIsPlaced()) {
                    this.tickInternal();
                    if (this.isDone()) {
                        if (this.error != null) {
                            this.listeners.forEach((gametestharnesslistener) -> {
                                gametestharnesslistener.testFailed(this, gametestharnessrunner);
                            });
                        } else {
                            this.listeners.forEach((gametestharnesslistener) -> {
                                gametestharnesslistener.testPassed(this, gametestharnessrunner);
                            });
                        }
                    }

                }
            }
        }
    }

    private void tickInternal() {
        this.tickCount = this.level.getGameTime() - this.startTick;
        if (this.tickCount >= 0L) {
            if (!this.started) {
                this.startTest();
            }

            ObjectIterator<Entry<Runnable>> objectiterator = this.runAtTickTimeMap.object2LongEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<Runnable> entry = (Entry) objectiterator.next();

                if (entry.getLongValue() <= this.tickCount) {
                    try {
                        ((Runnable) entry.getKey()).run();
                    } catch (Exception exception) {
                        this.fail(exception);
                    }

                    objectiterator.remove();
                }
            }

            if (this.tickCount > (long) this.timeoutTicks) {
                if (this.sequences.isEmpty()) {
                    this.fail(new GameTestHarnessTimeout("Didn't succeed or fail within " + this.testFunction.maxTicks() + " ticks"));
                } else {
                    this.sequences.forEach((gametestharnesssequence) -> {
                        gametestharnesssequence.tickAndFailIfNotComplete(this.tickCount);
                    });
                    if (this.error == null) {
                        this.fail(new GameTestHarnessTimeout("No sequences finished"));
                    }
                }
            } else {
                this.sequences.forEach((gametestharnesssequence) -> {
                    gametestharnesssequence.tickAndContinue(this.tickCount);
                });
            }

        }
    }

    private void startTest() {
        if (!this.started) {
            this.started = true;

            try {
                this.testFunction.run(new GameTestHarnessHelper(this));
            } catch (Exception exception) {
                this.fail(exception);
            }

        }
    }

    public void setRunAtTickTime(long i, Runnable runnable) {
        this.runAtTickTimeMap.put(runnable, i);
    }

    public String getTestName() {
        return this.testFunction.testName();
    }

    @Nullable
    public BlockPosition getStructureBlockPos() {
        return this.structureBlockPos;
    }

    public AxisAlignedBB getStructureBounds() {
        TileEntityStructure tileentitystructure = this.getStructureBlockEntity();

        return GameTestHarnessStructures.getStructureBounds(tileentitystructure);
    }

    public TileEntityStructure getStructureBlockEntity() {
        if (this.structureBlockEntity == null) {
            if (this.structureBlockPos == null) {
                throw new IllegalStateException("Could not find a structureBlockEntity for this GameTestInfo");
            }

            this.structureBlockEntity = (TileEntityStructure) this.level.getBlockEntity(this.structureBlockPos);
            if (this.structureBlockEntity == null) {
                throw new IllegalStateException("Could not find a structureBlockEntity at the given coordinate " + String.valueOf(this.structureBlockPos));
            }
        }

        return this.structureBlockEntity;
    }

    public WorldServer getLevel() {
        return this.level;
    }

    public boolean hasSucceeded() {
        return this.done && this.error == null;
    }

    public boolean hasFailed() {
        return this.error != null;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public boolean isDone() {
        return this.done;
    }

    public long getRunTime() {
        return this.timer.elapsed(TimeUnit.MILLISECONDS);
    }

    private void finish() {
        if (!this.done) {
            this.done = true;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }

    }

    public void succeed() {
        if (this.error == null) {
            this.finish();
            AxisAlignedBB axisalignedbb = this.getStructureBounds();
            List<Entity> list = this.getLevel().getEntitiesOfClass(Entity.class, axisalignedbb.inflate(1.0D), (entity) -> {
                return !(entity instanceof EntityHuman);
            });

            list.forEach((entity) -> {
                entity.remove(Entity.RemovalReason.DISCARDED);
            });
        }

    }

    public void fail(Throwable throwable) {
        this.error = throwable;
        this.finish();
    }

    @Nullable
    public Throwable getError() {
        return this.error;
    }

    public String toString() {
        return this.getTestName();
    }

    public void addListener(GameTestHarnessListener gametestharnesslistener) {
        this.listeners.add(gametestharnesslistener);
    }

    public GameTestHarnessInfo prepareTestStructure() {
        BlockPosition blockposition = this.getOrCalculateNorthwestCorner();

        this.structureBlockEntity = GameTestHarnessStructures.prepareTestStructure(this, blockposition, this.getRotation(), this.level);
        this.structureBlockPos = this.structureBlockEntity.getBlockPos();
        GameTestHarnessStructures.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPosition(1, 0, -1), this.getRotation(), this.level);
        GameTestHarnessStructures.encaseStructure(this.getStructureBounds(), this.level, !this.testFunction.skyAccess());
        this.listeners.forEach((gametestharnesslistener) -> {
            gametestharnesslistener.testStructureLoaded(this);
        });
        return this;
    }

    long getTick() {
        return this.tickCount;
    }

    GameTestHarnessSequence createSequence() {
        GameTestHarnessSequence gametestharnesssequence = new GameTestHarnessSequence(this);

        this.sequences.add(gametestharnesssequence);
        return gametestharnesssequence;
    }

    public boolean isRequired() {
        return this.testFunction.required();
    }

    public boolean isOptional() {
        return !this.testFunction.required();
    }

    public String getStructureName() {
        return this.testFunction.structureName();
    }

    public EnumBlockRotation getRotation() {
        return this.rotation;
    }

    public GameTestHarnessTestFunction getTestFunction() {
        return this.testFunction;
    }

    public int getTimeoutTicks() {
        return this.timeoutTicks;
    }

    public boolean isFlaky() {
        return this.testFunction.isFlaky();
    }

    public int maxAttempts() {
        return this.testFunction.maxAttempts();
    }

    public int requiredSuccesses() {
        return this.testFunction.requiredSuccesses();
    }

    public RetryOptions retryOptions() {
        return this.retryOptions;
    }

    public Stream<GameTestHarnessListener> getListeners() {
        return this.listeners.stream();
    }

    public GameTestHarnessInfo copyReset() {
        GameTestHarnessInfo gametestharnessinfo = new GameTestHarnessInfo(this.testFunction, this.rotation, this.level, this.retryOptions());

        if (this.northWestCorner != null) {
            gametestharnessinfo.setNorthWestCorner(this.northWestCorner);
        }

        if (this.structureBlockPos != null) {
            gametestharnessinfo.setStructureBlockPos(this.structureBlockPos);
        }

        return gametestharnessinfo;
    }

    private BlockPosition getOrCalculateNorthwestCorner() {
        if (this.northWestCorner == null) {
            StructureBoundingBox structureboundingbox = GameTestHarnessStructures.getStructureBoundingBox(this.getStructureBlockEntity());

            this.northWestCorner = new BlockPosition(structureboundingbox.minX(), structureboundingbox.minY(), structureboundingbox.minZ());
        }

        return this.northWestCorner;
    }

    public void setNorthWestCorner(BlockPosition blockposition) {
        this.northWestCorner = blockposition;
    }
}
