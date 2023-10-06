package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceDataJson;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import org.slf4j.Logger;

public class CraftingManager extends ResourceDataJson {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    public Map<Recipes<?>, Map<MinecraftKey, RecipeHolder<?>>> recipes = ImmutableMap.of();
    private Map<MinecraftKey, RecipeHolder<?>> byName = ImmutableMap.of();
    private boolean hasErrors;

    public CraftingManager() {
        super(CraftingManager.GSON, "recipes");
    }

    protected void apply(Map<MinecraftKey, JsonElement> map, IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        this.hasErrors = false;
        Map<Recipes<?>, Builder<MinecraftKey, RecipeHolder<?>>> map1 = Maps.newHashMap();
        Builder<MinecraftKey, RecipeHolder<?>> builder = ImmutableMap.builder();
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, JsonElement> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();

            try {
                RecipeHolder<?> recipeholder = fromJson(minecraftkey, ChatDeserializer.convertToJsonObject((JsonElement) entry.getValue(), "top element"));

                ((Builder) map1.computeIfAbsent(recipeholder.value().getType(), (recipes) -> {
                    return ImmutableMap.builder();
                })).put(minecraftkey, recipeholder);
                builder.put(minecraftkey, recipeholder);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                CraftingManager.LOGGER.error("Parsing error loading recipe {}", minecraftkey, jsonparseexception);
            }
        }

        this.recipes = (Map) map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (entry1) -> {
            return ((Builder) entry1.getValue()).build();
        }));
        this.byName = builder.build();
        CraftingManager.LOGGER.info("Loaded {} recipes", map1.size());
    }

    public boolean hadErrorsLoading() {
        return this.hasErrors;
    }

    public <C extends IInventory, T extends IRecipe<C>> Optional<RecipeHolder<T>> getRecipeFor(Recipes<T> recipes, C c0, World world) {
        return this.byType(recipes).values().stream().filter((recipeholder) -> {
            return recipeholder.value().matches(c0, world);
        }).findFirst();
    }

    public <C extends IInventory, T extends IRecipe<C>> Optional<Pair<MinecraftKey, RecipeHolder<T>>> getRecipeFor(Recipes<T> recipes, C c0, World world, @Nullable MinecraftKey minecraftkey) {
        Map<MinecraftKey, RecipeHolder<T>> map = this.byType(recipes);

        if (minecraftkey != null) {
            RecipeHolder<T> recipeholder = (RecipeHolder) map.get(minecraftkey);

            if (recipeholder != null && recipeholder.value().matches(c0, world)) {
                return Optional.of(Pair.of(minecraftkey, recipeholder));
            }
        }

        return map.entrySet().stream().filter((entry) -> {
            return ((RecipeHolder) entry.getValue()).value().matches(c0, world);
        }).findFirst().map((entry) -> {
            return Pair.of((MinecraftKey) entry.getKey(), (RecipeHolder) entry.getValue());
        });
    }

    public <C extends IInventory, T extends IRecipe<C>> List<RecipeHolder<T>> getAllRecipesFor(Recipes<T> recipes) {
        return List.copyOf(this.byType(recipes).values());
    }

    public <C extends IInventory, T extends IRecipe<C>> List<RecipeHolder<T>> getRecipesFor(Recipes<T> recipes, C c0, World world) {
        return (List) this.byType(recipes).values().stream().filter((recipeholder) -> {
            return recipeholder.value().matches(c0, world);
        }).sorted(Comparator.comparing((recipeholder) -> {
            return recipeholder.value().getResultItem(world.registryAccess()).getDescriptionId();
        })).collect(Collectors.toList());
    }

    private <C extends IInventory, T extends IRecipe<C>> Map<MinecraftKey, RecipeHolder<T>> byType(Recipes<T> recipes) {
        return (Map) this.recipes.getOrDefault(recipes, Collections.emptyMap());
    }

    public <C extends IInventory, T extends IRecipe<C>> NonNullList<ItemStack> getRemainingItemsFor(Recipes<T> recipes, C c0, World world) {
        Optional<RecipeHolder<T>> optional = this.getRecipeFor(recipes, c0, world);

        if (optional.isPresent()) {
            return ((RecipeHolder) optional.get()).value().getRemainingItems(c0);
        } else {
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(c0.getContainerSize(), ItemStack.EMPTY);

            for (int i = 0; i < nonnulllist.size(); ++i) {
                nonnulllist.set(i, c0.getItem(i));
            }

            return nonnulllist;
        }
    }

    public Optional<RecipeHolder<?>> byKey(MinecraftKey minecraftkey) {
        return Optional.ofNullable((RecipeHolder) this.byName.get(minecraftkey));
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        return (Collection) this.recipes.values().stream().flatMap((map) -> {
            return map.values().stream();
        }).collect(Collectors.toSet());
    }

    public Stream<MinecraftKey> getRecipeIds() {
        return this.recipes.values().stream().flatMap((map) -> {
            return map.keySet().stream();
        });
    }

    protected static RecipeHolder<?> fromJson(MinecraftKey minecraftkey, JsonObject jsonobject) {
        String s = ChatDeserializer.getAsString(jsonobject, "type");
        Codec<? extends IRecipe<?>> codec = ((RecipeSerializer) BuiltInRegistries.RECIPE_SERIALIZER.getOptional(new MinecraftKey(s)).orElseThrow(() -> {
            return new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
        })).codec();
        IRecipe<?> irecipe = (IRecipe) SystemUtils.getOrThrow(codec.parse(JsonOps.INSTANCE, jsonobject), JsonParseException::new);

        return new RecipeHolder<>(minecraftkey, irecipe);
    }

    public void replaceRecipes(Iterable<RecipeHolder<?>> iterable) {
        this.hasErrors = false;
        Map<Recipes<?>, Map<MinecraftKey, RecipeHolder<?>>> map = Maps.newHashMap();
        Builder<MinecraftKey, RecipeHolder<?>> builder = ImmutableMap.builder();

        iterable.forEach((recipeholder) -> {
            Map<MinecraftKey, RecipeHolder<?>> map1 = (Map) map.computeIfAbsent(recipeholder.value().getType(), (recipes) -> {
                return Maps.newHashMap();
            });
            MinecraftKey minecraftkey = recipeholder.id();
            RecipeHolder<?> recipeholder1 = (RecipeHolder) map1.put(minecraftkey, recipeholder);

            builder.put(minecraftkey, recipeholder);
            if (recipeholder1 != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + minecraftkey);
            }
        });
        this.recipes = ImmutableMap.copyOf(map);
        this.byName = builder.build();
    }

    public static <C extends IInventory, T extends IRecipe<C>> CraftingManager.a<C, T> createCheck(final Recipes<T> recipes) {
        return new CraftingManager.a<C, T>() {
            @Nullable
            private MinecraftKey lastRecipe;

            @Override
            public Optional<RecipeHolder<T>> getRecipeFor(C c0, World world) {
                CraftingManager craftingmanager = world.getRecipeManager();
                Optional<Pair<MinecraftKey, RecipeHolder<T>>> optional = craftingmanager.getRecipeFor(recipes, c0, world, this.lastRecipe);

                if (optional.isPresent()) {
                    Pair<MinecraftKey, RecipeHolder<T>> pair = (Pair) optional.get();

                    this.lastRecipe = (MinecraftKey) pair.getFirst();
                    return Optional.of((RecipeHolder) pair.getSecond());
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    public interface a<C extends IInventory, T extends IRecipe<C>> {

        Optional<RecipeHolder<T>> getRecipeFor(C c0, World world);
    }
}
