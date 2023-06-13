package net.minecraft.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootDataType<T> {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(LootSerialization.createConditionSerializer().create(), createSingleOrMultipleDeserialiser(LootItemCondition.class, LootDataManager::createComposite), "predicates", createSimpleValidator());
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(LootSerialization.createFunctionSerializer().create(), createSingleOrMultipleDeserialiser(LootItemFunction.class, LootDataManager::createComposite), "item_modifiers", createSimpleValidator());
    public static final LootDataType<LootTable> TABLE = new LootDataType<>(LootSerialization.createLootTableSerializer().create(), createSingleDeserialiser(LootTable.class), "loot_tables", createLootTableValidator());
    private final Gson parser;
    private final BiFunction<MinecraftKey, JsonElement, Optional<T>> topDeserializer;
    private final String directory;
    private final LootDataType.a<T> validator;

    private LootDataType(Gson gson, BiFunction<Gson, String, BiFunction<MinecraftKey, JsonElement, Optional<T>>> bifunction, String s, LootDataType.a<T> lootdatatype_a) {
        this.parser = gson;
        this.directory = s;
        this.validator = lootdatatype_a;
        this.topDeserializer = (BiFunction) bifunction.apply(gson, s);
    }

    public Gson parser() {
        return this.parser;
    }

    public String directory() {
        return this.directory;
    }

    public void runValidation(LootCollector lootcollector, LootDataId<T> lootdataid, T t0) {
        this.validator.run(lootcollector, lootdataid, t0);
    }

    public Optional<T> deserialize(MinecraftKey minecraftkey, JsonElement jsonelement) {
        return (Optional) this.topDeserializer.apply(minecraftkey, jsonelement);
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(LootDataType.PREDICATE, LootDataType.MODIFIER, LootDataType.TABLE);
    }

    private static <T> BiFunction<Gson, String, BiFunction<MinecraftKey, JsonElement, Optional<T>>> createSingleDeserialiser(Class<T> oclass) {
        return (gson, s) -> {
            return (minecraftkey, jsonelement) -> {
                try {
                    return Optional.of(gson.fromJson(jsonelement, oclass));
                } catch (Exception exception) {
                    LootDataType.LOGGER.error("Couldn't parse element {}:{}", new Object[]{s, minecraftkey, exception});
                    return Optional.empty();
                }
            };
        };
    }

    private static <T> BiFunction<Gson, String, BiFunction<MinecraftKey, JsonElement, Optional<T>>> createSingleOrMultipleDeserialiser(Class<T> oclass, Function<T[], T> function) {
        Class<T[]> oclass1 = oclass.arrayType();

        return (gson, s) -> {
            return (minecraftkey, jsonelement) -> {
                try {
                    if (jsonelement.isJsonArray()) {
                        T[] at = (Object[]) gson.fromJson(jsonelement, oclass1);

                        return Optional.of(function.apply(at));
                    } else {
                        return Optional.of(gson.fromJson(jsonelement, oclass));
                    }
                } catch (Exception exception) {
                    LootDataType.LOGGER.error("Couldn't parse element {}:{}", new Object[]{s, minecraftkey, exception});
                    return Optional.empty();
                }
            };
        };
    }

    private static <T extends LootItemUser> LootDataType.a<T> createSimpleValidator() {
        return (lootcollector, lootdataid, lootitemuser) -> {
            lootitemuser.validate(lootcollector.enterElement("{" + lootdataid.type().directory + ":" + lootdataid.location() + "}", lootdataid));
        };
    }

    private static LootDataType.a<LootTable> createLootTableValidator() {
        return (lootcollector, lootdataid, loottable) -> {
            loottable.validate(lootcollector.setParams(loottable.getParamSet()).enterElement("{" + lootdataid.type().directory + ":" + lootdataid.location() + "}", lootdataid));
        };
    }

    @FunctionalInterface
    public interface a<T> {

        void run(LootCollector lootcollector, LootDataId<T> lootdataid, T t0);
    }
}
