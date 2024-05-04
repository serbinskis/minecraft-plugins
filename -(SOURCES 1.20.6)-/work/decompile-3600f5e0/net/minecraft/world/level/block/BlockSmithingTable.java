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
import net.minecraft.world.inventory.ContainerSmithing;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockSmithingTable extends BlockWorkbench {

    public static final MapCodec<BlockSmithingTable> CODEC = simpleCodec(BlockSmithingTable::new);
    private static final IChatBaseComponent CONTAINER_TITLE = IChatBaseComponent.translatable("container.upgrade");

    @Override
    public MapCodec<BlockSmithingTable> codec() {
        return BlockSmithingTable.CODEC;
    }

    protected BlockSmithingTable(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected ITileInventory getMenuProvider(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return new TileInventory((i, playerinventory, entityhuman) -> {
            return new ContainerSmithing(i, playerinventory, ContainerAccess.create(world, blockposition));
        }, BlockSmithingTable.CONTAINER_TITLE);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            entityhuman.openMenu(iblockdata.getMenuProvider(world, blockposition));
            entityhuman.awardStat(StatisticList.INTERACT_WITH_SMITHING_TABLE);
            return EnumInteractionResult.CONSUME;
        }
    }
}
