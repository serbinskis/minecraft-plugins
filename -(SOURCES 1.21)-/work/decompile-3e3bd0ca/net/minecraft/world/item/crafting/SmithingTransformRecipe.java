package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public class SmithingTransformRecipe implements SmithingRecipe {

    final RecipeItemStack template;
    final RecipeItemStack base;
    final RecipeItemStack addition;
    final ItemStack result;

    public SmithingTransformRecipe(RecipeItemStack recipeitemstack, RecipeItemStack recipeitemstack1, RecipeItemStack recipeitemstack2, ItemStack itemstack) {
        this.template = recipeitemstack;
        this.base = recipeitemstack1;
        this.addition = recipeitemstack2;
        this.result = itemstack;
    }

    public boolean matches(SmithingRecipeInput smithingrecipeinput, World world) {
        return this.template.test(smithingrecipeinput.template()) && this.base.test(smithingrecipeinput.base()) && this.addition.test(smithingrecipeinput.addition());
    }

    public ItemStack assemble(SmithingRecipeInput smithingrecipeinput, HolderLookup.a holderlookup_a) {
        ItemStack itemstack = smithingrecipeinput.base().transmuteCopy(this.result.getItem(), this.result.getCount());

        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.a holderlookup_a) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack itemstack) {
        return this.template.test(itemstack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack itemstack) {
        return this.base.test(itemstack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack itemstack) {
        return this.addition.test(itemstack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(RecipeItemStack::isEmpty);
    }

    public static class a implements RecipeSerializer<SmithingTransformRecipe> {

        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(RecipeItemStack.CODEC.fieldOf("template").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.template;
            }), RecipeItemStack.CODEC.fieldOf("base").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.base;
            }), RecipeItemStack.CODEC.fieldOf("addition").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.addition;
            }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.result;
            })).apply(instance, SmithingTransformRecipe::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.of(SmithingTransformRecipe.a::toNetwork, SmithingTransformRecipe.a::fromNetwork);

        public a() {}

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return SmithingTransformRecipe.a.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
            return SmithingTransformRecipe.a.STREAM_CODEC;
        }

        private static SmithingTransformRecipe fromNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            RecipeItemStack recipeitemstack = (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
            RecipeItemStack recipeitemstack1 = (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
            RecipeItemStack recipeitemstack2 = (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
            ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(registryfriendlybytebuf);

            return new SmithingTransformRecipe(recipeitemstack, recipeitemstack1, recipeitemstack2, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf, SmithingTransformRecipe smithingtransformrecipe) {
            RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, smithingtransformrecipe.template);
            RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, smithingtransformrecipe.base);
            RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, smithingtransformrecipe.addition);
            ItemStack.STREAM_CODEC.encode(registryfriendlybytebuf, smithingtransformrecipe.result);
        }
    }
}
