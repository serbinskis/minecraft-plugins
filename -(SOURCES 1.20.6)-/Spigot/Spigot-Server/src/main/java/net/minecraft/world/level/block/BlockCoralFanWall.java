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

public class BlockCoralFanWall extends BlockCoralFanWallAbstract {

    public static final MapCodec<BlockCoralFanWall> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockCoral.DEAD_CORAL_FIELD.forGetter((blockcoralfanwall) -> {
            return blockcoralfanwall.deadBlock;
        }), propertiesCodec()).apply(instance, BlockCoralFanWall::new);
    });
    private final Block deadBlock;

    @Override
    public MapCodec<BlockCoralFanWall> codec() {
        return BlockCoralFanWall.CODEC;
    }

    protected BlockCoralFanWall(Block block, BlockBase.Info blockbase_info) {
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
            // CraftBukkit start
            if (org.bukkit.craftbukkit.event.CraftEventFactory.callBlockFadeEvent(worldserver, blockposition, this.deadBlock.defaultBlockState().setValue(BlockCoralFanWall.WATERLOGGED, false).setValue(BlockCoralFanWall.FACING, iblockdata.getValue(BlockCoralFanWall.FACING))).isCancelled()) {
                return;
            }
            // CraftBukkit end
            worldserver.setBlock(blockposition, (IBlockData) ((IBlockData) this.deadBlock.defaultBlockState().setValue(BlockCoralFanWall.WATERLOGGED, false)).setValue(BlockCoralFanWall.FACING, (EnumDirection) iblockdata.getValue(BlockCoralFanWall.FACING)), 2);
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if (enumdirection.getOpposite() == iblockdata.getValue(BlockCoralFanWall.FACING) && !iblockdata.canSurvive(generatoraccess, blockposition)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if ((Boolean) iblockdata.getValue(BlockCoralFanWall.WATERLOGGED)) {
                generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
            }

            this.tryScheduleDieTick(iblockdata, generatoraccess, blockposition);
            return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
        }
    }
}
