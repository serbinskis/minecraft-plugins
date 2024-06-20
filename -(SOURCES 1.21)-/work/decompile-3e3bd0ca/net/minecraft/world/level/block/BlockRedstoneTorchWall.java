package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamRedstone;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateDirection;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockRedstoneTorchWall extends BlockRedstoneTorch {

    public static final MapCodec<BlockRedstoneTorchWall> CODEC = simpleCodec(BlockRedstoneTorchWall::new);
    public static final BlockStateDirection FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateBoolean LIT = BlockRedstoneTorch.LIT;

    @Override
    public MapCodec<BlockRedstoneTorchWall> codec() {
        return BlockRedstoneTorchWall.CODEC;
    }

    protected BlockRedstoneTorchWall(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockRedstoneTorchWall.FACING, EnumDirection.NORTH)).setValue(BlockRedstoneTorchWall.LIT, true));
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockTorchWall.getShape(iblockdata);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return BlockTorchWall.canSurvive(iworldreader, blockposition, (EnumDirection) iblockdata.getValue(BlockRedstoneTorchWall.FACING));
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return enumdirection.getOpposite() == iblockdata.getValue(BlockRedstoneTorchWall.FACING) && !iblockdata.canSurvive(generatoraccess, blockposition) ? Blocks.AIR.defaultBlockState() : iblockdata;
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = Blocks.WALL_TORCH.getStateForPlacement(blockactioncontext);

        return iblockdata == null ? null : (IBlockData) this.defaultBlockState().setValue(BlockRedstoneTorchWall.FACING, (EnumDirection) iblockdata.getValue(BlockRedstoneTorchWall.FACING));
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockRedstoneTorchWall.LIT)) {
            EnumDirection enumdirection = ((EnumDirection) iblockdata.getValue(BlockRedstoneTorchWall.FACING)).getOpposite();
            double d0 = 0.27D;
            double d1 = (double) blockposition.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) enumdirection.getStepX();
            double d2 = (double) blockposition.getY() + 0.7D + (randomsource.nextDouble() - 0.5D) * 0.2D + 0.22D;
            double d3 = (double) blockposition.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) enumdirection.getStepZ();

            world.addParticle(ParticleParamRedstone.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected boolean hasNeighborSignal(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EnumDirection enumdirection = ((EnumDirection) iblockdata.getValue(BlockRedstoneTorchWall.FACING)).getOpposite();

        return world.hasSignal(blockposition.relative(enumdirection), enumdirection);
    }

    @Override
    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return (Boolean) iblockdata.getValue(BlockRedstoneTorchWall.LIT) && iblockdata.getValue(BlockRedstoneTorchWall.FACING) != enumdirection ? 15 : 0;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockRedstoneTorchWall.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockRedstoneTorchWall.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockRedstoneTorchWall.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockRedstoneTorchWall.FACING, BlockRedstoneTorchWall.LIT);
    }
}
