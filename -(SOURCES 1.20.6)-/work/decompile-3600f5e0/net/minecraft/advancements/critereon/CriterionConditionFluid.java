package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;

public record CriterionConditionFluid(Optional<HolderSet<FluidType>> fluids, Optional<CriterionTriggerProperties> properties) {

    public static final Codec<CriterionConditionFluid> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.FLUID).optionalFieldOf("fluids").forGetter(CriterionConditionFluid::fluids), CriterionTriggerProperties.CODEC.optionalFieldOf("state").forGetter(CriterionConditionFluid::properties)).apply(instance, CriterionConditionFluid::new);
    });

    public boolean matches(WorldServer worldserver, BlockPosition blockposition) {
        if (!worldserver.isLoaded(blockposition)) {
            return false;
        } else {
            Fluid fluid = worldserver.getFluidState(blockposition);

            return this.fluids.isPresent() && !fluid.is((HolderSet) this.fluids.get()) ? false : !this.properties.isPresent() || ((CriterionTriggerProperties) this.properties.get()).matches(fluid);
        }
    }

    public static class a {

        private Optional<HolderSet<FluidType>> fluids = Optional.empty();
        private Optional<CriterionTriggerProperties> properties = Optional.empty();

        private a() {}

        public static CriterionConditionFluid.a fluid() {
            return new CriterionConditionFluid.a();
        }

        public CriterionConditionFluid.a of(FluidType fluidtype) {
            this.fluids = Optional.of(HolderSet.direct(fluidtype.builtInRegistryHolder()));
            return this;
        }

        public CriterionConditionFluid.a of(HolderSet<FluidType> holderset) {
            this.fluids = Optional.of(holderset);
            return this;
        }

        public CriterionConditionFluid.a setProperties(CriterionTriggerProperties criteriontriggerproperties) {
            this.properties = Optional.of(criteriontriggerproperties);
            return this;
        }

        public CriterionConditionFluid build() {
            return new CriterionConditionFluid(this.fluids, this.properties);
        }
    }
}
