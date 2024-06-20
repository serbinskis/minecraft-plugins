package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.level.ChunkCache;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public class PathfinderWater extends PathfinderAbstract {

    private final boolean allowBreaching;
    private final Long2ObjectMap<PathType> pathTypesByPosCache = new Long2ObjectOpenHashMap();

    public PathfinderWater(boolean flag) {
        this.allowBreaching = flag;
    }

    @Override
    public void prepare(ChunkCache chunkcache, EntityInsentient entityinsentient) {
        super.prepare(chunkcache, entityinsentient);
        this.pathTypesByPosCache.clear();
    }

    @Override
    public void done() {
        super.done();
        this.pathTypesByPosCache.clear();
    }

    @Override
    public PathPoint getStart() {
        return this.getNode(MathHelper.floor(this.mob.getBoundingBox().minX), MathHelper.floor(this.mob.getBoundingBox().minY + 0.5D), MathHelper.floor(this.mob.getBoundingBox().minZ));
    }

    @Override
    public PathDestination getTarget(double d0, double d1, double d2) {
        return this.getTargetNodeAt(d0, d1, d2);
    }

    @Override
    public int getNeighbors(PathPoint[] apathpoint, PathPoint pathpoint) {
        int i = 0;
        Map<EnumDirection, PathPoint> map = Maps.newEnumMap(EnumDirection.class);
        EnumDirection[] aenumdirection = EnumDirection.values();
        int j = aenumdirection.length;

        for (int k = 0; k < j; ++k) {
            EnumDirection enumdirection = aenumdirection[k];
            PathPoint pathpoint1 = this.findAcceptedNode(pathpoint.x + enumdirection.getStepX(), pathpoint.y + enumdirection.getStepY(), pathpoint.z + enumdirection.getStepZ());

            map.put(enumdirection, pathpoint1);
            if (this.isNodeValid(pathpoint1)) {
                apathpoint[i++] = pathpoint1;
            }
        }

        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection1 = (EnumDirection) iterator.next();
            EnumDirection enumdirection2 = enumdirection1.getClockWise();

            if (hasMalus((PathPoint) map.get(enumdirection1)) && hasMalus((PathPoint) map.get(enumdirection2))) {
                PathPoint pathpoint2 = this.findAcceptedNode(pathpoint.x + enumdirection1.getStepX() + enumdirection2.getStepX(), pathpoint.y, pathpoint.z + enumdirection1.getStepZ() + enumdirection2.getStepZ());

                if (this.isNodeValid(pathpoint2)) {
                    apathpoint[i++] = pathpoint2;
                }
            }
        }

        return i;
    }

    protected boolean isNodeValid(@Nullable PathPoint pathpoint) {
        return pathpoint != null && !pathpoint.closed;
    }

    private static boolean hasMalus(@Nullable PathPoint pathpoint) {
        return pathpoint != null && pathpoint.costMalus >= 0.0F;
    }

    @Nullable
    protected PathPoint findAcceptedNode(int i, int j, int k) {
        PathPoint pathpoint = null;
        PathType pathtype = this.getCachedBlockType(i, j, k);

        if (this.allowBreaching && pathtype == PathType.BREACH || pathtype == PathType.WATER) {
            float f = this.mob.getPathfindingMalus(pathtype);

            if (f >= 0.0F) {
                pathpoint = this.getNode(i, j, k);
                pathpoint.type = pathtype;
                pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                if (this.currentContext.level().getFluidState(new BlockPosition(i, j, k)).isEmpty()) {
                    pathpoint.costMalus += 8.0F;
                }
            }
        }

        return pathpoint;
    }

    protected PathType getCachedBlockType(int i, int j, int k) {
        return (PathType) this.pathTypesByPosCache.computeIfAbsent(BlockPosition.asLong(i, j, k), (l) -> {
            return this.getPathType(this.currentContext, i, j, k);
        });
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingcontext, int i, int j, int k) {
        return this.getPathTypeOfMob(pathfindingcontext, i, j, k, this.mob);
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext pathfindingcontext, int i, int j, int k, EntityInsentient entityinsentient) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (int l = i; l < i + this.entityWidth; ++l) {
            for (int i1 = j; i1 < j + this.entityHeight; ++i1) {
                for (int j1 = k; j1 < k + this.entityDepth; ++j1) {
                    IBlockData iblockdata = pathfindingcontext.getBlockState(blockposition_mutableblockposition.set(l, i1, j1));
                    Fluid fluid = iblockdata.getFluidState();

                    if (fluid.isEmpty() && iblockdata.isPathfindable(PathMode.WATER) && iblockdata.isAir()) {
                        return PathType.BREACH;
                    }

                    if (!fluid.is(TagsFluid.WATER)) {
                        return PathType.BLOCKED;
                    }
                }
            }
        }

        IBlockData iblockdata1 = pathfindingcontext.getBlockState(blockposition_mutableblockposition);

        if (iblockdata1.isPathfindable(PathMode.WATER)) {
            return PathType.WATER;
        } else {
            return PathType.BLOCKED;
        }
    }
}
