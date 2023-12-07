package me.wobbychip.smptweaks.nms;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CustomComparatorBlock extends ComparatorBlock {
    public CustomComparatorBlock(Properties settings) {
        super(settings);
    }

    protected int getInputSignal(Level world, BlockPos pos, BlockState state) {
        return super.getInputSignal(world, pos, state);
    }
}

