package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionSetTable extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionSetTable> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("name").forGetter((lootitemfunctionsettable) -> {
            return lootitemfunctionsettable.name;
        }), Codec.LONG.optionalFieldOf("seed", 0L).forGetter((lootitemfunctionsettable) -> {
            return lootitemfunctionsettable.seed;
        }), BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter((lootitemfunctionsettable) -> {
            return lootitemfunctionsettable.type;
        }))).apply(instance, LootItemFunctionSetTable::new);
    });
    private final ResourceKey<LootTable> name;
    private final long seed;
    private final Holder<TileEntityTypes<?>> type;

    private LootItemFunctionSetTable(List<LootItemCondition> list, ResourceKey<LootTable> resourcekey, long i, Holder<TileEntityTypes<?>> holder) {
        super(list);
        this.name = resourcekey;
        this.seed = i;
        this.type = holder;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetTable> getType() {
        return LootItemFunctions.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.isEmpty()) {
            return itemstack;
        } else {
            itemstack.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.name, this.seed));
            return itemstack;
        }
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);
        if (lootcollector.resolver().get(Registries.LOOT_TABLE, this.name).isEmpty()) {
            lootcollector.reportProblem("Missing loot table used for container: " + String.valueOf(this.name.location()));
        }

    }

    public static LootItemFunctionConditional.a<?> withLootTable(TileEntityTypes<?> tileentitytypes, ResourceKey<LootTable> resourcekey) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetTable(list, resourcekey, 0L, tileentitytypes.builtInRegistryHolder());
        });
    }

    public static LootItemFunctionConditional.a<?> withLootTable(TileEntityTypes<?> tileentitytypes, ResourceKey<LootTable> resourcekey, long i) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetTable(list, resourcekey, i, tileentitytypes.builtInRegistryHolder());
        });
    }
}
