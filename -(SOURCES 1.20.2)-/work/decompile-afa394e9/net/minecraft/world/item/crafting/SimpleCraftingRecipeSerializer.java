package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.network.PacketDataSerializer;

public class SimpleCraftingRecipeSerializer<T extends RecipeCrafting> implements RecipeSerializer<T> {

    private final SimpleCraftingRecipeSerializer.a<T> constructor;
    private final Codec<T> codec;

    public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.a<T> simplecraftingrecipeserializer_a) {
        this.constructor = simplecraftingrecipeserializer_a;
        this.codec = RecordCodecBuilder.create((instance) -> {
            P1 p1 = instance.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(RecipeCrafting::category));

            Objects.requireNonNull(simplecraftingrecipeserializer_a);
            return p1.apply(instance, simplecraftingrecipeserializer_a::create);
        });
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }

    @Override
    public T fromNetwork(PacketDataSerializer packetdataserializer) {
        CraftingBookCategory craftingbookcategory = (CraftingBookCategory) packetdataserializer.readEnum(CraftingBookCategory.class);

        return this.constructor.create(craftingbookcategory);
    }

    public void toNetwork(PacketDataSerializer packetdataserializer, T t0) {
        packetdataserializer.writeEnum(t0.category());
    }

    @FunctionalInterface
    public interface a<T extends RecipeCrafting> {

        T create(CraftingBookCategory craftingbookcategory);
    }
}
