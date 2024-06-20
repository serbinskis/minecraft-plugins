package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;

public record LootEntryType(MapCodec<? extends LootEntryAbstract> codec) {

}
