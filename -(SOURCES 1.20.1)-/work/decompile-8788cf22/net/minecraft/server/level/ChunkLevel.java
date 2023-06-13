package net.minecraft.server.level;

import net.minecraft.world.level.chunk.ChunkStatus;

public class ChunkLevel {

    private static final int FULL_CHUNK_LEVEL = 33;
    private static final int BLOCK_TICKING_LEVEL = 32;
    private static final int ENTITY_TICKING_LEVEL = 31;
    public static final int MAX_LEVEL = 33 + ChunkStatus.maxDistance();

    public ChunkLevel() {}

    public static ChunkStatus generationStatus(int i) {
        return i < 33 ? ChunkStatus.FULL : ChunkStatus.getStatusAroundFullChunk(i - 33);
    }

    public static int byStatus(ChunkStatus chunkstatus) {
        return 33 + ChunkStatus.getDistance(chunkstatus);
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
                throw new IncompatibleClassChangeError();
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
