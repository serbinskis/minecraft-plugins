package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SimpleCraftingRecipeSerializer<T extends RecipeCrafting> implements RecipeSerializer<T> {

    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.a<T> simplecraftingrecipeserializer_a) {
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            P1 p1 = instance.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(RecipeCrafting::category));

            Objects.requireNonNull(simplecraftingrecipeserializer_a);
            return p1.apply(instance, simplecraftingrecipeserializer_a::create);
        });
        StreamCodec streamcodec = CraftingBookCategory.STREAM_CODEC;
        Function function = RecipeCrafting::category;

        Objects.requireNonNull(simplecraftingrecipeserializer_a);
        this.streamCodec = StreamCodec.composite(streamcodec, function, simplecraftingrecipeserializer_a::create);
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    @FunctionalInterface
    public interface a<T extends RecipeCrafting> {

        T create(CraftingBookCategory craftingbookcategory);
    }
}
