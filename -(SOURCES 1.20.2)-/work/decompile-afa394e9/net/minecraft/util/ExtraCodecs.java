package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Codec.ResultFunction;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.PartialResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapCodec.MapCodecCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ExtraCodecs {

    public static final Codec<JsonElement> JSON = Codec.PASSTHROUGH.xmap((dynamic) -> {
        return (JsonElement) dynamic.convert(JsonOps.INSTANCE).getValue();
    }, (jsonelement) -> {
        return new Dynamic(JsonOps.INSTANCE, jsonelement);
    });
    public static final Codec<IChatBaseComponent> COMPONENT = adaptJsonSerializer(IChatBaseComponent.ChatSerializer::fromJson, IChatBaseComponent.ChatSerializer::toJsonTree);
    public static final Codec<IChatBaseComponent> FLAT_COMPONENT = Codec.STRING.flatXmap((s) -> {
        try {
            return DataResult.success(IChatBaseComponent.ChatSerializer.fromJson(s));
        } catch (JsonParseException jsonparseexception) {
            Objects.requireNonNull(jsonparseexception);
            return DataResult.error(jsonparseexception::getMessage);
        }
    }, (ichatbasecomponent) -> {
        try {
            return DataResult.success(IChatBaseComponent.ChatSerializer.toJson(ichatbasecomponent));
        } catch (IllegalArgumentException illegalargumentexception) {
            Objects.requireNonNull(illegalargumentexception);
            return DataResult.error(illegalargumentexception::getMessage);
        }
    });
    public static final Codec<Vector3f> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 3).map((list1) -> {
            return new Vector3f((Float) list1.get(0), (Float) list1.get(1), (Float) list1.get(2));
        });
    }, (vector3f) -> {
        return List.of(vector3f.x(), vector3f.y(), vector3f.z());
    });
    public static final Codec<Quaternionf> QUATERNIONF_COMPONENTS = Codec.FLOAT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 4).map((list1) -> {
            return new Quaternionf((Float) list1.get(0), (Float) list1.get(1), (Float) list1.get(2), (Float) list1.get(3));
        });
    }, (quaternionf) -> {
        return List.of(quaternionf.x, quaternionf.y, quaternionf.z, quaternionf.w);
    });
    public static final Codec<AxisAngle4f> AXISANGLE4F = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("angle").forGetter((axisangle4f) -> {
            return axisangle4f.angle;
        }), ExtraCodecs.VECTOR3F.fieldOf("axis").forGetter((axisangle4f) -> {
            return new Vector3f(axisangle4f.x, axisangle4f.y, axisangle4f.z);
        })).apply(instance, AxisAngle4f::new);
    });
    public static final Codec<Quaternionf> QUATERNIONF = withAlternative(ExtraCodecs.QUATERNIONF_COMPONENTS, ExtraCodecs.AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new));
    public static Codec<Matrix4f> MATRIX4F = Codec.FLOAT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 16).map((list1) -> {
            Matrix4f matrix4f = new Matrix4f();

            for (int i = 0; i < list1.size(); ++i) {
                matrix4f.setRowColumn(i >> 2, i & 3, (Float) list1.get(i));
            }

            return matrix4f.determineProperties();
        });
    }, (matrix4f) -> {
        FloatArrayList floatarraylist = new FloatArrayList(16);

        for (int i = 0; i < 16; ++i) {
            floatarraylist.add(matrix4f.getRowColumn(i >> 2, i & 3));
        }

        return floatarraylist;
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, (integer) -> {
        return "Value must be non-negative: " + integer;
    });
    public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, (integer) -> {
        return "Value must be positive: " + integer;
    });
    public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, (ofloat) -> {
        return "Value must be positive: " + ofloat;
    });
    public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap((s) -> {
        try {
            return DataResult.success(Pattern.compile(s));
        } catch (PatternSyntaxException patternsyntaxexception) {
            return DataResult.error(() -> {
                return "Invalid regex pattern '" + s + "': " + patternsyntaxexception.getMessage();
            });
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT_ISO8601 = temporalCodec(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
    public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap((s) -> {
        try {
            return DataResult.success(Base64.getDecoder().decode(s));
        } catch (IllegalArgumentException illegalargumentexception) {
            return DataResult.error(() -> {
                return "Malformed base64 string";
            });
        }
    }, (abyte) -> {
        return Base64.getEncoder().encodeToString(abyte);
    });
    public static final Codec<String> ESCAPED_STRING = Codec.STRING.comapFlatMap((s) -> {
        return DataResult.success(StringEscapeUtils.unescapeJava(s));
    }, StringEscapeUtils::escapeJava);
    public static final Codec<ExtraCodecs.f> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap((s) -> {
        return s.startsWith("#") ? MinecraftKey.read(s.substring(1)).map((minecraftkey) -> {
            return new ExtraCodecs.f(minecraftkey, true);
        }) : MinecraftKey.read(s).map((minecraftkey) -> {
            return new ExtraCodecs.f(minecraftkey, false);
        });
    }, ExtraCodecs.f::decoratedId);
    public static final Function<Optional<Long>, OptionalLong> toOptionalLong = (optional) -> {
        return (OptionalLong) optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    };
    public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = (optionallong) -> {
        return optionallong.isPresent() ? Optional.of(optionallong.getAsLong()) : Optional.empty();
    };
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap((longstream) -> {
        return BitSet.valueOf(longstream.toArray());
    }, (bitset) -> {
        return Arrays.stream(bitset.toLongArray());
    });
    private static final Codec<Property> PROPERTY = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.STRING.fieldOf("name").forGetter(Property::name), Codec.STRING.fieldOf("value").forGetter(Property::value), Codec.STRING.optionalFieldOf("signature").forGetter((property) -> {
            return Optional.ofNullable(property.signature());
        })).apply(instance, (s, s1, optional) -> {
            return new Property(s, s1, (String) optional.orElse((Object) null));
        });
    });
    @VisibleForTesting
    public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()), ExtraCodecs.PROPERTY.listOf()).xmap((either) -> {
        PropertyMap propertymap = new PropertyMap();

        either.ifLeft((map) -> {
            map.forEach((s, list) -> {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    String s1 = (String) iterator.next();

                    propertymap.put(s, new Property(s, s1));
                }

            });
        }).ifRight((list) -> {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Property property = (Property) iterator.next();

                propertymap.put(property.name(), property);
            }

        });
        return propertymap;
    }, (propertymap) -> {
        return Either.right(propertymap.values().stream().toList());
    });
    private static final MapCodec<GameProfile> GAME_PROFILE_WITHOUT_PROPERTIES = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(UUIDUtil.AUTHLIB_CODEC.fieldOf("id").forGetter(GameProfile::getId), Codec.STRING.fieldOf("name").forGetter(GameProfile::getName)).apply(instance, GameProfile::new);
    });
    public static final Codec<GameProfile> GAME_PROFILE = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.GAME_PROFILE_WITHOUT_PROPERTIES.forGetter(Function.identity()), ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(GameProfile::getProperties)).apply(instance, (gameprofile, propertymap) -> {
            propertymap.forEach((s, property) -> {
                gameprofile.getProperties().put(s, property);
            });
            return gameprofile;
        });
    });
    public static final Codec<String> NON_EMPTY_STRING = validate((Codec) Codec.STRING, (s) -> {
        return s.isEmpty() ? DataResult.error(() -> {
            return "Expected non-empty string";
        }) : DataResult.success(s);
    });
    public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap((s) -> {
        int[] aint = s.codePoints().toArray();

        return aint.length != 1 ? DataResult.error(() -> {
            return "Expected one codepoint, got: " + s;
        }) : DataResult.success(aint[0]);
    }, Character::toString);
    public static Codec<String> RESOURCE_PATH_CODEC = validate((Codec) Codec.STRING, (s) -> {
        return !MinecraftKey.isValidPath(s) ? DataResult.error(() -> {
            return "Invalid string to use as a resource path element: " + s;
        }) : DataResult.success(s);
    });

    public ExtraCodecs() {}

    /** @deprecated */
    @Deprecated
    public static <T> Codec<T> adaptJsonSerializer(Function<JsonElement, T> function, Function<T, JsonElement> function1) {
        return ExtraCodecs.JSON.flatXmap((jsonelement) -> {
            try {
                return DataResult.success(function.apply(jsonelement));
            } catch (JsonParseException jsonparseexception) {
                Objects.requireNonNull(jsonparseexception);
                return DataResult.error(jsonparseexception::getMessage);
            }
        }, (object) -> {
            try {
                return DataResult.success((JsonElement) function1.apply(object));
            } catch (IllegalArgumentException illegalargumentexception) {
                Objects.requireNonNull(illegalargumentexception);
                return DataResult.error(illegalargumentexception::getMessage);
            }
        });
    }

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec1) {
        return new ExtraCodecs.g<>(codec, codec1);
    }

    public static <P, I> Codec<I> intervalCodec(Codec<P> codec, String s, String s1, BiFunction<P, P, DataResult<I>> bifunction, Function<I, P> function, Function<I, P> function1) {
        Codec<I> codec1 = Codec.list(codec).comapFlatMap((list) -> {
            return SystemUtils.fixedSize(list, 2).flatMap((list1) -> {
                P p0 = list1.get(0);
                P p1 = list1.get(1);

                return (DataResult) bifunction.apply(p0, p1);
            });
        }, (object) -> {
            return ImmutableList.of(function.apply(object), function1.apply(object));
        });
        Codec<I> codec2 = RecordCodecBuilder.create((instance) -> {
            return instance.group(codec.fieldOf(s).forGetter(Pair::getFirst), codec.fieldOf(s1).forGetter(Pair::getSecond)).apply(instance, Pair::of);
        }).comapFlatMap((pair) -> {
            return (DataResult) bifunction.apply(pair.getFirst(), pair.getSecond());
        }, (object) -> {
            return Pair.of(function.apply(object), function1.apply(object));
        });
        Codec<I> codec3 = withAlternative(codec1, codec2);

        return Codec.either(codec, codec3).comapFlatMap((either) -> {
            return (DataResult) either.map((object) -> {
                return (DataResult) bifunction.apply(object, object);
            }, DataResult::success);
        }, (object) -> {
            P p0 = function.apply(object);
            P p1 = function1.apply(object);

            return Objects.equals(p0, p1) ? Either.left(p0) : Either.right(object);
        });
    }

    public static <A> ResultFunction<A> orElsePartial(final A a0) {
        return new ResultFunction<A>() {
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> dynamicops, T t0, DataResult<Pair<A, T>> dataresult) {
                MutableObject<String> mutableobject = new MutableObject();

                Objects.requireNonNull(mutableobject);
                Optional<Pair<A, T>> optional = dataresult.resultOrPartial(mutableobject::setValue);

                return optional.isPresent() ? dataresult : DataResult.error(() -> {
                    return "(" + (String) mutableobject.getValue() + " -> using default)";
                }, Pair.of(a0, t0));
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicops, A a1, DataResult<T> dataresult) {
                return dataresult;
            }

            public String toString() {
                return "OrElsePartial[" + a0 + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> tointfunction, IntFunction<E> intfunction, int i) {
        return Codec.INT.flatXmap((integer) -> {
            return (DataResult) Optional.ofNullable(intfunction.apply(integer)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    return "Unknown element id: " + integer;
                });
            });
        }, (object) -> {
            int j = tointfunction.applyAsInt(object);

            return j == i ? DataResult.error(() -> {
                return "Element with unknown id: " + object;
            }) : DataResult.success(j);
        });
    }

    public static <E> Codec<E> stringResolverCodec(Function<E, String> function, Function<String, E> function1) {
        return Codec.STRING.flatXmap((s) -> {
            return (DataResult) Optional.ofNullable(function1.apply(s)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    return "Unknown element name:" + s;
                });
            });
        }, (object) -> {
            return (DataResult) Optional.ofNullable((String) function.apply(object)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    return "Element with unknown name: " + object;
                });
            });
        });
    }

    public static <E> Codec<E> orCompressed(final Codec<E> codec, final Codec<E> codec1) {
        return new Codec<E>() {
            public <T> DataResult<T> encode(E e0, DynamicOps<T> dynamicops, T t0) {
                return dynamicops.compressMaps() ? codec1.encode(e0, dynamicops, t0) : codec.encode(e0, dynamicops, t0);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicops, T t0) {
                return dynamicops.compressMaps() ? codec1.decode(dynamicops, t0) : codec.decode(dynamicops, t0);
            }

            public String toString() {
                return codec + " orCompressed " + codec1;
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, final Function<E, Lifecycle> function, final Function<E, Lifecycle> function1) {
        return codec.mapResult(new ResultFunction<E>() {
            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> dynamicops, T t0, DataResult<Pair<E, T>> dataresult) {
                return (DataResult) dataresult.result().map((pair) -> {
                    return dataresult.setLifecycle((Lifecycle) function.apply(pair.getFirst()));
                }).orElse(dataresult);
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicops, E e0, DataResult<T> dataresult) {
                return dataresult.setLifecycle((Lifecycle) function1.apply(e0));
            }

            public String toString() {
                return "WithLifecycle[" + function + " " + function1 + "]";
            }
        });
    }

    public static <F, S> ExtraCodecs.b<F, S> either(Codec<F> codec, Codec<S> codec1) {
        return new ExtraCodecs.b<>(codec, codec1);
    }

    public static <K, V> ExtraCodecs.e<K, V> strictUnboundedMap(Codec<K> codec, Codec<V> codec1) {
        return new ExtraCodecs.e<>(codec, codec1);
    }

    public static <T> Codec<T> validate(Codec<T> codec, Function<T, DataResult<T>> function) {
        if (codec instanceof MapCodecCodec) {
            MapCodecCodec<T> mapcodeccodec = (MapCodecCodec) codec;

            return validate(mapcodeccodec.codec(), function).codec();
        } else {
            return codec.flatXmap(function, function);
        }
    }

    public static <T> MapCodec<T> validate(MapCodec<T> mapcodec, Function<T, DataResult<T>> function) {
        return mapcodec.flatXmap(function, function);
    }

    private static Codec<Integer> intRangeWithMessage(int i, int j, Function<Integer, String> function) {
        return validate((Codec) Codec.INT, (integer) -> {
            return integer.compareTo(i) >= 0 && integer.compareTo(j) <= 0 ? DataResult.success(integer) : DataResult.error(() -> {
                return (String) function.apply(integer);
            });
        });
    }

    public static Codec<Integer> intRange(int i, int j) {
        return intRangeWithMessage(i, j, (integer) -> {
            return "Value must be within range [" + i + ";" + j + "]: " + integer;
        });
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float f1, Function<Float, String> function) {
        return validate((Codec) Codec.FLOAT, (ofloat) -> {
            return ofloat.compareTo(f) > 0 && ofloat.compareTo(f1) <= 0 ? DataResult.success(ofloat) : DataResult.error(() -> {
                return (String) function.apply(ofloat);
            });
        });
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
        return validate(codec, (list) -> {
            return list.isEmpty() ? DataResult.error(() -> {
                return "List must have contents";
            }) : DataResult.success(list);
        });
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> codec) {
        return validate(codec, (holderset) -> {
            return holderset.unwrap().right().filter(List::isEmpty).isPresent() ? DataResult.error(() -> {
                return "List must have contents";
            }) : DataResult.success(holderset);
        });
    }

    public static <T> Codec<T> recursive(Function<Codec<T>, Codec<T>> function) {
        return new ExtraCodecs.c<>(function);
    }

    public static <A> Codec<A> lazyInitializedCodec(Supplier<Codec<A>> supplier) {
        return new ExtraCodecs.c<>((codec) -> {
            return (Codec) supplier.get();
        });
    }

    public static <A> MapCodec<Optional<A>> strictOptionalField(Codec<A> codec, String s) {
        return new ExtraCodecs.d<>(s, codec);
    }

    public static <A> MapCodec<A> strictOptionalField(Codec<A> codec, String s, A a0) {
        return strictOptionalField(codec, s).xmap((optional) -> {
            return optional.orElse(a0);
        }, (object) -> {
            return Objects.equals(object, a0) ? Optional.empty() : Optional.of(object);
        });
    }

    public static <E> MapCodec<E> retrieveContext(final Function<DynamicOps<?>, DataResult<E>> function) {
        class a extends MapCodec<E> {

            a() {}

            public <T> RecordBuilder<T> encode(E e0, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
                return recordbuilder;
            }

            public <T> DataResult<E> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
                return (DataResult) function.apply(dynamicops);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + function + "]";
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
                return Stream.empty();
            }
        }

        return new a();
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> function) {
        return (collection) -> {
            Iterator<E> iterator = collection.iterator();

            if (iterator.hasNext()) {
                Object object = function.apply(iterator.next());

                while (iterator.hasNext()) {
                    E e0 = iterator.next();
                    T t0 = function.apply(e0);

                    if (t0 != object) {
                        return DataResult.error(() -> {
                            return "Mixed type list: element " + e0 + " had type " + t0 + ", but list is of type " + object;
                        });
                    }
                }
            }

            return DataResult.success(collection, Lifecycle.stable());
        };
    }

    public static <A> Codec<A> catchDecoderException(final Codec<A> codec) {
        return Codec.of(codec, new Decoder<A>() {
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicops, T t0) {
                try {
                    return codec.decode(dynamicops, t0);
                } catch (Exception exception) {
                    return DataResult.error(() -> {
                        return "Caught exception decoding " + t0 + ": " + exception.getMessage();
                    });
                }
            }
        });
    }

    public static Codec<TemporalAccessor> temporalCodec(DateTimeFormatter datetimeformatter) {
        PrimitiveCodec primitivecodec = Codec.STRING;
        Function function = (s) -> {
            try {
                return DataResult.success(datetimeformatter.parse(s));
            } catch (Exception exception) {
                Objects.requireNonNull(exception);
                return DataResult.error(exception::getMessage);
            }
        };

        Objects.requireNonNull(datetimeformatter);
        return primitivecodec.comapFlatMap(function, datetimeformatter::format);
    }

    public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> mapcodec) {
        return mapcodec.xmap(ExtraCodecs.toOptionalLong, ExtraCodecs.fromOptionalLong);
    }

    public static Codec<String> sizeLimitedString(int i, int j) {
        return validate((Codec) Codec.STRING, (s) -> {
            int k = s.length();

            return k < i ? DataResult.error(() -> {
                return "String \"" + s + "\" is too short: " + k + ", expected range [" + i + "-" + j + "]";
            }) : (k > j ? DataResult.error(() -> {
                return "String \"" + s + "\" is too long: " + k + ", expected range [" + i + "-" + j + "]";
            }) : DataResult.success(s));
        });
    }

    public static <T> Codec<T> withAlternative(Codec<T> codec, Codec<? extends T> codec1) {
        return Codec.either(codec, codec1).xmap((either) -> {
            return either.map((object) -> {
                return object;
            }, (object) -> {
                return object;
            });
        }, Either::left);
    }

    public static <T, U> Codec<T> withAlternative(Codec<T> codec, Codec<U> codec1, Function<U, T> function) {
        return Codec.either(codec, codec1).xmap((either) -> {
            return either.map((object) -> {
                return object;
            }, function);
        }, Either::left);
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> codec) {
        return Codec.unboundedMap(codec, Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    private static record g<F, S> (Codec<F> first, Codec<S> second) implements Codec<Either<F, S>> {

        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicops, T t0) {
            DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(dynamicops, t0).map((pair) -> {
                return pair.mapFirst(Either::left);
            });
            DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(dynamicops, t0).map((pair) -> {
                return pair.mapFirst(Either::right);
            });
            Optional<Pair<Either<F, S>, T>> optional = dataresult.result();
            Optional<Pair<Either<F, S>, T>> optional1 = dataresult1.result();

            return optional.isPresent() && optional1.isPresent() ? DataResult.error(() -> {
                Object object = optional.get();

                return "Both alternatives read successfully, can not pick the correct one; first: " + object + " second: " + optional1.get();
            }, (Pair) optional.get()) : (optional.isPresent() ? dataresult : (optional1.isPresent() ? dataresult1 : dataresult.apply2((pair, pair1) -> {
                return pair1;
            }, dataresult1)));
        }

        public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicops, T t0) {
            return (DataResult) either.map((object) -> {
                return this.first.encode(object, dynamicops, t0);
            }, (object) -> {
                return this.second.encode(object, dynamicops, t0);
            });
        }

        public String toString() {
            return "XorCodec[" + this.first + ", " + this.second + "]";
        }
    }

    public static final class b<F, S> implements Codec<Either<F, S>> {

        private final Codec<F> first;
        private final Codec<S> second;

        public b(Codec<F> codec, Codec<S> codec1) {
            this.first = codec;
            this.second = codec1;
        }

        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicops, T t0) {
            DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(dynamicops, t0).map((pair) -> {
                return pair.mapFirst(Either::left);
            });

            if (dataresult.error().isEmpty()) {
                return dataresult;
            } else {
                DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(dynamicops, t0).map((pair) -> {
                    return pair.mapFirst(Either::right);
                });

                return dataresult1.error().isEmpty() ? dataresult1 : dataresult.apply2((pair, pair1) -> {
                    return pair1;
                }, dataresult1);
            }
        }

        public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicops, T t0) {
            return (DataResult) either.map((object) -> {
                return this.first.encode(object, dynamicops, t0);
            }, (object) -> {
                return this.second.encode(object, dynamicops, t0);
            });
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object != null && this.getClass() == object.getClass()) {
                ExtraCodecs.b<?, ?> extracodecs_b = (ExtraCodecs.b) object;

                return Objects.equals(this.first, extracodecs_b.first) && Objects.equals(this.second, extracodecs_b.second);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.first, this.second});
        }

        public String toString() {
            return "EitherCodec[" + this.first + ", " + this.second + "]";
        }
    }

    public static record e<K, V> (Codec<K> keyCodec, Codec<V> elementCodec) implements Codec<Map<K, V>>, BaseMapCodec<K, V> {

        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
            Builder<K, V> builder = ImmutableMap.builder();
            Iterator iterator = maplike.entries().toList().iterator();

            while (iterator.hasNext()) {
                Pair<T, T> pair = (Pair) iterator.next();
                DataResult<K> dataresult = this.keyCodec().parse(dynamicops, pair.getFirst());
                DataResult<V> dataresult1 = this.elementCodec().parse(dynamicops, pair.getSecond());
                DataResult<Pair<K, V>> dataresult2 = dataresult.apply2stable(Pair::of, dataresult1);

                if (dataresult2.error().isPresent()) {
                    return DataResult.error(() -> {
                        PartialResult<Pair<K, V>> partialresult = (PartialResult) dataresult2.error().get();
                        String s;

                        if (dataresult.result().isPresent()) {
                            Object object = dataresult.result().get();

                            s = "Map entry '" + object + "' : " + partialresult.message();
                        } else {
                            s = partialresult.message();
                        }

                        return s;
                    });
                }

                if (!dataresult2.result().isPresent()) {
                    return DataResult.error(() -> {
                        return "Empty or invalid map contents are not allowed";
                    });
                }

                Pair<K, V> pair1 = (Pair) dataresult2.result().get();

                builder.put(pair1.getFirst(), pair1.getSecond());
            }

            Map<K, V> map = builder.build();

            return DataResult.success(map);
        }

        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> dynamicops, T t0) {
            return dynamicops.getMap(t0).setLifecycle(Lifecycle.stable()).flatMap((maplike) -> {
                return this.decode(dynamicops, maplike);
            }).map((map) -> {
                return Pair.of(map, t0);
            });
        }

        public <T> DataResult<T> encode(Map<K, V> map, DynamicOps<T> dynamicops, T t0) {
            return this.encode(map, dynamicops, dynamicops.mapBuilder()).build(t0);
        }

        public String toString() {
            return "StrictUnboundedMapCodec[" + this.keyCodec + " -> " + this.elementCodec + "]";
        }
    }

    private static class c<T> implements Codec<T> {

        private final Supplier<Codec<T>> wrapped;

        c(Function<Codec<T>, Codec<T>> function) {
            this.wrapped = Suppliers.memoize(() -> {
                return (Codec) function.apply(this);
            });
        }

        public <S> DataResult<Pair<T, S>> decode(DynamicOps<S> dynamicops, S s0) {
            return ((Codec) this.wrapped.get()).decode(dynamicops, s0);
        }

        public <S> DataResult<S> encode(T t0, DynamicOps<S> dynamicops, S s0) {
            return ((Codec) this.wrapped.get()).encode(t0, dynamicops, s0);
        }

        public String toString() {
            return "RecursiveCodec[" + this.wrapped + "]";
        }
    }

    private static final class d<A> extends MapCodec<Optional<A>> {

        private final String name;
        private final Codec<A> elementCodec;

        public d(String s, Codec<A> codec) {
            this.name = s;
            this.elementCodec = codec;
        }

        public <T> DataResult<Optional<A>> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
            T t0 = maplike.get(this.name);

            return t0 == null ? DataResult.success(Optional.empty()) : this.elementCodec.parse(dynamicops, t0).map(Optional::of);
        }

        public <T> RecordBuilder<T> encode(Optional<A> optional, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
            return optional.isPresent() ? recordbuilder.add(this.name, this.elementCodec.encodeStart(dynamicops, optional.get())) : recordbuilder;
        }

        public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
            return Stream.of(dynamicops.createString(this.name));
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (!(object instanceof ExtraCodecs.d)) {
                return false;
            } else {
                ExtraCodecs.d<?> extracodecs_d = (ExtraCodecs.d) object;

                return Objects.equals(this.name, extracodecs_d.name) && Objects.equals(this.elementCodec, extracodecs_d.elementCodec);
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.name, this.elementCodec});
        }

        public String toString() {
            return "StrictOptionalFieldCodec[" + this.name + ": " + this.elementCodec + "]";
        }
    }

    public static record f(MinecraftKey id, boolean tag) {

        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? "#" + this.id : this.id.toString();
        }
    }
}
