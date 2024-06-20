package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public final class RecipeItemStack implements Predicate<ItemStack> {

    public static final RecipeItemStack EMPTY = new RecipeItemStack(Stream.empty());
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeItemStack> CONTENTS_STREAM_CODEC = ItemStack.LIST_STREAM_CODEC.map((list) -> {
        return fromValues(list.stream().map(RecipeItemStack.StackProvider::new));
    }, (recipeitemstack) -> {
        return Arrays.asList(recipeitemstack.getItems());
    });
    private final RecipeItemStack.Provider[] values;
    @Nullable
    public ItemStack[] itemStacks;
    @Nullable
    private IntList stackingIds;
    public boolean exact; // CraftBukkit
    public static final Codec<RecipeItemStack> CODEC = codec(true);
    public static final Codec<RecipeItemStack> CODEC_NONEMPTY = codec(false);

    public RecipeItemStack(Stream<? extends RecipeItemStack.Provider> stream) {
        this.values = (RecipeItemStack.Provider[]) stream.toArray((i) -> {
            return new RecipeItemStack.Provider[i];
        });
    }

    private RecipeItemStack(RecipeItemStack.Provider[] arecipeitemstack_provider) {
        this.values = arecipeitemstack_provider;
    }

    public ItemStack[] getItems() {
        if (this.itemStacks == null) {
            this.itemStacks = (ItemStack[]) Arrays.stream(this.values).flatMap((recipeitemstack_provider) -> {
                return recipeitemstack_provider.getItems().stream();
            }).distinct().toArray((i) -> {
                return new ItemStack[i];
            });
        }

        return this.itemStacks;
    }

    public boolean test(@Nullable ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        } else if (this.isEmpty()) {
            return itemstack.isEmpty();
        } else {
            ItemStack[] aitemstack = this.getItems();
            int i = aitemstack.length;

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack1 = aitemstack[j];

                // CraftBukkit start
                if (exact) {
                    if (itemstack1.getItem() == itemstack.getItem() && ItemStack.isSameItemSameComponents(itemstack, itemstack1)) {
                        return true;
                    }

                    continue;
                }
                // CraftBukkit end
                if (itemstack1.is(itemstack.getItem())) {
                    return true;
                }
            }

            return false;
        }
    }

    public IntList getStackingIds() {
        if (this.stackingIds == null) {
            ItemStack[] aitemstack = this.getItems();

            this.stackingIds = new IntArrayList(aitemstack.length);
            ItemStack[] aitemstack1 = aitemstack;
            int i = aitemstack.length;

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = aitemstack1[j];

                this.stackingIds.add(AutoRecipeStackManager.getStackingIndex(itemstack));
            }

            this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return this.stackingIds;
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public boolean equals(Object object) {
        if (object instanceof RecipeItemStack recipeitemstack) {
            return Arrays.equals(this.values, recipeitemstack.values);
        } else {
            return false;
        }
    }

    private static RecipeItemStack fromValues(Stream<? extends RecipeItemStack.Provider> stream) {
        RecipeItemStack recipeitemstack = new RecipeItemStack(stream);

        return recipeitemstack.isEmpty() ? RecipeItemStack.EMPTY : recipeitemstack;
    }

    public static RecipeItemStack of() {
        return RecipeItemStack.EMPTY;
    }

    public static RecipeItemStack of(IMaterial... aimaterial) {
        return of(Arrays.stream(aimaterial).map(ItemStack::new));
    }

    public static RecipeItemStack of(ItemStack... aitemstack) {
        return of(Arrays.stream(aitemstack));
    }

    public static RecipeItemStack of(Stream<ItemStack> stream) {
        return fromValues(stream.filter((itemstack) -> {
            return !itemstack.isEmpty();
        }).map(RecipeItemStack.StackProvider::new));
    }

    public static RecipeItemStack of(TagKey<Item> tagkey) {
        return fromValues(Stream.of(new RecipeItemStack.b(tagkey)));
    }

    private static Codec<RecipeItemStack> codec(boolean flag) {
        Codec<RecipeItemStack.Provider[]> codec = Codec.list(RecipeItemStack.Provider.CODEC).comapFlatMap((list) -> {
            return !flag && list.size() < 1 ? DataResult.error(() -> {
                return "Item array cannot be empty, at least one item must be defined";
            }) : DataResult.success((RecipeItemStack.Provider[]) list.toArray(new RecipeItemStack.Provider[0]));
        }, List::of);

        return Codec.either(codec, RecipeItemStack.Provider.CODEC).flatComapMap((either) -> {
            return (RecipeItemStack) either.map(RecipeItemStack::new, (recipeitemstack_provider) -> {
                return new RecipeItemStack(new RecipeItemStack.Provider[]{recipeitemstack_provider});
            });
        }, (recipeitemstack) -> {
            return recipeitemstack.values.length == 1 ? DataResult.success(Either.right(recipeitemstack.values[0])) : (recipeitemstack.values.length == 0 && !flag ? DataResult.error(() -> {
                return "Item array cannot be empty, at least one item must be defined";
            }) : DataResult.success(Either.left(recipeitemstack.values)));
        });
    }

    public interface Provider {

        Codec<RecipeItemStack.Provider> CODEC = Codec.xor(RecipeItemStack.StackProvider.CODEC, RecipeItemStack.b.CODEC).xmap((either) -> {
            return (RecipeItemStack.Provider) either.map((recipeitemstack_stackprovider) -> {
                return recipeitemstack_stackprovider;
            }, (recipeitemstack_b) -> {
                return recipeitemstack_b;
            });
        }, (recipeitemstack_provider) -> {
            if (recipeitemstack_provider instanceof RecipeItemStack.b recipeitemstack_b) {
                return Either.right(recipeitemstack_b);
            } else if (recipeitemstack_provider instanceof RecipeItemStack.StackProvider recipeitemstack_stackprovider) {
                return Either.left(recipeitemstack_stackprovider);
            } else {
                throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
            }
        });

        Collection<ItemStack> getItems();
    }

    private static record b(TagKey<Item> tag) implements RecipeItemStack.Provider {

        static final Codec<RecipeItemStack.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter((recipeitemstack_b) -> {
                return recipeitemstack_b.tag;
            })).apply(instance, RecipeItemStack.b::new);
        });

        public boolean equals(Object object) {
            if (object instanceof RecipeItemStack.b recipeitemstack_b) {
                return recipeitemstack_b.tag.location().equals(this.tag.location());
            } else {
                return false;
            }
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> list = Lists.newArrayList();
            Iterator iterator = BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).iterator();

            while (iterator.hasNext()) {
                Holder<Item> holder = (Holder) iterator.next();

                list.add(new ItemStack(holder));
            }

            return list;
        }
    }

    public static record StackProvider(ItemStack item) implements RecipeItemStack.Provider {

        static final Codec<RecipeItemStack.StackProvider> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ItemStack.SIMPLE_ITEM_CODEC.fieldOf("item").forGetter((recipeitemstack_stackprovider) -> {
                return recipeitemstack_stackprovider.item;
            })).apply(instance, RecipeItemStack.StackProvider::new);
        });

        public boolean equals(Object object) {
            if (!(object instanceof RecipeItemStack.StackProvider recipeitemstack_stackprovider)) {
                return false;
            } else {
                return recipeitemstack_stackprovider.item.getItem().equals(this.item.getItem()) && recipeitemstack_stackprovider.item.getCount() == this.item.getCount();
            }
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.item);
        }
    }
}
