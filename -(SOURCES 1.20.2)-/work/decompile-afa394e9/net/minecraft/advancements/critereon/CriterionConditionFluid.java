package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;

public record CriterionConditionFluid(Optional<TagKey<FluidType>> tag, Optional<Holder<FluidType>> fluid, Optional<CriterionTriggerProperties> properties) {

    public static final Codec<CriterionConditionFluid> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(TagKey.codec(Registries.FLUID), "tag").forGetter(CriterionConditionFluid::tag), ExtraCodecs.strictOptionalField(BuiltInRegistries.FLUID.holderByNameCodec(), "fluid").forGetter(CriterionConditionFluid::fluid), ExtraCodecs.strictOptionalField(CriterionTriggerProperties.CODEC, "state").forGetter(CriterionConditionFluid::properties)).apply(instance, CriterionConditionFluid::new);
    });

    public boolean matches(WorldServer worldserver, BlockPosition blockposition) {
        if (!worldserver.isLoaded(blockposition)) {
            return false;
        } else {
            Fluid fluid = worldserver.getFluidState(blockposition);

            return this.tag.isPresent() && !fluid.is((TagKey) this.tag.get()) ? false : (this.fluid.isPresent() && !fluid.is((FluidType) ((Holder) this.fluid.get()).value()) ? false : !this.properties.isPresent() || ((CriterionTriggerProperties) this.properties.get()).matches(fluid));
        }
    }

    public static class a {

        private Optional<Holder<FluidType>> fluid = Optional.empty();
        private Optional<TagKey<FluidType>> fluids = Optional.empty();
        private Optional<CriterionTriggerProperties> properties = Optional.empty();

        private a() {}

        public static CriterionConditionFluid.a fluid() {
            return new CriterionConditionFluid.a();
        }

        public CriterionConditionFluid.a of(FluidType fluidtype) {
            this.fluid = Optional.of(fluidtype.builtInRegistryHolder());
            return this;
        }

        public CriterionConditionFluid.a of(TagKey<FluidType> tagkey) {
            this.fluids = Optional.of(tagkey);
            return this;
        }

        public CriterionConditionFluid.a setProperties(CriterionTriggerProperties criteriontriggerproperties) {
            this.properties = Optional.of(criteriontriggerproperties);
            return this;
        }

        public CriterionConditionFluid build() {
            return new CriterionConditionFluid(this.fluids, this.fluid, this.properties);
        }
    }
}
