package net.minecraft.world.level.block;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.event.block.BlockFormEvent;
// CraftBukkit end

public class BlockConcretePowder extends BlockFalling {

    private final IBlockData concrete;

    public BlockConcretePowder(Block block, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.concrete = block.defaultBlockState();
    }

    @Override
    public void onLand(World world, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, EntityFallingBlock entityfallingblock) {
        if (shouldSolidify(world, blockposition, iblockdata1)) {
            org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockFormEvent(world, blockposition, this.concrete, 3); // CraftBukkit
        }

    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);

        // CraftBukkit start
        if (!shouldSolidify(world, blockposition, iblockdata)) {
            return super.getStateForPlacement(blockactioncontext);
        }

        // TODO: An event factory call for methods like this
        CraftBlockState blockState = CraftBlockStates.getBlockState(world, blockposition);
        blockState.setData(this.concrete);

        BlockFormEvent event = new BlockFormEvent(blockState.getBlock(), blockState);
        world.getServer().server.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            return blockState.getHandle();
        }

        return super.getStateForPlacement(blockactioncontext);
        // CraftBukkit end
    }

    private static boolean shouldSolidify(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        return canSolidify(iblockdata) || touchesLiquid(iblockaccess, blockposition);
    }

    private static boolean touchesLiquid(IBlockAccess iblockaccess, BlockPosition blockposition) {
        boolean flag = false;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];
            IBlockData iblockdata = iblockaccess.getBlockState(blockposition_mutableblockposition);

            if (enumdirection != EnumDirection.DOWN || canSolidify(iblockdata)) {
                blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection);
                iblockdata = iblockaccess.getBlockState(blockposition_mutableblockposition);
                if (canSolidify(iblockdata) && !iblockdata.isFaceSturdy(iblockaccess, blockposition, enumdirection.getOpposite())) {
                    flag = true;
                    break;
                }
            }
        }

        return flag;
    }

    private static boolean canSolidify(IBlockData iblockdata) {
        return iblockdata.getFluidState().is(TagsFluid.WATER);
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        // CraftBukkit start
        if (touchesLiquid(generatoraccess, blockposition)) {
            // Suppress during worldgen
            if (!(generatoraccess instanceof World)) {
                return this.concrete;
            }
            CraftBlockState blockState = CraftBlockStates.getBlockState(generatoraccess, blockposition);
            blockState.setData(this.concrete);

            BlockFormEvent event = new BlockFormEvent(blockState.getBlock(), blockState);
            ((World) generatoraccess).getCraftServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                return blockState.getHandle();
            }
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
        // CraftBukkit end
    }

    @Override
    public int getDustColor(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.getMapColor(iblockaccess, blockposition).col;
    }
}
