package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.SystemUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.PacketPlayOutViewCentre;
import net.minecraft.server.level.progress.WorldLoadListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CSVWriter;
import net.minecraft.util.MathHelper;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.thread.IAsyncTaskHandler;
import net.minecraft.util.thread.Mailbox;
import net.minecraft.util.thread.ThreadedMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.village.poi.VillagePlace;
import net.minecraft.world.entity.boss.EntityComplexPart;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkConverter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunkExtension;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.chunk.storage.ChunkRegionLoader;
import net.minecraft.world.level.chunk.storage.IChunkLoader;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract;
import net.minecraft.world.level.levelgen.GeneratorSettingBase;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.WorldPersistentData;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class PlayerChunkMap extends IChunkLoader implements PlayerChunk.c {

    private static final byte CHUNK_TYPE_REPLACEABLE = -1;
    private static final byte CHUNK_TYPE_UNKNOWN = 0;
    private static final byte CHUNK_TYPE_FULL = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHUNK_SAVED_PER_TICK = 200;
    private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
    private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
    public static final int MIN_VIEW_DISTANCE = 2;
    public static final int MAX_VIEW_DISTANCE = 32;
    public static final int FORCED_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    public final Long2ObjectLinkedOpenHashMap<PlayerChunk> updatingChunkMap = new Long2ObjectLinkedOpenHashMap();
    public volatile Long2ObjectLinkedOpenHashMap<PlayerChunk> visibleChunkMap;
    private final Long2ObjectLinkedOpenHashMap<PlayerChunk> pendingUnloads;
    private final LongSet entitiesInLevel;
    public final WorldServer level;
    private final LightEngineThreaded lightEngine;
    private final IAsyncTaskHandler<Runnable> mainThreadExecutor;
    public ChunkGenerator generator;
    private final RandomState randomState;
    private final ChunkGeneratorStructureState chunkGeneratorState;
    private final Supplier<WorldPersistentData> overworldDataStorage;
    private final VillagePlace poiManager;
    public final LongSet toDrop;
    private boolean modified;
    private final ChunkTaskQueueSorter queueSorter;
    private final Mailbox<ChunkTaskQueueSorter.a<Runnable>> worldgenMailbox;
    private final Mailbox<ChunkTaskQueueSorter.a<Runnable>> mainThreadMailbox;
    public final WorldLoadListener progressListener;
    private final ChunkStatusUpdateListener chunkStatusListener;
    public final PlayerChunkMap.a distanceManager;
    private final AtomicInteger tickingGenerated;
    private final String storageName;
    private final PlayerMap playerMap;
    public final Int2ObjectMap<PlayerChunkMap.EntityTracker> entityMap;
    private final Long2ByteMap chunkTypeCache;
    private final Long2LongMap chunkSaveCooldowns;
    private final Queue<Runnable> unloadQueue;
    public int serverViewDistance;
    private WorldGenContext worldGenContext;

    public PlayerChunkMap(WorldServer worldserver, Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer, StructureTemplateManager structuretemplatemanager, Executor executor, IAsyncTaskHandler<Runnable> iasynctaskhandler, ILightAccess ilightaccess, ChunkGenerator chunkgenerator, WorldLoadListener worldloadlistener, ChunkStatusUpdateListener chunkstatusupdatelistener, Supplier<WorldPersistentData> supplier, int i, boolean flag) {
        super(new RegionStorageInfo(convertable_conversionsession.getLevelId(), worldserver.dimension(), "chunk"), convertable_conversionsession.getDimensionPath(worldserver.dimension()).resolve("region"), datafixer, flag);
        this.visibleChunkMap = this.updatingChunkMap.clone();
        this.pendingUnloads = new Long2ObjectLinkedOpenHashMap();
        this.entitiesInLevel = new LongOpenHashSet();
        this.toDrop = new LongOpenHashSet();
        this.tickingGenerated = new AtomicInteger();
        this.playerMap = new PlayerMap();
        this.entityMap = new Int2ObjectOpenHashMap();
        this.chunkTypeCache = new Long2ByteOpenHashMap();
        this.chunkSaveCooldowns = new Long2LongOpenHashMap();
        this.unloadQueue = Queues.newConcurrentLinkedQueue();
        Path path = convertable_conversionsession.getDimensionPath(worldserver.dimension());

        this.storageName = path.getFileName().toString();
        this.level = worldserver;
        this.generator = chunkgenerator;
        IRegistryCustom iregistrycustom = worldserver.registryAccess();
        long j = worldserver.getSeed();

        if (chunkgenerator instanceof ChunkGeneratorAbstract chunkgeneratorabstract) {
            this.randomState = RandomState.create((GeneratorSettingBase) chunkgeneratorabstract.generatorSettings().value(), (HolderGetter) iregistrycustom.lookupOrThrow(Registries.NOISE), j);
        } else {
            this.randomState = RandomState.create(GeneratorSettingBase.dummy(), (HolderGetter) iregistrycustom.lookupOrThrow(Registries.NOISE), j);
        }

        this.chunkGeneratorState = chunkgenerator.createState(iregistrycustom.lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, j);
        this.mainThreadExecutor = iasynctaskhandler;
        ThreadedMailbox<Runnable> threadedmailbox = ThreadedMailbox.create(executor, "worldgen");

        Objects.requireNonNull(iasynctaskhandler);
        Mailbox<Runnable> mailbox = Mailbox.of("main", iasynctaskhandler::tell);

        this.progressListener = worldloadlistener;
        this.chunkStatusListener = chunkstatusupdatelistener;
        ThreadedMailbox<Runnable> threadedmailbox1 = ThreadedMailbox.create(executor, "light");

        this.queueSorter = new ChunkTaskQueueSorter(ImmutableList.of(threadedmailbox, mailbox, threadedmailbox1), executor, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(threadedmailbox, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(mailbox, false);
        this.lightEngine = new LightEngineThreaded(ilightaccess, this, this.level.dimensionType().hasSkyLight(), threadedmailbox1, this.queueSorter.getProcessor(threadedmailbox1, false));
        this.distanceManager = new PlayerChunkMap.a(executor, iasynctaskhandler);
        this.overworldDataStorage = supplier;
        this.poiManager = new VillagePlace(new RegionStorageInfo(convertable_conversionsession.getLevelId(), worldserver.dimension(), "poi"), path.resolve("poi"), datafixer, flag, iregistrycustom, worldserver);
        this.setServerViewDistance(i);
        this.worldGenContext = new WorldGenContext(worldserver, chunkgenerator, structuretemplatemanager, this.lightEngine);
    }

    protected ChunkGenerator generator() {
        return this.generator;
    }

    protected ChunkGeneratorStructureState generatorState() {
        return this.chunkGeneratorState;
    }

    protected RandomState randomState() {
        return this.randomState;
    }

    public void debugReloadGenerator() {
        DataResult<JsonElement> dataresult = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, this.generator);
        DataResult<ChunkGenerator> dataresult1 = dataresult.flatMap((jsonelement) -> {
            return ChunkGenerator.CODEC.parse(JsonOps.INSTANCE, jsonelement);
        });

        dataresult1.ifSuccess((chunkgenerator) -> {
            this.generator = chunkgenerator;
            this.worldGenContext = new WorldGenContext(this.worldGenContext.level(), chunkgenerator, this.worldGenContext.structureManager(), this.worldGenContext.lightEngine());
        });
    }

    private static double euclideanDistanceSquared(ChunkCoordIntPair chunkcoordintpair, Entity entity) {
        double d0 = (double) SectionPosition.sectionToBlockCoord(chunkcoordintpair.x, 8);
        double d1 = (double) SectionPosition.sectionToBlockCoord(chunkcoordintpair.z, 8);
        double d2 = d0 - entity.getX();
        double d3 = d1 - entity.getZ();

        return d2 * d2 + d3 * d3;
    }

    boolean isChunkTracked(EntityPlayer entityplayer, int i, int j) {
        return entityplayer.getChunkTrackingView().contains(i, j) && !entityplayer.connection.chunkSender.isPending(ChunkCoordIntPair.asLong(i, j));
    }

    private boolean isChunkOnTrackedBorder(EntityPlayer entityplayer, int i, int j) {
        if (!this.isChunkTracked(entityplayer, i, j)) {
            return false;
        } else {
            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
                    if ((k != 0 || l != 0) && !this.isChunkTracked(entityplayer, i + k, j + l)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    protected LightEngineThreaded getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    protected PlayerChunk getUpdatingChunkIfPresent(long i) {
        return (PlayerChunk) this.updatingChunkMap.get(i);
    }

    @Nullable
    protected PlayerChunk getVisibleChunkIfPresent(long i) {
        return (PlayerChunk) this.visibleChunkMap.get(i);
    }

    protected IntSupplier getChunkQueueLevel(long i) {
        return () -> {
            PlayerChunk playerchunk = this.getVisibleChunkIfPresent(i);

            return playerchunk == null ? ChunkTaskQueue.PRIORITY_LEVEL_COUNT - 1 : Math.min(playerchunk.getQueueLevel(), ChunkTaskQueue.PRIORITY_LEVEL_COUNT - 1);
        };
    }

    public String getChunkDebugData(ChunkCoordIntPair chunkcoordintpair) {
        PlayerChunk playerchunk = this.getVisibleChunkIfPresent(chunkcoordintpair.toLong());

        if (playerchunk == null) {
            return "null";
        } else {
            String s = playerchunk.getTicketLevel() + "\n";
            ChunkStatus chunkstatus = playerchunk.getLastAvailableStatus();
            IChunkAccess ichunkaccess = playerchunk.getLastAvailable();

            if (chunkstatus != null) {
                s = s + "St: \u00a7" + chunkstatus.getIndex() + String.valueOf(chunkstatus) + "\u00a7r\n";
            }

            if (ichunkaccess != null) {
                s = s + "Ch: \u00a7" + ichunkaccess.getStatus().getIndex() + String.valueOf(ichunkaccess.getStatus()) + "\u00a7r\n";
            }

            FullChunkStatus fullchunkstatus = playerchunk.getFullStatus();

            s = s + String.valueOf('\u00a7') + fullchunkstatus.ordinal() + String.valueOf(fullchunkstatus);
            return s + "\u00a7r";
        }
    }

    private CompletableFuture<ChunkResult<List<IChunkAccess>>> getChunkRangeFuture(PlayerChunk playerchunk, int i, IntFunction<ChunkStatus> intfunction) {
        if (i == 0) {
            ChunkStatus chunkstatus = (ChunkStatus) intfunction.apply(0);

            return playerchunk.getOrScheduleFuture(chunkstatus, this).thenApply((chunkresult) -> {
                return chunkresult.map(List::of);
            });
        } else {
            List<CompletableFuture<ChunkResult<IChunkAccess>>> list = new ArrayList();
            List<PlayerChunk> list1 = new ArrayList();
            ChunkCoordIntPair chunkcoordintpair = playerchunk.getPos();
            int j = chunkcoordintpair.x;
            int k = chunkcoordintpair.z;

            for (int l = -i; l <= i; ++l) {
                for (int i1 = -i; i1 <= i; ++i1) {
                    int j1 = Math.max(Math.abs(i1), Math.abs(l));
                    ChunkCoordIntPair chunkcoordintpair1 = new ChunkCoordIntPair(j + i1, k + l);
                    long k1 = chunkcoordintpair1.toLong();
                    PlayerChunk playerchunk1 = this.getUpdatingChunkIfPresent(k1);

                    if (playerchunk1 == null) {
                        return CompletableFuture.completedFuture(ChunkResult.error(() -> {
                            return "Unloaded " + String.valueOf(chunkcoordintpair1);
                        }));
                    }

                    ChunkStatus chunkstatus1 = (ChunkStatus) intfunction.apply(j1);
                    CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = playerchunk1.getOrScheduleFuture(chunkstatus1, this);

                    list1.add(playerchunk1);
                    list.add(completablefuture);
                }
            }

            CompletableFuture<List<ChunkResult<IChunkAccess>>> completablefuture1 = SystemUtils.sequence(list);
            CompletableFuture<ChunkResult<List<IChunkAccess>>> completablefuture2 = completablefuture1.thenApply((list2) -> {
                List<IChunkAccess> list3 = Lists.newArrayList();
                int l1 = 0;

                for (Iterator iterator = list2.iterator(); iterator.hasNext(); ++l1) {
                    ChunkResult<IChunkAccess> chunkresult = (ChunkResult) iterator.next();

                    if (chunkresult == null) {
                        throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                    }

                    IChunkAccess ichunkaccess = (IChunkAccess) chunkresult.orElse((Object) null);

                    if (ichunkaccess == null) {
                        return ChunkResult.error(() -> {
                            String s = String.valueOf(new ChunkCoordIntPair(j + l1 % (i * 2 + 1), k + l1 / (i * 2 + 1)));

                            return "Unloaded " + s + " " + chunkresult.getError();
                        });
                    }

                    list3.add(ichunkaccess);
                }

                return ChunkResult.of(list3);
            });
            Iterator iterator = list1.iterator();

            while (iterator.hasNext()) {
                PlayerChunk playerchunk2 = (PlayerChunk) iterator.next();

                playerchunk2.addSaveDependency("getChunkRangeFuture " + String.valueOf(chunkcoordintpair) + " " + i, completablefuture2);
            }

            return completablefuture2;
        }
    }

    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException illegalstateexception, String s) {
        StringBuilder stringbuilder = new StringBuilder();
        Consumer<PlayerChunk> consumer = (playerchunk) -> {
            playerchunk.getAllFutures().forEach((pair) -> {
                ChunkStatus chunkstatus = (ChunkStatus) pair.getFirst();
                CompletableFuture<ChunkResult<IChunkAccess>> completablefuture = (CompletableFuture) pair.getSecond();

                if (completablefuture != null && completablefuture.isDone() && completablefuture.join() == null) {
                    stringbuilder.append(playerchunk.getPos()).append(" - status: ").append(chunkstatus).append(" future: ").append(completablefuture).append(System.lineSeparator());
                }

            });
        };

        stringbuilder.append("Updating:").append(System.lineSeparator());
        this.updatingChunkMap.values().forEach(consumer);
        stringbuilder.append("Visible:").append(System.lineSeparator());
        this.visibleChunkMap.values().forEach(consumer);
        CrashReport crashreport = CrashReport.forThrowable(illegalstateexception, "Chunk loading");
        CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Chunk loading");

        crashreportsystemdetails.setDetail("Details", (Object) s);
        crashreportsystemdetails.setDetail("Futures", (Object) stringbuilder);
        return new ReportedException(crashreport);
    }

    public CompletableFuture<ChunkResult<Chunk>> prepareEntityTickingChunk(PlayerChunk playerchunk) {
        return this.getChunkRangeFuture(playerchunk, 2, (i) -> {
            return ChunkStatus.FULL;
        }).thenApplyAsync((chunkresult) -> {
            return chunkresult.map((list) -> {
                return (Chunk) list.get(list.size() / 2);
            });
        }, this.mainThreadExecutor);
    }

    @Nullable
    PlayerChunk updateChunkScheduling(long i, int j, @Nullable PlayerChunk playerchunk, int k) {
        if (!ChunkLevel.isLoaded(k) && !ChunkLevel.isLoaded(j)) {
            return playerchunk;
        } else {
            if (playerchunk != null) {
                playerchunk.setTicketLevel(j);
            }

            if (playerchunk != null) {
                if (!ChunkLevel.isLoaded(j)) {
                    this.toDrop.add(i);
                } else {
                    this.toDrop.remove(i);
                }
            }

            if (ChunkLevel.isLoaded(j) && playerchunk == null) {
                playerchunk = (PlayerChunk) this.pendingUnloads.remove(i);
                if (playerchunk != null) {
                    playerchunk.setTicketLevel(j);
                } else {
                    playerchunk = new PlayerChunk(new ChunkCoordIntPair(i), j, this.level, this.lightEngine, this.queueSorter, this);
                }

                this.updatingChunkMap.put(i, playerchunk);
                this.modified = true;
            }

            return playerchunk;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.queueSorter.close();
            this.poiManager.close();
        } finally {
            super.close();
        }

    }

    protected void saveAllChunks(boolean flag) {
        if (flag) {
            List<PlayerChunk> list = this.visibleChunkMap.values().stream().filter(PlayerChunk::wasAccessibleSinceLastSave).peek(PlayerChunk::refreshAccessibility).toList();
            MutableBoolean mutableboolean = new MutableBoolean();

            do {
                mutableboolean.setFalse();
                list.stream().map((playerchunk) -> {
                    CompletableFuture completablefuture;

                    do {
                        completablefuture = playerchunk.getChunkToSave();
                        IAsyncTaskHandler iasynctaskhandler = this.mainThreadExecutor;

                        Objects.requireNonNull(completablefuture);
                        iasynctaskhandler.managedBlock(completablefuture::isDone);
                    } while (completablefuture != playerchunk.getChunkToSave());

                    return (IChunkAccess) completablefuture.join();
                }).filter((ichunkaccess) -> {
                    return ichunkaccess instanceof ProtoChunkExtension || ichunkaccess instanceof Chunk;
                }).filter(this::save).forEach((ichunkaccess) -> {
                    mutableboolean.setTrue();
                });
            } while (mutableboolean.isTrue());

            this.processUnloads(() -> {
                return true;
            });
            this.flushWorker();
        } else {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }

    }

    protected void tick(BooleanSupplier booleansupplier) {
        GameProfilerFiller gameprofilerfiller = this.level.getProfiler();

        gameprofilerfiller.push("poi");
        this.poiManager.tick(booleansupplier);
        gameprofilerfiller.popPush("chunk_unload");
        if (!this.level.noSave()) {
            this.processUnloads(booleansupplier);
        }

        gameprofilerfiller.pop();
    }

    public boolean hasWork() {
        return this.lightEngine.hasLightWork() || !this.pendingUnloads.isEmpty() || !this.updatingChunkMap.isEmpty() || this.poiManager.hasWork() || !this.toDrop.isEmpty() || !this.unloadQueue.isEmpty() || this.queueSorter.hasWork() || this.distanceManager.hasTickets();
    }

    private void processUnloads(BooleanSupplier booleansupplier) {
        LongIterator longiterator = this.toDrop.iterator();

        for (int i = 0; longiterator.hasNext() && (booleansupplier.getAsBoolean() || i < 200 || this.toDrop.size() > 2000); longiterator.remove()) {
            long j = longiterator.nextLong();
            PlayerChunk playerchunk = (PlayerChunk) this.updatingChunkMap.remove(j);

            if (playerchunk != null) {
                this.pendingUnloads.put(j, playerchunk);
                this.modified = true;
                ++i;
                this.scheduleUnload(j, playerchunk);
            }
        }

        int k = Math.max(0, this.unloadQueue.size() - 2000);

        Runnable runnable;

        while ((booleansupplier.getAsBoolean() || k > 0) && (runnable = (Runnable) this.unloadQueue.poll()) != null) {
            --k;
            runnable.run();
        }

        int l = 0;
        ObjectIterator<PlayerChunk> objectiterator = this.visibleChunkMap.values().iterator();

        while (l < 20 && booleansupplier.getAsBoolean() && objectiterator.hasNext()) {
            if (this.saveChunkIfNeeded((PlayerChunk) objectiterator.next())) {
                ++l;
            }
        }

    }

    private void scheduleUnload(long i, PlayerChunk playerchunk) {
        CompletableFuture<IChunkAccess> completablefuture = playerchunk.getChunkToSave();
        Consumer consumer = (ichunkaccess) -> {
            CompletableFuture<IChunkAccess> completablefuture1 = playerchunk.getChunkToSave();

            if (completablefuture1 != completablefuture) {
                this.scheduleUnload(i, playerchunk);
            } else {
                if (this.pendingUnloads.remove(i, playerchunk) && ichunkaccess != null) {
                    if (ichunkaccess instanceof Chunk) {
                        ((Chunk) ichunkaccess).setLoaded(false);
                    }

                    this.save(ichunkaccess);
                    if (this.entitiesInLevel.remove(i) && ichunkaccess instanceof Chunk) {
                        Chunk chunk = (Chunk) ichunkaccess;

                        this.level.unload(chunk);
                    }

                    this.lightEngine.updateChunkStatus(ichunkaccess.getPos());
                    this.lightEngine.tryScheduleUpdate();
                    this.progressListener.onStatusChange(ichunkaccess.getPos(), (ChunkStatus) null);
                    this.chunkSaveCooldowns.remove(ichunkaccess.getPos().toLong());
                }

            }
        };
        Queue queue = this.unloadQueue;

        Objects.requireNonNull(this.unloadQueue);
        completablefuture.thenAcceptAsync(consumer, queue::add).whenComplete((ovoid, throwable) -> {
            if (throwable != null) {
                PlayerChunkMap.LOGGER.error("Failed to save chunk {}", playerchunk.getPos(), throwable);
            }

        });
    }

    protected boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        } else {
            this.visibleChunkMap = this.updatingChunkMap.clone();
            this.modified = false;
            return true;
        }
    }

    public CompletableFuture<ChunkResult<IChunkAccess>> schedule(PlayerChunk playerchunk, ChunkStatus chunkstatus) {
        ChunkCoordIntPair chunkcoordintpair = playerchunk.getPos();

        if (chunkstatus == ChunkStatus.EMPTY) {
            return this.scheduleChunkLoad(chunkcoordintpair).thenApply(ChunkResult::of);
        } else {
            if (chunkstatus == ChunkStatus.LIGHT) {
                this.distanceManager.addTicket(TicketType.LIGHT, chunkcoordintpair, ChunkLevel.byStatus(ChunkStatus.LIGHT), chunkcoordintpair);
            }

            if (!chunkstatus.hasLoadDependencies()) {
                IChunkAccess ichunkaccess = (IChunkAccess) ((ChunkResult) playerchunk.getOrScheduleFuture(chunkstatus.getParent(), this).getNow(PlayerChunk.UNLOADED_CHUNK)).orElse((Object) null);

                if (ichunkaccess != null && ichunkaccess.getStatus().isOrAfter(chunkstatus)) {
                    CompletableFuture<IChunkAccess> completablefuture = chunkstatus.load(this.worldGenContext, (ichunkaccess1) -> {
                        return this.protoChunkToFullChunk(playerchunk, ichunkaccess1);
                    }, ichunkaccess);

                    this.progressListener.onStatusChange(chunkcoordintpair, chunkstatus);
                    return completablefuture.thenApply(ChunkResult::of);
                }
            }

            return this.scheduleChunkGeneration(playerchunk, chunkstatus);
        }
    }

    private CompletableFuture<IChunkAccess> scheduleChunkLoad(ChunkCoordIntPair chunkcoordintpair) {
        return this.readChunk(chunkcoordintpair).thenApply((optional) -> {
            return optional.filter((nbttagcompound) -> {
                boolean flag = isChunkDataValid(nbttagcompound);

                if (!flag) {
                    PlayerChunkMap.LOGGER.error("Chunk file at {} is missing level data, skipping", chunkcoordintpair);
                }

                return flag;
            });
        }).thenApplyAsync((optional) -> {
            this.level.getProfiler().incrementCounter("chunkLoad");
            if (optional.isPresent()) {
                ProtoChunk protochunk = ChunkRegionLoader.read(this.level, this.poiManager, chunkcoordintpair, (NBTTagCompound) optional.get());

                this.markPosition(chunkcoordintpair, protochunk.getStatus().getChunkType());
                return protochunk;
            } else {
                return this.createEmptyChunk(chunkcoordintpair);
            }
        }, this.mainThreadExecutor).exceptionallyAsync((throwable) -> {
            return this.handleChunkLoadFailure(throwable, chunkcoordintpair);
        }, this.mainThreadExecutor);
    }

    private static boolean isChunkDataValid(NBTTagCompound nbttagcompound) {
        return nbttagcompound.contains("Status", 8);
    }

    private IChunkAccess handleChunkLoadFailure(Throwable throwable, ChunkCoordIntPair chunkcoordintpair) {
        Throwable throwable1;

        if (throwable instanceof CompletionException completionexception) {
            throwable1 = completionexception.getCause();
        } else {
            throwable1 = throwable;
        }

        Throwable throwable2 = throwable1;

        if (throwable2 instanceof ReportedException reportedexception) {
            throwable1 = reportedexception.getCause();
        } else {
            throwable1 = throwable2;
        }

        Throwable throwable3 = throwable1;
        boolean flag = throwable3 instanceof Error;
        boolean flag1 = throwable3 instanceof IOException || throwable3 instanceof NbtException;

        if (!flag) {
            if (!flag1) {
                ;
            }

            PlayerChunkMap.LOGGER.error("Couldn't load chunk {}", chunkcoordintpair, throwable3);
            this.level.getServer().reportChunkLoadFailure(chunkcoordintpair);
            return this.createEmptyChunk(chunkcoordintpair);
        } else {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception loading chunk");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Chunk being loaded");

            crashreportsystemdetails.setDetail("pos", (Object) chunkcoordintpair);
            this.markPositionReplaceable(chunkcoordintpair);
            throw new ReportedException(crashreport);
        }
    }

    private IChunkAccess createEmptyChunk(ChunkCoordIntPair chunkcoordintpair) {
        this.markPositionReplaceable(chunkcoordintpair);
        return new ProtoChunk(chunkcoordintpair, ChunkConverter.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registries.BIOME), (BlendingData) null);
    }

    private void markPositionReplaceable(ChunkCoordIntPair chunkcoordintpair) {
        this.chunkTypeCache.put(chunkcoordintpair.toLong(), (byte) -1);
    }

    private byte markPosition(ChunkCoordIntPair chunkcoordintpair, ChunkType chunktype) {
        return this.chunkTypeCache.put(chunkcoordintpair.toLong(), (byte) (chunktype == ChunkType.PROTOCHUNK ? -1 : 1));
    }

    private CompletableFuture<ChunkResult<IChunkAccess>> scheduleChunkGeneration(PlayerChunk playerchunk, ChunkStatus chunkstatus) {
        ChunkCoordIntPair chunkcoordintpair = playerchunk.getPos();
        CompletableFuture<ChunkResult<List<IChunkAccess>>> completablefuture = this.getChunkRangeFuture(playerchunk, chunkstatus.getRange(), (i) -> {
            return this.getDependencyStatus(chunkstatus, i);
        });

        this.level.getProfiler().incrementCounter(() -> {
            return "chunkGenerate " + String.valueOf(chunkstatus);
        });
        Executor executor = (runnable) -> {
            this.worldgenMailbox.tell(ChunkTaskQueueSorter.message(playerchunk, runnable));
        };

        return completablefuture.thenComposeAsync((chunkresult) -> {
            List<IChunkAccess> list = (List) chunkresult.orElse((Object) null);

            if (list == null) {
                this.releaseLightTicket(chunkcoordintpair);
                Objects.requireNonNull(chunkresult);
                return CompletableFuture.completedFuture(ChunkResult.error(chunkresult::getError));
            } else {
                try {
                    IChunkAccess ichunkaccess = (IChunkAccess) list.get(list.size() / 2);
                    CompletableFuture completablefuture1;

                    if (ichunkaccess.getStatus().isOrAfter(chunkstatus)) {
                        completablefuture1 = chunkstatus.load(this.worldGenContext, (ichunkaccess1) -> {
                            return this.protoChunkToFullChunk(playerchunk, ichunkaccess1);
                        }, ichunkaccess);
                    } else {
                        completablefuture1 = chunkstatus.generate(this.worldGenContext, executor, (ichunkaccess1) -> {
                            return this.protoChunkToFullChunk(playerchunk, ichunkaccess1);
                        }, list);
                    }

                    this.progressListener.onStatusChange(chunkcoordintpair, chunkstatus);
                    return completablefuture1.thenApply(ChunkResult::of);
                } catch (Exception exception) {
                    exception.getStackTrace();
                    CrashReport crashreport = CrashReport.forThrowable(exception, "Exception generating new chunk");
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Chunk to be generated");

                    crashreportsystemdetails.setDetail("Status being generated", () -> {
                        return BuiltInRegistries.CHUNK_STATUS.getKey(chunkstatus).toString();
                    });
                    crashreportsystemdetails.setDetail("Location", (Object) String.format(Locale.ROOT, "%d,%d", chunkcoordintpair.x, chunkcoordintpair.z));
                    crashreportsystemdetails.setDetail("Position hash", (Object) ChunkCoordIntPair.asLong(chunkcoordintpair.x, chunkcoordintpair.z));
                    crashreportsystemdetails.setDetail("Generator", (Object) this.generator);
                    this.mainThreadExecutor.execute(() -> {
                        throw new ReportedException(crashreport);
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }, executor);
    }

    protected void releaseLightTicket(ChunkCoordIntPair chunkcoordintpair) {
        this.mainThreadExecutor.tell(SystemUtils.name(() -> {
            this.distanceManager.removeTicket(TicketType.LIGHT, chunkcoordintpair, ChunkLevel.byStatus(ChunkStatus.LIGHT), chunkcoordintpair);
        }, () -> {
            return "release light ticket " + String.valueOf(chunkcoordintpair);
        }));
    }

    private ChunkStatus getDependencyStatus(ChunkStatus chunkstatus, int i) {
        ChunkStatus chunkstatus1;

        if (i == 0) {
            chunkstatus1 = chunkstatus.getParent();
        } else {
            chunkstatus1 = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(chunkstatus) + i);
        }

        return chunkstatus1;
    }

    private static void postLoadProtoChunk(WorldServer worldserver, List<NBTTagCompound> list) {
        if (!list.isEmpty()) {
            worldserver.addWorldGenChunkEntities(EntityTypes.loadEntitiesRecursive(list, worldserver));
        }

    }

    private CompletableFuture<IChunkAccess> protoChunkToFullChunk(PlayerChunk playerchunk, IChunkAccess ichunkaccess) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkCoordIntPair chunkcoordintpair = playerchunk.getPos();
            ProtoChunk protochunk = (ProtoChunk) ichunkaccess;
            Chunk chunk;

            if (protochunk instanceof ProtoChunkExtension) {
                chunk = ((ProtoChunkExtension) protochunk).getWrapped();
            } else {
                chunk = new Chunk(this.level, protochunk, (chunk1) -> {
                    postLoadProtoChunk(this.level, protochunk.getEntities());
                });
                playerchunk.replaceProtoChunk(new ProtoChunkExtension(chunk, false));
            }

            chunk.setFullStatus(() -> {
                return ChunkLevel.fullStatus(playerchunk.getTicketLevel());
            });
            chunk.runPostLoad();
            if (this.entitiesInLevel.add(chunkcoordintpair.toLong())) {
                chunk.setLoaded(true);
                chunk.registerAllBlockEntitiesAfterLevelLoad();
                chunk.registerTickContainerInLevel(this.level);
            }

            return chunk;
        }, (runnable) -> {
            Mailbox mailbox = this.mainThreadMailbox;
            long i = playerchunk.getPos().toLong();

            Objects.requireNonNull(playerchunk);
            mailbox.tell(ChunkTaskQueueSorter.message(runnable, i, playerchunk::getTicketLevel));
        });
    }

    public CompletableFuture<ChunkResult<Chunk>> prepareTickingChunk(PlayerChunk playerchunk) {
        CompletableFuture<ChunkResult<List<IChunkAccess>>> completablefuture = this.getChunkRangeFuture(playerchunk, 1, (i) -> {
            return ChunkStatus.FULL;
        });
        CompletableFuture<ChunkResult<Chunk>> completablefuture1 = completablefuture.thenApplyAsync((chunkresult) -> {
            return chunkresult.map((list) -> {
                return (Chunk) list.get(list.size() / 2);
            });
        }, (runnable) -> {
            this.mainThreadMailbox.tell(ChunkTaskQueueSorter.message(playerchunk, runnable));
        }).thenApplyAsync((chunkresult) -> {
            return chunkresult.ifSuccess((chunk) -> {
                chunk.postProcessGeneration();
                this.level.startTickingChunk(chunk);
                CompletableFuture<?> completablefuture2 = playerchunk.getChunkSendSyncFuture();

                if (completablefuture2.isDone()) {
                    this.onChunkReadyToSend(chunk);
                } else {
                    completablefuture2.thenAcceptAsync((object) -> {
                        this.onChunkReadyToSend(chunk);
                    }, this.mainThreadExecutor);
                }

            });
        }, this.mainThreadExecutor);

        completablefuture1.handle((chunkresult, throwable) -> {
            this.tickingGenerated.getAndIncrement();
            return null;
        });
        return completablefuture1;
    }

    private void onChunkReadyToSend(Chunk chunk) {
        ChunkCoordIntPair chunkcoordintpair = chunk.getPos();
        Iterator iterator = this.playerMap.getAllPlayers().iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.getChunkTrackingView().contains(chunkcoordintpair)) {
                markChunkPendingToSend(entityplayer, chunk);
            }
        }

    }

    public CompletableFuture<ChunkResult<Chunk>> prepareAccessibleChunk(PlayerChunk playerchunk) {
        return this.getChunkRangeFuture(playerchunk, 1, ChunkStatus::getStatusAroundFullChunk).thenApplyAsync((chunkresult) -> {
            return chunkresult.map((list) -> {
                return (Chunk) list.get(list.size() / 2);
            });
        }, (runnable) -> {
            this.mainThreadMailbox.tell(ChunkTaskQueueSorter.message(playerchunk, runnable));
        });
    }

    public int getTickingGenerated() {
        return this.tickingGenerated.get();
    }

    private boolean saveChunkIfNeeded(PlayerChunk playerchunk) {
        if (!playerchunk.wasAccessibleSinceLastSave()) {
            return false;
        } else {
            IChunkAccess ichunkaccess = (IChunkAccess) playerchunk.getChunkToSave().getNow((Object) null);

            if (!(ichunkaccess instanceof ProtoChunkExtension) && !(ichunkaccess instanceof Chunk)) {
                return false;
            } else {
                long i = ichunkaccess.getPos().toLong();
                long j = this.chunkSaveCooldowns.getOrDefault(i, -1L);
                long k = System.currentTimeMillis();

                if (k < j) {
                    return false;
                } else {
                    boolean flag = this.save(ichunkaccess);

                    playerchunk.refreshAccessibility();
                    if (flag) {
                        this.chunkSaveCooldowns.put(i, k + 10000L);
                    }

                    return flag;
                }
            }
        }
    }

    public boolean save(IChunkAccess ichunkaccess) {
        this.poiManager.flush(ichunkaccess.getPos());
        if (!ichunkaccess.isUnsaved()) {
            return false;
        } else {
            ichunkaccess.setUnsaved(false);
            ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();

            try {
                ChunkStatus chunkstatus = ichunkaccess.getStatus();

                if (chunkstatus.getChunkType() != ChunkType.LEVELCHUNK) {
                    if (this.isExistingChunkFull(chunkcoordintpair)) {
                        return false;
                    }

                    if (chunkstatus == ChunkStatus.EMPTY && ichunkaccess.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                        return false;
                    }
                }

                this.level.getProfiler().incrementCounter("chunkSave");
                NBTTagCompound nbttagcompound = ChunkRegionLoader.write(this.level, ichunkaccess);

                this.write(chunkcoordintpair, nbttagcompound).exceptionallyAsync((throwable) -> {
                    this.level.getServer().reportChunkSaveFailure(chunkcoordintpair);
                    return null;
                }, this.mainThreadExecutor);
                this.markPosition(chunkcoordintpair, chunkstatus.getChunkType());
                return true;
            } catch (Exception exception) {
                PlayerChunkMap.LOGGER.error("Failed to save chunk {},{}", new Object[]{chunkcoordintpair.x, chunkcoordintpair.z, exception});
                this.level.getServer().reportChunkSaveFailure(chunkcoordintpair);
                return false;
            }
        }
    }

    private boolean isExistingChunkFull(ChunkCoordIntPair chunkcoordintpair) {
        byte b0 = this.chunkTypeCache.get(chunkcoordintpair.toLong());

        if (b0 != 0) {
            return b0 == 1;
        } else {
            NBTTagCompound nbttagcompound;

            try {
                nbttagcompound = (NBTTagCompound) ((Optional) this.readChunk(chunkcoordintpair).join()).orElse((Object) null);
                if (nbttagcompound == null) {
                    this.markPositionReplaceable(chunkcoordintpair);
                    return false;
                }
            } catch (Exception exception) {
                PlayerChunkMap.LOGGER.error("Failed to read chunk {}", chunkcoordintpair, exception);
                this.markPositionReplaceable(chunkcoordintpair);
                return false;
            }

            ChunkType chunktype = ChunkRegionLoader.getChunkTypeFromTag(nbttagcompound);

            return this.markPosition(chunkcoordintpair, chunktype) == 1;
        }
    }

    protected void setServerViewDistance(int i) {
        int j = MathHelper.clamp(i, 2, 32);

        if (j != this.serverViewDistance) {
            this.serverViewDistance = j;
            this.distanceManager.updatePlayerTickets(this.serverViewDistance);
            Iterator iterator = this.playerMap.getAllPlayers().iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                this.updateChunkTracking(entityplayer);
            }
        }

    }

    int getPlayerViewDistance(EntityPlayer entityplayer) {
        return MathHelper.clamp(entityplayer.requestedViewDistance(), 2, this.serverViewDistance);
    }

    private void markChunkPendingToSend(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
        Chunk chunk = this.getChunkToSend(chunkcoordintpair.toLong());

        if (chunk != null) {
            markChunkPendingToSend(entityplayer, chunk);
        }

    }

    private static void markChunkPendingToSend(EntityPlayer entityplayer, Chunk chunk) {
        entityplayer.connection.chunkSender.markChunkPendingToSend(chunk);
    }

    private static void dropChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
        entityplayer.connection.chunkSender.dropChunk(entityplayer, chunkcoordintpair);
    }

    @Nullable
    public Chunk getChunkToSend(long i) {
        PlayerChunk playerchunk = this.getVisibleChunkIfPresent(i);

        return playerchunk == null ? null : playerchunk.getChunkToSend();
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    public ChunkMapDistance getDistanceManager() {
        return this.distanceManager;
    }

    protected Iterable<PlayerChunk> getChunks() {
        return Iterables.unmodifiableIterable(this.visibleChunkMap.values());
    }

    void dumpChunks(Writer writer) throws IOException {
        CSVWriter csvwriter = CSVWriter.builder().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").build(writer);
        TickingTracker tickingtracker = this.distanceManager.tickingTracker();
        ObjectBidirectionalIterator objectbidirectionaliterator = this.visibleChunkMap.long2ObjectEntrySet().iterator();

        while (objectbidirectionaliterator.hasNext()) {
            Entry<PlayerChunk> entry = (Entry) objectbidirectionaliterator.next();
            long i = entry.getLongKey();
            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i);
            PlayerChunk playerchunk = (PlayerChunk) entry.getValue();
            Optional<IChunkAccess> optional = Optional.ofNullable(playerchunk.getLastAvailable());
            Optional<Chunk> optional1 = optional.flatMap((ichunkaccess) -> {
                return ichunkaccess instanceof Chunk ? Optional.of((Chunk) ichunkaccess) : Optional.empty();
            });

            csvwriter.writeRow(chunkcoordintpair.x, chunkcoordintpair.z, playerchunk.getTicketLevel(), optional.isPresent(), optional.map(IChunkAccess::getStatus).orElse((Object) null), optional1.map(Chunk::getFullStatus).orElse((Object) null), printFuture(playerchunk.getFullChunkFuture()), printFuture(playerchunk.getTickingChunkFuture()), printFuture(playerchunk.getEntityTickingChunkFuture()), this.distanceManager.getTicketDebugString(i), this.anyPlayerCloseEnoughForSpawning(chunkcoordintpair), optional1.map((chunk) -> {
                return chunk.getBlockEntities().size();
            }).orElse(0), tickingtracker.getTicketDebugString(i), tickingtracker.getLevel(i), optional1.map((chunk) -> {
                return chunk.getBlockTicks().count();
            }).orElse(0), optional1.map((chunk) -> {
                return chunk.getFluidTicks().count();
            }).orElse(0));
        }

    }

    private static String printFuture(CompletableFuture<ChunkResult<Chunk>> completablefuture) {
        try {
            ChunkResult<Chunk> chunkresult = (ChunkResult) completablefuture.getNow((Object) null);

            return chunkresult != null ? (chunkresult.isSuccess() ? "done" : "unloaded") : "not completed";
        } catch (CompletionException completionexception) {
            return "failed " + completionexception.getCause().getMessage();
        } catch (CancellationException cancellationexception) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<NBTTagCompound>> readChunk(ChunkCoordIntPair chunkcoordintpair) {
        return this.read(chunkcoordintpair).thenApplyAsync((optional) -> {
            return optional.map(this::upgradeChunkTag);
        }, SystemUtils.backgroundExecutor());
    }

    private NBTTagCompound upgradeChunkTag(NBTTagCompound nbttagcompound) {
        return this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, nbttagcompound, this.generator.getTypeNameForDataFixer());
    }

    boolean anyPlayerCloseEnoughForSpawning(ChunkCoordIntPair chunkcoordintpair) {
        if (!this.distanceManager.hasPlayersNearby(chunkcoordintpair.toLong())) {
            return false;
        } else {
            Iterator iterator = this.playerMap.getAllPlayers().iterator();

            EntityPlayer entityplayer;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                entityplayer = (EntityPlayer) iterator.next();
            } while (!this.playerIsCloseEnoughForSpawning(entityplayer, chunkcoordintpair));

            return true;
        }
    }

    public List<EntityPlayer> getPlayersCloseForSpawning(ChunkCoordIntPair chunkcoordintpair) {
        long i = chunkcoordintpair.toLong();

        if (!this.distanceManager.hasPlayersNearby(i)) {
            return List.of();
        } else {
            Builder<EntityPlayer> builder = ImmutableList.builder();
            Iterator iterator = this.playerMap.getAllPlayers().iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                if (this.playerIsCloseEnoughForSpawning(entityplayer, chunkcoordintpair)) {
                    builder.add(entityplayer);
                }
            }

            return builder.build();
        }
    }

    private boolean playerIsCloseEnoughForSpawning(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
        if (entityplayer.isSpectator()) {
            return false;
        } else {
            double d0 = euclideanDistanceSquared(chunkcoordintpair, entityplayer);

            return d0 < 16384.0D;
        }
    }

    private boolean skipPlayer(EntityPlayer entityplayer) {
        return entityplayer.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
    }

    void updatePlayerStatus(EntityPlayer entityplayer, boolean flag) {
        boolean flag1 = this.skipPlayer(entityplayer);
        boolean flag2 = this.playerMap.ignoredOrUnknown(entityplayer);

        if (flag) {
            this.playerMap.addPlayer(entityplayer, flag1);
            this.updatePlayerPos(entityplayer);
            if (!flag1) {
                this.distanceManager.addPlayer(SectionPosition.of((EntityAccess) entityplayer), entityplayer);
            }

            entityplayer.setChunkTrackingView(ChunkTrackingView.EMPTY);
            this.updateChunkTracking(entityplayer);
        } else {
            SectionPosition sectionposition = entityplayer.getLastSectionPos();

            this.playerMap.removePlayer(entityplayer);
            if (!flag2) {
                this.distanceManager.removePlayer(sectionposition, entityplayer);
            }

            this.applyChunkTrackingView(entityplayer, ChunkTrackingView.EMPTY);
        }

    }

    private void updatePlayerPos(EntityPlayer entityplayer) {
        SectionPosition sectionposition = SectionPosition.of((EntityAccess) entityplayer);

        entityplayer.setLastSectionPos(sectionposition);
    }

    public void move(EntityPlayer entityplayer) {
        ObjectIterator objectiterator = this.entityMap.values().iterator();

        while (objectiterator.hasNext()) {
            PlayerChunkMap.EntityTracker playerchunkmap_entitytracker = (PlayerChunkMap.EntityTracker) objectiterator.next();

            if (playerchunkmap_entitytracker.entity == entityplayer) {
                playerchunkmap_entitytracker.updatePlayers(this.level.players());
            } else {
                playerchunkmap_entitytracker.updatePlayer(entityplayer);
            }
        }

        SectionPosition sectionposition = entityplayer.getLastSectionPos();
        SectionPosition sectionposition1 = SectionPosition.of((EntityAccess) entityplayer);
        boolean flag = this.playerMap.ignored(entityplayer);
        boolean flag1 = this.skipPlayer(entityplayer);
        boolean flag2 = sectionposition.asLong() != sectionposition1.asLong();

        if (flag2 || flag != flag1) {
            this.updatePlayerPos(entityplayer);
            if (!flag) {
                this.distanceManager.removePlayer(sectionposition, entityplayer);
            }

            if (!flag1) {
                this.distanceManager.addPlayer(sectionposition1, entityplayer);
            }

            if (!flag && flag1) {
                this.playerMap.ignorePlayer(entityplayer);
            }

            if (flag && !flag1) {
                this.playerMap.unIgnorePlayer(entityplayer);
            }

            this.updateChunkTracking(entityplayer);
        }

    }

    private void updateChunkTracking(EntityPlayer entityplayer) {
        ChunkCoordIntPair chunkcoordintpair = entityplayer.chunkPosition();
        int i = this.getPlayerViewDistance(entityplayer);
        ChunkTrackingView chunktrackingview = entityplayer.getChunkTrackingView();

        if (chunktrackingview instanceof ChunkTrackingView.a chunktrackingview_a) {
            if (chunktrackingview_a.center().equals(chunkcoordintpair) && chunktrackingview_a.viewDistance() == i) {
                return;
            }
        }

        this.applyChunkTrackingView(entityplayer, ChunkTrackingView.of(chunkcoordintpair, i));
    }

    private void applyChunkTrackingView(EntityPlayer entityplayer, ChunkTrackingView chunktrackingview) {
        if (entityplayer.level() == this.level) {
            ChunkTrackingView chunktrackingview1 = entityplayer.getChunkTrackingView();

            if (chunktrackingview instanceof ChunkTrackingView.a) {
                label15:
                {
                    ChunkTrackingView.a chunktrackingview_a = (ChunkTrackingView.a) chunktrackingview;

                    if (chunktrackingview1 instanceof ChunkTrackingView.a) {
                        ChunkTrackingView.a chunktrackingview_a1 = (ChunkTrackingView.a) chunktrackingview1;

                        if (chunktrackingview_a1.center().equals(chunktrackingview_a.center())) {
                            break label15;
                        }
                    }

                    entityplayer.connection.send(new PacketPlayOutViewCentre(chunktrackingview_a.center().x, chunktrackingview_a.center().z));
                }
            }

            ChunkTrackingView.difference(chunktrackingview1, chunktrackingview, (chunkcoordintpair) -> {
                this.markChunkPendingToSend(entityplayer, chunkcoordintpair);
            }, (chunkcoordintpair) -> {
                dropChunk(entityplayer, chunkcoordintpair);
            });
            entityplayer.setChunkTrackingView(chunktrackingview);
        }
    }

    @Override
    public List<EntityPlayer> getPlayers(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        Set<EntityPlayer> set = this.playerMap.getAllPlayers();
        Builder<EntityPlayer> builder = ImmutableList.builder();
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (flag && this.isChunkOnTrackedBorder(entityplayer, chunkcoordintpair.x, chunkcoordintpair.z) || !flag && this.isChunkTracked(entityplayer, chunkcoordintpair.x, chunkcoordintpair.z)) {
                builder.add(entityplayer);
            }
        }

        return builder.build();
    }

    protected void addEntity(Entity entity) {
        if (!(entity instanceof EntityComplexPart)) {
            EntityTypes<?> entitytypes = entity.getType();
            int i = entitytypes.clientTrackingRange() * 16;

            if (i != 0) {
                int j = entitytypes.updateInterval();

                if (this.entityMap.containsKey(entity.getId())) {
                    throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException("Entity is already tracked!"));
                } else {
                    PlayerChunkMap.EntityTracker playerchunkmap_entitytracker = new PlayerChunkMap.EntityTracker(entity, i, j, entitytypes.trackDeltas());

                    this.entityMap.put(entity.getId(), playerchunkmap_entitytracker);
                    playerchunkmap_entitytracker.updatePlayers(this.level.players());
                    if (entity instanceof EntityPlayer) {
                        EntityPlayer entityplayer = (EntityPlayer) entity;

                        this.updatePlayerStatus(entityplayer, true);
                        ObjectIterator objectiterator = this.entityMap.values().iterator();

                        while (objectiterator.hasNext()) {
                            PlayerChunkMap.EntityTracker playerchunkmap_entitytracker1 = (PlayerChunkMap.EntityTracker) objectiterator.next();

                            if (playerchunkmap_entitytracker1.entity != entityplayer) {
                                playerchunkmap_entitytracker1.updatePlayer(entityplayer);
                            }
                        }
                    }

                }
            }
        }
    }

    protected void removeEntity(Entity entity) {
        if (entity instanceof EntityPlayer entityplayer) {
            this.updatePlayerStatus(entityplayer, false);
            ObjectIterator objectiterator = this.entityMap.values().iterator();

            while (objectiterator.hasNext()) {
                PlayerChunkMap.EntityTracker playerchunkmap_entitytracker = (PlayerChunkMap.EntityTracker) objectiterator.next();

                playerchunkmap_entitytracker.removePlayer(entityplayer);
            }
        }

        PlayerChunkMap.EntityTracker playerchunkmap_entitytracker1 = (PlayerChunkMap.EntityTracker) this.entityMap.remove(entity.getId());

        if (playerchunkmap_entitytracker1 != null) {
            playerchunkmap_entitytracker1.broadcastRemoved();
        }

    }

    protected void tick() {
        Iterator iterator = this.playerMap.getAllPlayers().iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            this.updateChunkTracking(entityplayer);
        }

        List<EntityPlayer> list = Lists.newArrayList();
        List<EntityPlayer> list1 = this.level.players();
        ObjectIterator objectiterator = this.entityMap.values().iterator();

        PlayerChunkMap.EntityTracker playerchunkmap_entitytracker;

        while (objectiterator.hasNext()) {
            playerchunkmap_entitytracker = (PlayerChunkMap.EntityTracker) objectiterator.next();
            SectionPosition sectionposition = playerchunkmap_entitytracker.lastSectionPos;
            SectionPosition sectionposition1 = SectionPosition.of((EntityAccess) playerchunkmap_entitytracker.entity);
            boolean flag = !Objects.equals(sectionposition, sectionposition1);

            if (flag) {
                playerchunkmap_entitytracker.updatePlayers(list1);
                Entity entity = playerchunkmap_entitytracker.entity;

                if (entity instanceof EntityPlayer) {
                    list.add((EntityPlayer) entity);
                }

                playerchunkmap_entitytracker.lastSectionPos = sectionposition1;
            }

            if (flag || this.distanceManager.inEntityTickingRange(sectionposition1.chunk().toLong())) {
                playerchunkmap_entitytracker.serverEntity.sendChanges();
            }
        }

        if (!list.isEmpty()) {
            objectiterator = this.entityMap.values().iterator();

            while (objectiterator.hasNext()) {
                playerchunkmap_entitytracker = (PlayerChunkMap.EntityTracker) objectiterator.next();
                playerchunkmap_entitytracker.updatePlayers(list);
            }
        }

    }

    public void broadcast(Entity entity, Packet<?> packet) {
        PlayerChunkMap.EntityTracker playerchunkmap_entitytracker = (PlayerChunkMap.EntityTracker) this.entityMap.get(entity.getId());

        if (playerchunkmap_entitytracker != null) {
            playerchunkmap_entitytracker.broadcast(packet);
        }

    }

    protected void broadcastAndSend(Entity entity, Packet<?> packet) {
        PlayerChunkMap.EntityTracker playerchunkmap_entitytracker = (PlayerChunkMap.EntityTracker) this.entityMap.get(entity.getId());

        if (playerchunkmap_entitytracker != null) {
            playerchunkmap_entitytracker.broadcastAndSend(packet);
        }

    }

    public void resendBiomesForChunks(List<IChunkAccess> list) {
        Map<EntityPlayer, List<Chunk>> map = new HashMap();
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            IChunkAccess ichunkaccess = (IChunkAccess) iterator.next();
            ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
            Chunk chunk;

            if (ichunkaccess instanceof Chunk chunk1) {
                chunk = chunk1;
            } else {
                chunk = this.level.getChunk(chunkcoordintpair.x, chunkcoordintpair.z);
            }

            Iterator iterator1 = this.getPlayers(chunkcoordintpair, false).iterator();

            while (iterator1.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator1.next();

                ((List) map.computeIfAbsent(entityplayer, (entityplayer1) -> {
                    return new ArrayList();
                })).add(chunk);
            }
        }

        map.forEach((entityplayer1, list1) -> {
            entityplayer1.connection.send(ClientboundChunksBiomesPacket.forChunks(list1));
        });
    }

    protected VillagePlace getPoiManager() {
        return this.poiManager;
    }

    public String getStorageName() {
        return this.storageName;
    }

    void onFullChunkStatusChange(ChunkCoordIntPair chunkcoordintpair, FullChunkStatus fullchunkstatus) {
        this.chunkStatusListener.onChunkStatusChange(chunkcoordintpair, fullchunkstatus);
    }

    public void waitForLightBeforeSending(ChunkCoordIntPair chunkcoordintpair, int i) {
        int j = i + 1;

        ChunkCoordIntPair.rangeClosed(chunkcoordintpair, j).forEach((chunkcoordintpair1) -> {
            PlayerChunk playerchunk = this.getVisibleChunkIfPresent(chunkcoordintpair1.toLong());

            if (playerchunk != null) {
                playerchunk.addSendDependency(this.lightEngine.waitForPendingTasks(chunkcoordintpair1.x, chunkcoordintpair1.z));
            }

        });
    }

    private class a extends ChunkMapDistance {

        protected a(final Executor executor, final Executor executor1) {
            super(executor, executor1);
        }

        @Override
        protected boolean isChunkToRemove(long i) {
            return PlayerChunkMap.this.toDrop.contains(i);
        }

        @Nullable
        @Override
        protected PlayerChunk getChunk(long i) {
            return PlayerChunkMap.this.getUpdatingChunkIfPresent(i);
        }

        @Nullable
        @Override
        protected PlayerChunk updateChunkScheduling(long i, int j, @Nullable PlayerChunk playerchunk, int k) {
            return PlayerChunkMap.this.updateChunkScheduling(i, j, playerchunk, k);
        }
    }

    public class EntityTracker {

        final EntityTrackerEntry serverEntity;
        final Entity entity;
        private final int range;
        SectionPosition lastSectionPos;
        public final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

        public EntityTracker(final Entity entity, final int i, final int j, final boolean flag) {
            this.serverEntity = new EntityTrackerEntry(PlayerChunkMap.this.level, entity, j, flag, this::broadcast);
            this.entity = entity;
            this.range = i;
            this.lastSectionPos = SectionPosition.of((EntityAccess) entity);
        }

        public boolean equals(Object object) {
            return object instanceof PlayerChunkMap.EntityTracker ? ((PlayerChunkMap.EntityTracker) object).entity.getId() == this.entity.getId() : false;
        }

        public int hashCode() {
            return this.entity.getId();
        }

        public void broadcast(Packet<?> packet) {
            Iterator iterator = this.seenBy.iterator();

            while (iterator.hasNext()) {
                ServerPlayerConnection serverplayerconnection = (ServerPlayerConnection) iterator.next();

                serverplayerconnection.send(packet);
            }

        }

        public void broadcastAndSend(Packet<?> packet) {
            this.broadcast(packet);
            if (this.entity instanceof EntityPlayer) {
                ((EntityPlayer) this.entity).connection.send(packet);
            }

        }

        public void broadcastRemoved() {
            Iterator iterator = this.seenBy.iterator();

            while (iterator.hasNext()) {
                ServerPlayerConnection serverplayerconnection = (ServerPlayerConnection) iterator.next();

                this.serverEntity.removePairing(serverplayerconnection.getPlayer());
            }

        }

        public void removePlayer(EntityPlayer entityplayer) {
            if (this.seenBy.remove(entityplayer.connection)) {
                this.serverEntity.removePairing(entityplayer);
            }

        }

        public void updatePlayer(EntityPlayer entityplayer) {
            if (entityplayer != this.entity) {
                Vec3D vec3d = entityplayer.position().subtract(this.entity.position());
                int i = PlayerChunkMap.this.getPlayerViewDistance(entityplayer);
                double d0 = (double) Math.min(this.getEffectiveRange(), i * 16);
                double d1 = vec3d.x * vec3d.x + vec3d.z * vec3d.z;
                double d2 = d0 * d0;
                boolean flag = d1 <= d2 && this.entity.broadcastToPlayer(entityplayer) && PlayerChunkMap.this.isChunkTracked(entityplayer, this.entity.chunkPosition().x, this.entity.chunkPosition().z);

                if (flag) {
                    if (this.seenBy.add(entityplayer.connection)) {
                        this.serverEntity.addPairing(entityplayer);
                    }
                } else if (this.seenBy.remove(entityplayer.connection)) {
                    this.serverEntity.removePairing(entityplayer);
                }

            }
        }

        private int scaledRange(int i) {
            return PlayerChunkMap.this.level.getServer().getScaledTrackingDistance(i);
        }

        private int getEffectiveRange() {
            int i = this.range;
            Iterator iterator = this.entity.getIndirectPassengers().iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();
                int j = entity.getType().clientTrackingRange() * 16;

                if (j > i) {
                    i = j;
                }
            }

            return this.scaledRange(i);
        }

        public void updatePlayers(List<EntityPlayer> list) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                this.updatePlayer(entityplayer);
            }

        }
    }
}
