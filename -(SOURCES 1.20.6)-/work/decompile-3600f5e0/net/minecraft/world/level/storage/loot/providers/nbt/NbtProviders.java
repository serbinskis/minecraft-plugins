package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public class NbtProviders {

    private static final Codec<NbtProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE.byNameCodec().dispatch(NbtProvider::getType, LootNbtProviderType::codec);
    public static final Codec<NbtProvider> CODEC = Codec.lazyInitialized(() -> {
        return Codec.either(ContextNbtProvider.INLINE_CODEC, NbtProviders.TYPED_CODEC).xmap(Either::unwrap, (nbtprovider) -> {
            Either either;

            if (nbtprovider instanceof ContextNbtProvider contextnbtprovider) {
                either = Either.left(contextnbtprovider);
            } else {
                either = Either.right(nbtprovider);
            }

            return either;
        });
    });
    public static final LootNbtProviderType STORAGE = register("storage", StorageNbtProvider.CODEC);
    public static final LootNbtProviderType CONTEXT = register("context", ContextNbtProvider.CODEC);

    public NbtProviders() {}

    private static LootNbtProviderType register(String s, MapCodec<? extends NbtProvider> mapcodec) {
        return (LootNbtProviderType) IRegistry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, new MinecraftKey(s), new LootNbtProviderType(mapcodec));
    }
}
