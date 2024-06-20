package net.minecraft.network.codec;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ByteBufCodecs {

    int MAX_INITIAL_COLLECTION_SIZE = 65536;
    StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>() {
        public Boolean decode(ByteBuf bytebuf) {
            return bytebuf.readBoolean();
        }

        public void encode(ByteBuf bytebuf, Boolean obool) {
            bytebuf.writeBoolean(obool);
        }
    };
    StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>() {
        public Byte decode(ByteBuf bytebuf) {
            return bytebuf.readByte();
        }

        public void encode(ByteBuf bytebuf, Byte obyte) {
            bytebuf.writeByte(obyte);
        }
    };
    StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>() {
        public Short decode(ByteBuf bytebuf) {
            return bytebuf.readShort();
        }

        public void encode(ByteBuf bytebuf, Short oshort) {
            bytebuf.writeShort(oshort);
        }
    };
    StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf bytebuf) {
            return bytebuf.readUnsignedShort();
        }

        public void encode(ByteBuf bytebuf, Integer integer) {
            bytebuf.writeShort(integer);
        }
    };
    StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf bytebuf) {
            return bytebuf.readInt();
        }

        public void encode(ByteBuf bytebuf, Integer integer) {
            bytebuf.writeInt(integer);
        }
    };
    StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf bytebuf) {
            return VarInt.read(bytebuf);
        }

        public void encode(ByteBuf bytebuf, Integer integer) {
            VarInt.write(bytebuf, integer);
        }
    };
    StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>() {
        public Long decode(ByteBuf bytebuf) {
            return VarLong.read(bytebuf);
        }

        public void encode(ByteBuf bytebuf, Long olong) {
            VarLong.write(bytebuf, olong);
        }
    };
    StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>() {
        public Float decode(ByteBuf bytebuf) {
            return bytebuf.readFloat();
        }

        public void encode(ByteBuf bytebuf, Float ofloat) {
            bytebuf.writeFloat(ofloat);
        }
    };
    StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>() {
        public Double decode(ByteBuf bytebuf) {
            return bytebuf.readDouble();
        }

        public void encode(ByteBuf bytebuf, Double odouble) {
            bytebuf.writeDouble(odouble);
        }
    };
    StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>() {
        public byte[] decode(ByteBuf bytebuf) {
            return PacketDataSerializer.readByteArray(bytebuf);
        }

        public void encode(ByteBuf bytebuf, byte[] abyte) {
            PacketDataSerializer.writeByteArray(bytebuf, abyte);
        }
    };
    StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
    StreamCodec<ByteBuf, NBTBase> TAG = tagCodec(() -> {
        return NBTReadLimiter.create(2097152L);
    });
    StreamCodec<ByteBuf, NBTBase> TRUSTED_TAG = tagCodec(NBTReadLimiter::unlimitedHeap);
    StreamCodec<ByteBuf, NBTTagCompound> COMPOUND_TAG = compoundTagCodec(() -> {
        return NBTReadLimiter.create(2097152L);
    });
    StreamCodec<ByteBuf, NBTTagCompound> TRUSTED_COMPOUND_TAG = compoundTagCodec(NBTReadLimiter::unlimitedHeap);
    StreamCodec<ByteBuf, Optional<NBTTagCompound>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<NBTTagCompound>>() {
        public Optional<NBTTagCompound> decode(ByteBuf bytebuf) {
            return Optional.ofNullable(PacketDataSerializer.readNbt(bytebuf));
        }

        public void encode(ByteBuf bytebuf, Optional<NBTTagCompound> optional) {
            PacketDataSerializer.writeNbt(bytebuf, (NBTBase) optional.orElse((Object) null));
        }
    };
    StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<ByteBuf, Vector3f>() {
        public Vector3f decode(ByteBuf bytebuf) {
            return PacketDataSerializer.readVector3f(bytebuf);
        }

        public void encode(ByteBuf bytebuf, Vector3f vector3f) {
            PacketDataSerializer.writeVector3f(bytebuf, vector3f);
        }
    };
    StreamCodec<ByteBuf, Quaternionf> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionf>() {
        public Quaternionf decode(ByteBuf bytebuf) {
            return PacketDataSerializer.readQuaternion(bytebuf);
        }

        public void encode(ByteBuf bytebuf, Quaternionf quaternionf) {
            PacketDataSerializer.writeQuaternion(bytebuf, quaternionf);
        }
    };
    StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>() {
        private static final int MAX_PROPERTY_NAME_LENGTH = 64;
        private static final int MAX_PROPERTY_VALUE_LENGTH = 32767;
        private static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
        private static final int MAX_PROPERTIES = 16;

        public PropertyMap decode(ByteBuf bytebuf) {
            int i = ByteBufCodecs.readCount(bytebuf, 16);
            PropertyMap propertymap = new PropertyMap();

            for (int j = 0; j < i; ++j) {
                String s = Utf8String.read(bytebuf, 64);
                String s1 = Utf8String.read(bytebuf, 32767);
                String s2 = (String) PacketDataSerializer.readNullable(bytebuf, (bytebuf1) -> {
                    return Utf8String.read(bytebuf1, 1024);
                });
                Property property = new Property(s, s1, s2);

                propertymap.put(property.name(), property);
            }

            return propertymap;
        }

        public void encode(ByteBuf bytebuf, PropertyMap propertymap) {
            ByteBufCodecs.writeCount(bytebuf, propertymap.size(), 16);
            Iterator iterator = propertymap.values().iterator();

            while (iterator.hasNext()) {
                Property property = (Property) iterator.next();

                Utf8String.write(bytebuf, property.name(), 64);
                Utf8String.write(bytebuf, property.value(), 32767);
                PacketDataSerializer.writeNullable(bytebuf, property.signature(), (bytebuf1, s) -> {
                    Utf8String.write(bytebuf1, s, 1024);
                });
            }

        }
    };
    StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = new StreamCodec<ByteBuf, GameProfile>() {
        public GameProfile decode(ByteBuf bytebuf) {
            UUID uuid = (UUID) UUIDUtil.STREAM_CODEC.decode(bytebuf);
            String s = Utf8String.read(bytebuf, 16);
            GameProfile gameprofile = new GameProfile(uuid, s);

            gameprofile.getProperties().putAll((Multimap) ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(bytebuf));
            return gameprofile;
        }

        public void encode(ByteBuf bytebuf, GameProfile gameprofile) {
            UUIDUtil.STREAM_CODEC.encode(bytebuf, gameprofile.getId());
            Utf8String.write(bytebuf, gameprofile.getName(), 16);
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(bytebuf, gameprofile.getProperties());
        }
    };

    static StreamCodec<ByteBuf, byte[]> byteArray(final int i) {
        return new StreamCodec<ByteBuf, byte[]>() {
            public byte[] decode(ByteBuf bytebuf) {
                return PacketDataSerializer.readByteArray(bytebuf, i);
            }

            public void encode(ByteBuf bytebuf, byte[] abyte) {
                if (abyte.length > i) {
                    throw new EncoderException("ByteArray with size " + abyte.length + " is bigger than allowed " + i);
                } else {
                    PacketDataSerializer.writeByteArray(bytebuf, abyte);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, String> stringUtf8(final int i) {
        return new StreamCodec<ByteBuf, String>() {
            public String decode(ByteBuf bytebuf) {
                return Utf8String.read(bytebuf, i);
            }

            public void encode(ByteBuf bytebuf, String s) {
                Utf8String.write(bytebuf, s, i);
            }
        };
    }

    static StreamCodec<ByteBuf, NBTBase> tagCodec(final Supplier<NBTReadLimiter> supplier) {
        return new StreamCodec<ByteBuf, NBTBase>() {
            public NBTBase decode(ByteBuf bytebuf) {
                NBTBase nbtbase = PacketDataSerializer.readNbt(bytebuf, (NBTReadLimiter) supplier.get());

                if (nbtbase == null) {
                    throw new DecoderException("Expected non-null compound tag");
                } else {
                    return nbtbase;
                }
            }

            public void encode(ByteBuf bytebuf, NBTBase nbtbase) {
                if (nbtbase == NBTTagEnd.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                } else {
                    PacketDataSerializer.writeNbt(bytebuf, nbtbase);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, NBTTagCompound> compoundTagCodec(Supplier<NBTReadLimiter> supplier) {
        return tagCodec(supplier).map((nbtbase) -> {
            if (nbtbase instanceof NBTTagCompound nbttagcompound) {
                return nbttagcompound;
            } else {
                throw new DecoderException("Not a compound tag: " + String.valueOf(nbtbase));
            }
        }, (nbttagcompound) -> {
            return nbttagcompound;
        });
    }

    static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> codec) {
        return fromCodec(codec, NBTReadLimiter::unlimitedHeap);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
        return fromCodec(codec, () -> {
            return NBTReadLimiter.create(2097152L);
        });
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec, Supplier<NBTReadLimiter> supplier) {
        return tagCodec(supplier).map((nbtbase) -> {
            return codec.parse(DynamicOpsNBT.INSTANCE, nbtbase).getOrThrow((s) -> {
                return new DecoderException("Failed to decode: " + s + " " + String.valueOf(nbtbase));
            });
        }, (object) -> {
            return (NBTBase) codec.encodeStart(DynamicOpsNBT.INSTANCE, object).getOrThrow((s) -> {
                return new EncoderException("Failed to encode: " + s + " " + String.valueOf(object));
            });
        });
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> codec) {
        return fromCodecWithRegistries(codec, NBTReadLimiter::unlimitedHeap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
        return fromCodecWithRegistries(codec, () -> {
            return NBTReadLimiter.create(2097152L);
        });
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> codec, Supplier<NBTReadLimiter> supplier) {
        final StreamCodec<ByteBuf, NBTBase> streamcodec = tagCodec(supplier);

        return new StreamCodec<RegistryFriendlyByteBuf, T>() {
            public T decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
                NBTBase nbtbase = (NBTBase) streamcodec.decode(registryfriendlybytebuf);
                RegistryOps<NBTBase> registryops = registryfriendlybytebuf.registryAccess().createSerializationContext(DynamicOpsNBT.INSTANCE);

                return codec.parse(registryops, nbtbase).getOrThrow((s) -> {
                    return new DecoderException("Failed to decode: " + s + " " + String.valueOf(nbtbase));
                });
            }

            public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, T t0) {
                RegistryOps<NBTBase> registryops = registryfriendlybytebuf.registryAccess().createSerializationContext(DynamicOpsNBT.INSTANCE);
                NBTBase nbtbase = (NBTBase) codec.encodeStart(registryops, t0).getOrThrow((s) -> {
                    return new EncoderException("Failed to encode: " + s + " " + String.valueOf(t0));
                });

                streamcodec.encode(registryfriendlybytebuf, nbtbase);
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<B, V> streamcodec) {
        return new StreamCodec<B, Optional<V>>() {
            public Optional<V> decode(B b0) {
                return b0.readBoolean() ? Optional.of(streamcodec.decode(b0)) : Optional.empty();
            }

            public void encode(B b0, Optional<V> optional) {
                if (optional.isPresent()) {
                    b0.writeBoolean(true);
                    streamcodec.encode(b0, optional.get());
                } else {
                    b0.writeBoolean(false);
                }

            }
        };
    }

    static int readCount(ByteBuf bytebuf, int i) {
        int j = VarInt.read(bytebuf);

        if (j > i) {
            throw new DecoderException("" + j + " elements exceeded max size of: " + i);
        } else {
            return j;
        }
    }

    static void writeCount(ByteBuf bytebuf, int i, int j) {
        if (i > j) {
            throw new EncoderException("" + i + " elements exceeded max size of: " + j);
        } else {
            VarInt.write(bytebuf, i);
        }
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> intfunction, StreamCodec<? super B, V> streamcodec) {
        return collection(intfunction, streamcodec, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(final IntFunction<C> intfunction, final StreamCodec<? super B, V> streamcodec, final int i) {
        return new StreamCodec<B, C>() {
            public C decode(B b0) {
                int j = ByteBufCodecs.readCount(b0, i);
                C c0 = (Collection) intfunction.apply(Math.min(j, 65536));

                for (int k = 0; k < j; ++k) {
                    c0.add(streamcodec.decode(b0));
                }

                return c0;
            }

            public void encode(B b0, C c0) {
                ByteBufCodecs.writeCount(b0, c0.size(), i);
                Iterator iterator = c0.iterator();

                while (iterator.hasNext()) {
                    V v0 = iterator.next();

                    streamcodec.encode(b0, v0);
                }

            }
        };
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.a<B, V, C> collection(IntFunction<C> intfunction) {
        return (streamcodec) -> {
            return collection(intfunction, streamcodec);
        };
    }

    static <B extends ByteBuf, V> StreamCodec.a<B, V, List<V>> list() {
        return (streamcodec) -> {
            return collection(ArrayList::new, streamcodec);
        };
    }

    static <B extends ByteBuf, V> StreamCodec.a<B, V, List<V>> list(int i) {
        return (streamcodec) -> {
            return collection(ArrayList::new, streamcodec, i);
        };
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(IntFunction<? extends M> intfunction, StreamCodec<? super B, K> streamcodec, StreamCodec<? super B, V> streamcodec1) {
        return map(intfunction, streamcodec, streamcodec1, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(final IntFunction<? extends M> intfunction, final StreamCodec<? super B, K> streamcodec, final StreamCodec<? super B, V> streamcodec1, final int i) {
        return new StreamCodec<B, M>() {
            public void encode(B b0, M m0) {
                ByteBufCodecs.writeCount(b0, m0.size(), i);
                m0.forEach((object, object1) -> {
                    streamcodec.encode(b0, object);
                    streamcodec1.encode(b0, object1);
                });
            }

            public M decode(B b0) {
                int j = ByteBufCodecs.readCount(b0, i);
                M m0 = (Map) intfunction.apply(Math.min(j, 65536));

                for (int k = 0; k < j; ++k) {
                    K k0 = streamcodec.decode(b0);
                    V v0 = streamcodec1.decode(b0);

                    m0.put(k0, v0);
                }

                return m0;
            }
        };
    }

    static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(final StreamCodec<? super B, L> streamcodec, final StreamCodec<? super B, R> streamcodec1) {
        return new StreamCodec<B, Either<L, R>>() {
            public Either<L, R> decode(B b0) {
                return b0.readBoolean() ? Either.left(streamcodec.decode(b0)) : Either.right(streamcodec1.decode(b0));
            }

            public void encode(B b0, Either<L, R> either) {
                either.ifLeft((object) -> {
                    b0.writeBoolean(true);
                    streamcodec.encode(b0, object);
                }).ifRight((object) -> {
                    b0.writeBoolean(false);
                    streamcodec1.encode(b0, object);
                });
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> intfunction, final ToIntFunction<T> tointfunction) {
        return new StreamCodec<ByteBuf, T>() {
            public T decode(ByteBuf bytebuf) {
                int i = VarInt.read(bytebuf);

                return intfunction.apply(i);
            }

            public void encode(ByteBuf bytebuf, T t0) {
                int i = tointfunction.applyAsInt(t0);

                VarInt.write(bytebuf, i);
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(Registry<T> registry) {
        Objects.requireNonNull(registry);
        IntFunction intfunction = registry::byIdOrThrow;

        Objects.requireNonNull(registry);
        return idMapper(intfunction, registry::getIdOrThrow);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(final ResourceKey<? extends IRegistry<T>> resourcekey, final Function<IRegistry<T>, Registry<R>> function) {
        return new StreamCodec<RegistryFriendlyByteBuf, R>() {
            private Registry<R> getRegistryOrThrow(RegistryFriendlyByteBuf registryfriendlybytebuf) {
                return (Registry) function.apply(registryfriendlybytebuf.registryAccess().registryOrThrow(resourcekey));
            }

            public R decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
                int i = VarInt.read(registryfriendlybytebuf);

                return this.getRegistryOrThrow(registryfriendlybytebuf).byIdOrThrow(i);
            }

            public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, R r0) {
                int i = this.getRegistryOrThrow(registryfriendlybytebuf).getIdOrThrow(r0);

                VarInt.write(registryfriendlybytebuf, i);
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends IRegistry<T>> resourcekey) {
        return registry(resourcekey, (iregistry) -> {
            return iregistry;
        });
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends IRegistry<T>> resourcekey) {
        return registry(resourcekey, IRegistry::asHolderIdMap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(final ResourceKey<? extends IRegistry<T>> resourcekey, final StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec) {
        return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>() {
            private static final int DIRECT_HOLDER_ID = 0;

            private Registry<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf registryfriendlybytebuf) {
                return registryfriendlybytebuf.registryAccess().registryOrThrow(resourcekey).asHolderIdMap();
            }

            public Holder<T> decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
                int i = VarInt.read(registryfriendlybytebuf);

                return i == 0 ? Holder.direct(streamcodec.decode(registryfriendlybytebuf)) : (Holder) this.getRegistryOrThrow(registryfriendlybytebuf).byIdOrThrow(i - 1);
            }

            public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, Holder<T> holder) {
                switch (holder.kind()) {
                    case REFERENCE:
                        int i = this.getRegistryOrThrow(registryfriendlybytebuf).getIdOrThrow(holder);

                        VarInt.write(registryfriendlybytebuf, i + 1);
                        break;
                    case DIRECT:
                        VarInt.write(registryfriendlybytebuf, 0);
                        streamcodec.encode(registryfriendlybytebuf, holder.value());
                }

            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends IRegistry<T>> resourcekey) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>() {
            private static final int NAMED_SET = -1;
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec = ByteBufCodecs.holderRegistry(resourcekey);

            public HolderSet<T> decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
                int i = VarInt.read(registryfriendlybytebuf) - 1;

                if (i == -1) {
                    IRegistry<T> iregistry = registryfriendlybytebuf.registryAccess().registryOrThrow(resourcekey);

                    return (HolderSet) iregistry.getTag(TagKey.create(resourcekey, (MinecraftKey) MinecraftKey.STREAM_CODEC.decode(registryfriendlybytebuf))).orElseThrow();
                } else {
                    List<Holder<T>> list = new ArrayList(Math.min(i, 65536));

                    for (int j = 0; j < i; ++j) {
                        list.add((Holder) this.holderCodec.decode(registryfriendlybytebuf));
                    }

                    return HolderSet.direct((List) list);
                }
            }

            public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, HolderSet<T> holderset) {
                Optional<TagKey<T>> optional = holderset.unwrapKey();

                if (optional.isPresent()) {
                    VarInt.write(registryfriendlybytebuf, 0);
                    MinecraftKey.STREAM_CODEC.encode(registryfriendlybytebuf, ((TagKey) optional.get()).location());
                } else {
                    VarInt.write(registryfriendlybytebuf, holderset.size() + 1);
                    Iterator iterator = holderset.iterator();

                    while (iterator.hasNext()) {
                        Holder<T> holder = (Holder) iterator.next();

                        this.holderCodec.encode(registryfriendlybytebuf, holder);
                    }
                }

            }
        };
    }
}
