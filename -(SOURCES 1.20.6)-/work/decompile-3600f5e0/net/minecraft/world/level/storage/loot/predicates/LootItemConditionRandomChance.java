package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public record LootItemConditionRandomChance(float probability) implements LootItemCondition {

    public static final MapCodec<LootItemConditionRandomChance> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("chance").forGetter(LootItemConditionRandomChance::probability)).apply(instance, LootItemConditionRandomChance::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE;
    }

    public boolean test(LootTableInfo loottableinfo) {
        return loottableinfo.getRandom().nextFloat() < this.probability;
    }

    public static LootItemCondition.a randomChance(float f) {
        return () -> {
            return new LootItemConditionRandomChance(f);
        };
    }
}
