package net.minecraft.advancements.critereon;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypes;

public record CriterionConditionEntityType(HolderSet<EntityTypes<?>> types) {

    public static final Codec<CriterionConditionEntityType> CODEC = Codec.either(TagKey.hashedCodec(Registries.ENTITY_TYPE), BuiltInRegistries.ENTITY_TYPE.holderByNameCodec()).flatComapMap((either) -> {
        return (CriterionConditionEntityType) either.map((tagkey) -> {
            return new CriterionConditionEntityType(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(tagkey));
        }, (holder) -> {
            return new CriterionConditionEntityType(HolderSet.direct(holder));
        });
    }, (criterionconditionentitytype) -> {
        HolderSet<EntityTypes<?>> holderset = criterionconditionentitytype.types();
        Optional<TagKey<EntityTypes<?>>> optional = holderset.unwrapKey();

        return optional.isPresent() ? DataResult.success(Either.left((TagKey) optional.get())) : (holderset.size() == 1 ? DataResult.success(Either.right(holderset.get(0))) : DataResult.error(() -> {
            return "Entity type set must have a single element, but got " + holderset.size();
        }));
    });

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
