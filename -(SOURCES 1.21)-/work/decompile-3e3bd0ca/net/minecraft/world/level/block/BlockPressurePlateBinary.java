package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;

public class BlockPressurePlateBinary extends BlockPressurePlateAbstract {

    public static final MapCodec<BlockPressurePlateBinary> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter((blockpressureplatebinary) -> {
            return blockpressureplatebinary.type;
        }), propertiesCodec()).apply(instance, BlockPressurePlateBinary::new);
    });
    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;

    @Override
    public MapCodec<BlockPressurePlateBinary> codec() {
        return BlockPressurePlateBinary.CODEC;
    }

    protected BlockPressurePlateBinary(BlockSetType blocksettype, BlockBase.Info blockbase_info) {
        super(blockbase_info, blocksettype);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockPressurePlateBinary.POWERED, false));
    }

    @Override
    protected int getSignalForState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockPressurePlateBinary.POWERED) ? 15 : 0;
    }

    @Override
    protected IBlockData setSignalForState(IBlockData iblockdata, int i) {
        return (IBlockData) iblockdata.setValue(BlockPressurePlateBinary.POWERED, i > 0);
    }

    @Override
    protected int getSignalStrength(World world, BlockPosition blockposition) {
        Class oclass;

        switch (this.type.pressurePlateSensitivity()) {
            case EVERYTHING:
                oclass = Entity.class;
                break;
            case MOBS:
                oclass = EntityLiving.class;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        Class<? extends Entity> oclass1 = oclass;

        return getEntityCount(world, BlockPressurePlateBinary.TOUCH_AABB.move(blockposition), oclass1) > 0 ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPressurePlateBinary.POWERED);
    }
}
