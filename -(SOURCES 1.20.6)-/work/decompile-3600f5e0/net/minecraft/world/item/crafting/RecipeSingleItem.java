package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;

public abstract class RecipeSingleItem implements IRecipe<IInventory> {

    protected final RecipeItemStack ingredient;
    protected final ItemStack result;
    private final Recipes<?> type;
    private final RecipeSerializer<?> serializer;
    protected final String group;

    public RecipeSingleItem(Recipes<?> recipes, RecipeSerializer<?> recipeserializer, String s, RecipeItemStack recipeitemstack, ItemStack itemstack) {
        this.type = recipes;
        this.serializer = recipeserializer;
        this.group = s;
        this.ingredient = recipeitemstack;
        this.result = itemstack;
    }

    @Override
    public Recipes<?> getType() {
        return this.type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.a holderlookup_a) {
        return this.result;
    }

    @Override
    public NonNullList<RecipeItemStack> getIngredients() {
        NonNullList<RecipeItemStack> nonnulllist = NonNullList.create();

        nonnulllist.add(this.ingredient);
        return nonnulllist;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return true;
    }

    @Override
    public ItemStack assemble(IInventory iinventory, HolderLookup.a holderlookup_a) {
        return this.result.copy();
    }

    public interface a<T extends RecipeSingleItem> {

        T create(String s, RecipeItemStack recipeitemstack, ItemStack itemstack);
    }

    public static class b<T extends RecipeSingleItem> implements RecipeSerializer<T> {

        final RecipeSingleItem.a<T> factory;
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        protected b(RecipeSingleItem.a<T> recipesingleitem_a) {
            this.factory = recipesingleitem_a;
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                P3 p3 = instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((recipesingleitem) -> {
                    return recipesingleitem.group;
                }), RecipeItemStack.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((recipesingleitem) -> {
                    return recipesingleitem.ingredient;
                }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((recipesingleitem) -> {
                    return recipesingleitem.result;
                }));

                Objects.requireNonNull(recipesingleitem_a);
                return p3.apply(instance, recipesingleitem_a::create);
            });
            StreamCodec streamcodec = ByteBufCodecs.STRING_UTF8;
            Function function = (recipesingleitem) -> {
                return recipesingleitem.group;
            };
            StreamCodec streamcodec1 = RecipeItemStack.CONTENTS_STREAM_CODEC;
            Function function1 = (recipesingleitem) -> {
                return recipesingleitem.ingredient;
            };
            StreamCodec streamcodec2 = ItemStack.STREAM_CODEC;
            Function function2 = (recipesingleitem) -> {
                return recipesingleitem.result;
            };

            Objects.requireNonNull(recipesingleitem_a);
            this.streamCodec = StreamCodec.composite(streamcodec, function, streamcodec1, function1, streamcodec2, function2, recipesingleitem_a::create);
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }
    }
}
