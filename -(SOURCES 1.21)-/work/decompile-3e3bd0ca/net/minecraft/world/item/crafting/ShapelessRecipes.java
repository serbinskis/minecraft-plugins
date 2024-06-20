package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public class ShapelessRecipes implements RecipeCrafting {

    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<RecipeItemStack> ingredients;

    public ShapelessRecipes(String s, CraftingBookCategory craftingbookcategory, ItemStack itemstack, NonNullList<RecipeItemStack> nonnulllist) {
        this.group = s;
        this.category = craftingbookcategory;
        this.result = itemstack;
        this.ingredients = nonnulllist;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.a holderlookup_a) {
        return this.result;
    }

    @Override
    public NonNullList<RecipeItemStack> getIngredients() {
        return this.ingredients;
    }

    public boolean matches(CraftingInput craftinginput, World world) {
        return craftinginput.ingredientCount() != this.ingredients.size() ? false : (craftinginput.size() == 1 && this.ingredients.size() == 1 ? ((RecipeItemStack) this.ingredients.getFirst()).test(craftinginput.getItem(0)) : craftinginput.stackedContents().canCraft(this, (IntList) null));
    }

    public ItemStack assemble(CraftingInput craftinginput, HolderLookup.a holderlookup_a) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= this.ingredients.size();
    }

    public static class a implements RecipeSerializer<ShapelessRecipes> {

        private static final MapCodec<ShapelessRecipes> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapelessrecipes) -> {
                return shapelessrecipes.group;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapelessrecipes) -> {
                return shapelessrecipes.category;
            }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapelessrecipes) -> {
                return shapelessrecipes.result;
            }), RecipeItemStack.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((list) -> {
                RecipeItemStack[] arecipeitemstack = (RecipeItemStack[]) list.stream().filter((recipeitemstack) -> {
                    return !recipeitemstack.isEmpty();
                }).toArray((i) -> {
                    return new RecipeItemStack[i];
                });

                return arecipeitemstack.length == 0 ? DataResult.error(() -> {
                    return "No ingredients for shapeless recipe";
                }) : (arecipeitemstack.length > 9 ? DataResult.error(() -> {
                    return "Too many ingredients for shapeless recipe";
                }) : DataResult.success(NonNullList.of(RecipeItemStack.EMPTY, arecipeitemstack)));
            }, DataResult::success).forGetter((shapelessrecipes) -> {
                return shapelessrecipes.ingredients;
            })).apply(instance, ShapelessRecipes::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipes> STREAM_CODEC = StreamCodec.of(ShapelessRecipes.a::toNetwork, ShapelessRecipes.a::fromNetwork);

        public a() {}

        @Override
        public MapCodec<ShapelessRecipes> codec() {
            return ShapelessRecipes.a.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipes> streamCodec() {
            return ShapelessRecipes.a.STREAM_CODEC;
        }

        private static ShapelessRecipes fromNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            String s = registryfriendlybytebuf.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) registryfriendlybytebuf.readEnum(CraftingBookCategory.class);
            int i = registryfriendlybytebuf.readVarInt();
            NonNullList<RecipeItemStack> nonnulllist = NonNullList.withSize(i, RecipeItemStack.EMPTY);

            nonnulllist.replaceAll((recipeitemstack) -> {
                return (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
            });
            ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(registryfriendlybytebuf);

            return new ShapelessRecipes(s, craftingbookcategory, itemstack, nonnulllist);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf, ShapelessRecipes shapelessrecipes) {
            registryfriendlybytebuf.writeUtf(shapelessrecipes.group);
            registryfriendlybytebuf.writeEnum(shapelessrecipes.category);
            registryfriendlybytebuf.writeVarInt(shapelessrecipes.ingredients.size());
            Iterator iterator = shapelessrecipes.ingredients.iterator();

            while (iterator.hasNext()) {
                RecipeItemStack recipeitemstack = (RecipeItemStack) iterator.next();

                RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, recipeitemstack);
            }

            ItemStack.STREAM_CODEC.encode(registryfriendlybytebuf, shapelessrecipes.result);
        }
    }
}
