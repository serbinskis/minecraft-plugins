package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.World;

public class SmithingTrimRecipe implements SmithingRecipe {

    final RecipeItemStack template;
    final RecipeItemStack base;
    final RecipeItemStack addition;

    public SmithingTrimRecipe(RecipeItemStack recipeitemstack, RecipeItemStack recipeitemstack1, RecipeItemStack recipeitemstack2) {
        this.template = recipeitemstack;
        this.base = recipeitemstack1;
        this.addition = recipeitemstack2;
    }

    public boolean matches(SmithingRecipeInput smithingrecipeinput, World world) {
        return this.template.test(smithingrecipeinput.template()) && this.base.test(smithingrecipeinput.base()) && this.addition.test(smithingrecipeinput.addition());
    }

    public ItemStack assemble(SmithingRecipeInput smithingrecipeinput, HolderLookup.a holderlookup_a) {
        ItemStack itemstack = smithingrecipeinput.base();

        if (this.base.test(itemstack)) {
            Optional<Holder.c<TrimMaterial>> optional = TrimMaterials.getFromIngredient(holderlookup_a, smithingrecipeinput.addition());
            Optional<Holder.c<TrimPattern>> optional1 = TrimPatterns.getFromTemplate(holderlookup_a, smithingrecipeinput.template());

            if (optional.isPresent() && optional1.isPresent()) {
                ArmorTrim armortrim = (ArmorTrim) itemstack.get(DataComponents.TRIM);

                if (armortrim != null && armortrim.hasPatternAndMaterial((Holder) optional1.get(), (Holder) optional.get())) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack1 = itemstack.copyWithCount(1);

                itemstack1.set(DataComponents.TRIM, new ArmorTrim((Holder) optional.get(), (Holder) optional1.get()));
                return itemstack1;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.a holderlookup_a) {
        ItemStack itemstack = new ItemStack(Items.IRON_CHESTPLATE);
        Optional<Holder.c<TrimPattern>> optional = holderlookup_a.lookupOrThrow(Registries.TRIM_PATTERN).listElements().findFirst();
        Optional<Holder.c<TrimMaterial>> optional1 = holderlookup_a.lookupOrThrow(Registries.TRIM_MATERIAL).get(TrimMaterials.REDSTONE);

        if (optional.isPresent() && optional1.isPresent()) {
            itemstack.set(DataComponents.TRIM, new ArmorTrim((Holder) optional1.get(), (Holder) optional.get()));
        }

        return itemstack;
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
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(RecipeItemStack::isEmpty);
    }

    public static class a implements RecipeSerializer<SmithingTrimRecipe> {

        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(RecipeItemStack.CODEC.fieldOf("template").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.template;
            }), RecipeItemStack.CODEC.fieldOf("base").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.base;
            }), RecipeItemStack.CODEC.fieldOf("addition").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.addition;
            })).apply(instance, SmithingTrimRecipe::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.of(SmithingTrimRecipe.a::toNetwork, SmithingTrimRecipe.a::fromNetwork);

        public a() {}

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return SmithingTrimRecipe.a.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
            return SmithingTrimRecipe.a.STREAM_CODEC;
        }

        private static SmithingTrimRecipe fromNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            RecipeItemStack recipeitemstack = (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
            RecipeItemStack recipeitemstack1 = (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
            RecipeItemStack recipeitemstack2 = (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);

            return new SmithingTrimRecipe(recipeitemstack, recipeitemstack1, recipeitemstack2);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf, SmithingTrimRecipe smithingtrimrecipe) {
            RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, smithingtrimrecipe.template);
            RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, smithingtrimrecipe.base);
            RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, smithingtrimrecipe.addition);
        }
    }
}
