package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.IReloadListener;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.Reloadable;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagRegistry;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.CraftingManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityFurnace;
import org.slf4j.Logger;

public class DataPackResources {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final ReloadableServerRegistries.b fullRegistryHolder;
    private final DataPackResources.a registryLookup;
    public CommandDispatcher commands;
    private final CraftingManager recipes;
    private final TagRegistry tagManager;
    private final AdvancementDataWorld advancements;
    private final CustomFunctionManager functionLibrary;

    private DataPackResources(IRegistryCustom.Dimension iregistrycustom_dimension, FeatureFlagSet featureflagset, CommandDispatcher.ServerType commanddispatcher_servertype, int i) {
        this.fullRegistryHolder = new ReloadableServerRegistries.b(iregistrycustom_dimension);
        this.registryLookup = new DataPackResources.a(iregistrycustom_dimension);
        this.registryLookup.missingTagAccessPolicy(DataPackResources.b.CREATE_NEW);
        this.recipes = new CraftingManager(this.registryLookup);
        this.tagManager = new TagRegistry(iregistrycustom_dimension);
        this.commands = new CommandDispatcher(commanddispatcher_servertype, CommandBuildContext.simple(this.registryLookup, featureflagset));
        this.advancements = new AdvancementDataWorld(this.registryLookup);
        this.functionLibrary = new CustomFunctionManager(i, this.commands.getDispatcher());
    }

    public CustomFunctionManager getFunctionLibrary() {
        return this.functionLibrary;
    }

    public ReloadableServerRegistries.b fullRegistries() {
        return this.fullRegistryHolder;
    }

    public CraftingManager getRecipeManager() {
        return this.recipes;
    }

    public CommandDispatcher getCommands() {
        return this.commands;
    }

    public AdvancementDataWorld getAdvancements() {
        return this.advancements;
    }

    public List<IReloadListener> listeners() {
        return List.of(this.tagManager, this.recipes, this.functionLibrary, this.advancements);
    }

    public static CompletableFuture<DataPackResources> loadResources(IResourceManager iresourcemanager, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, FeatureFlagSet featureflagset, CommandDispatcher.ServerType commanddispatcher_servertype, int i, Executor executor, Executor executor1) {
        return ReloadableServerRegistries.reload(layeredregistryaccess, iresourcemanager, executor).thenCompose((layeredregistryaccess1) -> {
            DataPackResources datapackresources = new DataPackResources(layeredregistryaccess1.compositeAccess(), featureflagset, commanddispatcher_servertype, i);

            return Reloadable.create(iresourcemanager, datapackresources.listeners(), executor, executor1, DataPackResources.DATA_RELOAD_INITIAL_TASK, DataPackResources.LOGGER.isDebugEnabled()).done().whenComplete((object, throwable) -> {
                datapackresources.registryLookup.missingTagAccessPolicy(DataPackResources.b.FAIL);
            }).thenApply((object) -> {
                return datapackresources;
            });
        });
    }

    public void updateRegistryTags() {
        this.tagManager.getResult().forEach((tagregistry_a) -> {
            updateRegistryTags(this.fullRegistryHolder.get(), tagregistry_a);
        });
        TileEntityFurnace.invalidateCache();
        Blocks.rebuildCache();
    }

    private static <T> void updateRegistryTags(IRegistryCustom iregistrycustom, TagRegistry.a<T> tagregistry_a) {
        ResourceKey<? extends IRegistry<T>> resourcekey = tagregistry_a.key();
        Map<TagKey<T>, List<Holder<T>>> map = (Map) tagregistry_a.tags().entrySet().stream().collect(Collectors.toUnmodifiableMap((entry) -> {
            return TagKey.create(resourcekey, (MinecraftKey) entry.getKey());
        }, (entry) -> {
            return List.copyOf((Collection) entry.getValue());
        }));

        iregistrycustom.registryOrThrow(resourcekey).bindTags(map);
    }

    private static class a implements HolderLookup.a {

        private final IRegistryCustom registryAccess;
        DataPackResources.b missingTagAccessPolicy;

        a(IRegistryCustom iregistrycustom) {
            this.missingTagAccessPolicy = DataPackResources.b.FAIL;
            this.registryAccess = iregistrycustom;
        }

        public void missingTagAccessPolicy(DataPackResources.b datapackresources_b) {
            this.missingTagAccessPolicy = datapackresources_b;
        }

        @Override
        public Stream<ResourceKey<? extends IRegistry<?>>> listRegistries() {
            return this.registryAccess.listRegistries();
        }

        @Override
        public <T> Optional<HolderLookup.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
            return this.registryAccess.registry(resourcekey).map((iregistry) -> {
                return this.createDispatchedLookup(iregistry.asLookup(), iregistry.asTagAddingLookup());
            });
        }

        private <T> HolderLookup.b<T> createDispatchedLookup(final HolderLookup.b<T> holderlookup_b, final HolderLookup.b<T> holderlookup_b1) {
            return new HolderLookup.b.a<T>() {
                @Override
                public HolderLookup.b<T> parent() {
                    HolderLookup.b holderlookup_b2;

                    switch (a.this.missingTagAccessPolicy.ordinal()) {
                        case 0:
                            holderlookup_b2 = holderlookup_b1;
                            break;
                        case 1:
                            holderlookup_b2 = holderlookup_b;
                            break;
                        default:
                            throw new MatchException((String) null, (Throwable) null);
                    }

                    return holderlookup_b2;
                }
            };
        }
    }

    private static enum b {

        CREATE_NEW, FAIL;

        private b() {}
    }
}
