package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange;
import net.minecraft.util.DebugBuffer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunkExtension;
import net.minecraft.world.level.lighting.LevelLightEngine;

// CraftBukkit start
import net.minecraft.server.MinecraftServer;
// CraftBukkit end

public class PlayerChunk {

    public static final Either<IChunkAccess, PlayerChunk.Failure> UNLOADED_CHUNK = Either.right(PlayerChunk.Failure.UNLOADED);
    public static final CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(PlayerChunk.UNLOADED_CHUNK);
    public static final Either<Chunk, PlayerChunk.Failure> UNLOADED_LEVEL_CHUNK = Either.right(PlayerChunk.Failure.UNLOADED);
    private static final Either<IChunkAccess, PlayerChunk.Failure> NOT_DONE_YET = Either.right(PlayerChunk.Failure.UNLOADED);
    private static final CompletableFuture<Either<Chunk, PlayerChunk.Failure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(PlayerChunk.UNLOADED_LEVEL_CHUNK);
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private final AtomicReferenceArray<CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>>> futures;
    private final LevelHeightAccessor levelHeightAccessor;
    private volatile CompletableFuture<Either<Chunk, PlayerChunk.Failure>> fullChunkFuture;
    private volatile CompletableFuture<Either<Chunk, PlayerChunk.Failure>> tickingChunkFuture;
    private volatile CompletableFuture<Either<Chunk, PlayerChunk.Failure>> entityTickingChunkFuture;
    private CompletableFuture<IChunkAccess> chunkToSave;
    @Nullable
    private final DebugBuffer<PlayerChunk.b> chunkToSaveHistory;
    public int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    final ChunkCoordIntPair pos;
    private boolean hasChangedSections;
    private final ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter;
    private final BitSet skyChangedLightSectionFilter;
    private final LevelLightEngine lightEngine;
    private final PlayerChunk.c onLevelChange;
    public final PlayerChunk.d playerProvider;
    private boolean wasAccessibleSinceLastSave;
    private CompletableFuture<Void> pendingFullStateConfirmation;

    public PlayerChunk(ChunkCoordIntPair chunkcoordintpair, int i, LevelHeightAccessor levelheightaccessor, LevelLightEngine levellightengine, PlayerChunk.c playerchunk_c, PlayerChunk.d playerchunk_d) {
        this.futures = new AtomicReferenceArray(PlayerChunk.CHUNK_STATUSES.size());
        this.fullChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        this.tickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        this.entityTickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        this.chunkToSave = CompletableFuture.completedFuture(null); // CraftBukkit - decompile error
        this.chunkToSaveHistory = null;
        this.blockChangedLightSectionFilter = new BitSet();
        this.skyChangedLightSectionFilter = new BitSet();
        this.pendingFullStateConfirmation = CompletableFuture.completedFuture(null); // CraftBukkit - decompile error
        this.pos = chunkcoordintpair;
        this.levelHeightAccessor = levelheightaccessor;
        this.lightEngine = levellightengine;
        this.onLevelChange = playerchunk_c;
        this.playerProvider = playerchunk_d;
        this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
        this.ticketLevel = this.oldTicketLevel;
        this.queueLevel = this.oldTicketLevel;
        this.setTicketLevel(i);
        this.changedBlocksPerSection = new ShortSet[levelheightaccessor.getSectionsCount()];
    }

    // CraftBukkit start
    public Chunk getFullChunkNow() {
        // Note: We use the oldTicketLevel for isLoaded checks.
        if (!ChunkLevel.fullStatus(this.oldTicketLevel).isOrAfter(FullChunkStatus.FULL)) return null;
        return this.getFullChunkNowUnchecked();
    }

    public Chunk getFullChunkNowUnchecked() {
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> statusFuture = this.getFutureIfPresentUnchecked(ChunkStatus.FULL);
        Either<IChunkAccess, PlayerChunk.Failure> either = (Either<IChunkAccess, PlayerChunk.Failure>) statusFuture.getNow(null);
        return (either == null) ? null : (Chunk) either.left().orElse(null);
    }
    // CraftBukkit end

    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getFutureIfPresentUnchecked(ChunkStatus chunkstatus) {
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = (CompletableFuture) this.futures.get(chunkstatus.getIndex());

        return completablefuture == null ? PlayerChunk.UNLOADED_CHUNK_FUTURE : completablefuture;
    }

    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getFutureIfPresent(ChunkStatus chunkstatus) {
        return ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(chunkstatus) ? this.getFutureIfPresentUnchecked(chunkstatus) : PlayerChunk.UNLOADED_CHUNK_FUTURE;
    }

    public CompletableFuture<Either<Chunk, PlayerChunk.Failure>> getTickingChunkFuture() {
        return this.tickingChunkFuture;
    }

    public CompletableFuture<Either<Chunk, PlayerChunk.Failure>> getEntityTickingChunkFuture() {
        return this.entityTickingChunkFuture;
    }

    public CompletableFuture<Either<Chunk, PlayerChunk.Failure>> getFullChunkFuture() {
        return this.fullChunkFuture;
    }

    @Nullable
    public Chunk getTickingChunk() {
        CompletableFuture<Either<Chunk, PlayerChunk.Failure>> completablefuture = this.getTickingChunkFuture();
        Either<Chunk, PlayerChunk.Failure> either = (Either) completablefuture.getNow(null); // CraftBukkit - decompile error

        return either == null ? null : (Chunk) either.left().orElse(null); // CraftBukkit - decompile error
    }

    @Nullable
    public Chunk getFullChunk() {
        CompletableFuture<Either<Chunk, PlayerChunk.Failure>> completablefuture = this.getFullChunkFuture();
        Either<Chunk, PlayerChunk.Failure> either = (Either) completablefuture.getNow(null); // CraftBukkit - decompile error

        return either == null ? null : (Chunk) either.left().orElse(null); // CraftBukkit - decompile error
    }

    @Nullable
    public ChunkStatus getLastAvailableStatus() {
        for (int i = PlayerChunk.CHUNK_STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus chunkstatus = (ChunkStatus) PlayerChunk.CHUNK_STATUSES.get(i);
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = this.getFutureIfPresentUnchecked(chunkstatus);

            if (((Either) completablefuture.getNow(PlayerChunk.UNLOADED_CHUNK)).left().isPresent()) {
                return chunkstatus;
            }
        }

        return null;
    }

    @Nullable
    public IChunkAccess getLastAvailable() {
        for (int i = PlayerChunk.CHUNK_STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus chunkstatus = (ChunkStatus) PlayerChunk.CHUNK_STATUSES.get(i);
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = this.getFutureIfPresentUnchecked(chunkstatus);

            if (!completablefuture.isCompletedExceptionally()) {
                Optional<IChunkAccess> optional = ((Either) completablefuture.getNow(PlayerChunk.UNLOADED_CHUNK)).left();

                if (optional.isPresent()) {
                    return (IChunkAccess) optional.get();
                }
            }
        }

        return null;
    }

    public CompletableFuture<IChunkAccess> getChunkToSave() {
        return this.chunkToSave;
    }

    public void blockChanged(BlockPosition blockposition) {
        Chunk chunk = this.getTickingChunk();

        if (chunk != null) {
            int i = this.levelHeightAccessor.getSectionIndex(blockposition.getY());

            if (i < 0 || i >= this.changedBlocksPerSection.length) return; // CraftBukkit - SPIGOT-6086, SPIGOT-6296
            if (this.changedBlocksPerSection[i] == null) {
                this.hasChangedSections = true;
                this.changedBlocksPerSection[i] = new ShortOpenHashSet();
            }

            this.changedBlocksPerSection[i].add(SectionPosition.sectionRelativePos(blockposition));
        }
    }

    public void sectionLightChanged(EnumSkyBlock enumskyblock, int i) {
        Either<IChunkAccess, PlayerChunk.Failure> either = (Either) this.getFutureIfPresent(ChunkStatus.INITIALIZE_LIGHT).getNow(null); // CraftBukkit - decompile error

        if (either != null) {
            IChunkAccess ichunkaccess = (IChunkAccess) either.left().orElse(null); // CraftBukkit - decompile error

            if (ichunkaccess != null) {
                ichunkaccess.setUnsaved(true);
                Chunk chunk = this.getTickingChunk();

                if (chunk != null) {
                    int j = this.lightEngine.getMinLightSection();
                    int k = this.lightEngine.getMaxLightSection();

                    if (i >= j && i <= k) {
                        int l = i - j;

                        if (enumskyblock == EnumSkyBlock.SKY) {
                            this.skyChangedLightSectionFilter.set(l);
                        } else {
                            this.blockChangedLightSectionFilter.set(l);
                        }

                    }
                }
            }
        }
    }

    public void broadcastChanges(Chunk chunk) {
        if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            World world = chunk.getLevel();
            List list;

            if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
                list = this.playerProvider.getPlayers(this.pos, true);
                if (!list.isEmpty()) {
                    PacketPlayOutLightUpdate packetplayoutlightupdate = new PacketPlayOutLightUpdate(chunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter);

                    this.broadcast(list, packetplayoutlightupdate);
                }

                this.skyChangedLightSectionFilter.clear();
                this.blockChangedLightSectionFilter.clear();
            }

            if (this.hasChangedSections) {
                list = this.playerProvider.getPlayers(this.pos, false);

                for (int i = 0; i < this.changedBlocksPerSection.length; ++i) {
                    ShortSet shortset = this.changedBlocksPerSection[i];

                    if (shortset != null) {
                        this.changedBlocksPerSection[i] = null;
                        if (!list.isEmpty()) {
                            int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
                            SectionPosition sectionposition = SectionPosition.of(chunk.getPos(), j);

                            if (shortset.size() == 1) {
                                BlockPosition blockposition = sectionposition.relativeToBlockPos(shortset.iterator().nextShort());
                                IBlockData iblockdata = world.getBlockState(blockposition);

                                this.broadcast(list, new PacketPlayOutBlockChange(blockposition, iblockdata));
                                this.broadcastBlockEntityIfNeeded(list, world, blockposition, iblockdata);
                            } else {
                                ChunkSection chunksection = chunk.getSection(i);
                                PacketPlayOutMultiBlockChange packetplayoutmultiblockchange = new PacketPlayOutMultiBlockChange(sectionposition, shortset, chunksection);

                                this.broadcast(list, packetplayoutmultiblockchange);
                                // CraftBukkit start
                                List finalList = list;
                                packetplayoutmultiblockchange.runUpdates((blockposition1, iblockdata1) -> {
                                    this.broadcastBlockEntityIfNeeded(finalList, world, blockposition1, iblockdata1);
                                    // CraftBukkit end
                                });
                            }
                        }
                    }
                }

                this.hasChangedSections = false;
            }
        }
    }

    private void broadcastBlockEntityIfNeeded(List<EntityPlayer> list, World world, BlockPosition blockposition, IBlockData iblockdata) {
        if (iblockdata.hasBlockEntity()) {
            this.broadcastBlockEntity(list, world, blockposition);
        }

    }

    private void broadcastBlockEntity(List<EntityPlayer> list, World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity != null) {
            Packet<?> packet = tileentity.getUpdatePacket();

            if (packet != null) {
                this.broadcast(list, packet);
            }
        }

    }

    private void broadcast(List<EntityPlayer> list, Packet<?> packet) {
        list.forEach((entityplayer) -> {
            entityplayer.connection.send(packet);
        });
    }

    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getOrScheduleFuture(ChunkStatus chunkstatus, PlayerChunkMap playerchunkmap) {
        int i = chunkstatus.getIndex();
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = (CompletableFuture) this.futures.get(i);

        if (completablefuture != null) {
            Either<IChunkAccess, PlayerChunk.Failure> either = (Either) completablefuture.getNow(PlayerChunk.NOT_DONE_YET);

            if (either == null) {
                String s = "value in future for status: " + chunkstatus + " was incorrectly set to null at chunk: " + this.pos;

                throw playerchunkmap.debugFuturesAndCreateReportedException(new IllegalStateException("null value previously set for chunk status"), s);
            }

            if (either == PlayerChunk.NOT_DONE_YET || either.right().isEmpty()) {
                return completablefuture;
            }
        }

        if (ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(chunkstatus)) {
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture1 = playerchunkmap.schedule(this, chunkstatus);

            this.updateChunkToSave(completablefuture1, "schedule " + chunkstatus);
            this.futures.set(i, completablefuture1);
            return completablefuture1;
        } else {
            return completablefuture == null ? PlayerChunk.UNLOADED_CHUNK_FUTURE : completablefuture;
        }
    }

    protected void addSaveDependency(String s, CompletableFuture<?> completablefuture) {
        if (this.chunkToSaveHistory != null) {
            this.chunkToSaveHistory.push(new PlayerChunk.b(Thread.currentThread(), completablefuture, s));
        }

        this.chunkToSave = this.chunkToSave.thenCombine(completablefuture, (ichunkaccess, object) -> {
            return ichunkaccess;
        });
    }

    private void updateChunkToSave(CompletableFuture<? extends Either<? extends IChunkAccess, PlayerChunk.Failure>> completablefuture, String s) {
        if (this.chunkToSaveHistory != null) {
            this.chunkToSaveHistory.push(new PlayerChunk.b(Thread.currentThread(), completablefuture, s));
        }

        this.chunkToSave = this.chunkToSave.thenCombine(completablefuture, (ichunkaccess, either) -> {
            return (IChunkAccess) either.map((ichunkaccess1) -> {
                return ichunkaccess1;
            }, (playerchunk_failure) -> {
                return ichunkaccess;
            });
        });
    }

    public FullChunkStatus getFullStatus() {
        return ChunkLevel.fullStatus(this.ticketLevel);
    }

    public ChunkCoordIntPair getPos() {
        return this.pos;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public int getQueueLevel() {
        return this.queueLevel;
    }

    private void setQueueLevel(int i) {
        this.queueLevel = i;
    }

    public void setTicketLevel(int i) {
        this.ticketLevel = i;
    }

    private void scheduleFullChunkPromotion(PlayerChunkMap playerchunkmap, CompletableFuture<Either<Chunk, PlayerChunk.Failure>> completablefuture, Executor executor, FullChunkStatus fullchunkstatus) {
        this.pendingFullStateConfirmation.cancel(false);
        CompletableFuture<Void> completablefuture1 = new CompletableFuture();

        completablefuture1.thenRunAsync(() -> {
            playerchunkmap.onFullChunkStatusChange(this.pos, fullchunkstatus);
        }, executor);
        this.pendingFullStateConfirmation = completablefuture1;
        completablefuture.thenAccept((either) -> {
            either.ifLeft((chunk) -> {
                completablefuture1.complete(null); // CraftBukkit - decompile error
            });
        });
    }

    private void demoteFullChunk(PlayerChunkMap playerchunkmap, FullChunkStatus fullchunkstatus) {
        this.pendingFullStateConfirmation.cancel(false);
        playerchunkmap.onFullChunkStatusChange(this.pos, fullchunkstatus);
    }

    protected void updateFutures(PlayerChunkMap playerchunkmap, Executor executor) {
        ChunkStatus chunkstatus = ChunkLevel.generationStatus(this.oldTicketLevel);
        ChunkStatus chunkstatus1 = ChunkLevel.generationStatus(this.ticketLevel);
        boolean flag = ChunkLevel.isLoaded(this.oldTicketLevel);
        boolean flag1 = ChunkLevel.isLoaded(this.ticketLevel);
        FullChunkStatus fullchunkstatus = ChunkLevel.fullStatus(this.oldTicketLevel);
        FullChunkStatus fullchunkstatus1 = ChunkLevel.fullStatus(this.ticketLevel);
        // CraftBukkit start
        // ChunkUnloadEvent: Called before the chunk is unloaded: isChunkLoaded is still true and chunk can still be modified by plugins.
        if (fullchunkstatus.isOrAfter(FullChunkStatus.FULL) && !fullchunkstatus1.isOrAfter(FullChunkStatus.FULL)) {
            this.getFutureIfPresentUnchecked(ChunkStatus.FULL).thenAccept((either) -> {
                Chunk chunk = (Chunk)either.left().orElse(null);
                if (chunk != null) {
                    playerchunkmap.callbackExecutor.execute(() -> {
                        // Minecraft will apply the chunks tick lists to the world once the chunk got loaded, and then store the tick
                        // lists again inside the chunk once the chunk becomes inaccessible and set the chunk's needsSaving flag.
                        // These actions may however happen deferred, so we manually set the needsSaving flag already here.
                        chunk.setUnsaved(true);
                        chunk.unloadCallback();
                    });
                }
            }).exceptionally((throwable) -> {
                // ensure exceptions are printed, by default this is not the case
                MinecraftServer.LOGGER.error("Failed to schedule unload callback for chunk " + PlayerChunk.this.pos, throwable);
                return null;
            });

            // Run callback right away if the future was already done
            playerchunkmap.callbackExecutor.run();
        }
        // CraftBukkit end

        if (flag) {
            Either<IChunkAccess, PlayerChunk.Failure> either = Either.right(new PlayerChunk.Failure() {
                public String toString() {
                    return "Unloaded ticket level " + PlayerChunk.this.pos;
                }
            });

            for (int i = flag1 ? chunkstatus1.getIndex() + 1 : 0; i <= chunkstatus.getIndex(); ++i) {
                CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = (CompletableFuture) this.futures.get(i);

                if (completablefuture == null) {
                    this.futures.set(i, CompletableFuture.completedFuture(either));
                }
            }
        }

        boolean flag2 = fullchunkstatus.isOrAfter(FullChunkStatus.FULL);
        boolean flag3 = fullchunkstatus1.isOrAfter(FullChunkStatus.FULL);

        this.wasAccessibleSinceLastSave |= flag3;
        if (!flag2 && flag3) {
            this.fullChunkFuture = playerchunkmap.prepareAccessibleChunk(this);
            this.scheduleFullChunkPromotion(playerchunkmap, this.fullChunkFuture, executor, FullChunkStatus.FULL);
            this.updateChunkToSave(this.fullChunkFuture, "full");
        }

        if (flag2 && !flag3) {
            this.fullChunkFuture.complete(PlayerChunk.UNLOADED_LEVEL_CHUNK);
            this.fullChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        boolean flag4 = fullchunkstatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        boolean flag5 = fullchunkstatus1.isOrAfter(FullChunkStatus.BLOCK_TICKING);

        if (!flag4 && flag5) {
            this.tickingChunkFuture = playerchunkmap.prepareTickingChunk(this);
            this.scheduleFullChunkPromotion(playerchunkmap, this.tickingChunkFuture, executor, FullChunkStatus.BLOCK_TICKING);
            this.updateChunkToSave(this.tickingChunkFuture, "ticking");
        }

        if (flag4 && !flag5) {
            this.tickingChunkFuture.complete(PlayerChunk.UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        boolean flag6 = fullchunkstatus.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        boolean flag7 = fullchunkstatus1.isOrAfter(FullChunkStatus.ENTITY_TICKING);

        if (!flag6 && flag7) {
            if (this.entityTickingChunkFuture != PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException());
            }

            this.entityTickingChunkFuture = playerchunkmap.prepareEntityTickingChunk(this);
            this.scheduleFullChunkPromotion(playerchunkmap, this.entityTickingChunkFuture, executor, FullChunkStatus.ENTITY_TICKING);
            this.updateChunkToSave(this.entityTickingChunkFuture, "entity ticking");
        }

        if (flag6 && !flag7) {
            this.entityTickingChunkFuture.complete(PlayerChunk.UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        if (!fullchunkstatus1.isOrAfter(fullchunkstatus)) {
            this.demoteFullChunk(playerchunkmap, fullchunkstatus1);
        }

        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
        // CraftBukkit start
        // ChunkLoadEvent: Called after the chunk is loaded: isChunkLoaded returns true and chunk is ready to be modified by plugins.
        if (!fullchunkstatus.isOrAfter(FullChunkStatus.FULL) && fullchunkstatus1.isOrAfter(FullChunkStatus.FULL)) {
            this.getFutureIfPresentUnchecked(ChunkStatus.FULL).thenAccept((either) -> {
                Chunk chunk = (Chunk)either.left().orElse(null);
                if (chunk != null) {
                    playerchunkmap.callbackExecutor.execute(() -> {
                        chunk.loadCallback();
                    });
                }
            }).exceptionally((throwable) -> {
                // ensure exceptions are printed, by default this is not the case
                MinecraftServer.LOGGER.error("Failed to schedule load callback for chunk " + PlayerChunk.this.pos, throwable);
                return null;
            });

            // Run callback right away if the future was already done
            playerchunkmap.callbackExecutor.run();
        }
        // CraftBukkit end
    }

    public boolean wasAccessibleSinceLastSave() {
        return this.wasAccessibleSinceLastSave;
    }

    public void refreshAccessibility() {
        this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
    }

    public void replaceProtoChunk(ProtoChunkExtension protochunkextension) {
        for (int i = 0; i < this.futures.length(); ++i) {
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = (CompletableFuture) this.futures.get(i);

            if (completablefuture != null) {
                Optional<IChunkAccess> optional = ((Either) completablefuture.getNow(PlayerChunk.UNLOADED_CHUNK)).left();

                if (!optional.isEmpty() && optional.get() instanceof ProtoChunk) {
                    this.futures.set(i, CompletableFuture.completedFuture(Either.left(protochunkextension)));
                }
            }
        }

        this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(protochunkextension.getWrapped())), "replaceProto");
    }

    public List<Pair<ChunkStatus, CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>>>> getAllFutures() {
        List<Pair<ChunkStatus, CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>>>> list = new ArrayList();

        for (int i = 0; i < PlayerChunk.CHUNK_STATUSES.size(); ++i) {
            list.add(Pair.of((ChunkStatus) PlayerChunk.CHUNK_STATUSES.get(i), (CompletableFuture) this.futures.get(i)));
        }

        return list;
    }

    @FunctionalInterface
    public interface c {

        void onLevelChange(ChunkCoordIntPair chunkcoordintpair, IntSupplier intsupplier, int i, IntConsumer intconsumer);
    }

    public interface d {

        List<EntityPlayer> getPlayers(ChunkCoordIntPair chunkcoordintpair, boolean flag);
    }

    private static final class b {

        private final Thread thread;
        private final CompletableFuture<?> future;
        private final String source;

        b(Thread thread, CompletableFuture<?> completablefuture, String s) {
            this.thread = thread;
            this.future = completablefuture;
            this.source = s;
        }
    }

    public interface Failure {

        PlayerChunk.Failure UNLOADED = new PlayerChunk.Failure() {
            public String toString() {
                return "UNLOADED";
            }
        };
    }
}
