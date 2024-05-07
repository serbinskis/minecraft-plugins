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
import net.minecraft.world.inventory.ContainerWorkbench;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockWorkbench extends Block {

    public static final MapCodec<BlockWorkbench> CODEC = simpleCodec(BlockWorkbench::new);
    private static final IChatBaseComponent CONTAINER_TITLE = IChatBaseComponent.translatable("container.crafting");

    @Override
    public MapCodec<? extends BlockWorkbench> codec() {
        return BlockWorkbench.CODEC;
    }

    protected BlockWorkbench(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            entityhuman.openMenu(iblockdata.getMenuProvider(world, blockposition));
            entityhuman.awardStat(StatisticList.INTERACT_WITH_CRAFTING_TABLE);
            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    protected ITileInventory getMenuProvider(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return new TileInventory((i, playerinventory, entityhuman) -> {
            return new ContainerWorkbench(i, playerinventory, ContainerAccess.create(world, blockposition));
        }, BlockWorkbench.CONTAINER_TITLE);
    }
}
