package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.entity.TileEntityDispenser;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public record SourceBlock(WorldServer level, BlockPosition pos, IBlockData state, TileEntityDispenser blockEntity) {

    public Vec3D center() {
        return this.pos.getCenter();
    }
}
