package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPosition;

public interface WorldDataMutable extends WorldData {

    void setSpawn(BlockPosition blockposition, float f);
}
