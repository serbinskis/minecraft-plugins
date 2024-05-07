package net.minecraft.world.level.chunk.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;

public record RegionStorageInfo(String level, ResourceKey<World> dimension, String type) {

    public RegionStorageInfo withTypeSuffix(String s) {
        return new RegionStorageInfo(this.level, this.dimension, this.type + s);
    }
}
