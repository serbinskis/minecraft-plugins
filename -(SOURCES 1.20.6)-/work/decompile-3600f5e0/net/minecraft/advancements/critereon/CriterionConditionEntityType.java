package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypes;

public record CriterionConditionEntityType(HolderSet<EntityTypes<?>> types) {

    public static final Codec<CriterionConditionEntityType> CODEC = RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).xmap(CriterionConditionEntityType::new, CriterionConditionEntityType::types);

    public static CriterionConditionEntityType of(EntityTypes<?> entitytypes) {
        return new CriterionConditionEntityType(HolderSet.direct(entitytypes.builtInRegistryHolder()));
    }

    public static CriterionConditionEntityType of(TagKey<EntityTypes<?>> tagkey) {
        return new CriterionConditionEntityType(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(tagkey));
    }

    public boolean matches(EntityTypes<?> entitytypes) {
        return entitytypes.is(this.types);
    }
}
