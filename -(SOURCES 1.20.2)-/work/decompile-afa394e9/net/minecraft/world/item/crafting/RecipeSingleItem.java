package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.ExtraCodecs;
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
    public ItemStack getResultItem(IRegistryCustom iregistrycustom) {
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
    public ItemStack assemble(IInventory iinventory, IRegistryCustom iregistrycustom) {
        return this.result.copy();
    }

    public static class a<T extends RecipeSingleItem> implements RecipeSerializer<T> {

        private static final MapCodec<ItemStack> RESULT_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(ItemStack::getItem), Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)).apply(instance, ItemStack::new);
        });
        final RecipeSingleItem.a.a<T> factory;
        private final Codec<T> codec;

        protected a(RecipeSingleItem.a.a<T> recipesingleitem_a_a) {
            this.factory = recipesingleitem_a_a;
            this.codec = RecordCodecBuilder.create((instance) -> {
                P3 p3 = instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((recipesingleitem) -> {
                    return recipesingleitem.group;
                }), RecipeItemStack.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((recipesingleitem) -> {
                    return recipesingleitem.ingredient;
                }), RecipeSingleItem.a.RESULT_CODEC.forGetter((recipesingleitem) -> {
                    return recipesingleitem.result;
                }));

                Objects.requireNonNull(recipesingleitem_a_a);
                return p3.apply(instance, recipesingleitem_a_a::create);
            });
        }

        @Override
        public Codec<T> codec() {
            return this.codec;
        }

        @Override
        public T fromNetwork(PacketDataSerializer packetdataserializer) {
            String s = packetdataserializer.readUtf();
            RecipeItemStack recipeitemstack = RecipeItemStack.fromNetwork(packetdataserializer);
            ItemStack itemstack = packetdataserializer.readItem();

            return this.factory.create(s, recipeitemstack, itemstack);
        }

        public void toNetwork(PacketDataSerializer packetdataserializer, T t0) {
            packetdataserializer.writeUtf(t0.group);
            t0.ingredient.toNetwork(packetdataserializer);
            packetdataserializer.writeItem(t0.result);
        }

        interface a<T extends RecipeSingleItem> {

            T create(String s, RecipeItemStack recipeitemstack, ItemStack itemstack);
        }
    }
}
