package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class RegistryMaterials<T> implements IRegistryWritable<T> {

    private static final Logger LOGGER = LogUtils.getLogger();
    final ResourceKey<? extends IRegistry<T>> key;
    private final ObjectList<Holder.c<T>> byId;
    private final Reference2IntMap<T> toId;
    private final Map<MinecraftKey, Holder.c<T>> byLocation;
    private final Map<ResourceKey<T>, Holder.c<T>> byKey;
    private final Map<T, Holder.c<T>> byValue;
    private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos;
    private Lifecycle registryLifecycle;
    private volatile Map<TagKey<T>, HolderSet.Named<T>> tags;
    private boolean frozen;
    @Nullable
    private Map<T, Holder.c<T>> unregisteredIntrusiveHolders;
    private final HolderLookup.b<T> lookup;
    private final Object tagAdditionLock;

    public RegistryMaterials(ResourceKey<? extends IRegistry<T>> resourcekey, Lifecycle lifecycle) {
        this(resourcekey, lifecycle, false);
    }

    public RegistryMaterials(ResourceKey<? extends IRegistry<T>> resourcekey, Lifecycle lifecycle, boolean flag) {
        this.byId = new ObjectArrayList(256);
        this.toId = (Reference2IntMap) SystemUtils.make(new Reference2IntOpenHashMap(), (reference2intopenhashmap) -> {
            reference2intopenhashmap.defaultReturnValue(-1);
        });
        this.byLocation = new HashMap();
        this.byKey = new HashMap();
        this.byValue = new IdentityHashMap();
        this.registrationInfos = new IdentityHashMap();
        this.tags = new IdentityHashMap();
        this.lookup = new HolderLookup.b<T>() {
            @Override
            public ResourceKey<? extends IRegistry<? extends T>> key() {
                return RegistryMaterials.this.key;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return RegistryMaterials.this.registryLifecycle();
            }

            @Override
            public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey1) {
                return RegistryMaterials.this.getHolder(resourcekey1);
            }

            @Override
            public Stream<Holder.c<T>> listElements() {
                return RegistryMaterials.this.holders();
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                return RegistryMaterials.this.getTag(tagkey);
            }

            @Override
            public Stream<HolderSet.Named<T>> listTags() {
                return RegistryMaterials.this.getTags().map(Pair::getSecond);
            }
        };
        this.tagAdditionLock = new Object();
        this.key = resourcekey;
        this.registryLifecycle = lifecycle;
        if (flag) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap();
        }

    }

    @Override
    public ResourceKey<? extends IRegistry<T>> key() {
        return this.key;
    }

    public String toString() {
        String s = String.valueOf(this.key);

        return "Registry[" + s + " (" + String.valueOf(this.registryLifecycle) + ")]";
    }

    private void validateWrite() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void validateWrite(ResourceKey<T> resourcekey) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + String.valueOf(resourcekey) + ")");
        }
    }

    @Override
    public Holder.c<T> register(ResourceKey<T> resourcekey, T t0, RegistrationInfo registrationinfo) {
        this.validateWrite(resourcekey);
        Objects.requireNonNull(resourcekey);
        Objects.requireNonNull(t0);
        if (this.byLocation.containsKey(resourcekey.location())) {
            SystemUtils.pauseInIde(new IllegalStateException("Adding duplicate key '" + String.valueOf(resourcekey) + "' to registry"));
        }

        if (this.byValue.containsKey(t0)) {
            SystemUtils.pauseInIde(new IllegalStateException("Adding duplicate value '" + String.valueOf(t0) + "' to registry"));
        }

        Holder.c holder_c;

        if (this.unregisteredIntrusiveHolders != null) {
            holder_c = (Holder.c) this.unregisteredIntrusiveHolders.remove(t0);
            if (holder_c == null) {
                String s = String.valueOf(resourcekey);

                throw new AssertionError("Missing intrusive holder for " + s + ":" + String.valueOf(t0));
            }

            holder_c.bindKey(resourcekey);
        } else {
            holder_c = (Holder.c) this.byKey.computeIfAbsent(resourcekey, (resourcekey1) -> {
                return Holder.c.createStandAlone(this.holderOwner(), resourcekey1);
            });
        }

        this.byKey.put(resourcekey, holder_c);
        this.byLocation.put(resourcekey.location(), holder_c);
        this.byValue.put(t0, holder_c);
        int i = this.byId.size();

        this.byId.add(holder_c);
        this.toId.put(t0, i);
        this.registrationInfos.put(resourcekey, registrationinfo);
        this.registryLifecycle = this.registryLifecycle.add(registrationinfo.lifecycle());
        return holder_c;
    }

    @Nullable
    @Override
    public MinecraftKey getKey(T t0) {
        Holder.c<T> holder_c = (Holder.c) this.byValue.get(t0);

        return holder_c != null ? holder_c.key().location() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T t0) {
        return Optional.ofNullable((Holder.c) this.byValue.get(t0)).map(Holder.c::key);
    }

    @Override
    public int getId(@Nullable T t0) {
        return this.toId.getInt(t0);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceKey<T> resourcekey) {
        return getValueFromNullable((Holder.c) this.byKey.get(resourcekey));
    }

    @Nullable
    @Override
    public T byId(int i) {
        return i >= 0 && i < this.byId.size() ? ((Holder.c) this.byId.get(i)).value() : null;
    }

    @Override
    public Optional<Holder.c<T>> getHolder(int i) {
        return i >= 0 && i < this.byId.size() ? Optional.ofNullable((Holder.c) this.byId.get(i)) : Optional.empty();
    }

    @Override
    public Optional<Holder.c<T>> getHolder(MinecraftKey minecraftkey) {
        return Optional.ofNullable((Holder.c) this.byLocation.get(minecraftkey));
    }

    @Override
    public Optional<Holder.c<T>> getHolder(ResourceKey<T> resourcekey) {
        return Optional.ofNullable((Holder.c) this.byKey.get(resourcekey));
    }

    @Override
    public Holder<T> wrapAsHolder(T t0) {
        Holder.c<T> holder_c = (Holder.c) this.byValue.get(t0);

        return (Holder) (holder_c != null ? holder_c : Holder.direct(t0));
    }

    Holder.c<T> getOrCreateHolderOrThrow(ResourceKey<T> resourcekey) {
        return (Holder.c) this.byKey.computeIfAbsent(resourcekey, (resourcekey1) -> {
            if (this.unregisteredIntrusiveHolders != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            } else {
                this.validateWrite(resourcekey1);
                return Holder.c.createStandAlone(this.holderOwner(), resourcekey1);
            }
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourcekey) {
        return Optional.ofNullable((RegistrationInfo) this.registrationInfos.get(resourcekey));
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.registryLifecycle;
    }

    public Iterator<T> iterator() {
        return Iterators.transform(this.byId.iterator(), Holder::value);
    }

    @Nullable
    @Override
    public T get(@Nullable MinecraftKey minecraftkey) {
        Holder.c<T> holder_c = (Holder.c) this.byLocation.get(minecraftkey);

        return getValueFromNullable(holder_c);
    }

    @Nullable
    private static <T> T getValueFromNullable(@Nullable Holder.c<T> holder_c) {
        return holder_c != null ? holder_c.value() : null;
    }

    @Override
    public Set<MinecraftKey> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Collections.unmodifiableSet(this.byKey.keySet());
    }

    @Override
    public Set<Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.c<T>> holders() {
        return this.byId.stream();
    }

    @Override
    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
        return this.tags.entrySet().stream().map((entry) -> {
            return Pair.of((TagKey) entry.getKey(), (HolderSet.Named) entry.getValue());
        });
    }

    @Override
    public HolderSet.Named<T> getOrCreateTag(TagKey<T> tagkey) {
        HolderSet.Named<T> holderset_named = (HolderSet.Named) this.tags.get(tagkey);

        if (holderset_named != null) {
            return holderset_named;
        } else {
            Object object = this.tagAdditionLock;

            synchronized (this.tagAdditionLock) {
                holderset_named = (HolderSet.Named) this.tags.get(tagkey);
                if (holderset_named != null) {
                    return holderset_named;
                } else {
                    holderset_named = this.createTag(tagkey);
                    Map<TagKey<T>, HolderSet.Named<T>> map = new IdentityHashMap(this.tags);

                    map.put(tagkey, holderset_named);
                    this.tags = map;
                    return holderset_named;
                }
            }
        }
    }

    private HolderSet.Named<T> createTag(TagKey<T> tagkey) {
        return new HolderSet.Named<>(this.holderOwner(), tagkey);
    }

    @Override
    public Stream<TagKey<T>> getTagNames() {
        return this.tags.keySet().stream();
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder.c<T>> getRandom(RandomSource randomsource) {
        return SystemUtils.getRandomSafe(this.byId, randomsource);
    }

    @Override
    public boolean containsKey(MinecraftKey minecraftkey) {
        return this.byLocation.containsKey(minecraftkey);
    }

    @Override
    public boolean containsKey(ResourceKey<T> resourcekey) {
        return this.byKey.containsKey(resourcekey);
    }

    @Override
    public IRegistry<T> freeze() {
        if (this.frozen) {
            return this;
        } else {
            this.frozen = true;
            this.byValue.forEach((object, holder_c) -> {
                holder_c.bindValue(object);
            });
            List<MinecraftKey> list = this.byKey.entrySet().stream().filter((entry) -> {
                return !((Holder.c) entry.getValue()).isBound();
            }).map((entry) -> {
                return ((ResourceKey) entry.getKey()).location();
            }).sorted().toList();

            if (!list.isEmpty()) {
                String s = String.valueOf(this.key());

                throw new IllegalStateException("Unbound values in registry " + s + ": " + String.valueOf(list));
            } else {
                if (this.unregisteredIntrusiveHolders != null) {
                    if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                        throw new IllegalStateException("Some intrusive holders were not registered: " + String.valueOf(this.unregisteredIntrusiveHolders.values()));
                    }

                    this.unregisteredIntrusiveHolders = null;
                }

                return this;
            }
        }
    }

    @Override
    public Holder.c<T> createIntrusiveHolder(T t0) {
        if (this.unregisteredIntrusiveHolders == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        } else {
            this.validateWrite();
            return (Holder.c) this.unregisteredIntrusiveHolders.computeIfAbsent(t0, (object) -> {
                return Holder.c.createIntrusive(this.asLookup(), object);
            });
        }
    }

    @Override
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> tagkey) {
        return Optional.ofNullable((HolderSet.Named) this.tags.get(tagkey));
    }

    @Override
    public void bindTags(Map<TagKey<T>, List<Holder<T>>> map) {
        Map<Holder.c<T>, List<TagKey<T>>> map1 = new IdentityHashMap();

        this.byKey.values().forEach((holder_c) -> {
            map1.put(holder_c, new ArrayList());
        });
        map.forEach((tagkey, list) -> {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Holder<T> holder = (Holder) iterator.next();
                String s;

                if (!holder.canSerializeIn(this.asLookup())) {
                    s = String.valueOf(tagkey);
                    throw new IllegalStateException("Can't create named set " + s + " containing value " + String.valueOf(holder) + " from outside registry " + String.valueOf(this));
                }

                if (!(holder instanceof Holder.c)) {
                    s = String.valueOf(holder);
                    throw new IllegalStateException("Found direct holder " + s + " value in tag " + String.valueOf(tagkey));
                }

                Holder.c<T> holder_c = (Holder.c) holder;

                ((List) map1.get(holder_c)).add(tagkey);
            }

        });
        Set<TagKey<T>> set = Sets.difference(this.tags.keySet(), map.keySet());

        if (!set.isEmpty()) {
            RegistryMaterials.LOGGER.warn("Not all defined tags for registry {} are present in data pack: {}", this.key(), set.stream().map((tagkey) -> {
                return tagkey.location().toString();
            }).sorted().collect(Collectors.joining(", ")));
        }

        Object object = this.tagAdditionLock;

        synchronized (this.tagAdditionLock) {
            Map<TagKey<T>, HolderSet.Named<T>> map2 = new IdentityHashMap(this.tags);

            map.forEach((tagkey, list) -> {
                ((HolderSet.Named) map2.computeIfAbsent(tagkey, this::createTag)).bind(list);
            });
            map1.forEach(Holder.c::bindTags);
            this.tags = map2;
        }
    }

    @Override
    public void resetTags() {
        this.tags.values().forEach((holderset_named) -> {
            holderset_named.bind(List.of());
        });
        this.byKey.values().forEach((holder_c) -> {
            holder_c.bindTags(Set.of());
        });
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        this.validateWrite();
        return new HolderGetter<T>() {
            @Override
            public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey) {
                return Optional.of(this.getOrThrow(resourcekey));
            }

            @Override
            public Holder.c<T> getOrThrow(ResourceKey<T> resourcekey) {
                return RegistryMaterials.this.getOrCreateHolderOrThrow(resourcekey);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                return Optional.of(this.getOrThrow(tagkey));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> tagkey) {
                return RegistryMaterials.this.getOrCreateTag(tagkey);
            }
        };
    }

    @Override
    public HolderOwner<T> holderOwner() {
        return this.lookup;
    }

    @Override
    public HolderLookup.b<T> asLookup() {
        return this.lookup;
    }
}
