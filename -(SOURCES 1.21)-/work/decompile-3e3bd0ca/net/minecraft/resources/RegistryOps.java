package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.IRegistry;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DynamicOpsWrapper<T> {

    private final RegistryOps.c lookupProvider;

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicops, HolderLookup.a holderlookup_a) {
        return create(dynamicops, (RegistryOps.c) (new RegistryOps.a(holderlookup_a)));
    }

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicops, RegistryOps.c registryops_c) {
        return new RegistryOps<>(dynamicops, registryops_c);
    }

    public static <T> Dynamic<T> injectRegistryContext(Dynamic<T> dynamic, HolderLookup.a holderlookup_a) {
        return new Dynamic(holderlookup_a.createSerializationContext(dynamic.getOps()), dynamic.getValue());
    }

    private RegistryOps(DynamicOps<T> dynamicops, RegistryOps.c registryops_c) {
        super(dynamicops);
        this.lookupProvider = registryops_c;
    }

    public <U> RegistryOps<U> withParent(DynamicOps<U> dynamicops) {
        return dynamicops == this.delegate ? this : new RegistryOps<>(dynamicops, this.lookupProvider);
    }

    public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends IRegistry<? extends E>> resourcekey) {
        return this.lookupProvider.lookup(resourcekey).map(RegistryOps.b::owner);
    }

    public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends IRegistry<? extends E>> resourcekey) {
        return this.lookupProvider.lookup(resourcekey).map(RegistryOps.b::getter);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            RegistryOps<?> registryops = (RegistryOps) object;

            return this.delegate.equals(registryops.delegate) && this.lookupProvider.equals(registryops.lookupProvider);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.delegate.hashCode() * 31 + this.lookupProvider.hashCode();
    }

    public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends IRegistry<? extends E>> resourcekey) {
        return ExtraCodecs.retrieveContext((dynamicops) -> {
            if (dynamicops instanceof RegistryOps<?> registryops) {
                return (DataResult) registryops.lookupProvider.lookup(resourcekey).map((registryops_b) -> {
                    return DataResult.success(registryops_b.getter(), registryops_b.elementsLifecycle());
                }).orElseGet(() -> {
                    return DataResult.error(() -> {
                        return "Unknown registry: " + String.valueOf(resourcekey);
                    });
                });
            } else {
                return DataResult.error(() -> {
                    return "Not a registry ops";
                });
            }
        }).forGetter((object) -> {
            return null;
        });
    }

    public static <E, O> RecordCodecBuilder<O, Holder.c<E>> retrieveElement(ResourceKey<E> resourcekey) {
        ResourceKey<? extends IRegistry<E>> resourcekey1 = ResourceKey.createRegistryKey(resourcekey.registry());

        return ExtraCodecs.retrieveContext((dynamicops) -> {
            if (dynamicops instanceof RegistryOps<?> registryops) {
                return (DataResult) registryops.lookupProvider.lookup(resourcekey1).flatMap((registryops_b) -> {
                    return registryops_b.getter().get(resourcekey);
                }).map(DataResult::success).orElseGet(() -> {
                    return DataResult.error(() -> {
                        return "Can't find value: " + String.valueOf(resourcekey);
                    });
                });
            } else {
                return DataResult.error(() -> {
                    return "Not a registry ops";
                });
            }
        }).forGetter((object) -> {
            return null;
        });
    }

    private static final class a implements RegistryOps.c {

        private final HolderLookup.a lookupProvider;
        private final Map<ResourceKey<? extends IRegistry<?>>, Optional<? extends RegistryOps.b<?>>> lookups = new ConcurrentHashMap();

        public a(HolderLookup.a holderlookup_a) {
            this.lookupProvider = holderlookup_a;
        }

        @Override
        public <E> Optional<RegistryOps.b<E>> lookup(ResourceKey<? extends IRegistry<? extends E>> resourcekey) {
            return (Optional) this.lookups.computeIfAbsent(resourcekey, this::createLookup);
        }

        private Optional<RegistryOps.b<Object>> createLookup(ResourceKey<? extends IRegistry<?>> resourcekey) {
            return this.lookupProvider.lookup(resourcekey).map(RegistryOps.b::fromRegistryLookup);
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else {
                boolean flag;

                if (object instanceof RegistryOps.a) {
                    RegistryOps.a registryops_a = (RegistryOps.a) object;

                    if (this.lookupProvider.equals(registryops_a.lookupProvider)) {
                        flag = true;
                        return flag;
                    }
                }

                flag = false;
                return flag;
            }
        }

        public int hashCode() {
            return this.lookupProvider.hashCode();
        }
    }

    public interface c {

        <T> Optional<RegistryOps.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey);
    }

    public static record b<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {

        public static <T> RegistryOps.b<T> fromRegistryLookup(HolderLookup.b<T> holderlookup_b) {
            return new RegistryOps.b<>(holderlookup_b, holderlookup_b, holderlookup_b.registryLifecycle());
        }
    }
}
