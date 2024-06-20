package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.BiomeBase;

public final class WolfVariant {

    public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("wild_texture").forGetter((wolfvariant) -> {
            return wolfvariant.wildTexture;
        }), MinecraftKey.CODEC.fieldOf("tame_texture").forGetter((wolfvariant) -> {
            return wolfvariant.tameTexture;
        }), MinecraftKey.CODEC.fieldOf("angry_texture").forGetter((wolfvariant) -> {
            return wolfvariant.angryTexture;
        }), RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(WolfVariant::biomes)).apply(instance, WolfVariant::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, WolfVariant> DIRECT_STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, WolfVariant::wildTexture, MinecraftKey.STREAM_CODEC, WolfVariant::tameTexture, MinecraftKey.STREAM_CODEC, WolfVariant::angryTexture, ByteBufCodecs.holderSet(Registries.BIOME), WolfVariant::biomes, WolfVariant::new);
    public static final Codec<Holder<WolfVariant>> CODEC = RegistryFileCodec.create(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfVariant>> STREAM_CODEC = ByteBufCodecs.holder(Registries.WOLF_VARIANT, WolfVariant.DIRECT_STREAM_CODEC);
    private final MinecraftKey wildTexture;
    private final MinecraftKey tameTexture;
    private final MinecraftKey angryTexture;
    private final MinecraftKey wildTextureFull;
    private final MinecraftKey tameTextureFull;
    private final MinecraftKey angryTextureFull;
    private final HolderSet<BiomeBase> biomes;

    public WolfVariant(MinecraftKey minecraftkey, MinecraftKey minecraftkey1, MinecraftKey minecraftkey2, HolderSet<BiomeBase> holderset) {
        this.wildTexture = minecraftkey;
        this.wildTextureFull = fullTextureId(minecraftkey);
        this.tameTexture = minecraftkey1;
        this.tameTextureFull = fullTextureId(minecraftkey1);
        this.angryTexture = minecraftkey2;
        this.angryTextureFull = fullTextureId(minecraftkey2);
        this.biomes = holderset;
    }

    private static MinecraftKey fullTextureId(MinecraftKey minecraftkey) {
        return minecraftkey.withPath((s) -> {
            return "textures/" + s + ".png";
        });
    }

    public MinecraftKey wildTexture() {
        return this.wildTextureFull;
    }

    public MinecraftKey tameTexture() {
        return this.tameTextureFull;
    }

    public MinecraftKey angryTexture() {
        return this.angryTextureFull;
    }

    public HolderSet<BiomeBase> biomes() {
        return this.biomes;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (!(object instanceof WolfVariant)) {
            return false;
        } else {
            WolfVariant wolfvariant = (WolfVariant) object;

            return Objects.equals(this.wildTexture, wolfvariant.wildTexture) && Objects.equals(this.tameTexture, wolfvariant.tameTexture) && Objects.equals(this.angryTexture, wolfvariant.angryTexture) && Objects.equals(this.biomes, wolfvariant.biomes);
        }
    }

    public int hashCode() {
        int i = 1;

        i = 31 * i + this.wildTexture.hashCode();
        i = 31 * i + this.tameTexture.hashCode();
        i = 31 * i + this.angryTexture.hashCode();
        i = 31 * i + this.biomes.hashCode();
        return i;
    }
}
