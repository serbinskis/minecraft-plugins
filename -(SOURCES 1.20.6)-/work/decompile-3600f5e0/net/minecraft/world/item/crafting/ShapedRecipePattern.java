package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.InventoryCrafting;

public record ShapedRecipePattern(int width, int height, NonNullList<RecipeItemStack> ingredients, Optional<ShapedRecipePattern.a> data) {

    private static final int MAX_SIZE = 3;
    public static final MapCodec<ShapedRecipePattern> MAP_CODEC = ShapedRecipePattern.a.MAP_CODEC.flatXmap(ShapedRecipePattern::unpack, (shapedrecipepattern) -> {
        return (DataResult) shapedrecipepattern.data().map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
                return "Cannot encode unpacked recipe";
            });
        });
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipePattern> STREAM_CODEC = StreamCodec.ofMember(ShapedRecipePattern::toNetwork, ShapedRecipePattern::fromNetwork);

    public static ShapedRecipePattern of(Map<Character, RecipeItemStack> map, String... astring) {
        return of(map, List.of(astring));
    }

    public static ShapedRecipePattern of(Map<Character, RecipeItemStack> map, List<String> list) {
        ShapedRecipePattern.a shapedrecipepattern_a = new ShapedRecipePattern.a(map, list);

        return (ShapedRecipePattern) unpack(shapedrecipepattern_a).getOrThrow();
    }

    private static DataResult<ShapedRecipePattern> unpack(ShapedRecipePattern.a shapedrecipepattern_a) {
        String[] astring = shrink(shapedrecipepattern_a.pattern);
        int i = astring[0].length();
        int j = astring.length;
        NonNullList<RecipeItemStack> nonnulllist = NonNullList.withSize(i * j, RecipeItemStack.EMPTY);
        CharArraySet chararrayset = new CharArraySet(shapedrecipepattern_a.key.keySet());

        for (int k = 0; k < astring.length; ++k) {
            String s = astring[k];

            for (int l = 0; l < s.length(); ++l) {
                char c0 = s.charAt(l);
                RecipeItemStack recipeitemstack = c0 == ' ' ? RecipeItemStack.EMPTY : (RecipeItemStack) shapedrecipepattern_a.key.get(c0);

                if (recipeitemstack == null) {
                    return DataResult.error(() -> {
                        return "Pattern references symbol '" + c0 + "' but it's not defined in the key";
                    });
                }

                chararrayset.remove(c0);
                nonnulllist.set(l + i * k, recipeitemstack);
            }
        }

        if (!chararrayset.isEmpty()) {
            return DataResult.error(() -> {
                return "Key defines symbols that aren't used in pattern: " + String.valueOf(chararrayset);
            });
        } else {
            return DataResult.success(new ShapedRecipePattern(i, j, nonnulllist, Optional.of(shapedrecipepattern_a)));
        }
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

    public boolean matches(InventoryCrafting inventorycrafting) {
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
                        recipeitemstack = (RecipeItemStack) this.ingredients.get(this.width - i1 - 1 + j1 * this.width);
                    } else {
                        recipeitemstack = (RecipeItemStack) this.ingredients.get(i1 + j1 * this.width);
                    }
                }

                if (!recipeitemstack.test(inventorycrafting.getItem(k + l * inventorycrafting.getWidth()))) {
                    return false;
                }
            }
        }

        return true;
    }

    private void toNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeVarInt(this.width);
        registryfriendlybytebuf.writeVarInt(this.height);
        Iterator iterator = this.ingredients.iterator();

        while (iterator.hasNext()) {
            RecipeItemStack recipeitemstack = (RecipeItemStack) iterator.next();

            RecipeItemStack.CONTENTS_STREAM_CODEC.encode(registryfriendlybytebuf, recipeitemstack);
        }

    }

    private static ShapedRecipePattern fromNetwork(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        int i = registryfriendlybytebuf.readVarInt();
        int j = registryfriendlybytebuf.readVarInt();
        NonNullList<RecipeItemStack> nonnulllist = NonNullList.withSize(i * j, RecipeItemStack.EMPTY);

        nonnulllist.replaceAll((recipeitemstack) -> {
            return (RecipeItemStack) RecipeItemStack.CONTENTS_STREAM_CODEC.decode(registryfriendlybytebuf);
        });
        return new ShapedRecipePattern(i, j, nonnulllist, Optional.empty());
    }

    public static record a(Map<Character, RecipeItemStack> key, List<String> pattern) {

        private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap((list) -> {
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
        }, Function.identity());
        private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap((s) -> {
            return s.length() != 1 ? DataResult.error(() -> {
                return "Invalid key entry: '" + s + "' is an invalid symbol (must be 1 character only).";
            }) : (" ".equals(s) ? DataResult.error(() -> {
                return "Invalid key entry: ' ' is a reserved symbol.";
            }) : DataResult.success(s.charAt(0)));
        }, String::valueOf);
        public static final MapCodec<ShapedRecipePattern.a> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.strictUnboundedMap(ShapedRecipePattern.a.SYMBOL_CODEC, RecipeItemStack.CODEC_NONEMPTY).fieldOf("key").forGetter((shapedrecipepattern_a) -> {
                return shapedrecipepattern_a.key;
            }), ShapedRecipePattern.a.PATTERN_CODEC.fieldOf("pattern").forGetter((shapedrecipepattern_a) -> {
                return shapedrecipepattern_a.pattern;
            })).apply(instance, ShapedRecipePattern.a::new);
        });
    }
}
