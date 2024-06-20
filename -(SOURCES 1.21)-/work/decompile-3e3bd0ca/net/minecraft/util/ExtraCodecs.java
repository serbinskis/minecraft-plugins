package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Codec.ResultFunction;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
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
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.MinecraftKey;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ExtraCodecs {

    public static final Codec<JsonElement> JSON = converter(JsonOps.INSTANCE);
    public static final Codec<Object> JAVA = converter(JavaOps.INSTANCE);
    public static final Codec<Vector3f> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 3).map((list1) -> {
            return new Vector3f((Float) list1.get(0), (Float) list1.get(1), (Float) list1.get(2));
        });
    }, (vector3f) -> {
        return List.of(vector3f.x(), vector3f.y(), vector3f.z());
    });
    public static final Codec<Vector4f> VECTOR4F = Codec.FLOAT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 4).map((list1) -> {
            return new Vector4f((Float) list1.get(0), (Float) list1.get(1), (Float) list1.get(2), (Float) list1.get(3));
        });
    }, (vector4f) -> {
        return List.of(vector4f.x(), vector4f.y(), vector4f.z(), vector4f.w());
    });
    public static final Codec<Quaternionf> QUATERNIONF_COMPONENTS = Codec.FLOAT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 4).map((list1) -> {
            return (new Quaternionf((Float) list1.get(0), (Float) list1.get(1), (Float) list1.get(2), (Float) list1.get(3))).normalize();
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
    public static final Codec<Quaternionf> QUATERNIONF = Codec.withAlternative(ExtraCodecs.QUATERNIONF_COMPONENTS, ExtraCodecs.AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new));
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
    public static final Codec<Integer> ARGB_COLOR_CODEC = Codec.withAlternative(Codec.INT, ExtraCodecs.VECTOR4F, (vector4f) -> {
        return ColorUtil.b.colorFromFloat(vector4f.w(), vector4f.x(), vector4f.y(), vector4f.z());
    });
    public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE.flatComapMap(UnsignedBytes::toInt, (integer) -> {
        return integer > 255 ? DataResult.error(() -> {
            return "Unsigned byte was too large: " + integer + " > 255";
        }) : DataResult.success(integer.byteValue());
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
    public static final Codec<ExtraCodecs.c> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap((s) -> {
        return s.startsWith("#") ? MinecraftKey.read(s.substring(1)).map((minecraftkey) -> {
            return new ExtraCodecs.c(minecraftkey, true);
        }) : MinecraftKey.read(s).map((minecraftkey) -> {
            return new ExtraCodecs.c(minecraftkey, false);
        });
    }, ExtraCodecs.c::decoratedId);
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
        return instance.group(Codec.STRING.fieldOf("name").forGetter(Property::name), Codec.STRING.fieldOf("value").forGetter(Property::value), Codec.STRING.lenientOptionalFieldOf("signature").forGetter((property) -> {
            return Optional.ofNullable(property.signature());
        })).apply(instance, (s, s1, optional) -> {
            return new Property(s, s1, (String) optional.orElse((Object) null));
        });
    });
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
    public static final Codec<String> PLAYER_NAME = Codec.string(0, 16).validate((s) -> {
        return UtilColor.isValidPlayerName(s) ? DataResult.success(s) : DataResult.error(() -> {
            return "Player name contained disallowed characters: '" + s + "'";
        });
    });
    private static final MapCodec<GameProfile> GAME_PROFILE_WITHOUT_PROPERTIES = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(UUIDUtil.AUTHLIB_CODEC.fieldOf("id").forGetter(GameProfile::getId), ExtraCodecs.PLAYER_NAME.fieldOf("name").forGetter(GameProfile::getName)).apply(instance, GameProfile::new);
    });
    public static final Codec<GameProfile> GAME_PROFILE = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.GAME_PROFILE_WITHOUT_PROPERTIES.forGetter(Function.identity()), ExtraCodecs.PROPERTY_MAP.lenientOptionalFieldOf("properties", new PropertyMap()).forGetter(GameProfile::getProperties)).apply(instance, (gameprofile, propertymap) -> {
            propertymap.forEach((s, property) -> {
                gameprofile.getProperties().put(s, property);
            });
            return gameprofile;
        });
    });
    public static final Codec<String> NON_EMPTY_STRING = Codec.STRING.validate((s) -> {
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
    public static Codec<String> RESOURCE_PATH_CODEC = Codec.STRING.validate((s) -> {
        return !MinecraftKey.isValidPath(s) ? DataResult.error(() -> {
            return "Invalid string to use as a resource path element: " + s;
        }) : DataResult.success(s);
    });

    public ExtraCodecs() {}

    public static <T> Codec<T> converter(DynamicOps<T> dynamicops) {
        return Codec.PASSTHROUGH.xmap((dynamic) -> {
            return dynamic.convert(dynamicops).getValue();
        }, (object) -> {
            return new Dynamic(dynamicops, object);
        });
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
        Codec<I> codec3 = Codec.withAlternative(codec1, codec2);

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
                return "OrElsePartial[" + String.valueOf(a0) + "]";
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
                return "Element with unknown id: " + String.valueOf(object);
            }) : DataResult.success(j);
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
                String s = String.valueOf(codec);

                return s + " orCompressed " + String.valueOf(codec1);
            }
        };
    }

    public static <E> MapCodec<E> orCompressed(final MapCodec<E> mapcodec, final MapCodec<E> mapcodec1) {
        return new MapCodec<E>() {
            public <T> RecordBuilder<T> encode(E e0, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
                return dynamicops.compressMaps() ? mapcodec1.encode(e0, dynamicops, recordbuilder) : mapcodec.encode(e0, dynamicops, recordbuilder);
            }

            public <T> DataResult<E> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
                return dynamicops.compressMaps() ? mapcodec1.decode(dynamicops, maplike) : mapcodec.decode(dynamicops, maplike);
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
                return mapcodec1.keys(dynamicops);
            }

            public String toString() {
                String s = String.valueOf(mapcodec);

                return s + " orCompressed " + String.valueOf(mapcodec1);
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
                String s = String.valueOf(function);

                return "WithLifecycle[" + s + " " + String.valueOf(function1) + "]";
            }
        });
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, Function<E, Lifecycle> function) {
        return overrideLifecycle(codec, function, function);
    }

    public static <K, V> ExtraCodecs.b<K, V> strictUnboundedMap(Codec<K> codec, Codec<V> codec1) {
        return new ExtraCodecs.b<>(codec, codec1);
    }

    private static Codec<Integer> intRangeWithMessage(int i, int j, Function<Integer, String> function) {
        return Codec.INT.validate((integer) -> {
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
        return Codec.FLOAT.validate((ofloat) -> {
            return ofloat.compareTo(f) > 0 && ofloat.compareTo(f1) <= 0 ? DataResult.success(ofloat) : DataResult.error(() -> {
                return (String) function.apply(ofloat);
            });
        });
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
        return codec.validate((list) -> {
            return list.isEmpty() ? DataResult.error(() -> {
                return "List must have contents";
            }) : DataResult.success(list);
        });
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> codec) {
        return codec.validate((holderset) -> {
            return holderset.unwrap().right().filter(List::isEmpty).isPresent() ? DataResult.error(() -> {
                return "List must have contents";
            }) : DataResult.success(holderset);
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
                return "ContextRetrievalCodec[" + String.valueOf(function) + "]";
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
                T t0 = function.apply(iterator.next());

                while (iterator.hasNext()) {
                    E e0 = iterator.next();
                    T t1 = function.apply(e0);

                    if (t1 != t0) {
                        return DataResult.error(() -> {
                            String s = String.valueOf(e0);

                            return "Mixed type list: element " + s + " had type " + String.valueOf(t1) + ", but list is of type " + String.valueOf(t0);
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
                        String s = String.valueOf(t0);

                        return "Caught exception decoding " + s + ": " + exception.getMessage();
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

    public static <K, V> Codec<Map<K, V>> sizeLimitedMap(Codec<Map<K, V>> codec, int i) {
        return codec.validate((map) -> {
            return map.size() > i ? DataResult.error(() -> {
                int j = map.size();

                return "Map is too long: " + j + ", expected range [0-" + i + "]";
            }) : DataResult.success(map);
        });
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> codec) {
        return Codec.unboundedMap(codec, Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    /** @deprecated */
    @Deprecated
    public static <K, V> MapCodec<V> dispatchOptionalValue(final String s, final String s1, final Codec<K> codec, final Function<? super V, ? extends K> function, final Function<? super K, ? extends Codec<? extends V>> function1) {
        return new MapCodec<V>() {
            public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
                return Stream.of(dynamicops.createString(s), dynamicops.createString(s1));
            }

            public <T> DataResult<V> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
                T t0 = maplike.get(s);

                return t0 == null ? DataResult.error(() -> {
                    return "Missing \"" + s + "\" in: " + String.valueOf(maplike);
                }) : codec.decode(dynamicops, t0).flatMap((pair) -> {
                    Object object = maplike.get(s1);

                    Objects.requireNonNull(dynamicops);
                    T t1 = Objects.requireNonNullElseGet(object, dynamicops::emptyMap);

                    return ((Codec) function1.apply(pair.getFirst())).decode(dynamicops, t1).map(Pair::getFirst);
                });
            }

            public <T> RecordBuilder<T> encode(V v0, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
                K k0 = function.apply(v0);

                recordbuilder.add(s, codec.encodeStart(dynamicops, k0));
                DataResult<T> dataresult = this.encode((Codec) function1.apply(k0), v0, dynamicops);

                if (dataresult.result().isEmpty() || !Objects.equals(dataresult.result().get(), dynamicops.emptyMap())) {
                    recordbuilder.add(s1, dataresult);
                }

                return recordbuilder;
            }

            private <T, V2 extends V> DataResult<T> encode(Codec<V2> codec1, V v0, DynamicOps<T> dynamicops) {
                return codec1.encodeStart(dynamicops, v0);
            }
        };
    }

    public static <A> Codec<Optional<A>> optionalEmptyMap(final Codec<A> codec) {
        return new Codec<Optional<A>>() {
            public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> dynamicops, T t0) {
                return isEmptyMap(dynamicops, t0) ? DataResult.success(Pair.of(Optional.empty(), t0)) : codec.decode(dynamicops, t0).map((pair) -> {
                    return pair.mapFirst(Optional::of);
                });
            }

            private static <T> boolean isEmptyMap(DynamicOps<T> dynamicops, T t0) {
                Optional<MapLike<T>> optional = dynamicops.getMap(t0).result();

                return optional.isPresent() && ((MapLike) optional.get()).entries().findAny().isEmpty();
            }

            public <T> DataResult<T> encode(Optional<A> optional, DynamicOps<T> dynamicops, T t0) {
                return optional.isEmpty() ? DataResult.success(dynamicops.emptyMap()) : codec.encode(optional.get(), dynamicops, t0);
            }
        };
    }

    public static record b<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements Codec<Map<K, V>>, BaseMapCodec<K, V> {

        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
            Builder<K, V> builder = ImmutableMap.builder();
            Iterator iterator = maplike.entries().toList().iterator();

            while (iterator.hasNext()) {
                Pair<T, T> pair = (Pair) iterator.next();
                DataResult<K> dataresult = this.keyCodec().parse(dynamicops, pair.getFirst());
                DataResult<V> dataresult1 = this.elementCodec().parse(dynamicops, pair.getSecond());
                DataResult<Pair<K, V>> dataresult2 = dataresult.apply2stable(Pair::of, dataresult1);
                Optional<Error<Pair<K, V>>> optional = dataresult2.error();

                if (optional.isPresent()) {
                    String s = ((Error) optional.get()).message();

                    return DataResult.error(() -> {
                        if (dataresult.result().isPresent()) {
                            String s1 = String.valueOf(dataresult.result().get());

                            return "Map entry '" + s1 + "' : " + s;
                        } else {
                            return s;
                        }
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
            String s = String.valueOf(this.keyCodec);

            return "StrictUnboundedMapCodec[" + s + " -> " + String.valueOf(this.elementCodec) + "]";
        }
    }

    public static record c(MinecraftKey id, boolean tag) {

        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? "#" + String.valueOf(this.id) : this.id.toString();
        }
    }
}
