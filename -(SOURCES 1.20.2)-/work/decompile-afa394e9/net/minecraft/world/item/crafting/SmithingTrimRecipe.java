package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.world.IInventory;
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

    @Override
    public boolean matches(IInventory iinventory, World world) {
        return this.template.test(iinventory.getItem(0)) && this.base.test(iinventory.getItem(1)) && this.addition.test(iinventory.getItem(2));
    }

    @Override
    public ItemStack assemble(IInventory iinventory, IRegistryCustom iregistrycustom) {
        ItemStack itemstack = iinventory.getItem(1);

        if (this.base.test(itemstack)) {
            Optional<Holder.c<TrimMaterial>> optional = TrimMaterials.getFromIngredient(iregistrycustom, iinventory.getItem(2));
            Optional<Holder.c<TrimPattern>> optional1 = TrimPatterns.getFromTemplate(iregistrycustom, iinventory.getItem(0));

            if (optional.isPresent() && optional1.isPresent()) {
                Optional<ArmorTrim> optional2 = ArmorTrim.getTrim(iregistrycustom, itemstack, false);

                if (optional2.isPresent() && ((ArmorTrim) optional2.get()).hasPatternAndMaterial((Holder) optional1.get(), (Holder) optional.get())) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack1 = itemstack.copy();

                itemstack1.setCount(1);
                ArmorTrim armortrim = new ArmorTrim((Holder) optional.get(), (Holder) optional1.get());

                if (ArmorTrim.setTrim(iregistrycustom, itemstack1, armortrim)) {
                    return itemstack1;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(IRegistryCustom iregistrycustom) {
        ItemStack itemstack = new ItemStack(Items.IRON_CHESTPLATE);
        Optional<Holder.c<TrimPattern>> optional = iregistrycustom.registryOrThrow(Registries.TRIM_PATTERN).holders().findFirst();

        if (optional.isPresent()) {
            Optional<Holder.c<TrimMaterial>> optional1 = iregistrycustom.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(TrimMaterials.REDSTONE);

            if (optional1.isPresent()) {
                ArmorTrim armortrim = new ArmorTrim((Holder) optional1.get(), (Holder) optional.get());

                ArmorTrim.setTrim(iregistrycustom, itemstack, armortrim);
            }
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

        private static final Codec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(RecipeItemStack.CODEC.fieldOf("template").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.template;
            }), RecipeItemStack.CODEC.fieldOf("base").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.base;
            }), RecipeItemStack.CODEC.fieldOf("addition").forGetter((smithingtrimrecipe) -> {
                return smithingtrimrecipe.addition;
            })).apply(instance, SmithingTrimRecipe::new);
        });

        public a() {}

        @Override
        public Codec<SmithingTrimRecipe> codec() {
            return SmithingTrimRecipe.a.CODEC;
        }

        @Override
        public SmithingTrimRecipe fromNetwork(PacketDataSerializer packetdataserializer) {
            RecipeItemStack recipeitemstack = RecipeItemStack.fromNetwork(packetdataserializer);
            RecipeItemStack recipeitemstack1 = RecipeItemStack.fromNetwork(packetdataserializer);
            RecipeItemStack recipeitemstack2 = RecipeItemStack.fromNetwork(packetdataserializer);

            return new SmithingTrimRecipe(recipeitemstack, recipeitemstack1, recipeitemstack2);
        }

        public void toNetwork(PacketDataSerializer packetdataserializer, SmithingTrimRecipe smithingtrimrecipe) {
            smithingtrimrecipe.template.toNetwork(packetdataserializer);
            smithingtrimrecipe.base.toNetwork(packetdataserializer);
            smithingtrimrecipe.addition.toNetwork(packetdataserializer);
        }
    }
}
