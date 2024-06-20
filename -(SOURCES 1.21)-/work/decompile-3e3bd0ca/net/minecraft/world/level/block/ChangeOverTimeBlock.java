package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.IBlockData;

public interface ChangeOverTimeBlock<T extends Enum<T>> {

    int SCAN_DISTANCE = 4;

    Optional<IBlockData> getNext(IBlockData iblockdata);

    float getChanceModifier();

    default void changeOverTime(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        float f = 0.05688889F;

        if (randomsource.nextFloat() < 0.05688889F) {
            this.getNextState(iblockdata, worldserver, blockposition, randomsource).ifPresent((iblockdata1) -> {
                worldserver.setBlockAndUpdate(blockposition, iblockdata1);
            });
        }

    }

    T getAge();

    default Optional<IBlockData> getNextState(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        int i = this.getAge().ordinal();
        int j = 0;
        int k = 0;
        Iterator iterator = BlockPosition.withinManhattan(blockposition, 4, 4, 4).iterator();

        while (iterator.hasNext()) {
            BlockPosition blockposition1 = (BlockPosition) iterator.next();
            int l = blockposition1.distManhattan(blockposition);

            if (l > 4) {
                break;
            }

            if (!blockposition1.equals(blockposition)) {
                Block block = worldserver.getBlockState(blockposition1).getBlock();

                if (block instanceof ChangeOverTimeBlock) {
                    ChangeOverTimeBlock<?> changeovertimeblock = (ChangeOverTimeBlock) block;
                    Enum<?> oenum = changeovertimeblock.getAge();

                    if (this.getAge().getClass() == oenum.getClass()) {
                        int i1 = oenum.ordinal();

                        if (i1 < i) {
                            return Optional.empty();
                        }

                        if (i1 > i) {
                            ++k;
                        } else {
                            ++j;
                        }
                    }
                }
            }
        }

        float f = (float) (k + 1) / (float) (k + j + 1);
        float f1 = f * f * this.getChanceModifier();

        return randomsource.nextFloat() < f1 ? this.getNext(iblockdata) : Optional.empty();
    }
}
