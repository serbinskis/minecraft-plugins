package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.NibbleArray;

public class LevelLightEngine implements ILightEngine {

    public static final int LIGHT_SECTION_PADDING = 1;
    protected final LevelHeightAccessor levelHeightAccessor;
    @Nullable
    private final LightEngine<?, ?> blockEngine;
    @Nullable
    private final LightEngine<?, ?> skyEngine;

    public LevelLightEngine(ILightAccess ilightaccess, boolean flag, boolean flag1) {
        this.levelHeightAccessor = ilightaccess.getLevel();
        this.blockEngine = flag ? new LightEngineBlock(ilightaccess) : null;
        this.skyEngine = flag1 ? new LightEngineSky(ilightaccess) : null;
    }

    @Override
    public void checkBlock(BlockPosition blockposition) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock(blockposition);
        }

        if (this.skyEngine != null) {
            this.skyEngine.checkBlock(blockposition);
        }

    }

    @Override
    public boolean hasLightWork() {
        return this.skyEngine != null && this.skyEngine.hasLightWork() ? true : this.blockEngine != null && this.blockEngine.hasLightWork();
    }

    @Override
    public int runLightUpdates() {
        int i = 0;

        if (this.blockEngine != null) {
            i += this.blockEngine.runLightUpdates();
        }

        if (this.skyEngine != null) {
            i += this.skyEngine.runLightUpdates();
        }

        return i;
    }

    @Override
    public void updateSectionStatus(SectionPosition sectionposition, boolean flag) {
        if (this.blockEngine != null) {
            this.blockEngine.updateSectionStatus(sectionposition, flag);
        }

        if (this.skyEngine != null) {
            this.skyEngine.updateSectionStatus(sectionposition, flag);
        }

    }

    @Override
    public void setLightEnabled(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        if (this.blockEngine != null) {
            this.blockEngine.setLightEnabled(chunkcoordintpair, flag);
        }

        if (this.skyEngine != null) {
            this.skyEngine.setLightEnabled(chunkcoordintpair, flag);
        }

    }

    @Override
    public void propagateLightSources(ChunkCoordIntPair chunkcoordintpair) {
        if (this.blockEngine != null) {
            this.blockEngine.propagateLightSources(chunkcoordintpair);
        }

        if (this.skyEngine != null) {
            this.skyEngine.propagateLightSources(chunkcoordintpair);
        }

    }

    public LightEngineLayerEventListener getLayerListener(EnumSkyBlock enumskyblock) {
        return (LightEngineLayerEventListener) (enumskyblock == EnumSkyBlock.BLOCK ? (this.blockEngine == null ? LightEngineLayerEventListener.Void.INSTANCE : this.blockEngine) : (this.skyEngine == null ? LightEngineLayerEventListener.Void.INSTANCE : this.skyEngine));
    }

    public String getDebugData(EnumSkyBlock enumskyblock, SectionPosition sectionposition) {
        if (enumskyblock == EnumSkyBlock.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugData(sectionposition.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugData(sectionposition.asLong());
        }

        return "n/a";
    }

    public LightEngineStorage.b getDebugSectionType(EnumSkyBlock enumskyblock, SectionPosition sectionposition) {
        if (enumskyblock == EnumSkyBlock.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugSectionType(sectionposition.asLong());
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugSectionType(sectionposition.asLong());
        }

        return LightEngineStorage.b.EMPTY;
    }

    public void queueSectionData(EnumSkyBlock enumskyblock, SectionPosition sectionposition, @Nullable NibbleArray nibblearray) {
        if (enumskyblock == EnumSkyBlock.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(sectionposition.asLong(), nibblearray);
            }
        } else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(sectionposition.asLong(), nibblearray);
        }

    }

    public void retainData(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        if (this.blockEngine != null) {
            this.blockEngine.retainData(chunkcoordintpair, flag);
        }

        if (this.skyEngine != null) {
            this.skyEngine.retainData(chunkcoordintpair, flag);
        }

    }

    public int getRawBrightness(BlockPosition blockposition, int i) {
        int j = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(blockposition) - i;
        int k = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(blockposition);

        return Math.max(k, j);
    }

    public boolean lightOnInSection(SectionPosition sectionposition) {
        long i = sectionposition.asLong();

        return this.blockEngine == null || this.blockEngine.storage.lightOnInSection(i) && (this.skyEngine == null || this.skyEngine.storage.lightOnInSection(i));
    }

    public int getLightSectionCount() {
        return this.levelHeightAccessor.getSectionsCount() + 2;
    }

    public int getMinLightSection() {
        return this.levelHeightAccessor.getMinSection() - 1;
    }

    public int getMaxLightSection() {
        return this.getMinLightSection() + this.getLightSectionCount();
    }
}
