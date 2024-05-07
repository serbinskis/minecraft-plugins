package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityFurnace;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public abstract class BlockFurnace extends BlockTileEntity {

    public static final BlockStateDirection FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateBoolean LIT = BlockProperties.LIT;

    protected BlockFurnace(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockFurnace.FACING, EnumDirection.NORTH)).setValue(BlockFurnace.LIT, false));
    }

    @Override
    protected abstract MapCodec<? extends BlockFurnace> codec();

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            this.openContainer(world, blockposition, entityhuman);
            return EnumInteractionResult.CONSUME;
        }
    }

    protected abstract void openContainer(World world, BlockPosition blockposition, EntityHuman entityhuman);

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockFurnace.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata.is(iblockdata1.getBlock())) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityFurnace) {
                if (world instanceof WorldServer) {
                    InventoryUtils.dropContents(world, blockposition, (IInventory) ((TileEntityFurnace) tileentity));
                    ((TileEntityFurnace) tileentity).getRecipesToAwardAndPopExperience((WorldServer) world, Vec3D.atCenterOf(blockposition));
                }

                super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
                world.updateNeighbourForOutputSignal(blockposition, this);
            } else {
                super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
            }

        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(blockposition));
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockFurnace.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockFurnace.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockFurnace.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockFurnace.FACING, BlockFurnace.LIT);
    }

    @Nullable
    protected static <T extends TileEntity> BlockEntityTicker<T> createFurnaceTicker(World world, TileEntityTypes<T> tileentitytypes, TileEntityTypes<? extends TileEntityFurnace> tileentitytypes1) {
        return world.isClientSide ? null : createTickerHelper(tileentitytypes, tileentitytypes1, TileEntityFurnace::serverTick);
    }
}
