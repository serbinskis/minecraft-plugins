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

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap; // CraftBukkit

public class CraftingManager extends ResourceDataJson {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    public Map<Recipes<?>, Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>>> recipes = ImmutableMap.of(); // CraftBukkit
    private Map<MinecraftKey, IRecipe<?>> byName = ImmutableMap.of();
    private boolean hasErrors;

    public CraftingManager() {
        super(CraftingManager.GSON, "recipes");
    }

    protected void apply(Map<MinecraftKey, JsonElement> map, IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        this.hasErrors = false;
        // CraftBukkit start - SPIGOT-5667 make sure all types are populated and mutable
        Map<Recipes<?>, Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>>> map1 = Maps.newHashMap();
        for (Recipes<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            map1.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
        }
        // CraftBukkit end
        Builder<MinecraftKey, IRecipe<?>> builder = ImmutableMap.builder();
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, JsonElement> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();

            try {
                IRecipe<?> irecipe = fromJson(minecraftkey, ChatDeserializer.convertToJsonObject((JsonElement) entry.getValue(), "top element"));

                // CraftBukkit start
                (map1.computeIfAbsent(irecipe.getType(), (recipes) -> {
                    return new Object2ObjectLinkedOpenHashMap<>();
                    // CraftBukkit end
                })).put(minecraftkey, irecipe);
                builder.put(minecraftkey, irecipe);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                CraftingManager.LOGGER.error("Parsing error loading recipe {}", minecraftkey, jsonparseexception);
            }
        }

        this.recipes = (Map) map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (entry1) -> {
            return (entry1.getValue()); // CraftBukkit
        }));
        this.byName = Maps.newHashMap(builder.build()); // CraftBukkit
        CraftingManager.LOGGER.info("Loaded {} recipes", map1.size());
    }

    // CraftBukkit start
    public void addRecipe(IRecipe<?> irecipe) {
        org.spigotmc.AsyncCatcher.catchOp("Recipe Add"); // Spigot
        Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>> map = this.recipes.get(irecipe.getType()); // CraftBukkit

        if (byName.containsKey(irecipe.getId()) || map.containsKey(irecipe.getId())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + irecipe.getId());
        } else {
            map.putAndMoveToFirst(irecipe.getId(), irecipe); // CraftBukkit - SPIGOT-4638: last recipe gets priority
            byName.put(irecipe.getId(), irecipe);
        }
    }
    // CraftBukkit end

    public boolean hadErrorsLoading() {
        return this.hasErrors;
    }

    public <C extends IInventory, T extends IRecipe<C>> Optional<T> getRecipeFor(Recipes<T> recipes, C c0, World world) {
        // CraftBukkit start
        Optional<T> recipe = this.byType(recipes).values().stream().filter((irecipe) -> {
            return irecipe.matches(c0, world);
        }).findFirst();
        c0.setCurrentRecipe(recipe.orElse(null)); // CraftBukkit - Clear recipe when no recipe is found
        // CraftBukkit end
        return recipe;
    }

    public <C extends IInventory, T extends IRecipe<C>> Optional<Pair<MinecraftKey, T>> getRecipeFor(Recipes<T> recipes, C c0, World world, @Nullable MinecraftKey minecraftkey) {
        Map<MinecraftKey, T> map = this.byType(recipes);

        if (minecraftkey != null) {
            T t0 = map.get(minecraftkey); // CraftBukkit - decompile error

            if (t0 != null && t0.matches(c0, world)) {
                return Optional.of(Pair.of(minecraftkey, t0));
            }
        }

        return map.entrySet().stream().filter((entry) -> {
            return ((IRecipe) entry.getValue()).matches(c0, world);
        }).findFirst().map((entry) -> {
            return Pair.of((MinecraftKey) entry.getKey(), entry.getValue()); // CraftBukkit - decompile error
        });
    }

    public <C extends IInventory, T extends IRecipe<C>> List<T> getAllRecipesFor(Recipes<T> recipes) {
        return List.copyOf(this.byType(recipes).values());
    }

    public <C extends IInventory, T extends IRecipe<C>> List<T> getRecipesFor(Recipes<T> recipes, C c0, World world) {
        return (List) this.byType(recipes).values().stream().filter((irecipe) -> {
            return irecipe.matches(c0, world);
        }).sorted(Comparator.comparing((irecipe) -> {
            return irecipe.getResultItem(world.registryAccess()).getDescriptionId();
        })).collect(Collectors.toList());
    }

    private <C extends IInventory, T extends IRecipe<C>> Map<MinecraftKey, T> byType(Recipes<T> recipes) {
        return (Map) this.recipes.getOrDefault(recipes, new Object2ObjectLinkedOpenHashMap<>()); // CraftBukkit
    }

    public <C extends IInventory, T extends IRecipe<C>> NonNullList<ItemStack> getRemainingItemsFor(Recipes<T> recipes, C c0, World world) {
        Optional<T> optional = this.getRecipeFor(recipes, c0, world);

        if (optional.isPresent()) {
            return ((IRecipe) optional.get()).getRemainingItems(c0);
        } else {
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(c0.getContainerSize(), ItemStack.EMPTY);

            for (int i = 0; i < nonnulllist.size(); ++i) {
                nonnulllist.set(i, c0.getItem(i));
            }

            return nonnulllist;
        }
    }

    public Optional<? extends IRecipe<?>> byKey(MinecraftKey minecraftkey) {
        return Optional.ofNullable(this.byName.get(minecraftkey)); // CraftBukkit - decompile error
    }

    public Collection<IRecipe<?>> getRecipes() {
        return (Collection) this.recipes.values().stream().flatMap((map) -> {
            return map.values().stream();
        }).collect(Collectors.toSet());
    }

    public Stream<MinecraftKey> getRecipeIds() {
        return this.recipes.values().stream().flatMap((map) -> {
            return map.keySet().stream();
        });
    }

    public static IRecipe<?> fromJson(MinecraftKey minecraftkey, JsonObject jsonobject) {
        String s = ChatDeserializer.getAsString(jsonobject, "type");

        return ((RecipeSerializer) BuiltInRegistries.RECIPE_SERIALIZER.getOptional(new MinecraftKey(s)).orElseThrow(() -> {
            return new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
        })).fromJson(minecraftkey, jsonobject);
    }

    public void replaceRecipes(Iterable<IRecipe<?>> iterable) {
        this.hasErrors = false;
        Map<Recipes<?>, Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>>> map = Maps.newHashMap(); // CraftBukkit
        Builder<MinecraftKey, IRecipe<?>> builder = ImmutableMap.builder();

        iterable.forEach((irecipe) -> {
            Map<MinecraftKey, IRecipe<?>> map1 = (Map) map.computeIfAbsent(irecipe.getType(), (recipes) -> {
                return new Object2ObjectLinkedOpenHashMap<>(); // CraftBukkit
            });
            MinecraftKey minecraftkey = irecipe.getId();
            IRecipe<?> irecipe1 = (IRecipe) map1.put(minecraftkey, irecipe);

            builder.put(minecraftkey, irecipe);
            if (irecipe1 != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + minecraftkey);
            }
        });
        this.recipes = ImmutableMap.copyOf(map);
        this.byName = Maps.newHashMap(builder.build()); // CraftBukkit
    }

    // CraftBukkit start
    public boolean removeRecipe(MinecraftKey mcKey) {
        for (Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>> recipes : recipes.values()) {
            recipes.remove(mcKey);
        }

        return byName.remove(mcKey) != null;
    }

    public void clearRecipes() {
        this.recipes = Maps.newHashMap();

        for (Recipes<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            this.recipes.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
        }

        this.byName = Maps.newHashMap();
    }
    // CraftBukkit end

    public static <C extends IInventory, T extends IRecipe<C>> CraftingManager.a<C, T> createCheck(final Recipes<T> recipes) {
        return new CraftingManager.a<C, T>() {
            @Nullable
            private MinecraftKey lastRecipe;

            @Override
            public Optional<T> getRecipeFor(C c0, World world) {
                CraftingManager craftingmanager = world.getRecipeManager();
                Optional<Pair<MinecraftKey, T>> optional = craftingmanager.getRecipeFor(recipes, c0, world, this.lastRecipe);

                if (optional.isPresent()) {
                    Pair<MinecraftKey, T> pair = (Pair) optional.get();

                    this.lastRecipe = (MinecraftKey) pair.getFirst();
                    return Optional.of(pair.getSecond()); // CraftBukkit - decompile error
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    public interface a<C extends IInventory, T extends IRecipe<C>> {

        Optional<T> getRecipeFor(C c0, World world);
    }
}
