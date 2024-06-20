package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record LootItemConditionRandomChance(NumberProvider chance) implements LootItemCondition {

    public static final MapCodec<LootItemConditionRandomChance> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(NumberProviders.CODEC.fieldOf("chance").forGetter(LootItemConditionRandomChance::chance)).apply(instance, LootItemConditionRandomChance::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE;
    }

    public boolean test(LootTableInfo loottableinfo) {
        float f = this.chance.getFloat(loottableinfo);

        return loottableinfo.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.a randomChance(float f) {
        return () -> {
            return new LootItemConditionRandomChance(ConstantValue.exactly(f));
        };
    }

    public static LootItemCondition.a randomChance(NumberProvider numberprovider) {
        return () -> {
            return new LootItemConditionRandomChance(numberprovider);
        };
    }
}
