package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.IResourcePack;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.metadata.pack.ResourcePackInfo;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class ResourcePackLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    public final ResourcePackLoader.c resources;
    private final ResourcePackLoader.a metadata;
    private final PackSelectionConfig selectionConfig;

    @Nullable
    public static ResourcePackLoader readMetaAndCreate(PackLocationInfo packlocationinfo, ResourcePackLoader.c resourcepackloader_c, EnumResourcePackType enumresourcepacktype, PackSelectionConfig packselectionconfig) {
        int i = SharedConstants.getCurrentVersion().getPackVersion(enumresourcepacktype);
        ResourcePackLoader.a resourcepackloader_a = readPackMetadata(packlocationinfo, resourcepackloader_c, i);

        return resourcepackloader_a != null ? new ResourcePackLoader(packlocationinfo, resourcepackloader_c, resourcepackloader_a, packselectionconfig) : null;
    }

    public ResourcePackLoader(PackLocationInfo packlocationinfo, ResourcePackLoader.c resourcepackloader_c, ResourcePackLoader.a resourcepackloader_a, PackSelectionConfig packselectionconfig) {
        this.location = packlocationinfo;
        this.resources = resourcepackloader_c;
        this.metadata = resourcepackloader_a;
        this.selectionConfig = packselectionconfig;
    }

    @Nullable
    public static ResourcePackLoader.a readPackMetadata(PackLocationInfo packlocationinfo, ResourcePackLoader.c resourcepackloader_c, int i) {
        try {
            IResourcePack iresourcepack = resourcepackloader_c.openPrimary(packlocationinfo);

            ResourcePackLoader.a resourcepackloader_a;
            label58:
            {
                FeatureFlagsMetadataSection featureflagsmetadatasection;

                try {
                    ResourcePackInfo resourcepackinfo = (ResourcePackInfo) iresourcepack.getMetadataSection(ResourcePackInfo.TYPE);

                    if (resourcepackinfo != null) {
                        featureflagsmetadatasection = (FeatureFlagsMetadataSection) iresourcepack.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
                        FeatureFlagSet featureflagset = featureflagsmetadatasection != null ? featureflagsmetadatasection.flags() : FeatureFlagSet.of();
                        InclusiveRange<Integer> inclusiverange = getDeclaredPackVersions(packlocationinfo.id(), resourcepackinfo);
                        EnumResourcePackVersion enumresourcepackversion = EnumResourcePackVersion.forVersion(inclusiverange, i);
                        OverlayMetadataSection overlaymetadatasection = (OverlayMetadataSection) iresourcepack.getMetadataSection(OverlayMetadataSection.TYPE);
                        List<String> list = overlaymetadatasection != null ? overlaymetadatasection.overlaysForVersion(i) : List.of();

                        resourcepackloader_a = new ResourcePackLoader.a(resourcepackinfo.description(), enumresourcepackversion, featureflagset, list);
                        break label58;
                    }

                    ResourcePackLoader.LOGGER.warn("Missing metadata in pack {}", packlocationinfo.id());
                    featureflagsmetadatasection = null;
                } catch (Throwable throwable) {
                    if (iresourcepack != null) {
                        try {
                            iresourcepack.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (iresourcepack != null) {
                    iresourcepack.close();
                }

                return featureflagsmetadatasection;
            }

            if (iresourcepack != null) {
                iresourcepack.close();
            }

            return resourcepackloader_a;
        } catch (Exception exception) {
            ResourcePackLoader.LOGGER.warn("Failed to read pack {} metadata", packlocationinfo.id(), exception);
            return null;
        }
    }

    private static InclusiveRange<Integer> getDeclaredPackVersions(String s, ResourcePackInfo resourcepackinfo) {
        int i = resourcepackinfo.packFormat();

        if (resourcepackinfo.supportedFormats().isEmpty()) {
            return new InclusiveRange<>(i);
        } else {
            InclusiveRange<Integer> inclusiverange = (InclusiveRange) resourcepackinfo.supportedFormats().get();

            if (!inclusiverange.isValueInRange(i)) {
                ResourcePackLoader.LOGGER.warn("Pack {} declared support for versions {} but declared main format is {}, defaulting to {}", new Object[]{s, inclusiverange, i, i});
                return new InclusiveRange<>(i);
            } else {
                return inclusiverange;
            }
        }
    }

    public PackLocationInfo location() {
        return this.location;
    }

    public IChatBaseComponent getTitle() {
        return this.location.title();
    }

    public IChatBaseComponent getDescription() {
        return this.metadata.description();
    }

    public IChatBaseComponent getChatLink(boolean flag) {
        return this.location.createChatLink(flag, this.metadata.description);
    }

    public EnumResourcePackVersion getCompatibility() {
        return this.metadata.compatibility();
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.metadata.requestedFeatures();
    }

    public IResourcePack open() {
        return this.resources.openFull(this.location, this.metadata);
    }

    public String getId() {
        return this.location.id();
    }

    public PackSelectionConfig selectionConfig() {
        return this.selectionConfig;
    }

    public boolean isRequired() {
        return this.selectionConfig.required();
    }

    public boolean isFixedPosition() {
        return this.selectionConfig.fixedPosition();
    }

    public ResourcePackLoader.Position getDefaultPosition() {
        return this.selectionConfig.defaultPosition();
    }

    public PackSource getPackSource() {
        return this.location.source();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof ResourcePackLoader)) {
            return false;
        } else {
            ResourcePackLoader resourcepackloader = (ResourcePackLoader) object;

            return this.location.equals(resourcepackloader.location);
        }
    }

    public int hashCode() {
        return this.location.hashCode();
    }

    public interface c {

        IResourcePack openPrimary(PackLocationInfo packlocationinfo);

        IResourcePack openFull(PackLocationInfo packlocationinfo, ResourcePackLoader.a resourcepackloader_a);
    }

    public static record a(IChatBaseComponent description, EnumResourcePackVersion compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {

    }

    public static enum Position {

        TOP, BOTTOM;

        private Position() {}

        public <T> int insert(List<T> list, T t0, Function<T, PackSelectionConfig> function, boolean flag) {
            ResourcePackLoader.Position resourcepackloader_position = flag ? this.opposite() : this;
            PackSelectionConfig packselectionconfig;
            int i;

            if (resourcepackloader_position == ResourcePackLoader.Position.BOTTOM) {
                for (i = 0; i < list.size(); ++i) {
                    packselectionconfig = (PackSelectionConfig) function.apply(list.get(i));
                    if (!packselectionconfig.fixedPosition() || packselectionconfig.defaultPosition() != this) {
                        break;
                    }
                }

                list.add(i, t0);
                return i;
            } else {
                for (i = list.size() - 1; i >= 0; --i) {
                    packselectionconfig = (PackSelectionConfig) function.apply(list.get(i));
                    if (!packselectionconfig.fixedPosition() || packselectionconfig.defaultPosition() != this) {
                        break;
                    }
                }

                list.add(i + 1, t0);
                return i + 1;
            }
        }

        public ResourcePackLoader.Position opposite() {
            return this == ResourcePackLoader.Position.TOP ? ResourcePackLoader.Position.BOTTOM : ResourcePackLoader.Position.TOP;
        }
    }
}
