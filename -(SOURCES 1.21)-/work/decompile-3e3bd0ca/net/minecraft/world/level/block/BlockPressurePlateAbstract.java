package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public abstract class BlockPressurePlateAbstract extends Block {

    protected static final VoxelShape PRESSED_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 0.5D, 15.0D);
    protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
    protected static final AxisAlignedBB TOUCH_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.25D, 0.9375D);
    protected final BlockSetType type;

    protected BlockPressurePlateAbstract(BlockBase.Info blockbase_info, BlockSetType blocksettype) {
        super(blockbase_info.sound(blocksettype.soundType()));
        this.type = blocksettype;
    }

    @Override
    protected abstract MapCodec<? extends BlockPressurePlateAbstract> codec();

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return this.getSignalForState(iblockdata) > 0 ? BlockPressurePlateAbstract.PRESSED_AABB : BlockPressurePlateAbstract.AABB;
    }

    protected int getPressedTime() {
        return 20;
    }

    @Override
    public boolean isPossibleToRespawnInThis(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return enumdirection == EnumDirection.DOWN && !iblockdata.canSurvive(generatoraccess, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();

        return canSupportRigidBlock(iworldreader, blockposition1) || canSupportCenter(iworldreader, blockposition1, EnumDirection.UP);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        int i = this.getSignalForState(iblockdata);

        if (i > 0) {
            this.checkPressed((Entity) null, worldserver, blockposition, iblockdata, i);
        }

    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (!world.isClientSide) {
            int i = this.getSignalForState(iblockdata);

            if (i == 0) {
                this.checkPressed(entity, world, blockposition, iblockdata, i);
            }

        }
    }

    private void checkPressed(@Nullable Entity entity, World world, BlockPosition blockposition, IBlockData iblockdata, int i) {
        int j = this.getSignalStrength(world, blockposition);
        boolean flag = i > 0;
        boolean flag1 = j > 0;

        if (i != j) {
            IBlockData iblockdata1 = this.setSignalForState(iblockdata, j);

            world.setBlock(blockposition, iblockdata1, 2);
            this.updateNeighbours(world, blockposition);
            world.setBlocksDirty(blockposition, iblockdata, iblockdata1);
        }

        if (!flag1 && flag) {
            world.playSound((EntityHuman) null, blockposition, this.type.pressurePlateClickOff(), SoundCategory.BLOCKS);
            world.gameEvent(entity, (Holder) GameEvent.BLOCK_DEACTIVATE, blockposition);
        } else if (flag1 && !flag) {
            world.playSound((EntityHuman) null, blockposition, this.type.pressurePlateClickOn(), SoundCategory.BLOCKS);
            world.gameEvent(entity, (Holder) GameEvent.BLOCK_ACTIVATE, blockposition);
        }

        if (flag1) {
            world.scheduleTick(new BlockPosition(blockposition), (Block) this, this.getPressedTime());
        }

    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!flag && !iblockdata.is(iblockdata1.getBlock())) {
            if (this.getSignalForState(iblockdata) > 0) {
                this.updateNeighbours(world, blockposition);
            }

            super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
        }
    }

    protected void updateNeighbours(World world, BlockPosition blockposition) {
        world.updateNeighborsAt(blockposition, this);
        world.updateNeighborsAt(blockposition.below(), this);
    }

    @Override
    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getSignalForState(iblockdata);
    }

    @Override
    protected int getDirectSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return enumdirection == EnumDirection.UP ? this.getSignalForState(iblockdata) : 0;
    }

    @Override
    protected boolean isSignalSource(IBlockData iblockdata) {
        return true;
    }

    protected static int getEntityCount(World world, AxisAlignedBB axisalignedbb, Class<? extends Entity> oclass) {
        return world.getEntitiesOfClass(oclass, axisalignedbb, IEntitySelector.NO_SPECTATORS.and((entity) -> {
            return !entity.isIgnoringBlockTriggers();
        })).size();
    }

    protected abstract int getSignalStrength(World world, BlockPosition blockposition);

    protected abstract int getSignalForState(IBlockData iblockdata);

    protected abstract IBlockData setSignalForState(IBlockData iblockdata, int i);
}
