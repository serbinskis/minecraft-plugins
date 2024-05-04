package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceDataJson;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import org.slf4j.Logger;

public class CraftingManager extends ResourceDataJson {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HolderLookup.a registries;
    public Multimap<Recipes<?>, RecipeHolder<?>> byType = ImmutableMultimap.of();
    private Map<MinecraftKey, RecipeHolder<?>> byName = ImmutableMap.of();
    private boolean hasErrors;

    public CraftingManager(HolderLookup.a holderlookup_a) {
        super(CraftingManager.GSON, "recipes");
        this.registries = holderlookup_a;
    }

    protected void apply(Map<MinecraftKey, JsonElement> map, IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        this.hasErrors = false;
        Builder<Recipes<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
        com.google.common.collect.ImmutableMap.Builder<MinecraftKey, RecipeHolder<?>> com_google_common_collect_immutablemap_builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.registries.createSerializationContext(JsonOps.INSTANCE);
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, JsonElement> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();

            try {
                IRecipe<?> irecipe = (IRecipe) IRecipe.CODEC.parse(registryops, (JsonElement) entry.getValue()).getOrThrow(JsonParseException::new);
                RecipeHolder<?> recipeholder = new RecipeHolder<>(minecraftkey, irecipe);

                builder.put(irecipe.getType(), recipeholder);
                com_google_common_collect_immutablemap_builder.put(minecraftkey, recipeholder);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                CraftingManager.LOGGER.error("Parsing error loading recipe {}", minecraftkey, jsonparseexception);
            }
        }

        this.byType = builder.build();
        this.byName = com_google_common_collect_immutablemap_builder.build();
        CraftingManager.LOGGER.info("Loaded {} recipes", this.byType.size());
    }

    public boolean hadErrorsLoading() {
        return this.hasErrors;
    }

    public <C extends IInventory, T extends IRecipe<C>> Optional<RecipeHolder<T>> getRecipeFor(Recipes<T> recipes, C c0, World world) {
        return this.byType(recipes).stream().filter((recipeholder) -> {
            return recipeholder.value().matches(c0, world);
        }).findFirst();
    }

    public <C extends IInventory, T extends IRecipe<C>> Optional<RecipeHolder<T>> getRecipeFor(Recipes<T> recipes, C c0, World world, @Nullable MinecraftKey minecraftkey) {
        if (minecraftkey != null) {
            RecipeHolder<T> recipeholder = this.byKeyTyped(recipes, minecraftkey);

            if (recipeholder != null && recipeholder.value().matches(c0, world)) {
                return Optional.of(recipeholder);
            }
        }

        return this.byType(recipes).stream().filter((recipeholder1) -> {
            return recipeholder1.value().matches(c0, world);
        }).findFirst();
    }

    public <C extends IInventory, T extends IRecipe<C>> List<RecipeHolder<T>> getAllRecipesFor(Recipes<T> recipes) {
        return List.copyOf(this.byType(recipes));
    }

    public <C extends IInventory, T extends IRecipe<C>> List<RecipeHolder<T>> getRecipesFor(Recipes<T> recipes, C c0, World world) {
        return (List) this.byType(recipes).stream().filter((recipeholder) -> {
            return recipeholder.value().matches(c0, world);
        }).sorted(Comparator.comparing((recipeholder) -> {
            return recipeholder.value().getResultItem(world.registryAccess()).getDescriptionId();
        })).collect(Collectors.toList());
    }

    private <C extends IInventory, T extends IRecipe<C>> Collection<RecipeHolder<T>> byType(Recipes<T> recipes) {
        return this.byType.get(recipes);
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

    @Nullable
    private <T extends IRecipe<?>> RecipeHolder<T> byKeyTyped(Recipes<T> recipes, MinecraftKey minecraftkey) {
        RecipeHolder<?> recipeholder = (RecipeHolder) this.byName.get(minecraftkey);

        return recipeholder != null && recipeholder.value().getType().equals(recipes) ? recipeholder : null;
    }

    public Collection<RecipeHolder<?>> getOrderedRecipes() {
        return this.byType.values();
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        return this.byName.values();
    }

    public Stream<MinecraftKey> getRecipeIds() {
        return this.byName.keySet().stream();
    }

    @VisibleForTesting
    protected static RecipeHolder<?> fromJson(MinecraftKey minecraftkey, JsonObject jsonobject, HolderLookup.a holderlookup_a) {
        IRecipe<?> irecipe = (IRecipe) IRecipe.CODEC.parse(holderlookup_a.createSerializationContext(JsonOps.INSTANCE), jsonobject).getOrThrow(JsonParseException::new);

        return new RecipeHolder<>(minecraftkey, irecipe);
    }

    public void replaceRecipes(Iterable<RecipeHolder<?>> iterable) {
        this.hasErrors = false;
        Builder<Recipes<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
        com.google.common.collect.ImmutableMap.Builder<MinecraftKey, RecipeHolder<?>> com_google_common_collect_immutablemap_builder = ImmutableMap.builder();
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            RecipeHolder<?> recipeholder = (RecipeHolder) iterator.next();
            Recipes<?> recipes = recipeholder.value().getType();

            builder.put(recipes, recipeholder);
            com_google_common_collect_immutablemap_builder.put(recipeholder.id(), recipeholder);
        }

        this.byType = builder.build();
        this.byName = com_google_common_collect_immutablemap_builder.build();
    }

    public static <C extends IInventory, T extends IRecipe<C>> CraftingManager.a<C, T> createCheck(final Recipes<T> recipes) {
        return new CraftingManager.a<C, T>() {
            @Nullable
            private MinecraftKey lastRecipe;

            @Override
            public Optional<RecipeHolder<T>> getRecipeFor(C c0, World world) {
                CraftingManager craftingmanager = world.getRecipeManager();
                Optional<RecipeHolder<T>> optional = craftingmanager.getRecipeFor(recipes, c0, world, this.lastRecipe);

                if (optional.isPresent()) {
                    RecipeHolder<T> recipeholder = (RecipeHolder) optional.get();

                    this.lastRecipe = recipeholder.id();
                    return Optional.of(recipeholder);
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
