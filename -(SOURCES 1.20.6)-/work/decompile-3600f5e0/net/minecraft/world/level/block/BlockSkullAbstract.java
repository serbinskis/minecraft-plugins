package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.pathfinder.PathMode;

public abstract class BlockSkullAbstract extends BlockTileEntity implements Equipable {

    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;
    private final BlockSkull.a type;

    public BlockSkullAbstract(BlockSkull.a blockskull_a, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.type = blockskull_a;
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockSkullAbstract.POWERED, false));
    }

    @Override
    protected abstract MapCodec<? extends BlockSkullAbstract> codec();

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntitySkull(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        if (world.isClientSide) {
            boolean flag = iblockdata.is(Blocks.DRAGON_HEAD) || iblockdata.is(Blocks.DRAGON_WALL_HEAD) || iblockdata.is(Blocks.PIGLIN_HEAD) || iblockdata.is(Blocks.PIGLIN_WALL_HEAD);

            if (flag) {
                return createTickerHelper(tileentitytypes, TileEntityTypes.SKULL, TileEntitySkull::animation);
            }
        }

        return null;
    }

    public BlockSkull.a getType() {
        return this.type;
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    @Override
    public EnumItemSlot getEquipmentSlot() {
        return EnumItemSlot.HEAD;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockSkullAbstract.POWERED);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockSkullAbstract.POWERED, blockactioncontext.getLevel().hasNeighborSignal(blockactioncontext.getClickedPos()));
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        if (!world.isClientSide) {
            boolean flag1 = world.hasNeighborSignal(blockposition);

            if (flag1 != (Boolean) iblockdata.getValue(BlockSkullAbstract.POWERED)) {
                world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockSkullAbstract.POWERED, flag1), 2);
            }

        }
    }
}
