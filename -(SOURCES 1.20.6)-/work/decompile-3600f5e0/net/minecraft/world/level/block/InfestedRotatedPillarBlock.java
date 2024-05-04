package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;

public class InfestedRotatedPillarBlock extends BlockMonsterEggs {

    public static final MapCodec<InfestedRotatedPillarBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host").forGetter(BlockMonsterEggs::getHostBlock), propertiesCodec()).apply(instance, InfestedRotatedPillarBlock::new);
    });

    @Override
    public MapCodec<InfestedRotatedPillarBlock> codec() {
        return InfestedRotatedPillarBlock.CODEC;
    }

    public InfestedRotatedPillarBlock(Block block, BlockBase.Info blockbase_info) {
        super(block, blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(BlockRotatable.AXIS, EnumDirection.EnumAxis.Y));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return BlockRotatable.rotatePillar(iblockdata, enumblockrotation);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockRotatable.AXIS);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(BlockRotatable.AXIS, blockactioncontext.getClickedFace().getAxis());
    }
}
