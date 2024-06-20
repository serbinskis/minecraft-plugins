package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.jetbrains.annotations.Contract;

public class ChunkLevel {

    private static final int FULL_CHUNK_LEVEL = 33;
    private static final int BLOCK_TICKING_LEVEL = 32;
    private static final int ENTITY_TICKING_LEVEL = 31;
    private static final ChunkStep FULL_CHUNK_STEP = ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FULL);
    public static final int RADIUS_AROUND_FULL_CHUNK = ChunkLevel.FULL_CHUNK_STEP.accumulatedDependencies().getRadius();
    public static final int MAX_LEVEL = 33 + ChunkLevel.RADIUS_AROUND_FULL_CHUNK;

    public ChunkLevel() {}

    @Nullable
    public static ChunkStatus generationStatus(int i) {
        return getStatusAroundFullChunk(i - 33, (ChunkStatus) null);
    }

    @Nullable
    @Contract("_,!null->!null;_,_->_")
    public static ChunkStatus getStatusAroundFullChunk(int i, @Nullable ChunkStatus chunkstatus) {
        return i > ChunkLevel.RADIUS_AROUND_FULL_CHUNK ? chunkstatus : (i <= 0 ? ChunkStatus.FULL : ChunkLevel.FULL_CHUNK_STEP.accumulatedDependencies().get(i));
    }

    public static ChunkStatus getStatusAroundFullChunk(int i) {
        return getStatusAroundFullChunk(i, ChunkStatus.EMPTY);
    }

    public static int byStatus(ChunkStatus chunkstatus) {
        return 33 + ChunkLevel.FULL_CHUNK_STEP.getAccumulatedRadiusOf(chunkstatus);
    }

    public static FullChunkStatus fullStatus(int i) {
        return i <= 31 ? FullChunkStatus.ENTITY_TICKING : (i <= 32 ? FullChunkStatus.BLOCK_TICKING : (i <= 33 ? FullChunkStatus.FULL : FullChunkStatus.INACCESSIBLE));
    }

    public static int byStatus(FullChunkStatus fullchunkstatus) {
        int i;

        switch (fullchunkstatus) {
            case INACCESSIBLE:
                i = ChunkLevel.MAX_LEVEL;
                break;
            case FULL:
                i = 33;
                break;
            case BLOCK_TICKING:
                i = 32;
                break;
            case ENTITY_TICKING:
                i = 31;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return i;
    }

    public static boolean isEntityTicking(int i) {
        return i <= 31;
    }

    public static boolean isBlockTicking(int i) {
        return i <= 32;
    }

    public static boolean isLoaded(int i) {
        return i <= ChunkLevel.MAX_LEVEL;
    }
}
