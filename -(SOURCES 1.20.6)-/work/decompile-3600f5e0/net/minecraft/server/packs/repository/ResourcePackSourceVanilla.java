package net.minecraft.server.packs.repository;

import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.IResourcePack;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.ResourcePackVanilla;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.ResourcePackInfo;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.SavedFile;
import net.minecraft.world.level.validation.DirectoryValidator;

public class ResourcePackSourceVanilla extends BuiltInPackSource {

    private static final ResourcePackInfo VERSION_METADATA_SECTION = new ResourcePackInfo(IChatBaseComponent.translatable("dataPack.vanilla.description"), SharedConstants.getCurrentVersion().getPackVersion(EnumResourcePackType.SERVER_DATA), Optional.empty());
    private static final FeatureFlagsMetadataSection FEATURE_FLAGS_METADATA_SECTION = new FeatureFlagsMetadataSection(FeatureFlags.DEFAULT_FLAGS);
    private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(ResourcePackInfo.TYPE, ResourcePackSourceVanilla.VERSION_METADATA_SECTION, FeatureFlagsMetadataSection.TYPE, ResourcePackSourceVanilla.FEATURE_FLAGS_METADATA_SECTION);
    private static final PackLocationInfo VANILLA_PACK_INFO = new PackLocationInfo("vanilla", IChatBaseComponent.translatable("dataPack.vanilla.name"), PackSource.BUILT_IN, Optional.of(ResourcePackSourceVanilla.CORE_PACK_INFO));
    private static final PackSelectionConfig VANILLA_SELECTION_CONFIG = new PackSelectionConfig(false, ResourcePackLoader.Position.BOTTOM, false);
    private static final PackSelectionConfig FEATURE_SELECTION_CONFIG = new PackSelectionConfig(false, ResourcePackLoader.Position.TOP, false);
    private static final MinecraftKey PACKS_DIR = new MinecraftKey("minecraft", "datapacks");

    public ResourcePackSourceVanilla(DirectoryValidator directoryvalidator) {
        super(EnumResourcePackType.SERVER_DATA, createVanillaPackSource(), ResourcePackSourceVanilla.PACKS_DIR, directoryvalidator);
    }

    private static PackLocationInfo createBuiltInPackLocation(String s, IChatBaseComponent ichatbasecomponent) {
        return new PackLocationInfo(s, ichatbasecomponent, PackSource.FEATURE, Optional.of(KnownPack.vanilla(s)));
    }

    @VisibleForTesting
    public static ResourcePackVanilla createVanillaPackSource() {
        return (new VanillaPackResourcesBuilder()).setMetadata(ResourcePackSourceVanilla.BUILT_IN_METADATA).exposeNamespace("minecraft").applyDevelopmentConfig().pushJarResources().build(ResourcePackSourceVanilla.VANILLA_PACK_INFO);
    }

    @Override
    protected IChatBaseComponent getPackTitle(String s) {
        return IChatBaseComponent.literal(s);
    }

    @Nullable
    @Override
    protected ResourcePackLoader createVanillaPack(IResourcePack iresourcepack) {
        return ResourcePackLoader.readMetaAndCreate(ResourcePackSourceVanilla.VANILLA_PACK_INFO, fixedResources(iresourcepack), EnumResourcePackType.SERVER_DATA, ResourcePackSourceVanilla.VANILLA_SELECTION_CONFIG);
    }

    @Nullable
    @Override
    protected ResourcePackLoader createBuiltinPack(String s, ResourcePackLoader.c resourcepackloader_c, IChatBaseComponent ichatbasecomponent) {
        return ResourcePackLoader.readMetaAndCreate(createBuiltInPackLocation(s, ichatbasecomponent), resourcepackloader_c, EnumResourcePackType.SERVER_DATA, ResourcePackSourceVanilla.FEATURE_SELECTION_CONFIG);
    }

    public static ResourcePackRepository createPackRepository(Path path, DirectoryValidator directoryvalidator) {
        return new ResourcePackRepository(new ResourcePackSource[]{new ResourcePackSourceVanilla(directoryvalidator), new ResourcePackSourceFolder(path, EnumResourcePackType.SERVER_DATA, PackSource.WORLD, directoryvalidator)});
    }

    public static ResourcePackRepository createVanillaTrustedRepository() {
        return new ResourcePackRepository(new ResourcePackSource[]{new ResourcePackSourceVanilla(new DirectoryValidator((path) -> {
                    return true;
                }))});
    }

    public static ResourcePackRepository createPackRepository(Convertable.ConversionSession convertable_conversionsession) {
        return createPackRepository(convertable_conversionsession.getLevelPath(SavedFile.DATAPACK_DIR), convertable_conversionsession.parent().getWorldDirValidator());
    }
}
