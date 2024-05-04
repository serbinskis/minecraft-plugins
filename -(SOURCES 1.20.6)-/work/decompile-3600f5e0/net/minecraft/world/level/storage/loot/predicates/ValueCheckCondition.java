package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record ValueCheckCondition(NumberProvider provider, IntRange range) implements LootItemCondition {

    public static final MapCodec<ValueCheckCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(NumberProviders.CODEC.fieldOf("value").forGetter(ValueCheckCondition::provider), IntRange.CODEC.fieldOf("range").forGetter(ValueCheckCondition::range)).apply(instance, ValueCheckCondition::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.VALUE_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return Sets.union(this.provider.getReferencedContextParams(), this.range.getReferencedContextParams());
    }

    public boolean test(LootTableInfo loottableinfo) {
        return this.range.test(loottableinfo, this.provider.getInt(loottableinfo));
    }

    public static LootItemCondition.a hasValue(NumberProvider numberprovider, IntRange intrange) {
        return () -> {
            return new ValueCheckCondition(numberprovider, intrange);
        };
    }
}
