package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;

public class ComponentSerialization {

    public static final Codec<IChatBaseComponent> CODEC = Codec.recursive("Component", ComponentSerialization::createCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, IChatBaseComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(ComponentSerialization.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<IChatBaseComponent>> OPTIONAL_STREAM_CODEC = ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<RegistryFriendlyByteBuf, IChatBaseComponent> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(ComponentSerialization.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<IChatBaseComponent>> TRUSTED_OPTIONAL_STREAM_CODEC = ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<ByteBuf, IChatBaseComponent> TRUSTED_CONTEXT_FREE_STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(ComponentSerialization.CODEC);
    public static final Codec<IChatBaseComponent> FLAT_CODEC = flatCodec(Integer.MAX_VALUE);

    public ComponentSerialization() {}

    public static Codec<IChatBaseComponent> flatCodec(int i) {
        final Codec<String> codec = Codec.string(0, i);

        return new Codec<IChatBaseComponent>() {
            public <T> DataResult<Pair<IChatBaseComponent, T>> decode(DynamicOps<T> dynamicops, T t0) {
                DynamicOps<JsonElement> dynamicops1 = asJsonOps(dynamicops);

                return codec.decode(dynamicops, t0).flatMap((pair) -> {
                    try {
                        JsonElement jsonelement = JsonParser.parseString((String) pair.getFirst());

                        return ComponentSerialization.CODEC.parse(dynamicops1, jsonelement).map((ichatbasecomponent) -> {
                            return Pair.of(ichatbasecomponent, pair.getSecond());
                        });
                    } catch (JsonParseException jsonparseexception) {
                        Objects.requireNonNull(jsonparseexception);
                        return DataResult.error(jsonparseexception::getMessage);
                    }
                });
            }

            public <T> DataResult<T> encode(IChatBaseComponent ichatbasecomponent, DynamicOps<T> dynamicops, T t0) {
                DynamicOps<JsonElement> dynamicops1 = asJsonOps(dynamicops);

                return ComponentSerialization.CODEC.encodeStart(dynamicops1, ichatbasecomponent).flatMap((jsonelement) -> {
                    try {
                        return codec.encodeStart(dynamicops, ChatDeserializer.toStableString(jsonelement));
                    } catch (IllegalArgumentException illegalargumentexception) {
                        Objects.requireNonNull(illegalargumentexception);
                        return DataResult.error(illegalargumentexception::getMessage);
                    }
                });
            }

            private static <T> DynamicOps<JsonElement> asJsonOps(DynamicOps<T> dynamicops) {
                if (dynamicops instanceof RegistryOps<T> registryops) {
                    return registryops.withParent(JsonOps.INSTANCE);
                } else {
                    return JsonOps.INSTANCE;
                }
            }
        };
    }

    private static IChatMutableComponent createFromList(List<IChatBaseComponent> list) {
        IChatMutableComponent ichatmutablecomponent = ((IChatBaseComponent) list.get(0)).copy();

        for (int i = 1; i < list.size(); ++i) {
            ichatmutablecomponent.append((IChatBaseComponent) list.get(i));
        }

        return ichatmutablecomponent;
    }

    public static <T extends INamable, E> MapCodec<E> createLegacyComponentMatcher(T[] at, Function<T, MapCodec<? extends E>> function, Function<E, T> function1, String s) {
        MapCodec<E> mapcodec = new ComponentSerialization.a<>(Stream.of(at).map(function).toList(), (object) -> {
            return (MapEncoder) function.apply((INamable) function1.apply(object));
        });
        Codec<T> codec = INamable.fromValues(() -> {
            return at;
        });
        MapCodec<E> mapcodec1 = codec.dispatchMap(s, function1, function);
        MapCodec<E> mapcodec2 = new ComponentSerialization.b<>(s, mapcodec1, mapcodec);

        return ExtraCodecs.orCompressed((MapCodec) mapcodec2, mapcodec1);
    }

    private static Codec<IChatBaseComponent> createCodec(Codec<IChatBaseComponent> codec) {
        ComponentContents.a<?>[] acomponentcontents_a = new ComponentContents.a[]{LiteralContents.TYPE, TranslatableContents.TYPE, KeybindContents.TYPE, ScoreContents.TYPE, SelectorContents.TYPE, NbtContents.TYPE};
        MapCodec<ComponentContents> mapcodec = createLegacyComponentMatcher(acomponentcontents_a, ComponentContents.a::codec, ComponentContents::type, "type");
        Codec<IChatBaseComponent> codec1 = RecordCodecBuilder.create((instance) -> {
            return instance.group(mapcodec.forGetter(IChatBaseComponent::getContents), ExtraCodecs.nonEmptyList(codec.listOf()).optionalFieldOf("extra", List.of()).forGetter(IChatBaseComponent::getSiblings), ChatModifier.ChatModifierSerializer.MAP_CODEC.forGetter(IChatBaseComponent::getStyle)).apply(instance, IChatMutableComponent::new);
        });

        return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList(codec.listOf())), codec1).xmap((either) -> {
            return (IChatBaseComponent) either.map((either1) -> {
                return (IChatBaseComponent) either1.map(IChatBaseComponent::literal, ComponentSerialization::createFromList);
            }, (ichatbasecomponent) -> {
                return ichatbasecomponent;
            });
        }, (ichatbasecomponent) -> {
            String s = ichatbasecomponent.tryCollapseToString();

            return s != null ? Either.left(Either.left(s)) : Either.right(ichatbasecomponent);
        });
    }

    private static class a<T> extends MapCodec<T> {

        private final List<MapCodec<? extends T>> codecs;
        private final Function<T, MapEncoder<? extends T>> encoderGetter;

        public a(List<MapCodec<? extends T>> list, Function<T, MapEncoder<? extends T>> function) {
            this.codecs = list;
            this.encoderGetter = function;
        }

        public <S> DataResult<T> decode(DynamicOps<S> dynamicops, MapLike<S> maplike) {
            Iterator iterator = this.codecs.iterator();

            DataResult dataresult;

            do {
                if (!iterator.hasNext()) {
                    return DataResult.error(() -> {
                        return "No matching codec found";
                    });
                }

                MapDecoder<? extends T> mapdecoder = (MapDecoder) iterator.next();

                dataresult = mapdecoder.decode(dynamicops, maplike);
            } while (!dataresult.result().isPresent());

            return dataresult;
        }

        public <S> RecordBuilder<S> encode(T t0, DynamicOps<S> dynamicops, RecordBuilder<S> recordbuilder) {
            MapEncoder<T> mapencoder = (MapEncoder) this.encoderGetter.apply(t0);

            return mapencoder.encode(t0, dynamicops, recordbuilder);
        }

        public <S> Stream<S> keys(DynamicOps<S> dynamicops) {
            return this.codecs.stream().flatMap((mapcodec) -> {
                return mapcodec.keys(dynamicops);
            }).distinct();
        }

        public String toString() {
            return "FuzzyCodec[" + String.valueOf(this.codecs) + "]";
        }
    }

    private static class b<T> extends MapCodec<T> {

        private final String typeFieldName;
        private final MapCodec<T> typed;
        private final MapCodec<T> fuzzy;

        public b(String s, MapCodec<T> mapcodec, MapCodec<T> mapcodec1) {
            this.typeFieldName = s;
            this.typed = mapcodec;
            this.fuzzy = mapcodec1;
        }

        public <O> DataResult<T> decode(DynamicOps<O> dynamicops, MapLike<O> maplike) {
            return maplike.get(this.typeFieldName) != null ? this.typed.decode(dynamicops, maplike) : this.fuzzy.decode(dynamicops, maplike);
        }

        public <O> RecordBuilder<O> encode(T t0, DynamicOps<O> dynamicops, RecordBuilder<O> recordbuilder) {
            return this.fuzzy.encode(t0, dynamicops, recordbuilder);
        }

        public <T1> Stream<T1> keys(DynamicOps<T1> dynamicops) {
            return Stream.concat(this.typed.keys(dynamicops), this.fuzzy.keys(dynamicops)).distinct();
        }
    }
}
