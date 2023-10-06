package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;

public class BlockBarrier extends Block implements IBlockWaterlogged {

    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;

    protected BlockBarrier(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(BlockBarrier.WATERLOGGED, false));
    }

    @Override
    public boolean propagatesSkylightDown(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return true;
    }

    @Override
    public EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.INVISIBLE;
    }

    @Override
    public float getShadeBrightness(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return 1.0F;
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(BlockBarrier.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockBarrier.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockBarrier.WATERLOGGED, blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos()).getType() == FluidTypes.WATER);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockBarrier.WATERLOGGED);
    }

    @Override
    public ItemStack pickupBlock(@Nullable EntityHuman entityhuman, GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata) {
        return entityhuman != null && entityhuman.isCreative() ? IBlockWaterlogged.super.pickupBlock(entityhuman, generatoraccess, blockposition, iblockdata) : ItemStack.EMPTY;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable EntityHuman entityhuman, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, FluidType fluidtype) {
        return entityhuman != null && entityhuman.isCreative() ? IBlockWaterlogged.super.canPlaceLiquid(entityhuman, iblockaccess, blockposition, iblockdata, fluidtype) : false;
    }
}
