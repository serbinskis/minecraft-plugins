package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDataType<T> {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(LootItemConditions.CODEC, "predicates", createSimpleValidator());
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(LootItemFunctions.CODEC, "item_modifiers", createSimpleValidator());
    public static final LootDataType<LootTable> TABLE = new LootDataType<>(LootTable.CODEC, "loot_tables", createLootTableValidator());
    private final Codec<T> codec;
    private final String directory;
    private final LootDataType.a<T> validator;

    private LootDataType(Codec<T> codec, String s, LootDataType.a<T> lootdatatype_a) {
        this.codec = codec;
        this.directory = s;
        this.validator = lootdatatype_a;
    }

    public String directory() {
        return this.directory;
    }

    public void runValidation(LootCollector lootcollector, LootDataId<T> lootdataid, T t0) {
        this.validator.run(lootcollector, lootdataid, t0);
    }

    public Optional<T> deserialize(MinecraftKey minecraftkey, JsonElement jsonelement) {
        DataResult<T> dataresult = this.codec.parse(JsonOps.INSTANCE, jsonelement);

        dataresult.error().ifPresent((partialresult) -> {
            LootDataType.LOGGER.error("Couldn't parse element {}:{} - {}", new Object[]{this.directory, minecraftkey, partialresult.message()});
        });
        return dataresult.result();
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(LootDataType.PREDICATE, LootDataType.MODIFIER, LootDataType.TABLE);
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
