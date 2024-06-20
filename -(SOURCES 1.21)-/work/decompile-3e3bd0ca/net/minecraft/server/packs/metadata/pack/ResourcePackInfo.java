package net.minecraft.server.packs.metadata.pack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record ResourcePackInfo(IChatBaseComponent description, int packFormat, Optional<InclusiveRange<Integer>> supportedFormats) {

    public static final Codec<ResourcePackInfo> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ComponentSerialization.CODEC.fieldOf("description").forGetter(ResourcePackInfo::description), Codec.INT.fieldOf("pack_format").forGetter(ResourcePackInfo::packFormat), InclusiveRange.codec(Codec.INT).lenientOptionalFieldOf("supported_formats").forGetter(ResourcePackInfo::supportedFormats)).apply(instance, ResourcePackInfo::new);
    });
    public static final MetadataSectionType<ResourcePackInfo> TYPE = MetadataSectionType.fromCodec("pack", ResourcePackInfo.CODEC);
}
