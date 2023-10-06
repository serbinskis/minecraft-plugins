package net.minecraft.world.level.block;

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

    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;
    private final BlockPressurePlateBinary.EnumMobType sensitivity;

    protected BlockPressurePlateBinary(BlockPressurePlateBinary.EnumMobType blockpressureplatebinary_enummobtype, BlockBase.Info blockbase_info, BlockSetType blocksettype) {
        super(blockbase_info, blocksettype);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockPressurePlateBinary.POWERED, false));
        this.sensitivity = blockpressureplatebinary_enummobtype;
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

        switch (this.sensitivity) {
            case EVERYTHING:
                oclass = Entity.class;
                break;
            case MOBS:
                oclass = EntityLiving.class;
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        Class oclass1 = oclass;

        return getEntityCount(world, BlockPressurePlateBinary.TOUCH_AABB.move(blockposition), oclass1) > 0 ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPressurePlateBinary.POWERED);
    }

    public static enum EnumMobType {

        EVERYTHING, MOBS;

        private EnumMobType() {}
    }
}
