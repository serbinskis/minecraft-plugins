package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockCoralFanAbstract extends BlockCoralBase {

    public static final MapCodec<BlockCoralFanAbstract> CODEC = simpleCodec(BlockCoralFanAbstract::new);
    private static final VoxelShape AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);

    @Override
    public MapCodec<? extends BlockCoralFanAbstract> codec() {
        return BlockCoralFanAbstract.CODEC;
    }

    protected BlockCoralFanAbstract(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockCoralFanAbstract.AABB;
    }
}
