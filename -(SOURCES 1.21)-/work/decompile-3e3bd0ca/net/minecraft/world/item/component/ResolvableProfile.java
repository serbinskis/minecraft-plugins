package net.minecraft.world.item.component;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SystemUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.TileEntitySkull;

public record ResolvableProfile(Optional<String> name, Optional<UUID> id, PropertyMap properties, GameProfile gameProfile) {

    private static final Codec<ResolvableProfile> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.PLAYER_NAME.optionalFieldOf("name").forGetter(ResolvableProfile::name), UUIDUtil.CODEC.optionalFieldOf("id").forGetter(ResolvableProfile::id), ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(ResolvableProfile::properties)).apply(instance, ResolvableProfile::new);
    });
    public static final Codec<ResolvableProfile> CODEC = Codec.withAlternative(ResolvableProfile.FULL_CODEC, ExtraCodecs.PLAYER_NAME, (s) -> {
        return new ResolvableProfile(Optional.of(s), Optional.empty(), new PropertyMap());
    });
    public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.stringUtf8(16).apply(ByteBufCodecs::optional), ResolvableProfile::name, UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), ResolvableProfile::id, ByteBufCodecs.GAME_PROFILE_PROPERTIES, ResolvableProfile::properties, ResolvableProfile::new);

    public ResolvableProfile(Optional<String> optional, Optional<UUID> optional1, PropertyMap propertymap) {
        this(optional, optional1, propertymap, createProfile(optional, optional1, propertymap));
    }

    public ResolvableProfile(GameProfile gameprofile) {
        this(Optional.of(gameprofile.getName()), Optional.of(gameprofile.getId()), gameprofile.getProperties(), gameprofile);
    }

    public CompletableFuture<ResolvableProfile> resolve() {
        return this.isResolved() ? CompletableFuture.completedFuture(this) : (this.id.isPresent() ? TileEntitySkull.fetchGameProfile((UUID) this.id.get()).thenApply((optional) -> {
            GameProfile gameprofile = (GameProfile) optional.orElseGet(() -> {
                return new GameProfile((UUID) this.id.get(), (String) this.name.orElse(""));
            });

            return new ResolvableProfile(gameprofile);
        }) : TileEntitySkull.fetchGameProfile((String) this.name.orElseThrow()).thenApply((optional) -> {
            GameProfile gameprofile = (GameProfile) optional.orElseGet(() -> {
                return new GameProfile(SystemUtils.NIL_UUID, (String) this.name.get());
            });

            return new ResolvableProfile(gameprofile);
        }));
    }

    private static GameProfile createProfile(Optional<String> optional, Optional<UUID> optional1, PropertyMap propertymap) {
        GameProfile gameprofile = new GameProfile((UUID) optional1.orElse(SystemUtils.NIL_UUID), (String) optional.orElse(""));

        gameprofile.getProperties().putAll(propertymap);
        return gameprofile;
    }

    public boolean isResolved() {
        return !this.properties.isEmpty() ? true : this.id.isPresent() == this.name.isPresent();
    }
}
