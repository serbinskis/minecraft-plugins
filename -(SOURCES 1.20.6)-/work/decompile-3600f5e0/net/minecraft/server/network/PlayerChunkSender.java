package net.minecraft.server.network;

import com.google.common.collect.Comparators;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.Chunk;
import org.slf4j.Logger;

public class PlayerChunkSender {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float MIN_CHUNKS_PER_TICK = 0.01F;
    public static final float MAX_CHUNKS_PER_TICK = 64.0F;
    private static final float START_CHUNKS_PER_TICK = 9.0F;
    private static final int MAX_UNACKNOWLEDGED_BATCHES = 10;
    private final LongSet pendingChunks = new LongOpenHashSet();
    private final boolean memoryConnection;
    private float desiredChunksPerTick = 9.0F;
    private float batchQuota;
    private int unacknowledgedBatches;
    private int maxUnacknowledgedBatches = 1;

    public PlayerChunkSender(boolean flag) {
        this.memoryConnection = flag;
    }

    public void markChunkPendingToSend(Chunk chunk) {
        this.pendingChunks.add(chunk.getPos().toLong());
    }

    public void dropChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
        if (!this.pendingChunks.remove(chunkcoordintpair.toLong()) && entityplayer.isAlive()) {
            entityplayer.connection.send(new PacketPlayOutUnloadChunk(chunkcoordintpair));
        }

    }

    public void sendNextChunks(EntityPlayer entityplayer) {
        if (this.unacknowledgedBatches < this.maxUnacknowledgedBatches) {
            float f = Math.max(1.0F, this.desiredChunksPerTick);

            this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, f);
            if (this.batchQuota >= 1.0F) {
                if (!this.pendingChunks.isEmpty()) {
                    WorldServer worldserver = entityplayer.serverLevel();
                    PlayerChunkMap playerchunkmap = worldserver.getChunkSource().chunkMap;
                    List<Chunk> list = this.collectChunksToSend(playerchunkmap, entityplayer.chunkPosition());

                    if (!list.isEmpty()) {
                        PlayerConnection playerconnection = entityplayer.connection;

                        ++this.unacknowledgedBatches;
                        playerconnection.send(ClientboundChunkBatchStartPacket.INSTANCE);
                        Iterator iterator = list.iterator();

                        while (iterator.hasNext()) {
                            Chunk chunk = (Chunk) iterator.next();

                            sendChunk(playerconnection, worldserver, chunk);
                        }

                        playerconnection.send(new ClientboundChunkBatchFinishedPacket(list.size()));
                        this.batchQuota -= (float) list.size();
                    }
                }
            }
        }
    }

    private static void sendChunk(PlayerConnection playerconnection, WorldServer worldserver, Chunk chunk) {
        playerconnection.send(new ClientboundLevelChunkWithLightPacket(chunk, worldserver.getLightEngine(), (BitSet) null, (BitSet) null));
        ChunkCoordIntPair chunkcoordintpair = chunk.getPos();

        PacketDebug.sendPoiPacketsForChunk(worldserver, chunkcoordintpair);
    }

    private List<Chunk> collectChunksToSend(PlayerChunkMap playerchunkmap, ChunkCoordIntPair chunkcoordintpair) {
        int i = MathHelper.floor(this.batchQuota);
        LongStream longstream;
        List list;

        if (!this.memoryConnection && this.pendingChunks.size() > i) {
            Stream stream = this.pendingChunks.stream();

            Objects.requireNonNull(chunkcoordintpair);
            longstream = ((List) stream.collect(Comparators.least(i, Comparator.comparingInt(chunkcoordintpair::distanceSquared)))).stream().mapToLong(Long::longValue);
            Objects.requireNonNull(playerchunkmap);
            list = longstream.mapToObj(playerchunkmap::getChunkToSend).filter(Objects::nonNull).toList();
        } else {
            longstream = this.pendingChunks.longStream();
            Objects.requireNonNull(playerchunkmap);
            list = longstream.mapToObj(playerchunkmap::getChunkToSend).filter(Objects::nonNull).sorted(Comparator.comparingInt((chunk) -> {
                return chunkcoordintpair.distanceSquared(chunk.getPos());
            })).toList();
        }

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();

            this.pendingChunks.remove(chunk.getPos().toLong());
        }

        return list;
    }

    public void onChunkBatchReceivedByClient(float f) {
        --this.unacknowledgedBatches;
        this.desiredChunksPerTick = Double.isNaN((double) f) ? 0.01F : MathHelper.clamp(f, 0.01F, 64.0F);
        if (this.unacknowledgedBatches == 0) {
            this.batchQuota = 1.0F;
        }

        this.maxUnacknowledgedBatches = 10;
    }

    public boolean isPending(long i) {
        return this.pendingChunks.contains(i);
    }
}
