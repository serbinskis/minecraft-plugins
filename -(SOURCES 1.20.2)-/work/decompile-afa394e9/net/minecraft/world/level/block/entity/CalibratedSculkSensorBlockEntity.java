package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {

    public CalibratedSculkSensorBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.CALIBRATED_SCULK_SENSOR, blockposition, iblockdata);
    }

    @Override
    public VibrationSystem.d createVibrationUser() {
        return new CalibratedSculkSensorBlockEntity.a(this.getBlockPos());
    }

    protected class a extends SculkSensorBlockEntity.a {

        public a(BlockPosition blockposition) {
            super(blockposition);
        }

        @Override
        public int getListenerRadius() {
            return 16;
        }

        @Override
        public boolean canReceiveVibration(WorldServer worldserver, BlockPosition blockposition, GameEvent gameevent, @Nullable GameEvent.a gameevent_a) {
            int i = this.getBackSignal(worldserver, this.blockPos, CalibratedSculkSensorBlockEntity.this.getBlockState());

            return i != 0 && VibrationSystem.getGameEventFrequency(gameevent) != i ? false : super.canReceiveVibration(worldserver, blockposition, gameevent, gameevent_a);
        }

        private int getBackSignal(World world, BlockPosition blockposition, IBlockData iblockdata) {
            EnumDirection enumdirection = ((EnumDirection) iblockdata.getValue(CalibratedSculkSensorBlock.FACING)).getOpposite();

            return world.getSignal(blockposition.relative(enumdirection), enumdirection);
        }
    }
}
