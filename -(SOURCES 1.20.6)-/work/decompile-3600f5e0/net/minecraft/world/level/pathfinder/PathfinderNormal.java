package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.level.ChunkCache;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockDoor;
import net.minecraft.world.level.block.BlockFenceGate;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.BlockMinecartTrackAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathfinderNormal extends PathfinderAbstract {

    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5D;
    private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125D;
    private final Long2ObjectMap<PathType> pathTypesByPosCacheByMob = new Long2ObjectOpenHashMap();
    private final Object2BooleanMap<AxisAlignedBB> collisionCache = new Object2BooleanOpenHashMap();
    private final PathPoint[] reusableNeighbors;

    public PathfinderNormal() {
        this.reusableNeighbors = new PathPoint[EnumDirection.EnumDirectionLimit.HORIZONTAL.length()];
    }

    @Override
    public void prepare(ChunkCache chunkcache, EntityInsentient entityinsentient) {
        super.prepare(chunkcache, entityinsentient);
        entityinsentient.onPathfindingStart();
    }

    @Override
    public void done() {
        this.mob.onPathfindingDone();
        this.pathTypesByPosCacheByMob.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public PathPoint getStart() {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        int i = this.mob.getBlockY();
        IBlockData iblockdata = this.currentContext.getBlockState(blockposition_mutableblockposition.set(this.mob.getX(), (double) i, this.mob.getZ()));

        if (this.mob.canStandOnFluid(iblockdata.getFluidState())) {
            while (this.mob.canStandOnFluid(iblockdata.getFluidState())) {
                ++i;
                iblockdata = this.currentContext.getBlockState(blockposition_mutableblockposition.set(this.mob.getX(), (double) i, this.mob.getZ()));
            }

            --i;
        } else if (this.canFloat() && this.mob.isInWater()) {
            while (iblockdata.is(Blocks.WATER) || iblockdata.getFluidState() == FluidTypes.WATER.getSource(false)) {
                ++i;
                iblockdata = this.currentContext.getBlockState(blockposition_mutableblockposition.set(this.mob.getX(), (double) i, this.mob.getZ()));
            }

            --i;
        } else if (this.mob.onGround()) {
            i = MathHelper.floor(this.mob.getY() + 0.5D);
        } else {
            blockposition_mutableblockposition.set(this.mob.getX(), this.mob.getY() + 1.0D, this.mob.getZ());

            while (blockposition_mutableblockposition.getY() > this.currentContext.level().getMinBuildHeight()) {
                i = blockposition_mutableblockposition.getY();
                blockposition_mutableblockposition.setY(blockposition_mutableblockposition.getY() - 1);
                IBlockData iblockdata1 = this.currentContext.getBlockState(blockposition_mutableblockposition);

                if (!iblockdata1.isAir() && !iblockdata1.isPathfindable(PathMode.LAND)) {
                    break;
                }
            }
        }

        BlockPosition blockposition = this.mob.blockPosition();

        if (!this.canStartAt(blockposition_mutableblockposition.set(blockposition.getX(), i, blockposition.getZ()))) {
            AxisAlignedBB axisalignedbb = this.mob.getBoundingBox();

            if (this.canStartAt(blockposition_mutableblockposition.set(axisalignedbb.minX, (double) i, axisalignedbb.minZ)) || this.canStartAt(blockposition_mutableblockposition.set(axisalignedbb.minX, (double) i, axisalignedbb.maxZ)) || this.canStartAt(blockposition_mutableblockposition.set(axisalignedbb.maxX, (double) i, axisalignedbb.minZ)) || this.canStartAt(blockposition_mutableblockposition.set(axisalignedbb.maxX, (double) i, axisalignedbb.maxZ))) {
                return this.getStartNode(blockposition_mutableblockposition);
            }
        }

        return this.getStartNode(new BlockPosition(blockposition.getX(), i, blockposition.getZ()));
    }

    protected PathPoint getStartNode(BlockPosition blockposition) {
        PathPoint pathpoint = this.getNode(blockposition);

        pathpoint.type = this.getCachedPathType(pathpoint.x, pathpoint.y, pathpoint.z);
        pathpoint.costMalus = this.mob.getPathfindingMalus(pathpoint.type);
        return pathpoint;
    }

    protected boolean canStartAt(BlockPosition blockposition) {
        PathType pathtype = this.getCachedPathType(blockposition.getX(), blockposition.getY(), blockposition.getZ());

        return pathtype != PathType.OPEN && this.mob.getPathfindingMalus(pathtype) >= 0.0F;
    }

    @Override
    public PathDestination getTarget(double d0, double d1, double d2) {
        return this.getTargetNodeAt(d0, d1, d2);
    }

    @Override
    public int getNeighbors(PathPoint[] apathpoint, PathPoint pathpoint) {
        int i = 0;
        int j = 0;
        PathType pathtype = this.getCachedPathType(pathpoint.x, pathpoint.y + 1, pathpoint.z);
        PathType pathtype1 = this.getCachedPathType(pathpoint.x, pathpoint.y, pathpoint.z);

        if (this.mob.getPathfindingMalus(pathtype) >= 0.0F && pathtype1 != PathType.STICKY_HONEY) {
            j = MathHelper.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }

        double d0 = this.getFloorLevel(new BlockPosition(pathpoint.x, pathpoint.y, pathpoint.z));
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        EnumDirection enumdirection;

        while (iterator.hasNext()) {
            enumdirection = (EnumDirection) iterator.next();
            PathPoint pathpoint1 = this.findAcceptedNode(pathpoint.x + enumdirection.getStepX(), pathpoint.y, pathpoint.z + enumdirection.getStepZ(), j, d0, enumdirection, pathtype1);

            this.reusableNeighbors[enumdirection.get2DDataValue()] = pathpoint1;
            if (this.isNeighborValid(pathpoint1, pathpoint)) {
                apathpoint[i++] = pathpoint1;
            }
        }

        iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            enumdirection = (EnumDirection) iterator.next();
            EnumDirection enumdirection1 = enumdirection.getClockWise();

            if (this.isDiagonalValid(pathpoint, this.reusableNeighbors[enumdirection.get2DDataValue()], this.reusableNeighbors[enumdirection1.get2DDataValue()])) {
                PathPoint pathpoint2 = this.findAcceptedNode(pathpoint.x + enumdirection.getStepX() + enumdirection1.getStepX(), pathpoint.y, pathpoint.z + enumdirection.getStepZ() + enumdirection1.getStepZ(), j, d0, enumdirection, pathtype1);

                if (this.isDiagonalValid(pathpoint2)) {
                    apathpoint[i++] = pathpoint2;
                }
            }
        }

        return i;
    }

    protected boolean isNeighborValid(@Nullable PathPoint pathpoint, PathPoint pathpoint1) {
        return pathpoint != null && !pathpoint.closed && (pathpoint.costMalus >= 0.0F || pathpoint1.costMalus < 0.0F);
    }

    protected boolean isDiagonalValid(PathPoint pathpoint, @Nullable PathPoint pathpoint1, @Nullable PathPoint pathpoint2) {
        if (pathpoint2 != null && pathpoint1 != null && pathpoint2.y <= pathpoint.y && pathpoint1.y <= pathpoint.y) {
            if (pathpoint1.type != PathType.WALKABLE_DOOR && pathpoint2.type != PathType.WALKABLE_DOOR) {
                boolean flag = pathpoint2.type == PathType.FENCE && pathpoint1.type == PathType.FENCE && (double) this.mob.getBbWidth() < 0.5D;

                return (pathpoint2.y < pathpoint.y || pathpoint2.costMalus >= 0.0F || flag) && (pathpoint1.y < pathpoint.y || pathpoint1.costMalus >= 0.0F || flag);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected boolean isDiagonalValid(@Nullable PathPoint pathpoint) {
        return pathpoint != null && !pathpoint.closed ? (pathpoint.type == PathType.WALKABLE_DOOR ? false : pathpoint.costMalus >= 0.0F) : false;
    }

    private static boolean doesBlockHavePartialCollision(PathType pathtype) {
        return pathtype == PathType.FENCE || pathtype == PathType.DOOR_WOOD_CLOSED || pathtype == PathType.DOOR_IRON_CLOSED;
    }

    private boolean canReachWithoutCollision(PathPoint pathpoint) {
        AxisAlignedBB axisalignedbb = this.mob.getBoundingBox();
        Vec3D vec3d = new Vec3D((double) pathpoint.x - this.mob.getX() + axisalignedbb.getXsize() / 2.0D, (double) pathpoint.y - this.mob.getY() + axisalignedbb.getYsize() / 2.0D, (double) pathpoint.z - this.mob.getZ() + axisalignedbb.getZsize() / 2.0D);
        int i = MathHelper.ceil(vec3d.length() / axisalignedbb.getSize());

        vec3d = vec3d.scale((double) (1.0F / (float) i));

        for (int j = 1; j <= i; ++j) {
            axisalignedbb = axisalignedbb.move(vec3d);
            if (this.hasCollisions(axisalignedbb)) {
                return false;
            }
        }

        return true;
    }

    protected double getFloorLevel(BlockPosition blockposition) {
        ICollisionAccess icollisionaccess = this.currentContext.level();

        return (this.canFloat() || this.isAmphibious()) && icollisionaccess.getFluidState(blockposition).is(TagsFluid.WATER) ? (double) blockposition.getY() + 0.5D : getFloorLevel(icollisionaccess, blockposition);
    }

    public static double getFloorLevel(IBlockAccess iblockaccess, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();
        VoxelShape voxelshape = iblockaccess.getBlockState(blockposition1).getCollisionShape(iblockaccess, blockposition1);

        return (double) blockposition1.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(EnumDirection.EnumAxis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected PathPoint findAcceptedNode(int i, int j, int k, int l, double d0, EnumDirection enumdirection, PathType pathtype) {
        PathPoint pathpoint = null;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        double d1 = this.getFloorLevel(blockposition_mutableblockposition.set(i, j, k));

        if (d1 - d0 > this.getMobJumpHeight()) {
            return null;
        } else {
            PathType pathtype1 = this.getCachedPathType(i, j, k);
            float f = this.mob.getPathfindingMalus(pathtype1);

            if (f >= 0.0F) {
                pathpoint = this.getNodeAndUpdateCostToMax(i, j, k, pathtype1, f);
            }

            if (doesBlockHavePartialCollision(pathtype) && pathpoint != null && pathpoint.costMalus >= 0.0F && !this.canReachWithoutCollision(pathpoint)) {
                pathpoint = null;
            }

            if (pathtype1 != PathType.WALKABLE && (!this.isAmphibious() || pathtype1 != PathType.WATER)) {
                if ((pathpoint == null || pathpoint.costMalus < 0.0F) && l > 0 && (pathtype1 != PathType.FENCE || this.canWalkOverFences()) && pathtype1 != PathType.UNPASSABLE_RAIL && pathtype1 != PathType.TRAPDOOR && pathtype1 != PathType.POWDER_SNOW) {
                    pathpoint = this.tryJumpOn(i, j, k, l, d0, enumdirection, pathtype, blockposition_mutableblockposition);
                } else if (!this.isAmphibious() && pathtype1 == PathType.WATER && !this.canFloat()) {
                    pathpoint = this.tryFindFirstNonWaterBelow(i, j, k, pathpoint);
                } else if (pathtype1 == PathType.OPEN) {
                    pathpoint = this.tryFindFirstGroundNodeBelow(i, j, k);
                } else if (doesBlockHavePartialCollision(pathtype1) && pathpoint == null) {
                    pathpoint = this.getClosedNode(i, j, k, pathtype1);
                }

                return pathpoint;
            } else {
                return pathpoint;
            }
        }
    }

    private double getMobJumpHeight() {
        return Math.max(1.125D, (double) this.mob.maxUpStep());
    }

    private PathPoint getNodeAndUpdateCostToMax(int i, int j, int k, PathType pathtype, float f) {
        PathPoint pathpoint = this.getNode(i, j, k);

        pathpoint.type = pathtype;
        pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
        return pathpoint;
    }

    private PathPoint getBlockedNode(int i, int j, int k) {
        PathPoint pathpoint = this.getNode(i, j, k);

        pathpoint.type = PathType.BLOCKED;
        pathpoint.costMalus = -1.0F;
        return pathpoint;
    }

    private PathPoint getClosedNode(int i, int j, int k, PathType pathtype) {
        PathPoint pathpoint = this.getNode(i, j, k);

        pathpoint.closed = true;
        pathpoint.type = pathtype;
        pathpoint.costMalus = pathtype.getMalus();
        return pathpoint;
    }

    @Nullable
    private PathPoint tryJumpOn(int i, int j, int k, int l, double d0, EnumDirection enumdirection, PathType pathtype, BlockPosition.MutableBlockPosition blockposition_mutableblockposition) {
        PathPoint pathpoint = this.findAcceptedNode(i, j + 1, k, l - 1, d0, enumdirection, pathtype);

        if (pathpoint == null) {
            return null;
        } else if (this.mob.getBbWidth() >= 1.0F) {
            return pathpoint;
        } else if (pathpoint.type != PathType.OPEN && pathpoint.type != PathType.WALKABLE) {
            return pathpoint;
        } else {
            double d1 = (double) (i - enumdirection.getStepX()) + 0.5D;
            double d2 = (double) (k - enumdirection.getStepZ()) + 0.5D;
            double d3 = (double) this.mob.getBbWidth() / 2.0D;
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(d1 - d3, this.getFloorLevel(blockposition_mutableblockposition.set(d1, (double) (j + 1), d2)) + 0.001D, d2 - d3, d1 + d3, (double) this.mob.getBbHeight() + this.getFloorLevel(blockposition_mutableblockposition.set((double) pathpoint.x, (double) pathpoint.y, (double) pathpoint.z)) - 0.002D, d2 + d3);

            return this.hasCollisions(axisalignedbb) ? null : pathpoint;
        }
    }

    @Nullable
    private PathPoint tryFindFirstNonWaterBelow(int i, int j, int k, @Nullable PathPoint pathpoint) {
        --j;

        while (j > this.mob.level().getMinBuildHeight()) {
            PathType pathtype = this.getCachedPathType(i, j, k);

            if (pathtype != PathType.WATER) {
                return pathpoint;
            }

            pathpoint = this.getNodeAndUpdateCostToMax(i, j, k, pathtype, this.mob.getPathfindingMalus(pathtype));
            --j;
        }

        return pathpoint;
    }

    private PathPoint tryFindFirstGroundNodeBelow(int i, int j, int k) {
        for (int l = j - 1; l >= this.mob.level().getMinBuildHeight(); --l) {
            if (j - l > this.mob.getMaxFallDistance()) {
                return this.getBlockedNode(i, l, k);
            }

            PathType pathtype = this.getCachedPathType(i, l, k);
            float f = this.mob.getPathfindingMalus(pathtype);

            if (pathtype != PathType.OPEN) {
                if (f >= 0.0F) {
                    return this.getNodeAndUpdateCostToMax(i, l, k, pathtype, f);
                }

                return this.getBlockedNode(i, l, k);
            }
        }

        return this.getBlockedNode(i, j, k);
    }

    private boolean hasCollisions(AxisAlignedBB axisalignedbb) {
        return this.collisionCache.computeIfAbsent(axisalignedbb, (object) -> {
            return !this.currentContext.level().noCollision(this.mob, axisalignedbb);
        });
    }

    protected PathType getCachedPathType(int i, int j, int k) {
        return (PathType) this.pathTypesByPosCacheByMob.computeIfAbsent(BlockPosition.asLong(i, j, k), (l) -> {
            return this.getPathTypeOfMob(this.currentContext, i, j, k, this.mob);
        });
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext pathfindingcontext, int i, int j, int k, EntityInsentient entityinsentient) {
        Set<PathType> set = this.getPathTypeWithinMobBB(pathfindingcontext, i, j, k);

        if (set.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (set.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType pathtype = PathType.BLOCKED;
            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                PathType pathtype1 = (PathType) iterator.next();

                if (entityinsentient.getPathfindingMalus(pathtype1) < 0.0F) {
                    return pathtype1;
                }

                if (entityinsentient.getPathfindingMalus(pathtype1) >= entityinsentient.getPathfindingMalus(pathtype)) {
                    pathtype = pathtype1;
                }
            }

            if (this.entityWidth <= 1 && pathtype != PathType.OPEN && entityinsentient.getPathfindingMalus(pathtype) == 0.0F && this.getPathType(pathfindingcontext, i, j, k) == PathType.OPEN) {
                return PathType.OPEN;
            } else {
                return pathtype;
            }
        }
    }

    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext pathfindingcontext, int i, int j, int k) {
        EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);

        for (int l = 0; l < this.entityWidth; ++l) {
            for (int i1 = 0; i1 < this.entityHeight; ++i1) {
                for (int j1 = 0; j1 < this.entityDepth; ++j1) {
                    int k1 = l + i;
                    int l1 = i1 + j;
                    int i2 = j1 + k;
                    PathType pathtype = this.getPathType(pathfindingcontext, k1, l1, i2);
                    BlockPosition blockposition = this.mob.blockPosition();
                    boolean flag = this.canPassDoors();

                    if (pathtype == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
                        pathtype = PathType.WALKABLE_DOOR;
                    }

                    if (pathtype == PathType.DOOR_OPEN && !flag) {
                        pathtype = PathType.BLOCKED;
                    }

                    if (pathtype == PathType.RAIL && this.getPathType(pathfindingcontext, blockposition.getX(), blockposition.getY(), blockposition.getZ()) != PathType.RAIL && this.getPathType(pathfindingcontext, blockposition.getX(), blockposition.getY() - 1, blockposition.getZ()) != PathType.RAIL) {
                        pathtype = PathType.UNPASSABLE_RAIL;
                    }

                    enumset.add(pathtype);
                }
            }
        }

        return enumset;
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingcontext, int i, int j, int k) {
        return getPathTypeStatic(pathfindingcontext, new BlockPosition.MutableBlockPosition(i, j, k));
    }

    public static PathType getPathTypeStatic(EntityInsentient entityinsentient, BlockPosition blockposition) {
        return getPathTypeStatic(new PathfindingContext(entityinsentient.level(), entityinsentient), blockposition.mutable());
    }

    public static PathType getPathTypeStatic(PathfindingContext pathfindingcontext, BlockPosition.MutableBlockPosition blockposition_mutableblockposition) {
        int i = blockposition_mutableblockposition.getX();
        int j = blockposition_mutableblockposition.getY();
        int k = blockposition_mutableblockposition.getZ();
        PathType pathtype = pathfindingcontext.getPathTypeFromState(i, j, k);

        if (pathtype == PathType.OPEN && j >= pathfindingcontext.level().getMinBuildHeight() + 1) {
            PathType pathtype1;

            switch (pathfindingcontext.getPathTypeFromState(i, j - 1, k)) {
                case OPEN:
                case WATER:
                case LAVA:
                case WALKABLE:
                    pathtype1 = PathType.OPEN;
                    break;
                case DAMAGE_FIRE:
                    pathtype1 = PathType.DAMAGE_FIRE;
                    break;
                case DAMAGE_OTHER:
                    pathtype1 = PathType.DAMAGE_OTHER;
                    break;
                case STICKY_HONEY:
                    pathtype1 = PathType.STICKY_HONEY;
                    break;
                case POWDER_SNOW:
                    pathtype1 = PathType.DANGER_POWDER_SNOW;
                    break;
                case DAMAGE_CAUTIOUS:
                    pathtype1 = PathType.DAMAGE_CAUTIOUS;
                    break;
                case TRAPDOOR:
                    pathtype1 = PathType.DANGER_TRAPDOOR;
                    break;
                default:
                    pathtype1 = checkNeighbourBlocks(pathfindingcontext, i, j, k, PathType.WALKABLE);
            }

            return pathtype1;
        } else {
            return pathtype;
        }
    }

    public static PathType checkNeighbourBlocks(PathfindingContext pathfindingcontext, int i, int j, int k, PathType pathtype) {
        for (int l = -1; l <= 1; ++l) {
            for (int i1 = -1; i1 <= 1; ++i1) {
                for (int j1 = -1; j1 <= 1; ++j1) {
                    if (l != 0 || j1 != 0) {
                        PathType pathtype1 = pathfindingcontext.getPathTypeFromState(i + l, j + i1, k + j1);

                        if (pathtype1 == PathType.DAMAGE_OTHER) {
                            return PathType.DANGER_OTHER;
                        }

                        if (pathtype1 == PathType.DAMAGE_FIRE || pathtype1 == PathType.LAVA) {
                            return PathType.DANGER_FIRE;
                        }

                        if (pathtype1 == PathType.WATER) {
                            return PathType.WATER_BORDER;
                        }

                        if (pathtype1 == PathType.DAMAGE_CAUTIOUS) {
                            return PathType.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }

        return pathtype;
    }

    protected static PathType getPathTypeFromState(IBlockAccess iblockaccess, BlockPosition blockposition) {
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition);
        Block block = iblockdata.getBlock();

        if (iblockdata.isAir()) {
            return PathType.OPEN;
        } else if (!iblockdata.is(TagsBlock.TRAPDOORS) && !iblockdata.is(Blocks.LILY_PAD) && !iblockdata.is(Blocks.BIG_DRIPLEAF)) {
            if (iblockdata.is(Blocks.POWDER_SNOW)) {
                return PathType.POWDER_SNOW;
            } else if (!iblockdata.is(Blocks.CACTUS) && !iblockdata.is(Blocks.SWEET_BERRY_BUSH)) {
                if (iblockdata.is(Blocks.HONEY_BLOCK)) {
                    return PathType.STICKY_HONEY;
                } else if (iblockdata.is(Blocks.COCOA)) {
                    return PathType.COCOA;
                } else if (!iblockdata.is(Blocks.WITHER_ROSE) && !iblockdata.is(Blocks.POINTED_DRIPSTONE)) {
                    Fluid fluid = iblockdata.getFluidState();

                    if (fluid.is(TagsFluid.LAVA)) {
                        return PathType.LAVA;
                    } else if (isBurningBlock(iblockdata)) {
                        return PathType.DAMAGE_FIRE;
                    } else if (block instanceof BlockDoor) {
                        BlockDoor blockdoor = (BlockDoor) block;

                        return (Boolean) iblockdata.getValue(BlockDoor.OPEN) ? PathType.DOOR_OPEN : (blockdoor.type().canOpenByHand() ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED);
                    } else {
                        return block instanceof BlockMinecartTrackAbstract ? PathType.RAIL : (block instanceof BlockLeaves ? PathType.LEAVES : (!iblockdata.is(TagsBlock.FENCES) && !iblockdata.is(TagsBlock.WALLS) && (!(block instanceof BlockFenceGate) || (Boolean) iblockdata.getValue(BlockFenceGate.OPEN)) ? (!iblockdata.isPathfindable(PathMode.LAND) ? PathType.BLOCKED : (fluid.is(TagsFluid.WATER) ? PathType.WATER : PathType.OPEN)) : PathType.FENCE));
                    }
                } else {
                    return PathType.DAMAGE_CAUTIOUS;
                }
            } else {
                return PathType.DAMAGE_OTHER;
            }
        } else {
            return PathType.TRAPDOOR;
        }
    }
}
