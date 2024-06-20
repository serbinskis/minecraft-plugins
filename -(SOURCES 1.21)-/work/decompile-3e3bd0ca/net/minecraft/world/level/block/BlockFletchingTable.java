package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockFletchingTable extends BlockWorkbench {

    public static final MapCodec<BlockFletchingTable> CODEC = simpleCodec(BlockFletchingTable::new);

    @Override
    public MapCodec<BlockFletchingTable> codec() {
        return BlockFletchingTable.CODEC;
    }

    protected BlockFletchingTable(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        return EnumInteractionResult.PASS;
    }
}
