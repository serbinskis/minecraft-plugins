package net.minecraft.world.level.entity;

import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkCoordIntPair;

@FunctionalInterface
public interface ChunkStatusUpdateListener {

    void onChunkStatusChange(ChunkCoordIntPair chunkcoordintpair, FullChunkStatus fullchunkstatus);
}
