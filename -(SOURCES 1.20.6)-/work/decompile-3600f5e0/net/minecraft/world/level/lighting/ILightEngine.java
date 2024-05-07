package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.ChunkCoordIntPair;

public interface ILightEngine {

    void checkBlock(BlockPosition blockposition);

    boolean hasLightWork();

    int runLightUpdates();

    default void updateSectionStatus(BlockPosition blockposition, boolean flag) {
        this.updateSectionStatus(SectionPosition.of(blockposition), flag);
    }

    void updateSectionStatus(SectionPosition sectionposition, boolean flag);

    void setLightEnabled(ChunkCoordIntPair chunkcoordintpair, boolean flag);

    void propagateLightSources(ChunkCoordIntPair chunkcoordintpair);
}
