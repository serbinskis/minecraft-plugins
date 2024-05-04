package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.IBlockAccess;
import org.jetbrains.annotations.Nullable;

public class PathTypeCache {

    private static final int SIZE = 4096;
    private static final int MASK = 4095;
    private final long[] positions = new long[4096];
    private final PathType[] pathTypes = new PathType[4096];

    public PathTypeCache() {}

    public PathType getOrCompute(IBlockAccess iblockaccess, BlockPosition blockposition) {
        long i = blockposition.asLong();
        int j = index(i);
        PathType pathtype = this.get(j, i);

        return pathtype != null ? pathtype : this.compute(iblockaccess, blockposition, j, i);
    }

    private @Nullable PathType get(int i, long j) {
        return this.positions[i] == j ? this.pathTypes[i] : null;
    }

    private PathType compute(IBlockAccess iblockaccess, BlockPosition blockposition, int i, long j) {
        PathType pathtype = PathfinderNormal.getPathTypeFromState(iblockaccess, blockposition);

        this.positions[i] = j;
        this.pathTypes[i] = pathtype;
        return pathtype;
    }

    public void invalidate(BlockPosition blockposition) {
        long i = blockposition.asLong();
        int j = index(i);

        if (this.positions[j] == i) {
            this.pathTypes[j] = null;
        }

    }

    private static int index(long i) {
        return (int) HashCommon.mix(i) & 4095;
    }
}
