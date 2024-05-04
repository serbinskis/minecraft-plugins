package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface IRegistry<T> extends Keyable, Registry<T> {

    ResourceKey<? extends IRegistry<T>> key();

    default Codec<T> byNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap(Holder.c::value, (object) -> {
            return this.safeCastToReference(this.wrapAsHolder(object));
        });
    }

    default Codec<Holder<T>> holderByNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap((holder_c) -> {
            return holder_c;
        }, this::safeCastToReference);
    }

    private Codec<Holder.c<T>> referenceHolderWithLifecycle() {
        Codec<Holder.c<T>> codec = MinecraftKey.CODEC.comapFlatMap((minecraftkey) -> {
            return (DataResult) this.getHolder(minecraftkey).map(DataResult::success).orElseGet(() -> {
                return DataResult.error(() -> {
                    String s = String.valueOf(this.key());

                    return "Unknown registry key in " + s + ": " + String.valueOf(minecraftkey);
                });
            });
        }, (holder_c) -> {
            return holder_c.key().location();
        });

        return ExtraCodecs.overrideLifecycle(codec, (holder_c) -> {
            return (Lifecycle) this.registrationInfo(holder_c.key()).map(RegistrationInfo::lifecycle).orElse(Lifecycle.experimental());
        });
    }

    private DataResult<Holder.c<T>> safeCastToReference(Holder<T> holder) {
        DataResult dataresult;

        if (holder instanceof Holder.c<T> holder_c) {
            dataresult = DataResult.success(holder_c);
        } else {
            dataresult = DataResult.error(() -> {
                String s = String.valueOf(this.key());

                return "Unregistered holder in " + s + ": " + String.valueOf(holder);
            });
        }

        return dataresult;
    }

    default <U> Stream<U> keys(DynamicOps<U> dynamicops) {
        return this.keySet().stream().map((minecraftkey) -> {
            return dynamicops.createString(minecraftkey.toString());
        });
    }

    @Nullable
    MinecraftKey getKey(T t0);

    Optional<ResourceKey<T>> getResourceKey(T t0);

    @Override
    int getId(@Nullable T t0);

    @Nullable
    T get(@Nullable ResourceKey<T> resourcekey);

    @Nullable
    T get(@Nullable MinecraftKey minecraftkey);

    Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourcekey);

    Lifecycle registryLifecycle();

    default Optional<T> getOptional(@Nullable MinecraftKey minecraftkey) {
        return Optional.ofNullable(this.get(minecraftkey));
    }

    default Optional<T> getOptional(@Nullable ResourceKey<T> resourcekey) {
        return Optional.ofNullable(this.get(resourcekey));
    }

    default T getOrThrow(ResourceKey<T> resourcekey) {
        T t0 = this.get(resourcekey);

        if (t0 == null) {
            String s = String.valueOf(this.key());

            throw new IllegalStateException("Missing key in " + s + ": " + String.valueOf(resourcekey));
        } else {
            return t0;
        }
    }

    Set<MinecraftKey> keySet();

    Set<Entry<ResourceKey<T>, T>> entrySet();

    Set<ResourceKey<T>> registryKeySet();

    Optional<Holder.c<T>> getRandom(RandomSource randomsource);

    default Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    boolean containsKey(MinecraftKey minecraftkey);

    boolean containsKey(ResourceKey<T> resourcekey);

    static <T> T register(IRegistry<? super T> iregistry, String s, T t0) {
        return register(iregistry, new MinecraftKey(s), t0);
    }

    static <V, T extends V> T register(IRegistry<V> iregistry, MinecraftKey minecraftkey, T t0) {
        return register(iregistry, ResourceKey.create(iregistry.key(), minecraftkey), t0);
    }

    static <V, T extends V> T register(IRegistry<V> iregistry, ResourceKey<V> resourcekey, T t0) {
        ((IRegistryWritable) iregistry).register(resourcekey, t0, RegistrationInfo.BUILT_IN);
        return t0;
    }

    static <T> Holder.c<T> registerForHolder(IRegistry<T> iregistry, ResourceKey<T> resourcekey, T t0) {
        return ((IRegistryWritable) iregistry).register(resourcekey, t0, RegistrationInfo.BUILT_IN);
    }

    static <T> Holder.c<T> registerForHolder(IRegistry<T> iregistry, MinecraftKey minecraftkey, T t0) {
        return registerForHolder(iregistry, ResourceKey.create(iregistry.key(), minecraftkey), t0);
    }

    IRegistry<T> freeze();

    Holder.c<T> createIntrusiveHolder(T t0);

    Optional<Holder.c<T>> getHolder(int i);

    Optional<Holder.c<T>> getHolder(MinecraftKey minecraftkey);

    Optional<Holder.c<T>> getHolder(ResourceKey<T> resourcekey);

    Holder<T> wrapAsHolder(T t0);

    default Holder.c<T> getHolderOrThrow(ResourceKey<T> resourcekey) {
        return (Holder.c) this.getHolder(resourcekey).orElseThrow(() -> {
            String s = String.valueOf(this.key());

            return new IllegalStateException("Missing key in " + s + ": " + String.valueOf(resourcekey));
        });
    }

    Stream<Holder.c<T>> holders();

    Optional<HolderSet.Named<T>> getTag(TagKey<T> tagkey);

    default Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagkey) {
        return (Iterable) DataFixUtils.orElse(this.getTag(tagkey), List.of());
    }

    default Optional<Holder<T>> getRandomElementOf(TagKey<T> tagkey, RandomSource randomsource) {
        return this.getTag(tagkey).flatMap((holderset_named) -> {
            return holderset_named.getRandomElement(randomsource);
        });
    }

    HolderSet.Named<T> getOrCreateTag(TagKey<T> tagkey);

    Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags();

    Stream<TagKey<T>> getTagNames();

    void resetTags();

    void bindTags(Map<TagKey<T>, List<Holder<T>>> map);

    default Registry<Holder<T>> asHolderIdMap() {
        return new Registry<Holder<T>>() {
            public int getId(Holder<T> holder) {
                return IRegistry.this.getId(holder.value());
            }

            @Nullable
            @Override
            public Holder<T> byId(int i) {
                return (Holder) IRegistry.this.getHolder(i).orElse((Object) null);
            }

            @Override
            public int size() {
                return IRegistry.this.size();
            }

            public Iterator<Holder<T>> iterator() {
                return IRegistry.this.holders().map((holder_c) -> {
                    return holder_c;
                }).iterator();
            }
        };
    }

    HolderOwner<T> holderOwner();

    HolderLookup.b<T> asLookup();

    default HolderLookup.b<T> asTagAddingLookup() {
        return new HolderLookup.b.a<T>() {
            @Override
            public HolderLookup.b<T> parent() {
                return IRegistry.this.asLookup();
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                return Optional.of(this.getOrThrow(tagkey));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> tagkey) {
                return IRegistry.this.getOrCreateTag(tagkey);
            }
        };
    }
}
