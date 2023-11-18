package me.wobbychip.smptweaks.library.customblocks.blocks;

import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.ticks.TickPriority;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;

import java.util.Objects;

public class ComparatorBlock {
    private final int power;
    private final CustomBlock cblock;
    private final org.bukkit.block.Block block_bukkit;
    public final net.minecraft.world.level.block.ComparatorBlock block_nms;
    private final BlockPos block_pos;
    private final ServerLevel block_world;
    private final BlockState block_sate;
    private final BlockEntity block_entity;


    public ComparatorBlock(org.bukkit.block.Block block, CustomBlock cblock, int power) {
        this.power = power;
        this.cblock = cblock;
        this.block_bukkit = block;
        this.block_pos = new BlockPos(block.getX(), block.getY(), block.getZ());
        this.block_world = Objects.requireNonNull(ReflectionUtils.getWorld(block.getWorld()));
        this.block_sate = block_world.getBlockState(block_pos);
        this.block_nms = (net.minecraft.world.level.block.ComparatorBlock) block_sate.getBlock();
        this.block_entity = block_world.getBlockEntity(block_pos);

    }

    public int getDelay() { return 2; }

    public int getOutputSignal() {
        return block_entity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity) block_entity).getOutputSignal() : 0;
    }

    public int calculateOutputSignal() {
        int i = this.getInputSignal();

        if (i == 0) {
            return 0;
        } else {
            int j = ReflectionUtils.getAlternateSignal(block_bukkit);
            return j > i ? 0 : (block_sate.getValue(net.minecraft.world.level.block.ComparatorBlock.MODE) == ComparatorMode.SUBTRACT ? i - j : i);
        }
    }

    public boolean shouldTurnOn() {
        int i = this.getInputSignal();

        if (i == 0) {
            return false;
        } else {
            int j = ReflectionUtils.getAlternateSignal(block_bukkit);
            return i > j ? true : i == j && block_sate.getValue(net.minecraft.world.level.block.ComparatorBlock.MODE) == ComparatorMode.COMPARE;
        }
    }

    public int getInputSignal() {
        return power;
    }

    public void checkTickOnNeighbor() {
        if (!block_world.getBlockTicks().willTickThisTick(block_pos, block_nms)) {
            int i = this.calculateOutputSignal();
            BlockEntity tileentity = block_world.getBlockEntity(block_pos);
            int j = tileentity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity) tileentity).getOutputSignal() : 0;

            if (i != j || (Boolean) block_sate.getValue(net.minecraft.world.level.block.ComparatorBlock.POWERED) != this.shouldTurnOn()) {
                TickPriority ticklistpriority = block_nms.shouldPrioritize(block_world, block_pos, block_sate) ? TickPriority.HIGH : TickPriority.NORMAL;

                //block_world.scheduleTick(block_pos, (Block) block_nms, getDelay(), ticklistpriority);

                TaskUtils.scheduleSyncDelayedTask(new Runnable() {
                    public void run() { tick(); }
                }, getDelay());
            }

        }
    }

    public void refreshOutputState() {
        int i = this.calculateOutputSignal();
        BlockEntity tileentity = block_world.getBlockEntity(block_pos);
        int j = 0;

        if (tileentity instanceof ComparatorBlockEntity) {
            ComparatorBlockEntity tileentitycomparator = (ComparatorBlockEntity) tileentity;

            j = tileentitycomparator.getOutputSignal();
            tileentitycomparator.setOutputSignal(i);
        }

        if (j != i || block_sate.getValue(net.minecraft.world.level.block.ComparatorBlock.MODE) == ComparatorMode.COMPARE) {
            boolean flag = this.shouldTurnOn();
            boolean flag1 = (Boolean) block_sate.getValue(net.minecraft.world.level.block.ComparatorBlock.POWERED);

            if (flag1 && !flag) {
                if (CraftEventFactory.callRedstoneChange(block_world, block_pos, 15, 0).getNewCurrent() != 0) { return; }
                block_world.setBlock(block_pos, (BlockState) block_sate.setValue(net.minecraft.world.level.block.ComparatorBlock.POWERED, false), 2);
            } else if (!flag1 && flag) {
                if (CraftEventFactory.callRedstoneChange(block_world, block_pos, 0, 15).getNewCurrent() != 15) { return; }
                block_world.setBlock(block_pos, (BlockState) block_sate.setValue(net.minecraft.world.level.block.ComparatorBlock.POWERED, true), 2);
            }
        }

        this.updateNeighborsInFront();
    }

    public void updateNeighborsInFront() {
        Direction enumdirection = (Direction) block_sate.getValue(DiodeBlock.FACING);
        BlockPos blockposition1 = block_pos.relative(enumdirection.getOpposite());

        block_world.neighborChanged(blockposition1, block_nms, block_pos);
        block_world.updateNeighborsAtExceptFromFacing(blockposition1, block_nms, enumdirection);
    }

    public void tick() {
        this.refreshOutputState();
    }
}
