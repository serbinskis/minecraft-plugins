package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;

public record LootItemConditionInverted(LootItemCondition term) implements LootItemCondition {

    public static final MapCodec<LootItemConditionInverted> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LootItemConditions.DIRECT_CODEC.fieldOf("term").forGetter(LootItemConditionInverted::term)).apply(instance, LootItemConditionInverted::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.INVERTED;
    }

    public boolean test(LootTableInfo loottableinfo) {
        return !this.term.test(loottableinfo);
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.term.getReferencedContextParams();
    }

    @Override
    public void validate(LootCollector lootcollector) {
        LootItemCondition.super.validate(lootcollector);
        this.term.validate(lootcollector);
    }

    public static LootItemCondition.a invert(LootItemCondition.a lootitemcondition_a) {
        LootItemConditionInverted lootitemconditioninverted = new LootItemConditionInverted(lootitemcondition_a.build());

        return () -> {
            return lootitemconditioninverted;
        };
    }
}
