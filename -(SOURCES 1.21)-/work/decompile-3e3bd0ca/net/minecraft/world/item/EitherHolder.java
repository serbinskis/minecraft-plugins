package net.minecraft.world.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record EitherHolder<T>(Optional<Holder<T>> holder, ResourceKey<T> key) {

    public EitherHolder(Holder<T> holder) {
        this(Optional.of(holder), (ResourceKey) holder.unwrapKey().orElseThrow());
    }

    public EitherHolder(ResourceKey<T> resourcekey) {
        this(Optional.empty(), resourcekey);
    }

    public static <T> Codec<EitherHolder<T>> codec(ResourceKey<IRegistry<T>> resourcekey, Codec<Holder<T>> codec) {
        return Codec.either(codec, ResourceKey.codec(resourcekey).comapFlatMap((resourcekey1) -> {
            return DataResult.error(() -> {
                return "Cannot parse as key without registry";
            });
        }, Function.identity())).xmap(EitherHolder::fromEither, EitherHolder::asEither);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, EitherHolder<T>> streamCodec(ResourceKey<IRegistry<T>> resourcekey, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> streamcodec) {
        return StreamCodec.composite(ByteBufCodecs.either(streamcodec, ResourceKey.streamCodec(resourcekey)), EitherHolder::asEither, EitherHolder::fromEither);
    }

    public Either<Holder<T>, ResourceKey<T>> asEither() {
        return (Either) this.holder.map(Either::left).orElseGet(() -> {
            return Either.right(this.key);
        });
    }

    public static <T> EitherHolder<T> fromEither(Either<Holder<T>, ResourceKey<T>> either) {
        return (EitherHolder) either.map(EitherHolder::new, EitherHolder::new);
    }

    public Optional<T> unwrap(IRegistry<T> iregistry) {
        return this.holder.map(Holder::value).or(() -> {
            return iregistry.getOptional(this.key);
        });
    }

    public Optional<Holder<T>> unwrap(HolderLookup.a holderlookup_a) {
        return this.holder.or(() -> {
            return holderlookup_a.lookupOrThrow(this.key.registryKey()).get(this.key);
        });
    }
}
