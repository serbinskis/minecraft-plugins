package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SystemUtils;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IResource;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GeneratorSettingBase;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverWrapper;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureStructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorNormal;
import org.slf4j.Logger;

public class RegistryDataLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RegistrationInfo NETWORK_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
    private static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = SystemUtils.memoize((optional) -> {
        Lifecycle lifecycle = (Lifecycle) optional.map(KnownPack::isVanilla).map((obool) -> {
            return Lifecycle.stable();
        }).orElse(Lifecycle.experimental());

        return new RegistrationInfo(optional, lifecycle);
    });
    public static final List<RegistryDataLoader.c<?>> WORLDGEN_REGISTRIES = List.of(new RegistryDataLoader.c<>(Registries.DIMENSION_TYPE, DimensionManager.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.BIOME, BiomeBase.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.CHAT_TYPE, ChatMessageType.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.CONFIGURED_CARVER, WorldGenCarverWrapper.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.CONFIGURED_FEATURE, WorldGenFeatureConfigured.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.STRUCTURE, Structure.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.PROCESSOR_LIST, DefinedStructureStructureProcessorType.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.TEMPLATE_POOL, WorldGenFeatureDefinedStructurePoolTemplate.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.NOISE_SETTINGS, GeneratorSettingBase.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.NOISE, NoiseGeneratorNormal.a.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.BANNER_PATTERN, EnumBannerPatternType.DIRECT_CODEC));
    public static final List<RegistryDataLoader.c<?>> DIMENSION_REGISTRIES = List.of(new RegistryDataLoader.c<>(Registries.LEVEL_STEM, WorldDimension.CODEC));
    public static final List<RegistryDataLoader.c<?>> SYNCHRONIZED_REGISTRIES = List.of(new RegistryDataLoader.c<>(Registries.BIOME, BiomeBase.NETWORK_CODEC), new RegistryDataLoader.c<>(Registries.CHAT_TYPE, ChatMessageType.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.DIMENSION_TYPE, DimensionManager.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC), new RegistryDataLoader.c<>(Registries.BANNER_PATTERN, EnumBannerPatternType.DIRECT_CODEC));

    public RegistryDataLoader() {}

    public static IRegistryCustom.Dimension load(IResourceManager iresourcemanager, IRegistryCustom iregistrycustom, List<RegistryDataLoader.c<?>> list) {
        return load((registrydataloader_a, registryops_c) -> {
            registrydataloader_a.loadFromResources(iresourcemanager, registryops_c);
        }, iregistrycustom, list);
    }

    public static IRegistryCustom.Dimension load(Map<ResourceKey<? extends IRegistry<?>>, List<RegistrySynchronization.a>> map, ResourceProvider resourceprovider, IRegistryCustom iregistrycustom, List<RegistryDataLoader.c<?>> list) {
        return load((registrydataloader_a, registryops_c) -> {
            registrydataloader_a.loadFromNetwork(map, resourceprovider, registryops_c);
        }, iregistrycustom, list);
    }

    public static IRegistryCustom.Dimension load(RegistryDataLoader.b registrydataloader_b, IRegistryCustom iregistrycustom, List<RegistryDataLoader.c<?>> list) {
        Map<ResourceKey<?>, Exception> map = new HashMap();
        List<RegistryDataLoader.a<?>> list1 = (List) list.stream().map((registrydataloader_c) -> {
            return registrydataloader_c.create(Lifecycle.stable(), map);
        }).collect(Collectors.toUnmodifiableList());
        RegistryOps.c registryops_c = createContext(iregistrycustom, list1);

        list1.forEach((registrydataloader_a) -> {
            registrydataloader_b.apply(registrydataloader_a, registryops_c);
        });
        list1.forEach((registrydataloader_a) -> {
            IRegistry<?> iregistry = registrydataloader_a.registry();

            try {
                iregistry.freeze();
            } catch (Exception exception) {
                map.put(iregistry.key(), exception);
            }

        });
        if (!map.isEmpty()) {
            logErrors(map);
            throw new IllegalStateException("Failed to load registries due to above errors");
        } else {
            return (new IRegistryCustom.c(list1.stream().map(RegistryDataLoader.a::registry).toList())).freeze();
        }
    }

    private static RegistryOps.c createContext(IRegistryCustom iregistrycustom, List<RegistryDataLoader.a<?>> list) {
        final Map<ResourceKey<? extends IRegistry<?>>, RegistryOps.b<?>> map = new HashMap();

        iregistrycustom.registries().forEach((iregistrycustom_d) -> {
            map.put(iregistrycustom_d.key(), createInfoForContextRegistry(iregistrycustom_d.value()));
        });
        list.forEach((registrydataloader_a) -> {
            map.put(registrydataloader_a.registry.key(), createInfoForNewRegistry(registrydataloader_a.registry));
        });
        return new RegistryOps.c() {
            @Override
            public <T> Optional<RegistryOps.b<T>> lookup(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
                return Optional.ofNullable((RegistryOps.b) map.get(resourcekey));
            }
        };
    }

    private static <T> RegistryOps.b<T> createInfoForNewRegistry(IRegistryWritable<T> iregistrywritable) {
        return new RegistryOps.b<>(iregistrywritable.asLookup(), iregistrywritable.createRegistrationLookup(), iregistrywritable.registryLifecycle());
    }

    private static <T> RegistryOps.b<T> createInfoForContextRegistry(IRegistry<T> iregistry) {
        return new RegistryOps.b<>(iregistry.asLookup(), iregistry.asTagAddingLookup(), iregistry.registryLifecycle());
    }

    private static void logErrors(Map<ResourceKey<?>, Exception> map) {
        StringWriter stringwriter = new StringWriter();
        PrintWriter printwriter = new PrintWriter(stringwriter);
        Map<MinecraftKey, Map<MinecraftKey, Exception>> map1 = (Map) map.entrySet().stream().collect(Collectors.groupingBy((entry) -> {
            return ((ResourceKey) entry.getKey()).registry();
        }, Collectors.toMap((entry) -> {
            return ((ResourceKey) entry.getKey()).location();
        }, Entry::getValue)));

        map1.entrySet().stream().sorted(Entry.comparingByKey()).forEach((entry) -> {
            printwriter.printf("> Errors in registry %s:%n", entry.getKey());
            ((Map) entry.getValue()).entrySet().stream().sorted(Entry.comparingByKey()).forEach((entry1) -> {
                printwriter.printf(">> Errors in element %s:%n", entry1.getKey());
                ((Exception) entry1.getValue()).printStackTrace(printwriter);
            });
        });
        printwriter.flush();
        RegistryDataLoader.LOGGER.error("Registry loading errors:\n{}", stringwriter);
    }

    private static String registryDirPath(MinecraftKey minecraftkey) {
        return minecraftkey.getPath();
    }

    private static <E> void loadElementFromResource(IRegistryWritable<E> iregistrywritable, Decoder<E> decoder, RegistryOps<JsonElement> registryops, ResourceKey<E> resourcekey, IResource iresource, RegistrationInfo registrationinfo) throws IOException {
        BufferedReader bufferedreader = iresource.openAsReader();

        try {
            JsonElement jsonelement = JsonParser.parseReader(bufferedreader);
            DataResult<E> dataresult = decoder.parse(registryops, jsonelement);
            E e0 = dataresult.getOrThrow();

            iregistrywritable.register(resourcekey, e0, registrationinfo);
        } catch (Throwable throwable) {
            if (bufferedreader != null) {
                try {
                    bufferedreader.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (bufferedreader != null) {
            bufferedreader.close();
        }

    }

    static <E> void loadContentsFromManager(IResourceManager iresourcemanager, RegistryOps.c registryops_c, IRegistryWritable<E> iregistrywritable, Decoder<E> decoder, Map<ResourceKey<?>, Exception> map) {
        String s = registryDirPath(iregistrywritable.key().location());
        FileToIdConverter filetoidconverter = FileToIdConverter.json(s);
        RegistryOps<JsonElement> registryops = RegistryOps.create(JsonOps.INSTANCE, registryops_c);
        Iterator iterator = filetoidconverter.listMatchingResources(iresourcemanager).entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, IResource> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();
            ResourceKey<E> resourcekey = ResourceKey.create(iregistrywritable.key(), filetoidconverter.fileToId(minecraftkey));
            IResource iresource = (IResource) entry.getValue();
            RegistrationInfo registrationinfo = (RegistrationInfo) RegistryDataLoader.REGISTRATION_INFO_CACHE.apply(iresource.knownPackInfo());

            try {
                loadElementFromResource(iregistrywritable, decoder, registryops, resourcekey, iresource, registrationinfo);
            } catch (Exception exception) {
                map.put(resourcekey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", minecraftkey, iresource.sourcePackId()), exception));
            }
        }

    }

    static <E> void loadContentsFromNetwork(Map<ResourceKey<? extends IRegistry<?>>, List<RegistrySynchronization.a>> map, ResourceProvider resourceprovider, RegistryOps.c registryops_c, IRegistryWritable<E> iregistrywritable, Decoder<E> decoder, Map<ResourceKey<?>, Exception> map1) {
        List<RegistrySynchronization.a> list = (List) map.get(iregistrywritable.key());

        if (list != null) {
            RegistryOps<NBTBase> registryops = RegistryOps.create(DynamicOpsNBT.INSTANCE, registryops_c);
            RegistryOps<JsonElement> registryops1 = RegistryOps.create(JsonOps.INSTANCE, registryops_c);
            String s = registryDirPath(iregistrywritable.key().location());
            FileToIdConverter filetoidconverter = FileToIdConverter.json(s);
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                RegistrySynchronization.a registrysynchronization_a = (RegistrySynchronization.a) iterator.next();
                ResourceKey<E> resourcekey = ResourceKey.create(iregistrywritable.key(), registrysynchronization_a.id());
                Optional<NBTBase> optional = registrysynchronization_a.data();

                if (optional.isPresent()) {
                    try {
                        DataResult<E> dataresult = decoder.parse(registryops, (NBTBase) optional.get());
                        E e0 = dataresult.getOrThrow();

                        iregistrywritable.register(resourcekey, e0, RegistryDataLoader.NETWORK_REGISTRATION_INFO);
                    } catch (Exception exception) {
                        map1.put(resourcekey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), exception));
                    }
                } else {
                    MinecraftKey minecraftkey = filetoidconverter.idToFile(registrysynchronization_a.id());

                    try {
                        IResource iresource = resourceprovider.getResourceOrThrow(minecraftkey);

                        loadElementFromResource(iregistrywritable, decoder, registryops1, resourcekey, iresource, RegistryDataLoader.NETWORK_REGISTRATION_INFO);
                    } catch (Exception exception1) {
                        map1.put(resourcekey, new IllegalStateException("Failed to parse local data", exception1));
                    }
                }
            }

        }
    }

    @FunctionalInterface
    private interface b {

        void apply(RegistryDataLoader.a<?> registrydataloader_a, RegistryOps.c registryops_c);
    }

    private static record a<T>(RegistryDataLoader.c<T> data, IRegistryWritable<T> registry, Map<ResourceKey<?>, Exception> loadingErrors) {

        public void loadFromResources(IResourceManager iresourcemanager, RegistryOps.c registryops_c) {
            RegistryDataLoader.loadContentsFromManager(iresourcemanager, registryops_c, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public void loadFromNetwork(Map<ResourceKey<? extends IRegistry<?>>, List<RegistrySynchronization.a>> map, ResourceProvider resourceprovider, RegistryOps.c registryops_c) {
            RegistryDataLoader.loadContentsFromNetwork(map, resourceprovider, registryops_c, this.registry, this.data.elementCodec, this.loadingErrors);
        }
    }

    public static record c<T>(ResourceKey<? extends IRegistry<T>> key, Codec<T> elementCodec) {

        RegistryDataLoader.a<T> create(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> map) {
            IRegistryWritable<T> iregistrywritable = new RegistryMaterials<>(this.key, lifecycle);

            return new RegistryDataLoader.a<>(this, iregistrywritable, map);
        }

        public void runWithArguments(BiConsumer<ResourceKey<? extends IRegistry<T>>, Codec<T>> biconsumer) {
            biconsumer.accept(this.key, this.elementCodec);
        }
    }
}
