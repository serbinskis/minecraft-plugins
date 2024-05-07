package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;

public class RegistrySetBuilder {

    private final List<RegistrySetBuilder.k<?>> entries = new ArrayList();

    public RegistrySetBuilder() {}

    static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.b<T> holderlookup_b) {
        return new RegistrySetBuilder.c<T>(holderlookup_b) {
            @Override
            public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey) {
                return holderlookup_b.get(resourcekey);
            }
        };
    }

    static <T> HolderLookup.b<T> lookupFromMap(final ResourceKey<? extends IRegistry<? extends T>> resourcekey, final Lifecycle lifecycle, HolderOwner<T> holderowner, final Map<ResourceKey<T>, Holder.c<T>> map) {
        return new RegistrySetBuilder.e<T>(holderowner) {
            @Override
            public ResourceKey<? extends IRegistry<? extends T>> key() {
                return resourcekey;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return lifecycle;
            }

            @Override
            public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey1) {
                return Optional.ofNullable((Holder.c) map.get(resourcekey1));
            }

            @Override
            public Stream<Holder.c<T>> listElements() {
                return map.values().stream();
            }
        };
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends IRegistry<T>> resourcekey, Lifecycle lifecycle, RegistrySetBuilder.i<T> registrysetbuilder_i) {
        this.entries.add(new RegistrySetBuilder.k<>(resourcekey, lifecycle, registrysetbuilder_i));
        return this;
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends IRegistry<T>> resourcekey, RegistrySetBuilder.i<T> registrysetbuilder_i) {
        return this.add(resourcekey, Lifecycle.stable(), registrysetbuilder_i);
    }

    private RegistrySetBuilder.b createState(IRegistryCustom iregistrycustom) {
        RegistrySetBuilder.b registrysetbuilder_b = RegistrySetBuilder.b.create(iregistrycustom, this.entries.stream().map(RegistrySetBuilder.k::key));

        this.entries.forEach((registrysetbuilder_k) -> {
            registrysetbuilder_k.apply(registrysetbuilder_b);
        });
        return registrysetbuilder_b;
    }

    private static HolderLookup.a buildProviderWithContext(RegistrySetBuilder.m registrysetbuilder_m, IRegistryCustom iregistrycustom, Stream<HolderLookup.b<?>> stream) {
        final Map<ResourceKey<? extends IRegistry<?>>, a<?>> map = new HashMap();

        iregistrycustom.registries().forEach((iregistrycustom_d) -> {
            map.put(iregistrycustom_d.key(), a.createForContextRegistry(iregistrycustom_d.value().asLookup()));
        });
        stream.forEach((holderlookup_b) -> {
            map.put(holderlookup_b.key(), a.createForNewRegistry(registrysetbuilder_m, holderlookup_b));
        });
        return new HolderLookup.a() {
            @Override
            public Stream<ResourceKey<? extends IRegistry<?>>> listRegistries() {
                return map.keySet().stream();
            }

            <T> Optional<a<T>> getEntry(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                return Optional.ofNullable((a) map.get(resourcekey));
            }

            @Override
            public <T> Optional<HolderLookup.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                return this.getEntry(resourcekey).map(a::lookup);
            }

            @Override
            public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicops) {
                return RegistryOps.create(dynamicops, new RegistryOps.c() {
                    @Override
                    public <T> Optional<RegistryOps.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                        return getEntry(resourcekey).map(a::opsInfo);
                    }
                });
            }
        };

        record a<T>(HolderLookup.b<T> lookup, RegistryOps.b<T> opsInfo) {

            public static <T> a<T> createForContextRegistry(HolderLookup.b<T> holderlookup_b) {
                return new a<>(new RegistrySetBuilder.d<>(holderlookup_b, holderlookup_b), RegistryOps.b.fromRegistryLookup(holderlookup_b));
            }

            public static <T> a<T> createForNewRegistry(RegistrySetBuilder.m registrysetbuilder_m1, HolderLookup.b<T> holderlookup_b) {
                return new a<>(new RegistrySetBuilder.d<>(registrysetbuilder_m1.cast(), holderlookup_b), new RegistryOps.b<>(registrysetbuilder_m1.cast(), holderlookup_b, holderlookup_b.registryLifecycle()));
            }
        }

    }

    public HolderLookup.a build(IRegistryCustom iregistrycustom) {
        RegistrySetBuilder.b registrysetbuilder_b = this.createState(iregistrycustom);
        Stream<HolderLookup.b<?>> stream = this.entries.stream().map((registrysetbuilder_k) -> {
            return registrysetbuilder_k.collectRegisteredValues(registrysetbuilder_b).buildAsLookup(registrysetbuilder_b.owner);
        });
        HolderLookup.a holderlookup_a = buildProviderWithContext(registrysetbuilder_b.owner, iregistrycustom, stream);

        registrysetbuilder_b.reportNotCollectedHolders();
        registrysetbuilder_b.reportUnclaimedRegisteredValues();
        registrysetbuilder_b.throwOnError();
        return holderlookup_a;
    }

    private HolderLookup.a createLazyFullPatchedRegistries(IRegistryCustom iregistrycustom, HolderLookup.a holderlookup_a, Cloner.a cloner_a, Map<ResourceKey<? extends IRegistry<?>>, RegistrySetBuilder.j<?>> map, HolderLookup.a holderlookup_a1) {
        RegistrySetBuilder.m registrysetbuilder_m = new RegistrySetBuilder.m();
        MutableObject<HolderLookup.a> mutableobject = new MutableObject();
        List<HolderLookup.b<?>> list = (List) map.keySet().stream().map((resourcekey) -> {
            return this.createLazyFullPatchedRegistries(registrysetbuilder_m, cloner_a, resourcekey, holderlookup_a1, holderlookup_a, mutableobject);
        }).collect(Collectors.toUnmodifiableList());
        HolderLookup.a holderlookup_a2 = buildProviderWithContext(registrysetbuilder_m, iregistrycustom, list.stream());

        mutableobject.setValue(holderlookup_a2);
        return holderlookup_a2;
    }

    private <T> HolderLookup.b<T> createLazyFullPatchedRegistries(HolderOwner<T> holderowner, Cloner.a cloner_a, ResourceKey<? extends IRegistry<? extends T>> resourcekey, HolderLookup.a holderlookup_a, HolderLookup.a holderlookup_a1, MutableObject<HolderLookup.a> mutableobject) {
        Cloner<T> cloner = cloner_a.cloner(resourcekey);

        if (cloner == null) {
            throw new NullPointerException("No cloner for " + String.valueOf(resourcekey.location()));
        } else {
            Map<ResourceKey<T>, Holder.c<T>> map = new HashMap();
            HolderLookup.b<T> holderlookup_b = holderlookup_a.lookupOrThrow(resourcekey);

            holderlookup_b.listElements().forEach((holder_c) -> {
                ResourceKey<T> resourcekey1 = holder_c.key();
                RegistrySetBuilder.f<T> registrysetbuilder_f = new RegistrySetBuilder.f<>(holderowner, resourcekey1);

                registrysetbuilder_f.supplier = () -> {
                    return cloner.clone(holder_c.value(), holderlookup_a, (HolderLookup.a) mutableobject.getValue());
                };
                map.put(resourcekey1, registrysetbuilder_f);
            });
            HolderLookup.b<T> holderlookup_b1 = holderlookup_a1.lookupOrThrow(resourcekey);

            holderlookup_b1.listElements().forEach((holder_c) -> {
                ResourceKey<T> resourcekey1 = holder_c.key();

                map.computeIfAbsent(resourcekey1, (resourcekey2) -> {
                    RegistrySetBuilder.f<T> registrysetbuilder_f = new RegistrySetBuilder.f<>(holderowner, resourcekey1);

                    registrysetbuilder_f.supplier = () -> {
                        return cloner.clone(holder_c.value(), holderlookup_a1, (HolderLookup.a) mutableobject.getValue());
                    };
                    return registrysetbuilder_f;
                });
            });
            Lifecycle lifecycle = holderlookup_b.registryLifecycle().add(holderlookup_b1.registryLifecycle());

            return lookupFromMap(resourcekey, lifecycle, holderowner, map);
        }
    }

    public RegistrySetBuilder.g buildPatch(IRegistryCustom iregistrycustom, HolderLookup.a holderlookup_a, Cloner.a cloner_a) {
        RegistrySetBuilder.b registrysetbuilder_b = this.createState(iregistrycustom);
        Map<ResourceKey<? extends IRegistry<?>>, RegistrySetBuilder.j<?>> map = new HashMap();

        this.entries.stream().map((registrysetbuilder_k) -> {
            return registrysetbuilder_k.collectRegisteredValues(registrysetbuilder_b);
        }).forEach((registrysetbuilder_j) -> {
            map.put(registrysetbuilder_j.key, registrysetbuilder_j);
        });
        Set<ResourceKey<? extends IRegistry<?>>> set = (Set) iregistrycustom.listRegistries().collect(Collectors.toUnmodifiableSet());

        holderlookup_a.listRegistries().filter((resourcekey) -> {
            return !set.contains(resourcekey);
        }).forEach((resourcekey) -> {
            map.putIfAbsent(resourcekey, new RegistrySetBuilder.j<>(resourcekey, Lifecycle.stable(), Map.of()));
        });
        Stream<HolderLookup.b<?>> stream = map.values().stream().map((registrysetbuilder_j) -> {
            return registrysetbuilder_j.buildAsLookup(registrysetbuilder_b.owner);
        });
        HolderLookup.a holderlookup_a1 = buildProviderWithContext(registrysetbuilder_b.owner, iregistrycustom, stream);

        registrysetbuilder_b.reportUnclaimedRegisteredValues();
        registrysetbuilder_b.throwOnError();
        HolderLookup.a holderlookup_a2 = this.createLazyFullPatchedRegistries(iregistrycustom, holderlookup_a, cloner_a, map, holderlookup_a1);

        return new RegistrySetBuilder.g(holderlookup_a2, holderlookup_a1);
    }

    private static record k<T>(ResourceKey<? extends IRegistry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.i<T> bootstrap) {

        void apply(RegistrySetBuilder.b registrysetbuilder_b) {
            this.bootstrap.run(registrysetbuilder_b.bootstrapContext());
        }

        public RegistrySetBuilder.j<T> collectRegisteredValues(RegistrySetBuilder.b registrysetbuilder_b) {
            Map<ResourceKey<T>, RegistrySetBuilder.n<T>> map = new HashMap();
            Iterator<Entry<ResourceKey<?>, RegistrySetBuilder.h<?>>> iterator = registrysetbuilder_b.registeredValues.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<ResourceKey<?>, RegistrySetBuilder.h<?>> entry = (Entry) iterator.next();
                ResourceKey<?> resourcekey = (ResourceKey) entry.getKey();

                if (resourcekey.isFor(this.key)) {
                    RegistrySetBuilder.h<T> registrysetbuilder_h = (RegistrySetBuilder.h) entry.getValue();
                    Holder.c<T> holder_c = (Holder.c) registrysetbuilder_b.lookup.holders.remove(resourcekey);

                    map.put(resourcekey, new RegistrySetBuilder.n<>(registrysetbuilder_h, Optional.ofNullable(holder_c)));
                    iterator.remove();
                }
            }

            return new RegistrySetBuilder.j<>(this.key, this.lifecycle, map);
        }
    }

    @FunctionalInterface
    public interface i<T> {

        void run(BootstrapContext<T> bootstrapcontext);
    }

    private static record b(RegistrySetBuilder.m owner, RegistrySetBuilder.l lookup, Map<MinecraftKey, HolderGetter<?>> registries, Map<ResourceKey<?>, RegistrySetBuilder.h<?>> registeredValues, List<RuntimeException> errors) {

        public static RegistrySetBuilder.b create(IRegistryCustom iregistrycustom, Stream<ResourceKey<? extends IRegistry<?>>> stream) {
            RegistrySetBuilder.m registrysetbuilder_m = new RegistrySetBuilder.m();
            List<RuntimeException> list = new ArrayList();
            RegistrySetBuilder.l registrysetbuilder_l = new RegistrySetBuilder.l(registrysetbuilder_m);
            Builder<MinecraftKey, HolderGetter<?>> builder = ImmutableMap.builder();

            iregistrycustom.registries().forEach((iregistrycustom_d) -> {
                builder.put(iregistrycustom_d.key().location(), RegistrySetBuilder.wrapContextLookup(iregistrycustom_d.value().asLookup()));
            });
            stream.forEach((resourcekey) -> {
                builder.put(resourcekey.location(), registrysetbuilder_l);
            });
            return new RegistrySetBuilder.b(registrysetbuilder_m, registrysetbuilder_l, builder.build(), new HashMap(), list);
        }

        public <T> BootstrapContext<T> bootstrapContext() {
            return new BootstrapContext<T>() {
                @Override
                public Holder.c<T> register(ResourceKey<T> resourcekey, T t0, Lifecycle lifecycle) {
                    RegistrySetBuilder.h<?> registrysetbuilder_h = (RegistrySetBuilder.h) b.this.registeredValues.put(resourcekey, new RegistrySetBuilder.h<>(t0, lifecycle));

                    if (registrysetbuilder_h != null) {
                        List list = b.this.errors;
                        String s = String.valueOf(resourcekey);

                        list.add(new IllegalStateException("Duplicate registration for " + s + ", new=" + String.valueOf(t0) + ", old=" + String.valueOf(registrysetbuilder_h.value)));
                    }

                    return b.this.lookup.getOrCreate(resourcekey);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends IRegistry<? extends S>> resourcekey) {
                    return (HolderGetter) b.this.registries.getOrDefault(resourcekey.location(), b.this.lookup);
                }
            };
        }

        public void reportUnclaimedRegisteredValues() {
            this.registeredValues.forEach((resourcekey, registrysetbuilder_h) -> {
                List list = this.errors;
                String s = String.valueOf(registrysetbuilder_h.value);

                list.add(new IllegalStateException("Orpaned value " + s + " for key " + String.valueOf(resourcekey)));
            });
        }

        public void reportNotCollectedHolders() {
            Iterator iterator = this.lookup.holders.keySet().iterator();

            while (iterator.hasNext()) {
                ResourceKey<Object> resourcekey = (ResourceKey) iterator.next();

                this.errors.add(new IllegalStateException("Unreferenced key: " + String.valueOf(resourcekey)));
            }

        }

        public void throwOnError() {
            if (!this.errors.isEmpty()) {
                IllegalStateException illegalstateexception = new IllegalStateException("Errors during registry creation");
                Iterator iterator = this.errors.iterator();

                while (iterator.hasNext()) {
                    RuntimeException runtimeexception = (RuntimeException) iterator.next();

                    illegalstateexception.addSuppressed(runtimeexception);
                }

                throw illegalstateexception;
            }
        }
    }

    private static class m implements HolderOwner<Object> {

        m() {}

        public <T> HolderOwner<T> cast() {
            return this;
        }
    }

    public static record g(HolderLookup.a full, HolderLookup.a patches) {

    }

    private static record j<T>(ResourceKey<? extends IRegistry<? extends T>> key, Lifecycle lifecycle, Map<ResourceKey<T>, RegistrySetBuilder.n<T>> values) {

        public HolderLookup.b<T> buildAsLookup(RegistrySetBuilder.m registrysetbuilder_m) {
            Map<ResourceKey<T>, Holder.c<T>> map = (Map) this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Entry::getKey, (entry) -> {
                RegistrySetBuilder.n<T> registrysetbuilder_n = (RegistrySetBuilder.n) entry.getValue();
                Holder.c<T> holder_c = (Holder.c) registrysetbuilder_n.holder().orElseGet(() -> {
                    return Holder.c.createStandAlone(registrysetbuilder_m.cast(), (ResourceKey) entry.getKey());
                });

                holder_c.bindValue(registrysetbuilder_n.value().value());
                return holder_c;
            }));

            return RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, registrysetbuilder_m.cast(), map);
        }
    }

    private static class f<T> extends Holder.c<T> {

        @Nullable
        Supplier<T> supplier;

        protected f(HolderOwner<T> holderowner, @Nullable ResourceKey<T> resourcekey) {
            super(Holder.c.a.STAND_ALONE, holderowner, resourcekey, (Object) null);
        }

        @Override
        protected void bindValue(T t0) {
            super.bindValue(t0);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.bindValue(this.supplier.get());
            }

            return super.value();
        }
    }

    private static record n<T>(RegistrySetBuilder.h<T> value, Optional<Holder.c<T>> holder) {

    }

    private static record h<T>(T value, Lifecycle lifecycle) {

    }

    private static class l extends RegistrySetBuilder.c<Object> {

        final Map<ResourceKey<Object>, Holder.c<Object>> holders = new HashMap();

        public l(HolderOwner<Object> holderowner) {
            super(holderowner);
        }

        @Override
        public Optional<Holder.c<Object>> get(ResourceKey<Object> resourcekey) {
            return Optional.of(this.getOrCreate(resourcekey));
        }

        <T> Holder.c<T> getOrCreate(ResourceKey<T> resourcekey) {
            return (Holder.c) this.holders.computeIfAbsent(resourcekey, (resourcekey1) -> {
                return Holder.c.createStandAlone(this.owner, resourcekey1);
            });
        }
    }

    private static class d<T> extends RegistrySetBuilder.e<T> implements HolderLookup.b.a<T> {

        private final HolderLookup.b<T> parent;

        d(HolderOwner<T> holderowner, HolderLookup.b<T> holderlookup_b) {
            super(holderowner);
            this.parent = holderlookup_b;
        }

        @Override
        public HolderLookup.b<T> parent() {
            return this.parent;
        }
    }

    private abstract static class e<T> extends RegistrySetBuilder.c<T> implements HolderLookup.b<T> {

        protected e(HolderOwner<T> holderowner) {
            super(holderowner);
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            throw new UnsupportedOperationException("Tags are not available in datagen");
        }
    }

    private abstract static class c<T> implements HolderGetter<T> {

        protected final HolderOwner<T> owner;

        protected c(HolderOwner<T> holderowner) {
            this.owner = holderowner;
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
            return Optional.of(HolderSet.emptyNamed(this.owner, tagkey));
        }
    }
}
