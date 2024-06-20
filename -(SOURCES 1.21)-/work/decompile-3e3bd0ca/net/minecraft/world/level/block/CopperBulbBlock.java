package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;

public class CopperBulbBlock extends Block {

    public static final MapCodec<CopperBulbBlock> CODEC = simpleCodec(CopperBulbBlock::new);
    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;
    public static final BlockStateBoolean LIT = BlockProperties.LIT;

    @Override
    protected MapCodec<? extends CopperBulbBlock> codec() {
        return CopperBulbBlock.CODEC;
    }

    public CopperBulbBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.defaultBlockState().setValue(CopperBulbBlock.LIT, false)).setValue(CopperBulbBlock.POWERED, false));
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (iblockdata1.getBlock() != iblockdata.getBlock() && world instanceof WorldServer worldserver) {
            this.checkAndFlip(iblockdata, worldserver, blockposition);
        }

    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        if (world instanceof WorldServer worldserver) {
            this.checkAndFlip(iblockdata, worldserver, blockposition);
        }

    }

    public void checkAndFlip(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition) {
        boolean flag = worldserver.hasNeighborSignal(blockposition);

        if (flag != (Boolean) iblockdata.getValue(CopperBulbBlock.POWERED)) {
            IBlockData iblockdata1 = iblockdata;

            if (!(Boolean) iblockdata.getValue(CopperBulbBlock.POWERED)) {
                iblockdata1 = (IBlockData) iblockdata.cycle(CopperBulbBlock.LIT);
                worldserver.playSound((EntityHuman) null, blockposition, (Boolean) iblockdata1.getValue(CopperBulbBlock.LIT) ? SoundEffects.COPPER_BULB_TURN_ON : SoundEffects.COPPER_BULB_TURN_OFF, SoundCategory.BLOCKS);
            }

            worldserver.setBlock(blockposition, (IBlockData) iblockdata1.setValue(CopperBulbBlock.POWERED, flag), 3);
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(CopperBulbBlock.LIT, CopperBulbBlock.POWERED);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return (Boolean) world.getBlockState(blockposition).getValue(CopperBulbBlock.LIT) ? 15 : 0;
    }
}
