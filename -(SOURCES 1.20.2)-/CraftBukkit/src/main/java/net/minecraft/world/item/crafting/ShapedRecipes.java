package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.NonNullList;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import org.apache.commons.lang3.NotImplementedException;

// CraftBukkit start
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
// CraftBukkit end

public class ShapedRecipes implements RecipeCrafting {

    final int width;
    final int height;
    final NonNullList<RecipeItemStack> recipeItems;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, int i, int j, NonNullList<RecipeItemStack> nonnulllist, ItemStack itemstack, boolean flag) {
        this.group = s;
        this.category = craftingbookcategory;
        this.width = i;
        this.height = j;
        this.recipeItems = nonnulllist;
        this.result = itemstack;
        this.showNotification = flag;
    }

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, int i, int j, NonNullList<RecipeItemStack> nonnulllist, ItemStack itemstack) {
        this(s, craftingbookcategory, i, j, nonnulllist, itemstack, true);
    }

    // CraftBukkit start
    @Override
    public org.bukkit.inventory.ShapedRecipe toBukkitRecipe(NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapedRecipe recipe = new CraftShapedRecipe(id, result, this);
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        switch (this.height) {
        case 1:
            switch (this.width) {
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
            switch (this.width) {
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
            switch (this.width) {
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
        for (RecipeItemStack list : this.recipeItems) {
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
    public ItemStack getResultItem(IRegistryCustom iregistrycustom) {
        return this.result;
    }

    @Override
    public NonNullList<RecipeItemStack> getIngredients() {
        return this.recipeItems;
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i >= this.width && j >= this.height;
    }

    public boolean matches(InventoryCrafting inventorycrafting, World world) {
        for (int i = 0; i <= inventorycrafting.getWidth() - this.width; ++i) {
            for (int j = 0; j <= inventorycrafting.getHeight() - this.height; ++j) {
                if (this.matches(inventorycrafting, i, j, true)) {
                    return true;
                }

                if (this.matches(inventorycrafting, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matches(InventoryCrafting inventorycrafting, int i, int j, boolean flag) {
        for (int k = 0; k < inventorycrafting.getWidth(); ++k) {
            for (int l = 0; l < inventorycrafting.getHeight(); ++l) {
                int i1 = k - i;
                int j1 = l - j;
                RecipeItemStack recipeitemstack = RecipeItemStack.EMPTY;

                if (i1 >= 0 && j1 >= 0 && i1 < this.width && j1 < this.height) {
                    if (flag) {
                        recipeitemstack = (RecipeItemStack) this.recipeItems.get(this.width - i1 - 1 + j1 * this.width);
                    } else {
                        recipeitemstack = (RecipeItemStack) this.recipeItems.get(i1 + j1 * this.width);
                    }
                }

                if (!recipeitemstack.test(inventorycrafting.getItem(k + l * inventorycrafting.getWidth()))) {
                    return false;
                }
            }
        }

        return true;
    }

    public ItemStack assemble(InventoryCrafting inventorycrafting, IRegistryCustom iregistrycustom) {
        return this.getResultItem(iregistrycustom).copy();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @VisibleForTesting
    static String[] shrink(List<String> list) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for (int i1 = 0; i1 < list.size(); ++i1) {
            String s = (String) list.get(i1);

            i = Math.min(i, firstNonSpace(s));
            int j1 = lastNonSpace(s);

            j = Math.max(j, j1);
            if (j1 < 0) {
                if (k == i1) {
                    ++k;
                }

                ++l;
            } else {
                l = 0;
            }
        }

        if (list.size() == l) {
            return new String[0];
        } else {
            String[] astring = new String[list.size() - l - k];

            for (int k1 = 0; k1 < astring.length; ++k1) {
                astring[k1] = ((String) list.get(k1 + k)).substring(i, j + 1);
            }

            return astring;
        }
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

    private static int firstNonSpace(String s) {
        int i;

        for (i = 0; i < s.length() && s.charAt(i) == ' '; ++i) {
            ;
        }

        return i;
    }

    private static int lastNonSpace(String s) {
        int i;

        for (i = s.length() - 1; i >= 0 && s.charAt(i) == ' '; --i) {
            ;
        }

        return i;
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipes> {

        static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().flatXmap((list) -> {
            if (list.size() > 3) {
                return DataResult.error(() -> {
                    return "Invalid pattern: too many rows, 3 is maximum";
                });
            } else if (list.isEmpty()) {
                return DataResult.error(() -> {
                    return "Invalid pattern: empty pattern not allowed";
                });
            } else {
                int i = ((String) list.get(0)).length();
                Iterator iterator = list.iterator();

                String s;

                do {
                    if (!iterator.hasNext()) {
                        return DataResult.success(list);
                    }

                    s = (String) iterator.next();
                    if (s.length() > 3) {
                        return DataResult.error(() -> {
                            return "Invalid pattern: too many columns, 3 is maximum";
                        });
                    }
                } while (i == s.length());

                return DataResult.error(() -> {
                    return "Invalid pattern: each row must be the same width";
                });
            }
        }, DataResult::success);
        static final Codec<String> SINGLE_CHARACTER_STRING_CODEC = Codec.STRING.flatXmap((s) -> {
            return s.length() != 1 ? DataResult.error(() -> {
                return "Invalid key entry: '" + s + "' is an invalid symbol (must be 1 character only).";
            }) : (" ".equals(s) ? DataResult.error(() -> {
                return "Invalid key entry: ' ' is a reserved symbol.";
            }) : DataResult.success(s));
        }, DataResult::success);
        private static final Codec<ShapedRecipes> CODEC = ShapedRecipes.Serializer.RawShapedRecipe.CODEC.flatXmap((shapedrecipes_serializer_rawshapedrecipe) -> {
            String[] astring = ShapedRecipes.shrink(shapedrecipes_serializer_rawshapedrecipe.pattern);
            int i = astring[0].length();
            int j = astring.length;
            NonNullList<RecipeItemStack> nonnulllist = NonNullList.withSize(i * j, RecipeItemStack.EMPTY);
            Set<String> set = Sets.newHashSet(shapedrecipes_serializer_rawshapedrecipe.key.keySet());

            for (int k = 0; k < astring.length; ++k) {
                String s = astring[k];

                for (int l = 0; l < s.length(); ++l) {
                    String s1 = s.substring(l, l + 1);
                    RecipeItemStack recipeitemstack = s1.equals(" ") ? RecipeItemStack.EMPTY : (RecipeItemStack) shapedrecipes_serializer_rawshapedrecipe.key.get(s1);

                    if (recipeitemstack == null) {
                        return DataResult.error(() -> {
                            return "Pattern references symbol '" + s1 + "' but it's not defined in the key";
                        });
                    }

                    set.remove(s1);
                    nonnulllist.set(l + i * k, recipeitemstack);
                }
            }

            if (!set.isEmpty()) {
                return DataResult.error(() -> {
                    return "Key defines symbols that aren't used in pattern: " + set;
                });
            } else {
                ShapedRecipes shapedrecipes = new ShapedRecipes(shapedrecipes_serializer_rawshapedrecipe.group, shapedrecipes_serializer_rawshapedrecipe.category, i, j, nonnulllist, shapedrecipes_serializer_rawshapedrecipe.result, shapedrecipes_serializer_rawshapedrecipe.showNotification);

                return DataResult.success(shapedrecipes);
            }
        }, (shapedrecipes) -> {
            throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
        });

        public Serializer() {}

        @Override
        public Codec<ShapedRecipes> codec() {
            return ShapedRecipes.Serializer.CODEC;
        }

        @Override
        public ShapedRecipes fromNetwork(PacketDataSerializer packetdataserializer) {
            int i = packetdataserializer.readVarInt();
            int j = packetdataserializer.readVarInt();
            String s = packetdataserializer.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) packetdataserializer.readEnum(CraftingBookCategory.class);
            NonNullList<RecipeItemStack> nonnulllist = NonNullList.withSize(i * j, RecipeItemStack.EMPTY);

            for (int k = 0; k < nonnulllist.size(); ++k) {
                nonnulllist.set(k, RecipeItemStack.fromNetwork(packetdataserializer));
            }

            ItemStack itemstack = packetdataserializer.readItem();
            boolean flag = packetdataserializer.readBoolean();

            return new ShapedRecipes(s, craftingbookcategory, i, j, nonnulllist, itemstack, flag);
        }

        public void toNetwork(PacketDataSerializer packetdataserializer, ShapedRecipes shapedrecipes) {
            packetdataserializer.writeVarInt(shapedrecipes.width);
            packetdataserializer.writeVarInt(shapedrecipes.height);
            packetdataserializer.writeUtf(shapedrecipes.group);
            packetdataserializer.writeEnum(shapedrecipes.category);
            Iterator iterator = shapedrecipes.recipeItems.iterator();

            while (iterator.hasNext()) {
                RecipeItemStack recipeitemstack = (RecipeItemStack) iterator.next();

                recipeitemstack.toNetwork(packetdataserializer);
            }

            packetdataserializer.writeItem(shapedrecipes.result);
            packetdataserializer.writeBoolean(shapedrecipes.showNotification);
        }

        private static record RawShapedRecipe(String group, CraftingBookCategory category, Map<String, RecipeItemStack> key, List<String> pattern, ItemStack result, boolean showNotification) {

            public static final Codec<ShapedRecipes.Serializer.RawShapedRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
                return instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((shapedrecipes_serializer_rawshapedrecipe) -> {
                    return shapedrecipes_serializer_rawshapedrecipe.group;
                }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapedrecipes_serializer_rawshapedrecipe) -> {
                    return shapedrecipes_serializer_rawshapedrecipe.category;
                }), ExtraCodecs.strictUnboundedMap(ShapedRecipes.Serializer.SINGLE_CHARACTER_STRING_CODEC, RecipeItemStack.CODEC_NONEMPTY).fieldOf("key").forGetter((shapedrecipes_serializer_rawshapedrecipe) -> {
                    return shapedrecipes_serializer_rawshapedrecipe.key;
                }), ShapedRecipes.Serializer.PATTERN_CODEC.fieldOf("pattern").forGetter((shapedrecipes_serializer_rawshapedrecipe) -> {
                    return shapedrecipes_serializer_rawshapedrecipe.pattern;
                }), CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter((shapedrecipes_serializer_rawshapedrecipe) -> {
                    return shapedrecipes_serializer_rawshapedrecipe.result;
                }), ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter((shapedrecipes_serializer_rawshapedrecipe) -> {
                    return shapedrecipes_serializer_rawshapedrecipe.showNotification;
                })).apply(instance, ShapedRecipes.Serializer.RawShapedRecipe::new);
            });
        }
    }
}
