package net.minecraft.world.level.storage.loot;

import net.minecraft.resources.MinecraftKey;

public record LootDataId<T> (LootDataType<T> type, MinecraftKey location) {

}
