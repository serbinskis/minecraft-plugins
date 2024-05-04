package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public class LootEntries {

    public static final Codec<LootEntryAbstract> CODEC = BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.byNameCodec().dispatch(LootEntryAbstract::getType, LootEntryType::codec);
    public static final LootEntryType EMPTY = register("empty", LootSelectorEmpty.CODEC);
    public static final LootEntryType ITEM = register("item", LootItem.CODEC);
    public static final LootEntryType LOOT_TABLE = register("loot_table", NestedLootTable.CODEC);
    public static final LootEntryType DYNAMIC = register("dynamic", LootSelectorDynamic.CODEC);
    public static final LootEntryType TAG = register("tag", LootSelectorTag.CODEC);
    public static final LootEntryType ALTERNATIVES = register("alternatives", LootEntryAlternatives.CODEC);
    public static final LootEntryType SEQUENCE = register("sequence", LootEntrySequence.CODEC);
    public static final LootEntryType GROUP = register("group", LootEntryGroup.CODEC);

    public LootEntries() {}

    private static LootEntryType register(String s, MapCodec<? extends LootEntryAbstract> mapcodec) {
        return (LootEntryType) IRegistry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, new MinecraftKey(s), new LootEntryType(mapcodec));
    }
}
