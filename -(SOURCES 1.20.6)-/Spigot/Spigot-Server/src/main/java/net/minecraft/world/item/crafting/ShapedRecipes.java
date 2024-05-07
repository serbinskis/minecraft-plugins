package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

// CraftBukkit start
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
// CraftBukkit end

public class ShapedRecipes implements RecipeCrafting {

    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, ShapedRecipePattern shapedrecipepattern, ItemStack itemstack, boolean flag) {
        this.group = s;
        this.category = craftingbookcategory;
        this.pattern = shapedrecipepattern;
        this.result = itemstack;
        this.showNotification = flag;
    }

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, ShapedRecipePattern shapedrecipepattern, ItemStack itemstack) {
        this(s, craftingbookcategory, shapedrecipepattern, itemstack, true);
    }

    // CraftBukkit start
    @Override
    public org.bukkit.inventory.ShapedRecipe toBukkitRecipe(NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapedRecipe recipe = new CraftShapedRecipe(id, result, this);
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        switch (this.pattern.height()) {
        case 1:
            switch (this.pattern.width()) {
            case 1:
                recipe.shape("a");
                break;
            case 2:
                recipe.shape("ab");
                break;
            case 3:
                recipe.shape("abc");
                break;
            }
            break;
        case 2:
            switch (this.pattern.width()) {
            case 1:
                recipe.shape("a","b");
                break;
            case 2:
                recipe.shape("ab","cd");
                break;
            case 3:
                recipe.shape("abc","def");
                break;
            }
            break;
        case 3:
            switch (this.pattern.width()) {
            case 1:
                recipe.shape("a","b","c");
                break;
            case 2:
                recipe.shape("ab","cd","ef");
                break;
            case 3:
                recipe.shape("abc","def","ghi");
                break;
            }
            break;
        }
        char c = 'a';
        for (RecipeItemStack list : this.pattern.ingredients()) {
            RecipeChoice choice = CraftRecipe.toBukkit(list);
            if (choice != null) {
                recipe.setIngredient(c, choice);
            }

            c++;
        }
        return recipe;
    }
    // CraftBukkit end

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
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
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i >= this.pattern.width() && j >= this.pattern.height();
    }

    public boolean matches(InventoryCrafting inventorycrafting, World world) {
        return this.pattern.matches(inventorycrafting);
    }

    public ItemStack assemble(InventoryCrafting inventorycrafting, HolderLookup.a holderlookup_a) {
        return this.getResultItem(holderlookup_a).copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<RecipeItemStack> nonnulllist = this.getIngredients();

        return nonnulllist.isEmpty() || nonnulllist.stream().filter((recipeitemstack) -> {
            return !recipeitemstack.isEmpty();
        }).anyMatch((recipeitemstack) -> {
            return recipeitemstack.getItems().length == 0;
        });
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipes> {

        public static final MapCodec<ShapedRecipes> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapedrecipes) -> {
                return shapedrecipes.group;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapedrecipes) -> {
                return shapedrecipes.category;
            }), ShapedRecipePattern.MAP_CODEC.forGetter((shapedrecipes) -> {
                return shapedrecipes.pattern;
            }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapedrecipes) -> {
                return shapedrecipes.result;
            }), Codec.BOOL.optionalFieldOf("show_notification", true).forGetter((shapedrecipes) -> {
                return shapedrecipes.showNotification;
            })).apply(instance, ShapedRecipes::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipes> STREAM_CODEC = StreamCodec.of(ShapedRecipes.Serializer::toNetwork, ShapedRecipes.Serializer::fromNetwork);

        public Serializer() {}

        @Override
        public MapCodec<ShapedRecipes> codec() {
            return ShapedRecipes.Serializer.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipes> streamCodec() {
            return ShapedRecipes.Serializer.STREAM_CODEC;
        }

        private static ShapedRecipes fromNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            String s = registryfriendlybytebuf.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) registryfriendlybytebuf.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = (ShapedRecipePattern) ShapedRecipePattern.STREAM_CODEC.decode(registryfriendlybytebuf);
            ItemStack itemstack = (ItemStack) ItemStack.STREAM_CODEC.decode(registryfriendlybytebuf);
            boolean flag = registryfriendlybytebuf.readBoolean();

            return new ShapedRecipes(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        private static void toNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf, ShapedRecipes shapedrecipes) {
            registryfriendlybytebuf.writeUtf(shapedrecipes.group);
            registryfriendlybytebuf.writeEnum(shapedrecipes.category);
            ShapedRecipePattern.STREAM_CODEC.encode(registryfriendlybytebuf, shapedrecipes.pattern);
            ItemStack.STREAM_CODEC.encode(registryfriendlybytebuf, shapedrecipes.result);
            registryfriendlybytebuf.writeBoolean(shapedrecipes.showNotification);
        }
    }
}
