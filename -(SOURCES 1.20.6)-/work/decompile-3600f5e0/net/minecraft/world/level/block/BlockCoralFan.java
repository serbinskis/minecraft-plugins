package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;

public class BlockCoralFan extends BlockCoralFanAbstract {

    public static final MapCodec<BlockCoralFan> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockCoral.DEAD_CORAL_FIELD.forGetter((blockcoralfan) -> {
            return blockcoralfan.deadBlock;
        }), propertiesCodec()).apply(instance, BlockCoralFan::new);
    });
    private final Block deadBlock;

    @Override
    public MapCodec<BlockCoralFan> codec() {
        return BlockCoralFan.CODEC;
    }

    protected BlockCoralFan(Block block, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.deadBlock = block;
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        this.tryScheduleDieTick(iblockdata, world, blockposition);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!scanForWater(iblockdata, worldserver, blockposition)) {
            worldserver.setBlock(blockposition, (IBlockData) this.deadBlock.defaultBlockState().setValue(BlockCoralFan.WATERLOGGED, false), 2);
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if (enumdirection == EnumDirection.DOWN && !iblockdata.canSurvive(generatoraccess, blockposition)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            this.tryScheduleDieTick(iblockdata, generatoraccess, blockposition);
            if ((Boolean) iblockdata.getValue(BlockCoralFan.WATERLOGGED)) {
                generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
            }

            return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
        }
    }
}
