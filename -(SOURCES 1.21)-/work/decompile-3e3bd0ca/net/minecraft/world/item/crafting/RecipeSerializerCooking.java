package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class RecipeSerializerCooking<T extends RecipeCooking> implements RecipeSerializer<T> {

    private final RecipeCooking.a<T> factory;
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public RecipeSerializerCooking(RecipeCooking.a<T> recipecooking_a, int i) {
        this.factory = recipecooking_a;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            P6 p6 = instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((recipecooking) -> {
                return recipecooking.group;
            }), CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter((recipecooking) -> {
                return recipecooking.category;
            }), RecipeItemStack.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((recipecooking) -> {
                return recipecooking.ingredient;
            }), ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter((recipecooking) -> {
                return recipecooking.result;
            }), Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter((recipecooking) -> {
                return recipecooking.experience;
            }), Codec.INT.fieldOf("cookingtime").orElse(i).forGetter((recipecooking) -> {
                return recipecooking.cookingTime;
            }));

            Objects.requireNonNull(recipecooking_a);
            return p6.apply(instance, recipecooking_a::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    private T fromNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        String s = registryfriendlybytebuf.readUtf();
        CookingBookCategory cookingbookcategory = (CookingBookCategory) registryfriendlybytebuf.readEnum(CookingBookCategory.class);
        RecipeItemStack recipeitemstack = (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
        ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(registryfriendlybytebuf);
        float f = registryfriendlybytebuf.readFloat();
        int i = registryfriendlybytebuf.readVarInt();

        return this.factory.create(s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }

    private void toNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf, T t0) {
        registryfriendlybytebuf.writeUtf(t0.group);
        registryfriendlybytebuf.writeEnum(t0.category());
        RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, t0.ingredient);
        ItemStack.STREAM_CODEC.encode(registryfriendlybytebuf, t0.result);
        registryfriendlybytebuf.writeFloat(t0.experience);
        registryfriendlybytebuf.writeVarInt(t0.cookingTime);
    }

    public RecipeCooking create(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        return this.factory.create(s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }
}
