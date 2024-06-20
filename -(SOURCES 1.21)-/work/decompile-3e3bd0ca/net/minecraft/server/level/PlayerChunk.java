package net.minecraft.server.level;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class PlayerChunk extends GenerationChunkHolder {

    public static final ChunkResult<Chunk> UNLOADED_LEVEL_CHUNK = ChunkResult.error("Unloaded level chunk");
    private static final CompletableFuture<ChunkResult<Chunk>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(PlayerChunk.UNLOADED_LEVEL_CHUNK);
    private final LevelHeightAccessor levelHeightAccessor;
    private volatile CompletableFuture<ChunkResult<Chunk>> fullChunkFuture;
    private volatile CompletableFuture<ChunkResult<Chunk>> tickingChunkFuture;
    private volatile CompletableFuture<ChunkResult<Chunk>> entityTickingChunkFuture;
    public int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    private boolean hasChangedSections;
    private final ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter;
    private final BitSet skyChangedLightSectionFilter;
    private final LevelLightEngine lightEngine;
    private final PlayerChunk.a onLevelChange;
    public final PlayerChunk.b playerProvider;
    private boolean wasAccessibleSinceLastSave;
    private CompletableFuture<?> pendingFullStateConfirmation;
    private CompletableFuture<?> sendSync;
    private CompletableFuture<?> saveSync;

    public PlayerChunk(ChunkCoordIntPair chunkcoordintpair, int i, LevelHeightAccessor levelheightaccessor, LevelLightEngine levellightengine, PlayerChunk.a playerchunk_a, PlayerChunk.b playerchunk_b) {
        super(chunkcoordintpair);
        this.fullChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        this.tickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        this.entityTickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        this.blockChangedLightSectionFilter = new BitSet();
        this.skyChangedLightSectionFilter = new BitSet();
        this.pendingFullStateConfirmation = CompletableFuture.completedFuture((Object) null);
        this.sendSync = CompletableFuture.completedFuture((Object) null);
        this.saveSync = CompletableFuture.completedFuture((Object) null);
        this.levelHeightAccessor = levelheightaccessor;
        this.lightEngine = levellightengine;
        this.onLevelChange = playerchunk_a;
        this.playerProvider = playerchunk_b;
        this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
        this.ticketLevel = this.oldTicketLevel;
        this.queueLevel = this.oldTicketLevel;
        this.setTicketLevel(i);
        this.changedBlocksPerSection = new ShortSet[levelheightaccessor.getSectionsCount()];
    }

    public CompletableFuture<ChunkResult<Chunk>> getTickingChunkFuture() {
        return this.tickingChunkFuture;
    }

    public CompletableFuture<ChunkResult<Chunk>> getEntityTickingChunkFuture() {
        return this.entityTickingChunkFuture;
    }

    public CompletableFuture<ChunkResult<Chunk>> getFullChunkFuture() {
        return this.fullChunkFuture;
    }

    @Nullable
    public Chunk getTickingChunk() {
        return (Chunk) ((ChunkResult) this.getTickingChunkFuture().getNow(PlayerChunk.UNLOADED_LEVEL_CHUNK)).orElse((Object) null);
    }

    @Nullable
    public Chunk getChunkToSend() {
        return !this.sendSync.isDone() ? null : this.getTickingChunk();
    }

    public CompletableFuture<?> getSendSyncFuture() {
        return this.sendSync;
    }

    public void addSendDependency(CompletableFuture<?> completablefuture) {
        if (this.sendSync.isDone()) {
            this.sendSync = completablefuture;
        } else {
            this.sendSync = this.sendSync.thenCombine(completablefuture, (object, object1) -> {
                return null;
            });
        }

    }

    public CompletableFuture<?> getSaveSyncFuture() {
        return this.saveSync;
    }

    public boolean isReadyForSaving() {
        return this.getGenerationRefCount() == 0 && this.saveSync.isDone();
    }

    private void addSaveDependency(CompletableFuture<?> completablefuture) {
        if (this.saveSync.isDone()) {
            this.saveSync = completablefuture;
        } else {
            this.saveSync = this.saveSync.thenCombine(completablefuture, (object, object1) -> {
                return null;
            });
        }

    }

    public void blockChanged(BlockPosition blockposition) {
        Chunk chunk = this.getTickingChunk();

        if (chunk != null) {
            int i = this.levelHeightAccessor.getSectionIndex(blockposition.getY());

            if (this.changedBlocksPerSection[i] == null) {
                this.hasChangedSections = true;
                this.changedBlocksPerSection[i] = new ShortOpenHashSet();
            }

            this.changedBlocksPerSection[i].add(SectionPosition.sectionRelativePos(blockposition));
        }
    }

    public void sectionLightChanged(EnumSkyBlock enumskyblock, int i) {
        IChunkAccess ichunkaccess = this.getChunkIfPresent(ChunkStatus.INITIALIZE_LIGHT);

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
                                packetplayoutmultiblockchange.runUpdates((blockposition1, iblockdata1) -> {
                                    this.broadcastBlockEntityIfNeeded(list, world, blockposition1, iblockdata1);
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

    @Override
    public int getTicketLevel() {
        return this.ticketLevel;
    }

    @Override
    public int getQueueLevel() {
        return this.queueLevel;
    }

    private void setQueueLevel(int i) {
        this.queueLevel = i;
    }

    public void setTicketLevel(int i) {
        this.ticketLevel = i;
    }

    private void scheduleFullChunkPromotion(PlayerChunkMap playerchunkmap, CompletableFuture<ChunkResult<Chunk>> completablefuture, Executor executor, FullChunkStatus fullchunkstatus) {
        this.pendingFullStateConfirmation.cancel(false);
        CompletableFuture<Void> completablefuture1 = new CompletableFuture();

        completablefuture1.thenRunAsync(() -> {
            playerchunkmap.onFullChunkStatusChange(this.pos, fullchunkstatus);
        }, executor);
        this.pendingFullStateConfirmation = completablefuture1;
        completablefuture.thenAccept((chunkresult) -> {
            chunkresult.ifSuccess((chunk) -> {
                completablefuture1.complete((Object) null);
            });
        });
    }

    private void demoteFullChunk(PlayerChunkMap playerchunkmap, FullChunkStatus fullchunkstatus) {
        this.pendingFullStateConfirmation.cancel(false);
        playerchunkmap.onFullChunkStatusChange(this.pos, fullchunkstatus);
    }

    protected void updateFutures(PlayerChunkMap playerchunkmap, Executor executor) {
        FullChunkStatus fullchunkstatus = ChunkLevel.fullStatus(this.oldTicketLevel);
        FullChunkStatus fullchunkstatus1 = ChunkLevel.fullStatus(this.ticketLevel);
        boolean flag = fullchunkstatus.isOrAfter(FullChunkStatus.FULL);
        boolean flag1 = fullchunkstatus1.isOrAfter(FullChunkStatus.FULL);

        this.wasAccessibleSinceLastSave |= flag1;
        if (!flag && flag1) {
            this.fullChunkFuture = playerchunkmap.prepareAccessibleChunk(this);
            this.scheduleFullChunkPromotion(playerchunkmap, this.fullChunkFuture, executor, FullChunkStatus.FULL);
            this.addSaveDependency(this.fullChunkFuture);
        }

        if (flag && !flag1) {
            this.fullChunkFuture.complete(PlayerChunk.UNLOADED_LEVEL_CHUNK);
            this.fullChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        boolean flag2 = fullchunkstatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        boolean flag3 = fullchunkstatus1.isOrAfter(FullChunkStatus.BLOCK_TICKING);

        if (!flag2 && flag3) {
            this.tickingChunkFuture = playerchunkmap.prepareTickingChunk(this);
            this.scheduleFullChunkPromotion(playerchunkmap, this.tickingChunkFuture, executor, FullChunkStatus.BLOCK_TICKING);
            this.addSaveDependency(this.tickingChunkFuture);
        }

        if (flag2 && !flag3) {
            this.tickingChunkFuture.complete(PlayerChunk.UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        boolean flag4 = fullchunkstatus.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        boolean flag5 = fullchunkstatus1.isOrAfter(FullChunkStatus.ENTITY_TICKING);

        if (!flag4 && flag5) {
            if (this.entityTickingChunkFuture != PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw (IllegalStateException) SystemUtils.pauseInIde(new IllegalStateException());
            }

            this.entityTickingChunkFuture = playerchunkmap.prepareEntityTickingChunk(this);
            this.scheduleFullChunkPromotion(playerchunkmap, this.entityTickingChunkFuture, executor, FullChunkStatus.ENTITY_TICKING);
            this.addSaveDependency(this.entityTickingChunkFuture);
        }

        if (flag4 && !flag5) {
            this.entityTickingChunkFuture.complete(PlayerChunk.UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = PlayerChunk.UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        if (!fullchunkstatus1.isOrAfter(fullchunkstatus)) {
            this.demoteFullChunk(playerchunkmap, fullchunkstatus1);
        }

        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public boolean wasAccessibleSinceLastSave() {
        return this.wasAccessibleSinceLastSave;
    }

    public void refreshAccessibility() {
        this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
    }

    @FunctionalInterface
    public interface a {

        void onLevelChange(ChunkCoordIntPair chunkcoordintpair, IntSupplier intsupplier, int i, IntConsumer intconsumer);
    }

    public interface b {

        List<EntityPlayer> getPlayers(ChunkCoordIntPair chunkcoordintpair, boolean flag);
    }
}
