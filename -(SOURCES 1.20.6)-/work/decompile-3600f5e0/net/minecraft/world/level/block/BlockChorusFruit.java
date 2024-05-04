package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.pathfinder.PathMode;

public class BlockChorusFruit extends BlockSprawling {

    public static final MapCodec<BlockChorusFruit> CODEC = simpleCodec(BlockChorusFruit::new);

    @Override
    public MapCodec<BlockChorusFruit> codec() {
        return BlockChorusFruit.CODEC;
    }

    protected BlockChorusFruit(BlockBase.Info blockbase_info) {
        super(0.3125F, blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockChorusFruit.NORTH, false)).setValue(BlockChorusFruit.EAST, false)).setValue(BlockChorusFruit.SOUTH, false)).setValue(BlockChorusFruit.WEST, false)).setValue(BlockChorusFruit.UP, false)).setValue(BlockChorusFruit.DOWN, false));
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return getStateWithConnections(blockactioncontext.getLevel(), blockactioncontext.getClickedPos(), this.defaultBlockState());
    }

    public static IBlockData getStateWithConnections(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        IBlockData iblockdata1 = iblockaccess.getBlockState(blockposition.below());
        IBlockData iblockdata2 = iblockaccess.getBlockState(blockposition.above());
        IBlockData iblockdata3 = iblockaccess.getBlockState(blockposition.north());
        IBlockData iblockdata4 = iblockaccess.getBlockState(blockposition.east());
        IBlockData iblockdata5 = iblockaccess.getBlockState(blockposition.south());
        IBlockData iblockdata6 = iblockaccess.getBlockState(blockposition.west());
        Block block = iblockdata.getBlock();

        return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.trySetValue(BlockChorusFruit.DOWN, iblockdata1.is(block) || iblockdata1.is(Blocks.CHORUS_FLOWER) || iblockdata1.is(Blocks.END_STONE))).trySetValue(BlockChorusFruit.UP, iblockdata2.is(block) || iblockdata2.is(Blocks.CHORUS_FLOWER))).trySetValue(BlockChorusFruit.NORTH, iblockdata3.is(block) || iblockdata3.is(Blocks.CHORUS_FLOWER))).trySetValue(BlockChorusFruit.EAST, iblockdata4.is(block) || iblockdata4.is(Blocks.CHORUS_FLOWER))).trySetValue(BlockChorusFruit.SOUTH, iblockdata5.is(block) || iblockdata5.is(Blocks.CHORUS_FLOWER))).trySetValue(BlockChorusFruit.WEST, iblockdata6.is(block) || iblockdata6.is(Blocks.CHORUS_FLOWER));
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if (!iblockdata.canSurvive(generatoraccess, blockposition)) {
            generatoraccess.scheduleTick(blockposition, (Block) this, 1);
            return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
        } else {
            boolean flag = iblockdata1.is((Block) this) || iblockdata1.is(Blocks.CHORUS_FLOWER) || enumdirection == EnumDirection.DOWN && iblockdata1.is(Blocks.END_STONE);

            return (IBlockData) iblockdata.setValue((IBlockState) BlockChorusFruit.PROPERTY_BY_DIRECTION.get(enumdirection), flag);
        }
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!iblockdata.canSurvive(worldserver, blockposition)) {
            worldserver.destroyBlock(blockposition, true);
        }

    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.below());
        boolean flag = !iworldreader.getBlockState(blockposition.above()).isAir() && !iblockdata1.isAir();
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        IBlockData iblockdata2;

        do {
            BlockPosition blockposition1;
            IBlockData iblockdata3;

            do {
                if (!iterator.hasNext()) {
                    return iblockdata1.is((Block) this) || iblockdata1.is(Blocks.END_STONE);
                }

                EnumDirection enumdirection = (EnumDirection) iterator.next();

                blockposition1 = blockposition.relative(enumdirection);
                iblockdata3 = iworldreader.getBlockState(blockposition1);
            } while (!iblockdata3.is((Block) this));

            if (flag) {
                return false;
            }

            iblockdata2 = iworldreader.getBlockState(blockposition1.below());
        } while (!iblockdata2.is((Block) this) && !iblockdata2.is(Blocks.END_STONE));

        return true;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockChorusFruit.NORTH, BlockChorusFruit.EAST, BlockChorusFruit.SOUTH, BlockChorusFruit.WEST, BlockChorusFruit.UP, BlockChorusFruit.DOWN);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
