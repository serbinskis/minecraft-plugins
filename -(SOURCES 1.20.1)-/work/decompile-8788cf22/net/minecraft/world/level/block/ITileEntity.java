package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEventListener;

public interface ITileEntity {

    @Nullable
    TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata);

    @Nullable
    default <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return null;
    }

    @Nullable
    default <T extends TileEntity> GameEventListener getListener(WorldServer worldserver, T t0) {
        if (t0 instanceof GameEventListener.b) {
            GameEventListener.b<?> gameeventlistener_b = (GameEventListener.b) t0;

            return gameeventlistener_b.getListener();
        } else {
            return null;
        }
    }
}
