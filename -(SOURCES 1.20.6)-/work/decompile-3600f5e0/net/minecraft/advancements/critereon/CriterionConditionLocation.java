package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.levelgen.structure.Structure;

public record CriterionConditionLocation(Optional<CriterionConditionLocation.b> position, Optional<HolderSet<BiomeBase>> biomes, Optional<HolderSet<Structure>> structures, Optional<ResourceKey<World>> dimension, Optional<Boolean> smokey, Optional<CriterionConditionLight> light, Optional<CriterionConditionBlock> block, Optional<CriterionConditionFluid> fluid) {

    public static final Codec<CriterionConditionLocation> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionLocation.b.CODEC.optionalFieldOf("position").forGetter(CriterionConditionLocation::position), RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(CriterionConditionLocation::biomes), RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(CriterionConditionLocation::structures), ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(CriterionConditionLocation::dimension), Codec.BOOL.optionalFieldOf("smokey").forGetter(CriterionConditionLocation::smokey), CriterionConditionLight.CODEC.optionalFieldOf("light").forGetter(CriterionConditionLocation::light), CriterionConditionBlock.CODEC.optionalFieldOf("block").forGetter(CriterionConditionLocation::block), CriterionConditionFluid.CODEC.optionalFieldOf("fluid").forGetter(CriterionConditionLocation::fluid)).apply(instance, CriterionConditionLocation::new);
    });

    public boolean matches(WorldServer worldserver, double d0, double d1, double d2) {
        if (this.position.isPresent() && !((CriterionConditionLocation.b) this.position.get()).matches(d0, d1, d2)) {
            return false;
        } else if (this.dimension.isPresent() && this.dimension.get() != worldserver.dimension()) {
            return false;
        } else {
            BlockPosition blockposition = BlockPosition.containing(d0, d1, d2);
            boolean flag = worldserver.isLoaded(blockposition);

            return this.biomes.isPresent() && (!flag || !((HolderSet) this.biomes.get()).contains(worldserver.getBiome(blockposition))) ? false : (this.structures.isPresent() && (!flag || !worldserver.structureManager().getStructureWithPieceAt(blockposition, (HolderSet) this.structures.get()).isValid()) ? false : (this.smokey.isPresent() && (!flag || (Boolean) this.smokey.get() != BlockCampfire.isSmokeyPos(worldserver, blockposition)) ? false : (this.light.isPresent() && !((CriterionConditionLight) this.light.get()).matches(worldserver, blockposition) ? false : (this.block.isPresent() && !((CriterionConditionBlock) this.block.get()).matches(worldserver, blockposition) ? false : !this.fluid.isPresent() || ((CriterionConditionFluid) this.fluid.get()).matches(worldserver, blockposition)))));
        }
    }

    private static record b(CriterionConditionValue.DoubleRange x, CriterionConditionValue.DoubleRange y, CriterionConditionValue.DoubleRange z) {

        public static final Codec<CriterionConditionLocation.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("x", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionLocation.b::x), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("y", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionLocation.b::y), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("z", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionLocation.b::z)).apply(instance, CriterionConditionLocation.b::new);
        });

        static Optional<CriterionConditionLocation.b> of(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange, CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange1, CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange2) {
            return criterionconditionvalue_doublerange.isAny() && criterionconditionvalue_doublerange1.isAny() && criterionconditionvalue_doublerange2.isAny() ? Optional.empty() : Optional.of(new CriterionConditionLocation.b(criterionconditionvalue_doublerange, criterionconditionvalue_doublerange1, criterionconditionvalue_doublerange2));
        }

        public boolean matches(double d0, double d1, double d2) {
            return this.x.matches(d0) && this.y.matches(d1) && this.z.matches(d2);
        }
    }

    public static class a {

        private CriterionConditionValue.DoubleRange x;
        private CriterionConditionValue.DoubleRange y;
        private CriterionConditionValue.DoubleRange z;
        private Optional<HolderSet<BiomeBase>> biomes;
        private Optional<HolderSet<Structure>> structures;
        private Optional<ResourceKey<World>> dimension;
        private Optional<Boolean> smokey;
        private Optional<CriterionConditionLight> light;
        private Optional<CriterionConditionBlock> block;
        private Optional<CriterionConditionFluid> fluid;

        public a() {
            this.x = CriterionConditionValue.DoubleRange.ANY;
            this.y = CriterionConditionValue.DoubleRange.ANY;
            this.z = CriterionConditionValue.DoubleRange.ANY;
            this.biomes = Optional.empty();
            this.structures = Optional.empty();
            this.dimension = Optional.empty();
            this.smokey = Optional.empty();
            this.light = Optional.empty();
            this.block = Optional.empty();
            this.fluid = Optional.empty();
        }

        public static CriterionConditionLocation.a location() {
            return new CriterionConditionLocation.a();
        }

        public static CriterionConditionLocation.a inBiome(Holder<BiomeBase> holder) {
            return location().setBiomes(HolderSet.direct(holder));
        }

        public static CriterionConditionLocation.a inDimension(ResourceKey<World> resourcekey) {
            return location().setDimension(resourcekey);
        }

        public static CriterionConditionLocation.a inStructure(Holder<Structure> holder) {
            return location().setStructures(HolderSet.direct(holder));
        }

        public static CriterionConditionLocation.a atYLocation(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            return location().setY(criterionconditionvalue_doublerange);
        }

        public CriterionConditionLocation.a setX(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            this.x = criterionconditionvalue_doublerange;
            return this;
        }

        public CriterionConditionLocation.a setY(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            this.y = criterionconditionvalue_doublerange;
            return this;
        }

        public CriterionConditionLocation.a setZ(CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange) {
            this.z = criterionconditionvalue_doublerange;
            return this;
        }

        public CriterionConditionLocation.a setBiomes(HolderSet<BiomeBase> holderset) {
            this.biomes = Optional.of(holderset);
            return this;
        }

        public CriterionConditionLocation.a setStructures(HolderSet<Structure> holderset) {
            this.structures = Optional.of(holderset);
            return this;
        }

        public CriterionConditionLocation.a setDimension(ResourceKey<World> resourcekey) {
            this.dimension = Optional.of(resourcekey);
            return this;
        }

        public CriterionConditionLocation.a setLight(CriterionConditionLight.a criterionconditionlight_a) {
            this.light = Optional.of(criterionconditionlight_a.build());
            return this;
        }

        public CriterionConditionLocation.a setBlock(CriterionConditionBlock.a criterionconditionblock_a) {
            this.block = Optional.of(criterionconditionblock_a.build());
            return this;
        }

        public CriterionConditionLocation.a setFluid(CriterionConditionFluid.a criterionconditionfluid_a) {
            this.fluid = Optional.of(criterionconditionfluid_a.build());
            return this;
        }

        public CriterionConditionLocation.a setSmokey(boolean flag) {
            this.smokey = Optional.of(flag);
            return this;
        }

        public CriterionConditionLocation build() {
            Optional<CriterionConditionLocation.b> optional = CriterionConditionLocation.b.of(this.x, this.y, this.z);

            return new CriterionConditionLocation(optional, this.biomes, this.structures, this.dimension, this.smokey, this.light, this.block, this.fluid);
        }
    }
}
