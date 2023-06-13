package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.NibbleArray;

public interface LightEngineLayerEventListener extends ILightEngine {

    @Nullable
    NibbleArray getDataLayerData(SectionPosition sectionposition);

    int getLightValue(BlockPosition blockposition);

    public static enum Void implements LightEngineLayerEventListener {

        INSTANCE;

        private Void() {}

        @Nullable
        @Override
        public NibbleArray getDataLayerData(SectionPosition sectionposition) {
            return null;
        }

        @Override
        public int getLightValue(BlockPosition blockposition) {
            return 0;
        }

        @Override
        public void checkBlock(BlockPosition blockposition) {}

        @Override
        public boolean hasLightWork() {
            return false;
        }

        @Override
        public int runLightUpdates() {
            return 0;
        }

        @Override
        public void updateSectionStatus(SectionPosition sectionposition, boolean flag) {}

        @Override
        public void setLightEnabled(ChunkCoordIntPair chunkcoordintpair, boolean flag) {}

        @Override
        public void propagateLightSources(ChunkCoordIntPair chunkcoordintpair) {}
    }
}
