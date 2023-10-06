package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;

public record LootEntryType(Codec<? extends LootEntryAbstract> codec) {

}
