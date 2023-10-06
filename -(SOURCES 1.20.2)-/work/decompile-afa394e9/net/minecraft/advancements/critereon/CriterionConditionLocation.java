package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.levelgen.structure.Structure;

public record CriterionConditionLocation(Optional<CriterionConditionLocation.b> position, Optional<ResourceKey<BiomeBase>> biome, Optional<ResourceKey<Structure>> structure, Optional<ResourceKey<World>> dimension, Optional<Boolean> smokey, Optional<CriterionConditionLight> light, Optional<CriterionConditionBlock> block, Optional<CriterionConditionFluid> fluid) {

    public static final Codec<CriterionConditionLocation> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionLocation.b.CODEC, "position").forGetter(CriterionConditionLocation::position), ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.BIOME), "biome").forGetter(CriterionConditionLocation::biome), ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.STRUCTURE), "structure").forGetter(CriterionConditionLocation::structure), ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.DIMENSION), "dimension").forGetter(CriterionConditionLocation::dimension), ExtraCodecs.strictOptionalField(Codec.BOOL, "smokey").forGetter(CriterionConditionLocation::smokey), ExtraCodecs.strictOptionalField(CriterionConditionLight.CODEC, "light").forGetter(CriterionConditionLocation::light), ExtraCodecs.strictOptionalField(CriterionConditionBlock.CODEC, "block").forGetter(CriterionConditionLocation::block), ExtraCodecs.strictOptionalField(CriterionConditionFluid.CODEC, "fluid").forGetter(CriterionConditionLocation::fluid)).apply(instance, CriterionConditionLocation::new);
    });

    private static Optional<CriterionConditionLocation> of(Optional<CriterionConditionLocation.b> optional, Optional<ResourceKey<BiomeBase>> optional1, Optional<ResourceKey<Structure>> optional2, Optional<ResourceKey<World>> optional3, Optional<Boolean> optional4, Optional<CriterionConditionLight> optional5, Optional<CriterionConditionBlock> optional6, Optional<CriterionConditionFluid> optional7) {
        return optional.isEmpty() && optional1.isEmpty() && optional2.isEmpty() && optional3.isEmpty() && optional4.isEmpty() && optional5.isEmpty() && optional6.isEmpty() && optional7.isEmpty() ? Optional.empty() : Optional.of(new CriterionConditionLocation(optional, optional1, optional2, optional3, optional4, optional5, optional6, optional7));
    }

    public boolean matches(WorldServer worldserver, double d0, double d1, double d2) {
        if (this.position.isPresent() && !((CriterionConditionLocation.b) this.position.get()).matches(d0, d1, d2)) {
            return false;
        } else if (this.dimension.isPresent() && this.dimension.get() != worldserver.dimension()) {
            return false;
        } else {
            BlockPosition blockposition = BlockPosition.containing(d0, d1, d2);
            boolean flag = worldserver.isLoaded(blockposition);

            return this.biome.isPresent() && (!flag || !worldserver.getBiome(blockposition).is((ResourceKey) this.biome.get())) ? false : (this.structure.isPresent() && (!flag || !worldserver.structureManager().getStructureWithPieceAt(blockposition, (ResourceKey) this.structure.get()).isValid()) ? false : (this.smokey.isPresent() && (!flag || (Boolean) this.smokey.get() != BlockCampfire.isSmokeyPos(worldserver, blockposition)) ? false : (this.light.isPresent() && !((CriterionConditionLight) this.light.get()).matches(worldserver, blockposition) ? false : (this.block.isPresent() && !((CriterionConditionBlock) this.block.get()).matches(worldserver, blockposition) ? false : !this.fluid.isPresent() || ((CriterionConditionFluid) this.fluid.get()).matches(worldserver, blockposition)))));
        }
    }

    public JsonElement serializeToJson() {
        return (JsonElement) SystemUtils.getOrThrow(CriterionConditionLocation.CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    public static Optional<CriterionConditionLocation> fromJson(@Nullable JsonElement jsonelement) {
        return jsonelement != null && !jsonelement.isJsonNull() ? Optional.of((CriterionConditionLocation) SystemUtils.getOrThrow(CriterionConditionLocation.CODEC.parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new)) : Optional.empty();
    }

    private static record b(CriterionConditionValue.DoubleRange x, CriterionConditionValue.DoubleRange y, CriterionConditionValue.DoubleRange z) {

        public static final Codec<CriterionConditionLocation.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionValue.DoubleRange.CODEC, "x", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionLocation.b::x), ExtraCodecs.strictOptionalField(CriterionConditionValue.DoubleRange.CODEC, "y", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionLocation.b::y), ExtraCodecs.strictOptionalField(CriterionConditionValue.DoubleRange.CODEC, "z", CriterionConditionValue.DoubleRange.ANY).forGetter(CriterionConditionLocation.b::z)).apply(instance, CriterionConditionLocation.b::new);
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
        private Optional<ResourceKey<BiomeBase>> biome;
        private Optional<ResourceKey<Structure>> structure;
        private Optional<ResourceKey<World>> dimension;
        private Optional<Boolean> smokey;
        private Optional<CriterionConditionLight> light;
        private Optional<CriterionConditionBlock> block;
        private Optional<CriterionConditionFluid> fluid;

        public a() {
            this.x = CriterionConditionValue.DoubleRange.ANY;
            this.y = CriterionConditionValue.DoubleRange.ANY;
            this.z = CriterionConditionValue.DoubleRange.ANY;
            this.biome = Optional.empty();
            this.structure = Optional.empty();
            this.dimension = Optional.empty();
            this.smokey = Optional.empty();
            this.light = Optional.empty();
            this.block = Optional.empty();
            this.fluid = Optional.empty();
        }

        public static CriterionConditionLocation.a location() {
            return new CriterionConditionLocation.a();
        }

        public static CriterionConditionLocation.a inBiome(ResourceKey<BiomeBase> resourcekey) {
            return location().setBiome(resourcekey);
        }

        public static CriterionConditionLocation.a inDimension(ResourceKey<World> resourcekey) {
            return location().setDimension(resourcekey);
        }

        public static CriterionConditionLocation.a inStructure(ResourceKey<Structure> resourcekey) {
            return location().setStructure(resourcekey);
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

        public CriterionConditionLocation.a setBiome(ResourceKey<BiomeBase> resourcekey) {
            this.biome = Optional.of(resourcekey);
            return this;
        }

        public CriterionConditionLocation.a setStructure(ResourceKey<Structure> resourcekey) {
            this.structure = Optional.of(resourcekey);
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

            return new CriterionConditionLocation(optional, this.biome, this.structure, this.dimension, this.smokey, this.light, this.block, this.fluid);
        }
    }
}
