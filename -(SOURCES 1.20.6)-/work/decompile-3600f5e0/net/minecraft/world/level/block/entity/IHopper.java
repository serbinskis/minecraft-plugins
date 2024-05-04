package net.minecraft.world.level.block.entity;

import net.minecraft.world.IInventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AxisAlignedBB;

public interface IHopper extends IInventory {

    AxisAlignedBB SUCK_AABB = (AxisAlignedBB) Block.box(0.0D, 11.0D, 0.0D, 16.0D, 32.0D, 16.0D).toAabbs().get(0);

    default AxisAlignedBB getSuckAabb() {
        return IHopper.SUCK_AABB;
    }

    double getLevelX();

    double getLevelY();

    double getLevelZ();

    boolean isGridAligned();
}
