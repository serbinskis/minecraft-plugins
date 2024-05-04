package net.minecraft.world.entity.animal;

import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public record FrogVariant(MinecraftKey texture) {

    public static final ResourceKey<FrogVariant> TEMPERATE = createKey("temperate");
    public static final ResourceKey<FrogVariant> WARM = createKey("warm");
    public static final ResourceKey<FrogVariant> COLD = createKey("cold");

    private static ResourceKey<FrogVariant> createKey(String s) {
        return ResourceKey.create(Registries.FROG_VARIANT, new MinecraftKey(s));
    }

    public static FrogVariant bootstrap(IRegistry<FrogVariant> iregistry) {
        register(iregistry, FrogVariant.TEMPERATE, "textures/entity/frog/temperate_frog.png");
        register(iregistry, FrogVariant.WARM, "textures/entity/frog/warm_frog.png");
        return register(iregistry, FrogVariant.COLD, "textures/entity/frog/cold_frog.png");
    }

    private static FrogVariant register(IRegistry<FrogVariant> iregistry, ResourceKey<FrogVariant> resourcekey, String s) {
        return (FrogVariant) IRegistry.register(iregistry, resourcekey, new FrogVariant(new MinecraftKey(s)));
    }
}
