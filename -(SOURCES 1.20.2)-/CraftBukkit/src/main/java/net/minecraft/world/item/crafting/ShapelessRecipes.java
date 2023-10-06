package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.NonNullList;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

// CraftBukkit start
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
// CraftBukkit end

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

    // CraftBukkit start
    @SuppressWarnings("unchecked")
    @Override
    public org.bukkit.inventory.ShapelessRecipe toBukkitRecipe(NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapelessRecipe recipe = new CraftShapelessRecipe(id, result, this);
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        for (RecipeItemStack list : this.ingredients) {
            recipe.addIngredient(CraftRecipe.toBukkit(list));
        }
        return recipe;
    }
    // CraftBukkit end

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
    public ItemStack getResultItem(IRegistryCustom iregistrycustom) {
        return this.result;
    }

    @Override
    public NonNullList<RecipeItemStack> getIngredients() {
        return this.ingredients;
    }

    public boolean matches(InventoryCrafting inventorycrafting, World world) {
        AutoRecipeStackManager autorecipestackmanager = new AutoRecipeStackManager();
        int i = 0;

        for (int j = 0; j < inventorycrafting.getContainerSize(); ++j) {
            ItemStack itemstack = inventorycrafting.getItem(j);

            if (!itemstack.isEmpty()) {
                ++i;
                autorecipestackmanager.accountStack(itemstack, 1);
            }
        }

        return i == this.ingredients.size() && autorecipestackmanager.canCraft(this, (IntList) null);
    }

    public ItemStack assemble(InventoryCrafting inventorycrafting, IRegistryCustom iregistrycustom) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= this.ingredients.size();
    }

    public static class a implements RecipeSerializer<ShapelessRecipes> {

        private static final Codec<ShapelessRecipes> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((shapelessrecipes) -> {
                return shapelessrecipes.group;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapelessrecipes) -> {
                return shapelessrecipes.category;
            }), CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter((shapelessrecipes) -> {
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

        public a() {}

        @Override
        public Codec<ShapelessRecipes> codec() {
            return ShapelessRecipes.a.CODEC;
        }

        @Override
        public ShapelessRecipes fromNetwork(PacketDataSerializer packetdataserializer) {
            String s = packetdataserializer.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) packetdataserializer.readEnum(CraftingBookCategory.class);
            int i = packetdataserializer.readVarInt();
            NonNullList<RecipeItemStack> nonnulllist = NonNullList.withSize(i, RecipeItemStack.EMPTY);

            for (int j = 0; j < nonnulllist.size(); ++j) {
                nonnulllist.set(j, RecipeItemStack.fromNetwork(packetdataserializer));
            }

            ItemStack itemstack = packetdataserializer.readItem();

            return new ShapelessRecipes(s, craftingbookcategory, itemstack, nonnulllist);
        }

        public void toNetwork(PacketDataSerializer packetdataserializer, ShapelessRecipes shapelessrecipes) {
            packetdataserializer.writeUtf(shapelessrecipes.group);
            packetdataserializer.writeEnum(shapelessrecipes.category);
            packetdataserializer.writeVarInt(shapelessrecipes.ingredients.size());
            Iterator iterator = shapelessrecipes.ingredients.iterator();

            while (iterator.hasNext()) {
                RecipeItemStack recipeitemstack = (RecipeItemStack) iterator.next();

                recipeitemstack.toNetwork(packetdataserializer);
            }

            packetdataserializer.writeItem(shapelessrecipes.result);
        }
    }
}
