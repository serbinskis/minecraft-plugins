package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockStructureVoid extends Block {

    public static final MapCodec<BlockStructureVoid> CODEC = simpleCodec(BlockStructureVoid::new);
    private static final double SIZE = 5.0D;
    private static final VoxelShape SHAPE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    @Override
    public MapCodec<BlockStructureVoid> codec() {
        return BlockStructureVoid.CODEC;
    }

    protected BlockStructureVoid(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockStructureVoid.SHAPE;
    }

    @Override
    protected float getShadeBrightness(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return 1.0F;
    }
}
