package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;

public class BlockPressurePlateWeighted extends BlockPressurePlateAbstract {

    public static final MapCodec<BlockPressurePlateWeighted> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.intRange(1, 1024).fieldOf("max_weight").forGetter((blockpressureplateweighted) -> {
            return blockpressureplateweighted.maxWeight;
        }), BlockSetType.CODEC.fieldOf("block_set_type").forGetter((blockpressureplateweighted) -> {
            return blockpressureplateweighted.type;
        }), propertiesCodec()).apply(instance, BlockPressurePlateWeighted::new);
    });
    public static final BlockStateInteger POWER = BlockProperties.POWER;
    private final int maxWeight;

    @Override
    public MapCodec<BlockPressurePlateWeighted> codec() {
        return BlockPressurePlateWeighted.CODEC;
    }

    protected BlockPressurePlateWeighted(int i, BlockSetType blocksettype, BlockBase.Info blockbase_info) {
        super(blockbase_info, blocksettype);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockPressurePlateWeighted.POWER, 0));
        this.maxWeight = i;
    }

    @Override
    protected int getSignalStrength(World world, BlockPosition blockposition) {
        int i = Math.min(getEntityCount(world, BlockPressurePlateWeighted.TOUCH_AABB.move(blockposition), Entity.class), this.maxWeight);

        if (i > 0) {
            float f = (float) Math.min(this.maxWeight, i) / (float) this.maxWeight;

            return MathHelper.ceil(f * 15.0F);
        } else {
            return 0;
        }
    }

    @Override
    protected int getSignalForState(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(BlockPressurePlateWeighted.POWER);
    }

    @Override
    protected IBlockData setSignalForState(IBlockData iblockdata, int i) {
        return (IBlockData) iblockdata.setValue(BlockPressurePlateWeighted.POWER, i);
    }

    @Override
    protected int getPressedTime() {
        return 10;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPressurePlateWeighted.POWER);
    }
}
