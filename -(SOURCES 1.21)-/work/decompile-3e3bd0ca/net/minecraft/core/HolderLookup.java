package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> extends HolderGetter<T> {

    Stream<Holder.c<T>> listElements();

    default Stream<ResourceKey<T>> listElementIds() {
        return this.listElements().map(Holder.c::key);
    }

    Stream<HolderSet.Named<T>> listTags();

    default Stream<TagKey<T>> listTagIds() {
        return this.listTags().map(HolderSet.Named::key);
    }

    public interface a {

        Stream<ResourceKey<? extends IRegistry<?>>> listRegistries();

        <T> Optional<HolderLookup.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey);

        default <T> HolderLookup.b<T> lookupOrThrow(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
            return (HolderLookup.b) this.lookup(resourcekey).orElseThrow(() -> {
                return new IllegalStateException("Registry " + String.valueOf(resourcekey.location()) + " not found");
            });
        }

        default <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicops) {
            return RegistryOps.create(dynamicops, this);
        }

        default HolderGetter.a asGetterLookup() {
            return new HolderGetter.a() {
                @Override
                public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                    return a.this.lookup(resourcekey).map((holderlookup_b) -> {
                        return holderlookup_b;
                    });
                }
            };
        }

        static HolderLookup.a create(Stream<HolderLookup.b<?>> stream) {
            final Map<ResourceKey<? extends IRegistry<?>>, HolderLookup.b<?>> map = (Map) stream.collect(Collectors.toUnmodifiableMap(HolderLookup.b::key, (holderlookup_b) -> {
                return holderlookup_b;
            }));

            return new HolderLookup.a() {
                @Override
                public Stream<ResourceKey<? extends IRegistry<?>>> listRegistries() {
                    return map.keySet().stream();
                }

                @Override
                public <T> Optional<HolderLookup.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                    return Optional.ofNullable((HolderLookup.b) map.get(resourcekey));
                }
            };
        }
    }

    public interface b<T> extends HolderLookup<T>, HolderOwner<T> {

        ResourceKey<? extends IRegistry<? extends T>> key();

        Lifecycle registryLifecycle();

        default HolderLookup.b<T> filterFeatures(FeatureFlagSet featureflagset) {
            return FeatureElement.FILTERED_REGISTRIES.contains(this.key()) ? this.filterElements((object) -> {
                return ((FeatureElement) object).isEnabled(featureflagset);
            }) : this;
        }

        default HolderLookup.b<T> filterElements(final Predicate<T> predicate) {
            return new HolderLookup.b.a<T>() {
                @Override
                public HolderLookup.b<T> parent() {
                    return b.this;
                }

                @Override
                public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey) {
                    return this.parent().get(resourcekey).filter((holder_c) -> {
                        return predicate.test(holder_c.value());
                    });
                }

                @Override
                public Stream<Holder.c<T>> listElements() {
                    return this.parent().listElements().filter((holder_c) -> {
                        return predicate.test(holder_c.value());
                    });
                }
            };
        }

        public interface a<T> extends HolderLookup.b<T> {

            HolderLookup.b<T> parent();

            @Override
            default ResourceKey<? extends IRegistry<? extends T>> key() {
                return this.parent().key();
            }

            @Override
            default Lifecycle registryLifecycle() {
                return this.parent().registryLifecycle();
            }

            @Override
            default Optional<Holder.c<T>> get(ResourceKey<T> resourcekey) {
                return this.parent().get(resourcekey);
            }

            @Override
            default Stream<Holder.c<T>> listElements() {
                return this.parent().listElements();
            }

            @Override
            default Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                return this.parent().get(tagkey);
            }

            @Override
            default Stream<HolderSet.Named<T>> listTags() {
                return this.parent().listTags();
            }
        }
    }
}
