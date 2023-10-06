package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.IBlockDataHolder;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.material.Fluid;

public record CriterionTriggerProperties(List<CriterionTriggerProperties.c> properties) {

    private static final Codec<List<CriterionTriggerProperties.c>> PROPERTIES_CODEC = Codec.unboundedMap(Codec.STRING, CriterionTriggerProperties.e.CODEC).xmap((map) -> {
        return map.entrySet().stream().map((entry) -> {
            return new CriterionTriggerProperties.c((String) entry.getKey(), (CriterionTriggerProperties.e) entry.getValue());
        }).toList();
    }, (list) -> {
        return (Map) list.stream().collect(Collectors.toMap(CriterionTriggerProperties.c::name, CriterionTriggerProperties.c::valueMatcher));
    });
    public static final Codec<CriterionTriggerProperties> CODEC = CriterionTriggerProperties.PROPERTIES_CODEC.xmap(CriterionTriggerProperties::new, CriterionTriggerProperties::properties);

    public <S extends IBlockDataHolder<?, S>> boolean matches(BlockStateList<?, S> blockstatelist, S s0) {
        Iterator iterator = this.properties.iterator();

        CriterionTriggerProperties.c criteriontriggerproperties_c;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            criteriontriggerproperties_c = (CriterionTriggerProperties.c) iterator.next();
        } while (criteriontriggerproperties_c.match(blockstatelist, s0));

        return false;
    }

    public boolean matches(IBlockData iblockdata) {
        return this.matches(iblockdata.getBlock().getStateDefinition(), iblockdata);
    }

    public boolean matches(Fluid fluid) {
        return this.matches(fluid.getType().getStateDefinition(), fluid);
    }

    public Optional<String> checkState(BlockStateList<?, ?> blockstatelist) {
        Iterator iterator = this.properties.iterator();

        Optional optional;

        do {
            if (!iterator.hasNext()) {
                return Optional.empty();
            }

            CriterionTriggerProperties.c criteriontriggerproperties_c = (CriterionTriggerProperties.c) iterator.next();

            optional = criteriontriggerproperties_c.checkState(blockstatelist);
        } while (!optional.isPresent());

        return optional;
    }

    public void checkState(BlockStateList<?, ?> blockstatelist, Consumer<String> consumer) {
        this.properties.forEach((criteriontriggerproperties_c) -> {
            criteriontriggerproperties_c.checkState(blockstatelist).ifPresent(consumer);
        });
    }

    public static Optional<CriterionTriggerProperties> fromJson(@Nullable JsonElement jsonelement) {
        return jsonelement != null && !jsonelement.isJsonNull() ? Optional.of((CriterionTriggerProperties) SystemUtils.getOrThrow(CriterionTriggerProperties.CODEC.parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new)) : Optional.empty();
    }

    public JsonElement serializeToJson() {
        return (JsonElement) SystemUtils.getOrThrow(CriterionTriggerProperties.CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
    }

    private static record c(String name, CriterionTriggerProperties.e valueMatcher) {

        public <S extends IBlockDataHolder<?, S>> boolean match(BlockStateList<?, S> blockstatelist, S s0) {
            IBlockState<?> iblockstate = blockstatelist.getProperty(this.name);

            return iblockstate != null && this.valueMatcher.match(s0, iblockstate);
        }

        public Optional<String> checkState(BlockStateList<?, ?> blockstatelist) {
            IBlockState<?> iblockstate = blockstatelist.getProperty(this.name);

            return iblockstate != null ? Optional.empty() : Optional.of(this.name);
        }
    }

    private interface e {

        Codec<CriterionTriggerProperties.e> CODEC = Codec.either(CriterionTriggerProperties.b.CODEC, CriterionTriggerProperties.d.CODEC).xmap((either) -> {
            return (CriterionTriggerProperties.e) either.map((criteriontriggerproperties_b) -> {
                return criteriontriggerproperties_b;
            }, (criteriontriggerproperties_d) -> {
                return criteriontriggerproperties_d;
            });
        }, (criteriontriggerproperties_e) -> {
            if (criteriontriggerproperties_e instanceof CriterionTriggerProperties.b) {
                CriterionTriggerProperties.b criteriontriggerproperties_b = (CriterionTriggerProperties.b) criteriontriggerproperties_e;

                return Either.left(criteriontriggerproperties_b);
            } else if (criteriontriggerproperties_e instanceof CriterionTriggerProperties.d) {
                CriterionTriggerProperties.d criteriontriggerproperties_d = (CriterionTriggerProperties.d) criteriontriggerproperties_e;

                return Either.right(criteriontriggerproperties_d);
            } else {
                throw new UnsupportedOperationException();
            }
        });

        <T extends Comparable<T>> boolean match(IBlockDataHolder<?, ?> iblockdataholder, IBlockState<T> iblockstate);
    }

    public static class a {

        private final Builder<CriterionTriggerProperties.c> matchers = ImmutableList.builder();

        private a() {}

        public static CriterionTriggerProperties.a properties() {
            return new CriterionTriggerProperties.a();
        }

        public CriterionTriggerProperties.a hasProperty(IBlockState<?> iblockstate, String s) {
            this.matchers.add(new CriterionTriggerProperties.c(iblockstate.getName(), new CriterionTriggerProperties.b(s)));
            return this;
        }

        public CriterionTriggerProperties.a hasProperty(IBlockState<Integer> iblockstate, int i) {
            return this.hasProperty(iblockstate, Integer.toString(i));
        }

        public CriterionTriggerProperties.a hasProperty(IBlockState<Boolean> iblockstate, boolean flag) {
            return this.hasProperty(iblockstate, Boolean.toString(flag));
        }

        public <T extends Comparable<T> & INamable> CriterionTriggerProperties.a hasProperty(IBlockState<T> iblockstate, T t0) {
            return this.hasProperty(iblockstate, ((INamable) t0).getSerializedName());
        }

        public Optional<CriterionTriggerProperties> build() {
            return Optional.of(new CriterionTriggerProperties(this.matchers.build()));
        }
    }

    private static record d(Optional<String> minValue, Optional<String> maxValue) implements CriterionTriggerProperties.e {

        public static final Codec<CriterionTriggerProperties.d> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "min").forGetter(CriterionTriggerProperties.d::minValue), ExtraCodecs.strictOptionalField(Codec.STRING, "max").forGetter(CriterionTriggerProperties.d::maxValue)).apply(instance, CriterionTriggerProperties.d::new);
        });

        @Override
        public <T extends Comparable<T>> boolean match(IBlockDataHolder<?, ?> iblockdataholder, IBlockState<T> iblockstate) {
            T t0 = iblockdataholder.getValue(iblockstate);
            Optional optional;

            if (this.minValue.isPresent()) {
                optional = iblockstate.getValue((String) this.minValue.get());
                if (optional.isEmpty() || t0.compareTo((Comparable) optional.get()) < 0) {
                    return false;
                }
            }

            if (this.maxValue.isPresent()) {
                optional = iblockstate.getValue((String) this.maxValue.get());
                if (optional.isEmpty() || t0.compareTo((Comparable) optional.get()) > 0) {
                    return false;
                }
            }

            return true;
        }
    }

    private static record b(String value) implements CriterionTriggerProperties.e {

        public static final Codec<CriterionTriggerProperties.b> CODEC = Codec.STRING.xmap(CriterionTriggerProperties.b::new, CriterionTriggerProperties.b::value);

        @Override
        public <T extends Comparable<T>> boolean match(IBlockDataHolder<?, ?> iblockdataholder, IBlockState<T> iblockstate) {
            T t0 = iblockdataholder.getValue(iblockstate);
            Optional<T> optional = iblockstate.getValue(this.value);

            return optional.isPresent() && t0.compareTo((Comparable) optional.get()) == 0;
        }
    }
}
