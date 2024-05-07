package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.CriterionConditionDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;

public record LootItemConditionDamageSourceProperties(Optional<CriterionConditionDamageSource> predicate) implements LootItemCondition {

    public static final MapCodec<LootItemConditionDamageSourceProperties> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(CriterionConditionDamageSource.CODEC.optionalFieldOf("predicate").forGetter(LootItemConditionDamageSourceProperties::predicate)).apply(instance, LootItemConditionDamageSourceProperties::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.DAMAGE_SOURCE_PROPERTIES;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParameters.ORIGIN, LootContextParameters.DAMAGE_SOURCE);
    }

    public boolean test(LootTableInfo loottableinfo) {
        DamageSource damagesource = (DamageSource) loottableinfo.getParamOrNull(LootContextParameters.DAMAGE_SOURCE);
        Vec3D vec3d = (Vec3D) loottableinfo.getParamOrNull(LootContextParameters.ORIGIN);

        return vec3d != null && damagesource != null ? this.predicate.isEmpty() || ((CriterionConditionDamageSource) this.predicate.get()).matches(loottableinfo.getLevel(), vec3d, damagesource) : false;
    }

    public static LootItemCondition.a hasDamageSource(CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
        return () -> {
            return new LootItemConditionDamageSourceProperties(Optional.of(criterionconditiondamagesource_a.build()));
        };
    }
}
