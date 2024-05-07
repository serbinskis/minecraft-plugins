package net.minecraft.world.level.storage.loot;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public record LootDataType<T>(ResourceKey<IRegistry<T>> registryKey, Codec<T> codec, String directory, LootDataType.a<T> validator) {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(Registries.PREDICATE, LootItemConditions.DIRECT_CODEC, "predicates", createSimpleValidator());
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC, "item_modifiers", createSimpleValidator());
    public static final LootDataType<LootTable> TABLE = new LootDataType<>(Registries.LOOT_TABLE, LootTable.DIRECT_CODEC, "loot_tables", createLootTableValidator());

    public void runValidation(LootCollector lootcollector, ResourceKey<T> resourcekey, T t0) {
        this.validator.run(lootcollector, resourcekey, t0);
    }

    public <V> Optional<T> deserialize(MinecraftKey minecraftkey, DynamicOps<V> dynamicops, V v0) {
        DataResult<T> dataresult = this.codec.parse(dynamicops, v0);

        dataresult.error().ifPresent((error) -> {
            LootDataType.LOGGER.error("Couldn't parse element {}:{} - {}", new Object[]{this.directory, minecraftkey, error.message()});
        });
        return dataresult.result();
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(LootDataType.PREDICATE, LootDataType.MODIFIER, LootDataType.TABLE);
    }

    private static <T extends LootItemUser> LootDataType.a<T> createSimpleValidator() {
        return (lootcollector, resourcekey, lootitemuser) -> {
            lootitemuser.validate(lootcollector.enterElement("{" + String.valueOf(resourcekey.registry()) + "/" + String.valueOf(resourcekey.location()) + "}", resourcekey));
        };
    }

    private static LootDataType.a<LootTable> createLootTableValidator() {
        return (lootcollector, resourcekey, loottable) -> {
            loottable.validate(lootcollector.setParams(loottable.getParamSet()).enterElement("{" + String.valueOf(resourcekey.registry()) + "/" + String.valueOf(resourcekey.location()) + "}", resourcekey));
        };
    }

    @FunctionalInterface
    public interface a<T> {

        void run(LootCollector lootcollector, ResourceKey<T> resourcekey, T t0);
    }
}
