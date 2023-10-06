package net.minecraft.world.level;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.block.BlockDiodeAbstract;
import net.minecraft.world.level.block.BlockRedstoneWire;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public interface SignalGetter extends IBlockAccess {

    EnumDirection[] DIRECTIONS = EnumDirection.values();

    default int getDirectSignal(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getBlockState(blockposition).getDirectSignal(this, blockposition, enumdirection);
    }

    default int getDirectSignalTo(BlockPosition blockposition) {
        byte b0 = 0;
        int i = Math.max(b0, this.getDirectSignal(blockposition.below(), EnumDirection.DOWN));

        if (i >= 15) {
            return i;
        } else {
            i = Math.max(i, this.getDirectSignal(blockposition.above(), EnumDirection.UP));
            if (i >= 15) {
                return i;
            } else {
                i = Math.max(i, this.getDirectSignal(blockposition.north(), EnumDirection.NORTH));
                if (i >= 15) {
                    return i;
                } else {
                    i = Math.max(i, this.getDirectSignal(blockposition.south(), EnumDirection.SOUTH));
                    if (i >= 15) {
                        return i;
                    } else {
                        i = Math.max(i, this.getDirectSignal(blockposition.west(), EnumDirection.WEST));
                        if (i >= 15) {
                            return i;
                        } else {
                            i = Math.max(i, this.getDirectSignal(blockposition.east(), EnumDirection.EAST));
                            return i >= 15 ? i : i;
                        }
                    }
                }
            }
        }
    }

    default int getControlInputSignal(BlockPosition blockposition, EnumDirection enumdirection, boolean flag) {
        IBlockData iblockdata = this.getBlockState(blockposition);

        return flag ? (BlockDiodeAbstract.isDiode(iblockdata) ? this.getDirectSignal(blockposition, enumdirection) : 0) : (iblockdata.is(Blocks.REDSTONE_BLOCK) ? 15 : (iblockdata.is(Blocks.REDSTONE_WIRE) ? (Integer) iblockdata.getValue(BlockRedstoneWire.POWER) : (iblockdata.isSignalSource() ? this.getDirectSignal(blockposition, enumdirection) : 0)));
    }

    default boolean hasSignal(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getSignal(blockposition, enumdirection) > 0;
    }

    default int getSignal(BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata = this.getBlockState(blockposition);
        int i = iblockdata.getSignal(this, blockposition, enumdirection);

        return iblockdata.isRedstoneConductor(this, blockposition) ? Math.max(i, this.getDirectSignalTo(blockposition)) : i;
    }

    default boolean hasNeighborSignal(BlockPosition blockposition) {
        return this.getSignal(blockposition.below(), EnumDirection.DOWN) > 0 ? true : (this.getSignal(blockposition.above(), EnumDirection.UP) > 0 ? true : (this.getSignal(blockposition.north(), EnumDirection.NORTH) > 0 ? true : (this.getSignal(blockposition.south(), EnumDirection.SOUTH) > 0 ? true : (this.getSignal(blockposition.west(), EnumDirection.WEST) > 0 ? true : this.getSignal(blockposition.east(), EnumDirection.EAST) > 0))));
    }

    default int getBestNeighborSignal(BlockPosition blockposition) {
        int i = 0;
        EnumDirection[] aenumdirection = SignalGetter.DIRECTIONS;
        int j = aenumdirection.length;

        for (int k = 0; k < j; ++k) {
            EnumDirection enumdirection = aenumdirection[k];
            int l = this.getSignal(blockposition.relative(enumdirection), enumdirection);

            if (l >= 15) {
                return 15;
            }

            if (l > i) {
                i = l;
            }
        }

        return i;
    }
}
