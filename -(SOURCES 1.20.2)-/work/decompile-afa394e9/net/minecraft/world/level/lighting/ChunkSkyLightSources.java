package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.DataBits;
import net.minecraft.util.MathHelper;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class ChunkSkyLightSources {

    private static final int SIZE = 16;
    public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
    private final int minY;
    private final DataBits heightmap;
    private final BlockPosition.MutableBlockPosition mutablePos1 = new BlockPosition.MutableBlockPosition();
    private final BlockPosition.MutableBlockPosition mutablePos2 = new BlockPosition.MutableBlockPosition();

    public ChunkSkyLightSources(LevelHeightAccessor levelheightaccessor) {
        this.minY = levelheightaccessor.getMinBuildHeight() - 1;
        int i = levelheightaccessor.getMaxBuildHeight();
        int j = MathHelper.ceillog2(i - this.minY + 1);

        this.heightmap = new SimpleBitStorage(j, 256);
    }

    public void fillFrom(IChunkAccess ichunkaccess) {
        int i = ichunkaccess.getHighestFilledSectionIndex();

        if (i == -1) {
            this.fill(this.minY);
        } else {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    int l = Math.max(this.findLowestSourceY(ichunkaccess, i, k, j), this.minY);

                    this.set(index(k, j), l);
                }
            }

        }
    }

    private int findLowestSourceY(IChunkAccess ichunkaccess, int i, int j, int k) {
        int l = SectionPosition.sectionToBlockCoord(ichunkaccess.getSectionYFromSectionIndex(i) + 1);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = this.mutablePos1.set(j, l, k);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = this.mutablePos2.setWithOffset(blockposition_mutableblockposition, EnumDirection.DOWN);
        IBlockData iblockdata = Blocks.AIR.defaultBlockState();

        for (int i1 = i; i1 >= 0; --i1) {
            ChunkSection chunksection = ichunkaccess.getSection(i1);
            int j1;

            if (chunksection.hasOnlyAir()) {
                iblockdata = Blocks.AIR.defaultBlockState();
                j1 = ichunkaccess.getSectionYFromSectionIndex(i1);
                blockposition_mutableblockposition.setY(SectionPosition.sectionToBlockCoord(j1));
                blockposition_mutableblockposition1.setY(blockposition_mutableblockposition.getY() - 1);
            } else {
                for (j1 = 15; j1 >= 0; --j1) {
                    IBlockData iblockdata1 = chunksection.getBlockState(j, j1, k);

                    if (isEdgeOccluded(ichunkaccess, blockposition_mutableblockposition, iblockdata, blockposition_mutableblockposition1, iblockdata1)) {
                        return blockposition_mutableblockposition.getY();
                    }

                    iblockdata = iblockdata1;
                    blockposition_mutableblockposition.set(blockposition_mutableblockposition1);
                    blockposition_mutableblockposition1.move(EnumDirection.DOWN);
                }
            }
        }

        return this.minY;
    }

    public boolean update(IBlockAccess iblockaccess, int i, int j, int k) {
        int l = j + 1;
        int i1 = index(i, k);
        int j1 = this.get(i1);

        if (l < j1) {
            return false;
        } else {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = this.mutablePos1.set(i, j + 1, k);
            IBlockData iblockdata = iblockaccess.getBlockState(blockposition_mutableblockposition);
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = this.mutablePos2.set(i, j, k);
            IBlockData iblockdata1 = iblockaccess.getBlockState(blockposition_mutableblockposition1);

            if (this.updateEdge(iblockaccess, i1, j1, blockposition_mutableblockposition, iblockdata, blockposition_mutableblockposition1, iblockdata1)) {
                return true;
            } else {
                BlockPosition.MutableBlockPosition blockposition_mutableblockposition2 = this.mutablePos1.set(i, j - 1, k);
                IBlockData iblockdata2 = iblockaccess.getBlockState(blockposition_mutableblockposition2);

                return this.updateEdge(iblockaccess, i1, j1, blockposition_mutableblockposition1, iblockdata1, blockposition_mutableblockposition2, iblockdata2);
            }
        }
    }

    private boolean updateEdge(IBlockAccess iblockaccess, int i, int j, BlockPosition blockposition, IBlockData iblockdata, BlockPosition blockposition1, IBlockData iblockdata1) {
        int k = blockposition.getY();

        if (isEdgeOccluded(iblockaccess, blockposition, iblockdata, blockposition1, iblockdata1)) {
            if (k > j) {
                this.set(i, k);
                return true;
            }
        } else if (k == j) {
            this.set(i, this.findLowestSourceBelow(iblockaccess, blockposition1, iblockdata1));
            return true;
        }

        return false;
    }

    private int findLowestSourceBelow(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = this.mutablePos1.set(blockposition);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = this.mutablePos2.setWithOffset(blockposition, EnumDirection.DOWN);
        IBlockData iblockdata1 = iblockdata;

        while (blockposition_mutableblockposition1.getY() >= this.minY) {
            IBlockData iblockdata2 = iblockaccess.getBlockState(blockposition_mutableblockposition1);

            if (isEdgeOccluded(iblockaccess, blockposition_mutableblockposition, iblockdata1, blockposition_mutableblockposition1, iblockdata2)) {
                return blockposition_mutableblockposition.getY();
            }

            iblockdata1 = iblockdata2;
            blockposition_mutableblockposition.set(blockposition_mutableblockposition1);
            blockposition_mutableblockposition1.move(EnumDirection.DOWN);
        }

        return this.minY;
    }

    private static boolean isEdgeOccluded(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, BlockPosition blockposition1, IBlockData iblockdata1) {
        if (iblockdata1.getLightBlock(iblockaccess, blockposition1) != 0) {
            return true;
        } else {
            VoxelShape voxelshape = LightEngine.getOcclusionShape(iblockaccess, blockposition, iblockdata, EnumDirection.DOWN);
            VoxelShape voxelshape1 = LightEngine.getOcclusionShape(iblockaccess, blockposition1, iblockdata1, EnumDirection.UP);

            return VoxelShapes.faceShapeOccludes(voxelshape, voxelshape1);
        }
    }

    public int getLowestSourceY(int i, int j) {
        int k = this.get(index(i, j));

        return this.extendSourcesBelowWorld(k);
    }

    public int getHighestLowestSourceY() {
        int i = Integer.MIN_VALUE;

        for (int j = 0; j < this.heightmap.getSize(); ++j) {
            int k = this.heightmap.get(j);

            if (k > i) {
                i = k;
            }
        }

        return this.extendSourcesBelowWorld(i + this.minY);
    }

    private void fill(int i) {
        int j = i - this.minY;

        for (int k = 0; k < this.heightmap.getSize(); ++k) {
            this.heightmap.set(k, j);
        }

    }

    private void set(int i, int j) {
        this.heightmap.set(i, j - this.minY);
    }

    private int get(int i) {
        return this.heightmap.get(i) + this.minY;
    }

    private int extendSourcesBelowWorld(int i) {
        return i == this.minY ? Integer.MIN_VALUE : i;
    }

    private static int index(int i, int j) {
        return i + j * 16;
    }
}
