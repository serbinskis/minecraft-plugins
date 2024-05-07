package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.level.ChunkCache;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public abstract class PathfinderAbstract {

    protected PathfindingContext currentContext;
    protected EntityInsentient mob;
    protected final Int2ObjectMap<PathPoint> nodes = new Int2ObjectOpenHashMap();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors;
    protected boolean canOpenDoors;
    protected boolean canFloat;
    protected boolean canWalkOverFences;

    public PathfinderAbstract() {}

    public void prepare(ChunkCache chunkcache, EntityInsentient entityinsentient) {
        this.currentContext = new PathfindingContext(chunkcache, entityinsentient);
        this.mob = entityinsentient;
        this.nodes.clear();
        this.entityWidth = MathHelper.floor(entityinsentient.getBbWidth() + 1.0F);
        this.entityHeight = MathHelper.floor(entityinsentient.getBbHeight() + 1.0F);
        this.entityDepth = MathHelper.floor(entityinsentient.getBbWidth() + 1.0F);
    }

    public void done() {
        this.currentContext = null;
        this.mob = null;
    }

    protected PathPoint getNode(BlockPosition blockposition) {
        return this.getNode(blockposition.getX(), blockposition.getY(), blockposition.getZ());
    }

    protected PathPoint getNode(int i, int j, int k) {
        return (PathPoint) this.nodes.computeIfAbsent(PathPoint.createHash(i, j, k), (l) -> {
            return new PathPoint(i, j, k);
        });
    }

    public abstract PathPoint getStart();

    public abstract PathDestination getTarget(double d0, double d1, double d2);

    protected PathDestination getTargetNodeAt(double d0, double d1, double d2) {
        return new PathDestination(this.getNode(MathHelper.floor(d0), MathHelper.floor(d1), MathHelper.floor(d2)));
    }

    public abstract int getNeighbors(PathPoint[] apathpoint, PathPoint pathpoint);

    public abstract PathType getPathTypeOfMob(PathfindingContext pathfindingcontext, int i, int j, int k, EntityInsentient entityinsentient);

    public abstract PathType getPathType(PathfindingContext pathfindingcontext, int i, int j, int k);

    public PathType getPathType(EntityInsentient entityinsentient, BlockPosition blockposition) {
        return this.getPathType(new PathfindingContext(entityinsentient.level(), entityinsentient), blockposition.getX(), blockposition.getY(), blockposition.getZ());
    }

    public void setCanPassDoors(boolean flag) {
        this.canPassDoors = flag;
    }

    public void setCanOpenDoors(boolean flag) {
        this.canOpenDoors = flag;
    }

    public void setCanFloat(boolean flag) {
        this.canFloat = flag;
    }

    public void setCanWalkOverFences(boolean flag) {
        this.canWalkOverFences = flag;
    }

    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }

    public boolean canWalkOverFences() {
        return this.canWalkOverFences;
    }

    public static boolean isBurningBlock(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.FIRE) || iblockdata.is(Blocks.LAVA) || iblockdata.is(Blocks.MAGMA_BLOCK) || BlockCampfire.isLitCampfire(iblockdata) || iblockdata.is(Blocks.LAVA_CAULDRON);
    }
}
