package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ConditionalEffect<T>(T effect, Optional<LootItemCondition> requirements) {

    public static Codec<LootItemCondition> conditionCodec(LootContextParameterSet lootcontextparameterset) {
        return LootItemCondition.DIRECT_CODEC.validate((lootitemcondition) -> {
            ProblemReporter.a problemreporter_a = new ProblemReporter.a();
            LootCollector lootcollector = new LootCollector(problemreporter_a, lootcontextparameterset);

            lootitemcondition.validate(lootcollector);
            return (DataResult) problemreporter_a.getReport().map((s) -> {
                return DataResult.error(() -> {
                    return "Validation error in enchantment effect condition: " + s;
                });
            }).orElseGet(() -> {
                return DataResult.success(lootitemcondition);
            });
        });
    }

    public static <T> Codec<ConditionalEffect<T>> codec(Codec<T> codec, LootContextParameterSet lootcontextparameterset) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(codec.fieldOf("effect").forGetter(ConditionalEffect::effect), conditionCodec(lootcontextparameterset).optionalFieldOf("requirements").forGetter(ConditionalEffect::requirements)).apply(instance, ConditionalEffect::new);
        });
    }

    public boolean matches(LootTableInfo loottableinfo) {
        return this.requirements.isEmpty() ? true : ((LootItemCondition) this.requirements.get()).test(loottableinfo);
    }
}
