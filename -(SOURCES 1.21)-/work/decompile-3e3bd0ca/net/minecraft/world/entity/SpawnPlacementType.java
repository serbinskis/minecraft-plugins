package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.IWorldReader;

public interface SpawnPlacementType {

    boolean isSpawnPositionOk(IWorldReader iworldreader, BlockPosition blockposition, @Nullable EntityTypes<?> entitytypes);

    default BlockPosition adjustSpawnPosition(IWorldReader iworldreader, BlockPosition blockposition) {
        return blockposition;
    }
}
