package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceDataJson;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import org.slf4j.Logger;

public class ReloadableServerRegistries {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = (new GsonBuilder()).create();
    private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

    public ReloadableServerRegistries() {}

    public static CompletableFuture<LayeredRegistryAccess<RegistryLayer>> reload(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, IResourceManager iresourcemanager, Executor executor) {
        IRegistryCustom.Dimension iregistrycustom_dimension = layeredregistryaccess.getAccessForLoading(RegistryLayer.RELOADABLE);
        RegistryOps<JsonElement> registryops = (new ReloadableServerRegistries.a(iregistrycustom_dimension)).createSerializationContext(JsonOps.INSTANCE);
        List<CompletableFuture<IRegistryWritable<?>>> list = LootDataType.values().map((lootdatatype) -> {
            return scheduleElementParse(lootdatatype, registryops, iresourcemanager, executor);
        }).toList();
        CompletableFuture<List<IRegistryWritable<?>>> completablefuture = SystemUtils.sequence(list);

        return completablefuture.thenApplyAsync((list1) -> {
            return apply(layeredregistryaccess, list1);
        }, executor);
    }

    private static <T> CompletableFuture<IRegistryWritable<?>> scheduleElementParse(LootDataType<T> lootdatatype, RegistryOps<JsonElement> registryops, IResourceManager iresourcemanager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            IRegistryWritable<T> iregistrywritable = new RegistryMaterials<>(lootdatatype.registryKey(), Lifecycle.experimental());
            Map<MinecraftKey, JsonElement> map = new HashMap();
            String s = Registries.elementsDirPath(lootdatatype.registryKey());

            ResourceDataJson.scanDirectory(iresourcemanager, s, ReloadableServerRegistries.GSON, map);
            map.forEach((minecraftkey, jsonelement) -> {
                lootdatatype.deserialize(minecraftkey, registryops, jsonelement).ifPresent((object) -> {
                    iregistrywritable.register(ResourceKey.create(lootdatatype.registryKey(), minecraftkey), object, ReloadableServerRegistries.DEFAULT_REGISTRATION_INFO);
                });
            });
            return iregistrywritable;
        }, executor);
    }

    private static LayeredRegistryAccess<RegistryLayer> apply(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, List<IRegistryWritable<?>> list) {
        LayeredRegistryAccess<RegistryLayer> layeredregistryaccess1 = createUpdatedRegistries(layeredregistryaccess, list);
        ProblemReporter.a problemreporter_a = new ProblemReporter.a();
        IRegistryCustom.Dimension iregistrycustom_dimension = layeredregistryaccess1.compositeAccess();
        LootCollector lootcollector = new LootCollector(problemreporter_a, LootContextParameterSets.ALL_PARAMS, iregistrycustom_dimension.asGetterLookup());

        LootDataType.values().forEach((lootdatatype) -> {
            validateRegistry(lootcollector, lootdatatype, iregistrycustom_dimension);
        });
        problemreporter_a.get().forEach((s, s1) -> {
            ReloadableServerRegistries.LOGGER.warn("Found loot table element validation problem in {}: {}", s, s1);
        });
        return layeredregistryaccess1;
    }

    private static LayeredRegistryAccess<RegistryLayer> createUpdatedRegistries(LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, List<IRegistryWritable<?>> list) {
        IRegistryCustom.c iregistrycustom_c = new IRegistryCustom.c(list);

        ((IRegistryWritable) iregistrycustom_c.registryOrThrow(Registries.LOOT_TABLE)).register(LootTables.EMPTY, (Object) LootTable.EMPTY, ReloadableServerRegistries.DEFAULT_REGISTRATION_INFO);
        return layeredregistryaccess.replaceFrom(RegistryLayer.RELOADABLE, iregistrycustom_c.freeze());
    }

    private static <T> void validateRegistry(LootCollector lootcollector, LootDataType<T> lootdatatype, IRegistryCustom iregistrycustom) {
        IRegistry<T> iregistry = iregistrycustom.registryOrThrow(lootdatatype.registryKey());

        iregistry.holders().forEach((holder_c) -> {
            lootdatatype.runValidation(lootcollector, holder_c.key(), holder_c.value());
        });
    }

    private static class a implements HolderLookup.a {

        private final IRegistryCustom registryAccess;

        a(IRegistryCustom iregistrycustom) {
            this.registryAccess = iregistrycustom;
        }

        @Override
        public Stream<ResourceKey<? extends IRegistry<?>>> listRegistries() {
            return this.registryAccess.listRegistries();
        }

        @Override
        public <T> Optional<HolderLookup.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
            return this.registryAccess.registry(resourcekey).map(IRegistry::asTagAddingLookup);
        }
    }

    public static class b {

        private final IRegistryCustom.Dimension registries;

        public b(IRegistryCustom.Dimension iregistrycustom_dimension) {
            this.registries = iregistrycustom_dimension;
        }

        public IRegistryCustom.Dimension get() {
            return this.registries;
        }

        public HolderGetter.a lookup() {
            return this.registries.asGetterLookup();
        }

        public Collection<MinecraftKey> getKeys(ResourceKey<? extends IRegistry<?>> resourcekey) {
            return this.registries.registry(resourcekey).stream().flatMap((iregistry) -> {
                return iregistry.holders().map((holder_c) -> {
                    return holder_c.key().location();
                });
            }).toList();
        }

        public LootTable getLootTable(ResourceKey<LootTable> resourcekey) {
            return (LootTable) this.registries.lookup(Registries.LOOT_TABLE).flatMap((holderlookup_b) -> {
                return holderlookup_b.get(resourcekey);
            }).map(Holder::value).orElse(LootTable.EMPTY);
        }
    }
}
