package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
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
    private double maxInteractionRange;

    public ContainerOpenersCounter() {}

    protected abstract void onOpen(World world, BlockPosition blockposition, IBlockData iblockdata);

    protected abstract void onClose(World world, BlockPosition blockposition, IBlockData iblockdata);

    protected abstract void openerCountChanged(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j);

    protected abstract boolean isOwnContainer(EntityHuman entityhuman);

    public void incrementOpeners(EntityHuman entityhuman, World world, BlockPosition blockposition, IBlockData iblockdata) {
        int i = this.openCount++;

        if (i == 0) {
            this.onOpen(world, blockposition, iblockdata);
            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.CONTAINER_OPEN, blockposition);
            scheduleRecheck(world, blockposition, iblockdata);
        }

        this.openerCountChanged(world, blockposition, iblockdata, i, this.openCount);
        this.maxInteractionRange = Math.max(entityhuman.blockInteractionRange(), this.maxInteractionRange);
    }

    public void decrementOpeners(EntityHuman entityhuman, World world, BlockPosition blockposition, IBlockData iblockdata) {
        int i = this.openCount--;

        if (this.openCount == 0) {
            this.onClose(world, blockposition, iblockdata);
            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.CONTAINER_CLOSE, blockposition);
            this.maxInteractionRange = 0.0D;
        }

        this.openerCountChanged(world, blockposition, iblockdata, i, this.openCount);
    }

    private List<EntityHuman> getPlayersWithContainerOpen(World world, BlockPosition blockposition) {
        double d0 = this.maxInteractionRange + 4.0D;
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition)).inflate(d0);

        return world.getEntities(EntityTypeTest.forClass(EntityHuman.class), axisalignedbb, this::isOwnContainer);
    }

    public void recheckOpeners(World world, BlockPosition blockposition, IBlockData iblockdata) {
        List<EntityHuman> list = this.getPlayersWithContainerOpen(world, blockposition);

        this.maxInteractionRange = 0.0D;

        EntityHuman entityhuman;

        for (Iterator iterator = list.iterator(); iterator.hasNext(); this.maxInteractionRange = Math.max(entityhuman.blockInteractionRange(), this.maxInteractionRange)) {
            entityhuman = (EntityHuman) iterator.next();
        }

        int i = list.size();
        int j = this.openCount;

        if (j != i) {
            boolean flag = i != 0;
            boolean flag1 = j != 0;

            if (flag && !flag1) {
                this.onOpen(world, blockposition, iblockdata);
                world.gameEvent((Entity) null, (Holder) GameEvent.CONTAINER_OPEN, blockposition);
            } else if (!flag) {
                this.onClose(world, blockposition, iblockdata);
                world.gameEvent((Entity) null, (Holder) GameEvent.CONTAINER_CLOSE, blockposition);
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
