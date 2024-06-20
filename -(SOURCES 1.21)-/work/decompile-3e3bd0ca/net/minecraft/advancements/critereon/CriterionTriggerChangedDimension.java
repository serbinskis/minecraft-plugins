package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.World;

public class CriterionTriggerChangedDimension extends CriterionTriggerAbstract<CriterionTriggerChangedDimension.a> {

    public CriterionTriggerChangedDimension() {}

    @Override
    public Codec<CriterionTriggerChangedDimension.a> codec() {
        return CriterionTriggerChangedDimension.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ResourceKey<World> resourcekey, ResourceKey<World> resourcekey1) {
        this.trigger(entityplayer, (criteriontriggerchangeddimension_a) -> {
            return criteriontriggerchangeddimension_a.matches(resourcekey, resourcekey1);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ResourceKey<World>> from, Optional<ResourceKey<World>> to) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerChangedDimension.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerChangedDimension.a::player), ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("from").forGetter(CriterionTriggerChangedDimension.a::from), ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("to").forGetter(CriterionTriggerChangedDimension.a::to)).apply(instance, CriterionTriggerChangedDimension.a::new);
        });

        public static Criterion<CriterionTriggerChangedDimension.a> changedDimension() {
            return CriterionTriggers.CHANGED_DIMENSION.createCriterion(new CriterionTriggerChangedDimension.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerChangedDimension.a> changedDimension(ResourceKey<World> resourcekey, ResourceKey<World> resourcekey1) {
            return CriterionTriggers.CHANGED_DIMENSION.createCriterion(new CriterionTriggerChangedDimension.a(Optional.empty(), Optional.of(resourcekey), Optional.of(resourcekey1)));
        }

        public static Criterion<CriterionTriggerChangedDimension.a> changedDimensionTo(ResourceKey<World> resourcekey) {
            return CriterionTriggers.CHANGED_DIMENSION.createCriterion(new CriterionTriggerChangedDimension.a(Optional.empty(), Optional.empty(), Optional.of(resourcekey)));
        }

        public static Criterion<CriterionTriggerChangedDimension.a> changedDimensionFrom(ResourceKey<World> resourcekey) {
            return CriterionTriggers.CHANGED_DIMENSION.createCriterion(new CriterionTriggerChangedDimension.a(Optional.empty(), Optional.of(resourcekey), Optional.empty()));
        }

        public boolean matches(ResourceKey<World> resourcekey, ResourceKey<World> resourcekey1) {
            return this.from.isPresent() && this.from.get() != resourcekey ? false : !this.to.isPresent() || this.to.get() == resourcekey1;
        }
    }
}
