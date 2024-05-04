package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.TileInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerLoom;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockLoom extends BlockFacingHorizontal {

    public static final MapCodec<BlockLoom> CODEC = simpleCodec(BlockLoom::new);
    private static final IChatBaseComponent CONTAINER_TITLE = IChatBaseComponent.translatable("container.loom");

    @Override
    public MapCodec<BlockLoom> codec() {
        return BlockLoom.CODEC;
    }

    protected BlockLoom(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            entityhuman.openMenu(iblockdata.getMenuProvider(world, blockposition));
            entityhuman.awardStat(StatisticList.INTERACT_WITH_LOOM);
            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    protected ITileInventory getMenuProvider(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return new TileInventory((i, playerinventory, entityhuman) -> {
            return new ContainerLoom(i, playerinventory, ContainerAccess.create(world, blockposition));
        }, BlockLoom.CONTAINER_TITLE);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockLoom.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockLoom.FACING);
    }
}
