package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface WorldLoadListener {

    void updateSpawnPos(ChunkCoordIntPair chunkcoordintpair);

    void onStatusChange(ChunkCoordIntPair chunkcoordintpair, @Nullable ChunkStatus chunkstatus);

    void start();

    void stop();

    static int calculateDiameter(int i) {
        return 2 * i + 1;
    }
}
