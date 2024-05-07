package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;

public class PathfindingContext {

    private final ICollisionAccess level;
    @Nullable
    private final PathTypeCache cache;
    private final BlockPosition mobPosition;
    private final BlockPosition.MutableBlockPosition mutablePos = new BlockPosition.MutableBlockPosition();

    public PathfindingContext(ICollisionAccess icollisionaccess, EntityInsentient entityinsentient) {
        this.level = icollisionaccess;
        World world = entityinsentient.level();

        if (world instanceof WorldServer worldserver) {
            this.cache = worldserver.getPathTypeCache();
        } else {
            this.cache = null;
        }

        this.mobPosition = entityinsentient.blockPosition();
    }

    public PathType getPathTypeFromState(int i, int j, int k) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = this.mutablePos.set(i, j, k);

        return this.cache == null ? PathfinderNormal.getPathTypeFromState(this.level, blockposition_mutableblockposition) : this.cache.getOrCompute(this.level, blockposition_mutableblockposition);
    }

    public IBlockData getBlockState(BlockPosition blockposition) {
        return this.level.getBlockState(blockposition);
    }

    public ICollisionAccess level() {
        return this.level;
    }

    public BlockPosition mobPosition() {
        return this.mobPosition;
    }
}
