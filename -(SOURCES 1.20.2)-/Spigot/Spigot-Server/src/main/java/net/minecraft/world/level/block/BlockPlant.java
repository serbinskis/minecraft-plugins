package net.minecraft.world.level.block;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathMode;

public class BlockPlant extends Block {

    protected BlockPlant(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.is(TagsBlock.DIRT) || iblockdata.is(Blocks.FARMLAND);
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        // CraftBukkit start
        if (!iblockdata.canSurvive(generatoraccess, blockposition)) {
            if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPhysicsEvent(generatoraccess, blockposition).isCancelled()) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
        // CraftBukkit end
    }

    @Override
    public boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();

        return this.mayPlaceOn(iworldreader.getBlockState(blockposition1), iworldreader, blockposition1);
    }

    @Override
    public boolean propagatesSkylightDown(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.getFluidState().isEmpty();
    }

    @Override
    public boolean isPathfindable(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, PathMode pathmode) {
        return pathmode == PathMode.AIR && !this.hasCollision ? true : super.isPathfindable(iblockdata, iblockaccess, blockposition, pathmode);
    }
}
