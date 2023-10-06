package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.IResourcePack;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.metadata.pack.ResourcePackInfo;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class ResourcePackLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final String id;
    public final ResourcePackLoader.c resources;
    private final IChatBaseComponent title;
    private final ResourcePackLoader.a info;
    private final ResourcePackLoader.Position defaultPosition;
    private final boolean required;
    private final boolean fixedPosition;
    private final PackSource packSource;

    @Nullable
    public static ResourcePackLoader readMetaAndCreate(String s, IChatBaseComponent ichatbasecomponent, boolean flag, ResourcePackLoader.c resourcepackloader_c, EnumResourcePackType enumresourcepacktype, ResourcePackLoader.Position resourcepackloader_position, PackSource packsource) {
        int i = SharedConstants.getCurrentVersion().getPackVersion(enumresourcepacktype);
        ResourcePackLoader.a resourcepackloader_a = readPackInfo(s, resourcepackloader_c, i);

        return resourcepackloader_a != null ? create(s, ichatbasecomponent, flag, resourcepackloader_c, resourcepackloader_a, resourcepackloader_position, false, packsource) : null;
    }

    public static ResourcePackLoader create(String s, IChatBaseComponent ichatbasecomponent, boolean flag, ResourcePackLoader.c resourcepackloader_c, ResourcePackLoader.a resourcepackloader_a, ResourcePackLoader.Position resourcepackloader_position, boolean flag1, PackSource packsource) {
        return new ResourcePackLoader(s, flag, resourcepackloader_c, ichatbasecomponent, resourcepackloader_a, resourcepackloader_position, flag1, packsource);
    }

    private ResourcePackLoader(String s, boolean flag, ResourcePackLoader.c resourcepackloader_c, IChatBaseComponent ichatbasecomponent, ResourcePackLoader.a resourcepackloader_a, ResourcePackLoader.Position resourcepackloader_position, boolean flag1, PackSource packsource) {
        this.id = s;
        this.resources = resourcepackloader_c;
        this.title = ichatbasecomponent;
        this.info = resourcepackloader_a;
        this.required = flag;
        this.defaultPosition = resourcepackloader_position;
        this.fixedPosition = flag1;
        this.packSource = packsource;
    }

    @Nullable
    public static ResourcePackLoader.a readPackInfo(String s, ResourcePackLoader.c resourcepackloader_c, int i) {
        try {
            IResourcePack iresourcepack = resourcepackloader_c.openPrimary(s);

            ResourcePackLoader.a resourcepackloader_a;
            label58:
            {
                FeatureFlagsMetadataSection featureflagsmetadatasection;

                try {
                    ResourcePackInfo resourcepackinfo = (ResourcePackInfo) iresourcepack.getMetadataSection(ResourcePackInfo.TYPE);

                    if (resourcepackinfo != null) {
                        featureflagsmetadatasection = (FeatureFlagsMetadataSection) iresourcepack.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
                        FeatureFlagSet featureflagset = featureflagsmetadatasection != null ? featureflagsmetadatasection.flags() : FeatureFlagSet.of();
                        InclusiveRange<Integer> inclusiverange = getDeclaredPackVersions(s, resourcepackinfo);
                        EnumResourcePackVersion enumresourcepackversion = EnumResourcePackVersion.forVersion(inclusiverange, i);
                        OverlayMetadataSection overlaymetadatasection = (OverlayMetadataSection) iresourcepack.getMetadataSection(OverlayMetadataSection.TYPE);
                        List<String> list = overlaymetadatasection != null ? overlaymetadatasection.overlaysForVersion(i) : List.of();

                        resourcepackloader_a = new ResourcePackLoader.a(resourcepackinfo.description(), enumresourcepackversion, featureflagset, list);
                        break label58;
                    }

                    ResourcePackLoader.LOGGER.warn("Missing metadata in pack {}", s);
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
            ResourcePackLoader.LOGGER.warn("Failed to read pack {} metadata", s, exception);
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

    public IChatBaseComponent getTitle() {
        return this.title;
    }

    public IChatBaseComponent getDescription() {
        return this.info.description();
    }

    public IChatBaseComponent getChatLink(boolean flag) {
        return ChatComponentUtils.wrapInSquareBrackets(this.packSource.decorate(IChatBaseComponent.literal(this.id))).withStyle((chatmodifier) -> {
            return chatmodifier.withColor(flag ? EnumChatFormat.GREEN : EnumChatFormat.RED).withInsertion(StringArgumentType.escapeIfRequired(this.id)).withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, IChatBaseComponent.empty().append(this.title).append("\n").append(this.info.description)));
        });
    }

    public EnumResourcePackVersion getCompatibility() {
        return this.info.compatibility();
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.info.requestedFeatures();
    }

    public IResourcePack open() {
        return this.resources.openFull(this.id, this.info);
    }

    public String getId() {
        return this.id;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isFixedPosition() {
        return this.fixedPosition;
    }

    public ResourcePackLoader.Position getDefaultPosition() {
        return this.defaultPosition;
    }

    public PackSource getPackSource() {
        return this.packSource;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof ResourcePackLoader)) {
            return false;
        } else {
            ResourcePackLoader resourcepackloader = (ResourcePackLoader) object;

            return this.id.equals(resourcepackloader.id);
        }
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public interface c {

        IResourcePack openPrimary(String s);

        IResourcePack openFull(String s, ResourcePackLoader.a resourcepackloader_a);
    }

    public static record a(IChatBaseComponent description, EnumResourcePackVersion compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {

    }

    public static enum Position {

        TOP, BOTTOM;

        private Position() {}

        public <T> int insert(List<T> list, T t0, Function<T, ResourcePackLoader> function, boolean flag) {
            ResourcePackLoader.Position resourcepackloader_position = flag ? this.opposite() : this;
            ResourcePackLoader resourcepackloader;
            int i;

            if (resourcepackloader_position == ResourcePackLoader.Position.BOTTOM) {
                for (i = 0; i < list.size(); ++i) {
                    resourcepackloader = (ResourcePackLoader) function.apply(list.get(i));
                    if (!resourcepackloader.isFixedPosition() || resourcepackloader.getDefaultPosition() != this) {
                        break;
                    }
                }

                list.add(i, t0);
                return i;
            } else {
                for (i = list.size() - 1; i >= 0; --i) {
                    resourcepackloader = (ResourcePackLoader) function.apply(list.get(i));
                    if (!resourcepackloader.isFixedPosition() || resourcepackloader.getDefaultPosition() != this) {
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
