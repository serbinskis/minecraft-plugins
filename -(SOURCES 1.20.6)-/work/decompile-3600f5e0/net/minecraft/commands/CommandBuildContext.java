package net.minecraft.commands;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext extends HolderLookup.a {

    static CommandBuildContext simple(final HolderLookup.a holderlookup_a, final FeatureFlagSet featureflagset) {
        return new CommandBuildContext() {
            @Override
            public Stream<ResourceKey<? extends IRegistry<?>>> listRegistries() {
                return holderlookup_a.listRegistries();
            }

            @Override
            public <T> Optional<HolderLookup.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                return holderlookup_a.lookup(resourcekey).map((holderlookup_b) -> {
                    return holderlookup_b.filterFeatures(featureflagset);
                });
            }
        };
    }
}
