package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.NibbleArray;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public abstract class LightEngine<M extends LightEngineStorageArray<M>, S extends LightEngineStorage<M>> implements LightEngineLayerEventListener {

    public static final int MAX_LEVEL = 15;
    protected static final int MIN_OPACITY = 1;
    protected static final long PULL_LIGHT_IN_ENTRY = LightEngine.a.decreaseAllDirections(1);
    private static final int MIN_QUEUE_SIZE = 512;
    protected static final EnumDirection[] PROPAGATION_DIRECTIONS = EnumDirection.values();
    protected final ILightAccess chunkSource;
    protected final S storage;
    private final LongOpenHashSet blockNodesToCheck = new LongOpenHashSet(512, 0.5F);
    private final LongArrayFIFOQueue decreaseQueue = new LongArrayFIFOQueue();
    private final LongArrayFIFOQueue increaseQueue = new LongArrayFIFOQueue();
    private final BlockPosition.MutableBlockPosition mutablePos = new BlockPosition.MutableBlockPosition();
    private static final int CACHE_SIZE = 2;
    private final long[] lastChunkPos = new long[2];
    private final LightChunk[] lastChunk = new LightChunk[2];

    protected LightEngine(ILightAccess ilightaccess, S s0) {
        this.chunkSource = ilightaccess;
        this.storage = s0;
        this.clearChunkCache();
    }

    public static boolean hasDifferentLightProperties(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1) {
        return iblockdata1 == iblockdata ? false : iblockdata1.getLightBlock(iblockaccess, blockposition) != iblockdata.getLightBlock(iblockaccess, blockposition) || iblockdata1.getLightEmission() != iblockdata.getLightEmission() || iblockdata1.useShapeForLightOcclusion() || iblockdata.useShapeForLightOcclusion();
    }

    public static int getLightBlockInto(IBlockAccess iblockaccess, IBlockData iblockdata, BlockPosition blockposition, IBlockData iblockdata1, BlockPosition blockposition1, EnumDirection enumdirection, int i) {
        boolean flag = isEmptyShape(iblockdata);
        boolean flag1 = isEmptyShape(iblockdata1);

        if (flag && flag1) {
            return i;
        } else {
            VoxelShape voxelshape = flag ? VoxelShapes.empty() : iblockdata.getOcclusionShape(iblockaccess, blockposition);
            VoxelShape voxelshape1 = flag1 ? VoxelShapes.empty() : iblockdata1.getOcclusionShape(iblockaccess, blockposition1);

            return VoxelShapes.mergedFaceOccludes(voxelshape, voxelshape1, enumdirection) ? 16 : i;
        }
    }

    public static VoxelShape getOcclusionShape(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, EnumDirection enumdirection) {
        return isEmptyShape(iblockdata) ? VoxelShapes.empty() : iblockdata.getFaceOcclusionShape(iblockaccess, blockposition, enumdirection);
    }

    protected static boolean isEmptyShape(IBlockData iblockdata) {
        return !iblockdata.canOcclude() || !iblockdata.useShapeForLightOcclusion();
    }

    protected IBlockData getState(BlockPosition blockposition) {
        int i = SectionPosition.blockToSectionCoord(blockposition.getX());
        int j = SectionPosition.blockToSectionCoord(blockposition.getZ());
        LightChunk lightchunk = this.getChunk(i, j);

        return lightchunk == null ? Blocks.BEDROCK.defaultBlockState() : lightchunk.getBlockState(blockposition);
    }

    protected int getOpacity(IBlockData iblockdata, BlockPosition blockposition) {
        return Math.max(1, iblockdata.getLightBlock(this.chunkSource.getLevel(), blockposition));
    }

    protected boolean shapeOccludes(long i, IBlockData iblockdata, long j, IBlockData iblockdata1, EnumDirection enumdirection) {
        VoxelShape voxelshape = this.getOcclusionShape(iblockdata, i, enumdirection);
        VoxelShape voxelshape1 = this.getOcclusionShape(iblockdata1, j, enumdirection.getOpposite());

        return VoxelShapes.faceShapeOccludes(voxelshape, voxelshape1);
    }

    protected VoxelShape getOcclusionShape(IBlockData iblockdata, long i, EnumDirection enumdirection) {
        return getOcclusionShape(this.chunkSource.getLevel(), this.mutablePos.set(i), iblockdata, enumdirection);
    }

    @Nullable
    protected LightChunk getChunk(int i, int j) {
        long k = ChunkCoordIntPair.asLong(i, j);

        for (int l = 0; l < 2; ++l) {
            if (k == this.lastChunkPos[l]) {
                return this.lastChunk[l];
            }
        }

        LightChunk lightchunk = this.chunkSource.getChunkForLighting(i, j);

        for (int i1 = 1; i1 > 0; --i1) {
            this.lastChunkPos[i1] = this.lastChunkPos[i1 - 1];
            this.lastChunk[i1] = this.lastChunk[i1 - 1];
        }

        this.lastChunkPos[0] = k;
        this.lastChunk[0] = lightchunk;
        return lightchunk;
    }

    private void clearChunkCache() {
        Arrays.fill(this.lastChunkPos, ChunkCoordIntPair.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunk, (Object) null);
    }

    @Override
    public void checkBlock(BlockPosition blockposition) {
        this.blockNodesToCheck.add(blockposition.asLong());
    }

    public void queueSectionData(long i, @Nullable NibbleArray nibblearray) {
        this.storage.queueSectionData(i, nibblearray);
    }

    public void retainData(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.storage.retainData(SectionPosition.getZeroNode(chunkcoordintpair.x, chunkcoordintpair.z), flag);
    }

    @Override
    public void updateSectionStatus(SectionPosition sectionposition, boolean flag) {
        this.storage.updateSectionStatus(sectionposition.asLong(), flag);
    }

    @Override
    public void setLightEnabled(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.storage.setLightEnabled(SectionPosition.getZeroNode(chunkcoordintpair.x, chunkcoordintpair.z), flag);
    }

    @Override
    public int runLightUpdates() {
        LongIterator longiterator = this.blockNodesToCheck.iterator();

        while (longiterator.hasNext()) {
            this.checkNode(longiterator.nextLong());
        }

        this.blockNodesToCheck.clear();
        this.blockNodesToCheck.trim(512);
        byte b0 = 0;
        int i = b0 + this.propagateDecreases();

        i += this.propagateIncreases();
        this.clearChunkCache();
        this.storage.markNewInconsistencies(this);
        this.storage.swapSectionMap();
        return i;
    }

    private int propagateIncreases() {
        int i;

        for (i = 0; !this.increaseQueue.isEmpty(); ++i) {
            long j = this.increaseQueue.dequeueLong();
            long k = this.increaseQueue.dequeueLong();
            int l = this.storage.getStoredLevel(j);
            int i1 = LightEngine.a.getFromLevel(k);

            if (LightEngine.a.isIncreaseFromEmission(k) && l < i1) {
                this.storage.setStoredLevel(j, i1);
                l = i1;
            }

            if (l == i1) {
                this.propagateIncrease(j, k, l);
            }
        }

        return i;
    }

    private int propagateDecreases() {
        int i;

        for (i = 0; !this.decreaseQueue.isEmpty(); ++i) {
            long j = this.decreaseQueue.dequeueLong();
            long k = this.decreaseQueue.dequeueLong();

            this.propagateDecrease(j, k);
        }

        return i;
    }

    protected void enqueueDecrease(long i, long j) {
        this.decreaseQueue.enqueue(i);
        this.decreaseQueue.enqueue(j);
    }

    protected void enqueueIncrease(long i, long j) {
        this.increaseQueue.enqueue(i);
        this.increaseQueue.enqueue(j);
    }

    @Override
    public boolean hasLightWork() {
        return this.storage.hasInconsistencies() || !this.blockNodesToCheck.isEmpty() || !this.decreaseQueue.isEmpty() || !this.increaseQueue.isEmpty();
    }

    @Nullable
    @Override
    public NibbleArray getDataLayerData(SectionPosition sectionposition) {
        return this.storage.getDataLayerData(sectionposition.asLong());
    }

    @Override
    public int getLightValue(BlockPosition blockposition) {
        return this.storage.getLightValue(blockposition.asLong());
    }

    public String getDebugData(long i) {
        return this.getDebugSectionType(i).display();
    }

    public LightEngineStorage.b getDebugSectionType(long i) {
        return this.storage.getDebugSectionType(i);
    }

    protected abstract void checkNode(long i);

    protected abstract void propagateIncrease(long i, long j, int k);

    protected abstract void propagateDecrease(long i, long j);

    public static class a {

        private static final int FROM_LEVEL_BITS = 4;
        private static final int DIRECTION_BITS = 6;
        private static final long LEVEL_MASK = 15L;
        private static final long DIRECTIONS_MASK = 1008L;
        private static final long FLAG_FROM_EMPTY_SHAPE = 1024L;
        private static final long FLAG_INCREASE_FROM_EMISSION = 2048L;

        public a() {}

        public static long decreaseSkipOneDirection(int i, EnumDirection enumdirection) {
            long j = withoutDirection(1008L, enumdirection);

            return withLevel(j, i);
        }

        public static long decreaseAllDirections(int i) {
            return withLevel(1008L, i);
        }

        public static long increaseLightFromEmission(int i, boolean flag) {
            long j = 1008L;

            j |= 2048L;
            if (flag) {
                j |= 1024L;
            }

            return withLevel(j, i);
        }

        public static long increaseSkipOneDirection(int i, boolean flag, EnumDirection enumdirection) {
            long j = withoutDirection(1008L, enumdirection);

            if (flag) {
                j |= 1024L;
            }

            return withLevel(j, i);
        }

        public static long increaseOnlyOneDirection(int i, boolean flag, EnumDirection enumdirection) {
            long j = 0L;

            if (flag) {
                j |= 1024L;
            }

            j = withDirection(j, enumdirection);
            return withLevel(j, i);
        }

        public static long increaseSkySourceInDirections(boolean flag, boolean flag1, boolean flag2, boolean flag3, boolean flag4) {
            long i = withLevel(0L, 15);

            if (flag) {
                i = withDirection(i, EnumDirection.DOWN);
            }

            if (flag1) {
                i = withDirection(i, EnumDirection.NORTH);
            }

            if (flag2) {
                i = withDirection(i, EnumDirection.SOUTH);
            }

            if (flag3) {
                i = withDirection(i, EnumDirection.WEST);
            }

            if (flag4) {
                i = withDirection(i, EnumDirection.EAST);
            }

            return i;
        }

        public static int getFromLevel(long i) {
            return (int) (i & 15L);
        }

        public static boolean isFromEmptyShape(long i) {
            return (i & 1024L) != 0L;
        }

        public static boolean isIncreaseFromEmission(long i) {
            return (i & 2048L) != 0L;
        }

        public static boolean shouldPropagateInDirection(long i, EnumDirection enumdirection) {
            return (i & 1L << enumdirection.ordinal() + 4) != 0L;
        }

        private static long withLevel(long i, int j) {
            return i & -16L | (long) j & 15L;
        }

        private static long withDirection(long i, EnumDirection enumdirection) {
            return i | 1L << enumdirection.ordinal() + 4;
        }

        private static long withoutDirection(long i, EnumDirection enumdirection) {
            return i & ~(1L << enumdirection.ordinal() + 4);
        }
    }
}
