package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.world.IInventory;
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

    @Override
    public boolean matches(IInventory iinventory, World world) {
        return this.template.test(iinventory.getItem(0)) && this.base.test(iinventory.getItem(1)) && this.addition.test(iinventory.getItem(2));
    }

    @Override
    public ItemStack assemble(IInventory iinventory, IRegistryCustom iregistrycustom) {
        ItemStack itemstack = this.result.copy();
        NBTTagCompound nbttagcompound = iinventory.getItem(1).getTag();

        if (nbttagcompound != null) {
            itemstack.setTag(nbttagcompound.copy());
        }

        return itemstack;
    }

    @Override
    public ItemStack getResultItem(IRegistryCustom iregistrycustom) {
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

        private static final Codec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(RecipeItemStack.CODEC.fieldOf("template").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.template;
            }), RecipeItemStack.CODEC.fieldOf("base").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.base;
            }), RecipeItemStack.CODEC.fieldOf("addition").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.addition;
            }), CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter((smithingtransformrecipe) -> {
                return smithingtransformrecipe.result;
            })).apply(instance, SmithingTransformRecipe::new);
        });

        public a() {}

        @Override
        public Codec<SmithingTransformRecipe> codec() {
            return SmithingTransformRecipe.a.CODEC;
        }

        @Override
        public SmithingTransformRecipe fromNetwork(PacketDataSerializer packetdataserializer) {
            RecipeItemStack recipeitemstack = RecipeItemStack.fromNetwork(packetdataserializer);
            RecipeItemStack recipeitemstack1 = RecipeItemStack.fromNetwork(packetdataserializer);
            RecipeItemStack recipeitemstack2 = RecipeItemStack.fromNetwork(packetdataserializer);
            ItemStack itemstack = packetdataserializer.readItem();

            return new SmithingTransformRecipe(recipeitemstack, recipeitemstack1, recipeitemstack2, itemstack);
        }

        public void toNetwork(PacketDataSerializer packetdataserializer, SmithingTransformRecipe smithingtransformrecipe) {
            smithingtransformrecipe.template.toNetwork(packetdataserializer);
            smithingtransformrecipe.base.toNetwork(packetdataserializer);
            smithingtransformrecipe.addition.toNetwork(packetdataserializer);
            packetdataserializer.writeItem(smithingtransformrecipe.result);
        }
    }
}
