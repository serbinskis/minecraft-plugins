package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;

public abstract class ContainerOpenersCounter {

    private static final int CHECK_TICK_DELAY = 5;
    private int openCount;
    public boolean opened; // CraftBukkit

    public ContainerOpenersCounter() {}

    protected abstract void onOpen(World world, BlockPosition blockposition, IBlockData iblockdata);

    protected abstract void onClose(World world, BlockPosition blockposition, IBlockData iblockdata);

    protected abstract void openerCountChanged(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j);

    // CraftBukkit start
    public void onAPIOpen(World world, BlockPosition blockposition, IBlockData iblockdata) {
        onOpen(world, blockposition, iblockdata);
    }

    public void onAPIClose(World world, BlockPosition blockposition, IBlockData iblockdata) {
        onClose(world, blockposition, iblockdata);
    }

    public void openerAPICountChanged(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        openerCountChanged(world, blockposition, iblockdata, i, j);
    }
    // CraftBukkit end

    protected abstract boolean isOwnContainer(EntityHuman entityhuman);

    public void incrementOpeners(EntityHuman entityhuman, World world, BlockPosition blockposition, IBlockData iblockdata) {
        int oldPower = Math.max(0, Math.min(15, this.openCount)); // CraftBukkit - Get power before new viewer is added
        int i = this.openCount++;

        // CraftBukkit start - Call redstone event
        if (world.getBlockState(blockposition).is(net.minecraft.world.level.block.Blocks.TRAPPED_CHEST)) {
            int newPower = Math.max(0, Math.min(15, this.openCount));

            if (oldPower != newPower) {
                org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(world, blockposition, oldPower, newPower);
            }
        }
        // CraftBukkit end

        if (i == 0) {
            this.onOpen(world, blockposition, iblockdata);
            world.gameEvent((Entity) entityhuman, GameEvent.CONTAINER_OPEN, blockposition);
            scheduleRecheck(world, blockposition, iblockdata);
        }

        this.openerCountChanged(world, blockposition, iblockdata, i, this.openCount);
    }

    public void decrementOpeners(EntityHuman entityhuman, World world, BlockPosition blockposition, IBlockData iblockdata) {
        int oldPower = Math.max(0, Math.min(15, this.openCount)); // CraftBukkit - Get power before new viewer is added
        int i = this.openCount--;

        // CraftBukkit start - Call redstone event
        if (world.getBlockState(blockposition).is(net.minecraft.world.level.block.Blocks.TRAPPED_CHEST)) {
            int newPower = Math.max(0, Math.min(15, this.openCount));

            if (oldPower != newPower) {
                org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(world, blockposition, oldPower, newPower);
            }
        }
        // CraftBukkit end

        if (this.openCount == 0) {
            this.onClose(world, blockposition, iblockdata);
            world.gameEvent((Entity) entityhuman, GameEvent.CONTAINER_CLOSE, blockposition);
        }

        this.openerCountChanged(world, blockposition, iblockdata, i, this.openCount);
    }

    private int getOpenCount(World world, BlockPosition blockposition) {
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();
        float f = 5.0F;
        AxisAlignedBB axisalignedbb = new AxisAlignedBB((double) ((float) i - 5.0F), (double) ((float) j - 5.0F), (double) ((float) k - 5.0F), (double) ((float) (i + 1) + 5.0F), (double) ((float) (j + 1) + 5.0F), (double) ((float) (k + 1) + 5.0F));

        return world.getEntities(EntityTypeTest.forClass(EntityHuman.class), axisalignedbb, this::isOwnContainer).size();
    }

    public void recheckOpeners(World world, BlockPosition blockposition, IBlockData iblockdata) {
        int i = this.getOpenCount(world, blockposition);
        if (opened) i++; // CraftBukkit - add dummy count from API
        int j = this.openCount;

        if (j != i) {
            boolean flag = i != 0;
            boolean flag1 = j != 0;

            if (flag && !flag1) {
                this.onOpen(world, blockposition, iblockdata);
                world.gameEvent((Entity) null, GameEvent.CONTAINER_OPEN, blockposition);
            } else if (!flag) {
                this.onClose(world, blockposition, iblockdata);
                world.gameEvent((Entity) null, GameEvent.CONTAINER_CLOSE, blockposition);
            }

            this.openCount = i;
        }

        this.openerCountChanged(world, blockposition, iblockdata, j, i);
        if (i > 0) {
            scheduleRecheck(world, blockposition, iblockdata);
        }

    }

    public int getOpenerCount() {
        return this.openCount;
    }

    private static void scheduleRecheck(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.scheduleTick(blockposition, iblockdata.getBlock(), 5);
    }
}
