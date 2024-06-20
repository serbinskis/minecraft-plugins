package net.minecraft.server.packs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record OverlayMetadataSection(List<OverlayMetadataSection.a> overlays) {

    private static final Pattern DIR_VALIDATOR = Pattern.compile("[-_a-zA-Z0-9.]+");
    private static final Codec<OverlayMetadataSection> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(OverlayMetadataSection.a.CODEC.listOf().fieldOf("entries").forGetter(OverlayMetadataSection::overlays)).apply(instance, OverlayMetadataSection::new);
    });
    public static final MetadataSectionType<OverlayMetadataSection> TYPE = MetadataSectionType.fromCodec("overlays", OverlayMetadataSection.CODEC);

    private static DataResult<String> validateOverlayDir(String s) {
        return !OverlayMetadataSection.DIR_VALIDATOR.matcher(s).matches() ? DataResult.error(() -> {
            return s + " is not accepted directory name";
        }) : DataResult.success(s);
    }

    public List<String> overlaysForVersion(int i) {
        return this.overlays.stream().filter((overlaymetadatasection_a) -> {
            return overlaymetadatasection_a.isApplicable(i);
        }).map(OverlayMetadataSection.a::overlay).toList();
    }

    public static record a(InclusiveRange<Integer> format, String overlay) {

        static final Codec<OverlayMetadataSection.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(InclusiveRange.codec(Codec.INT).fieldOf("formats").forGetter(OverlayMetadataSection.a::format), Codec.STRING.validate(OverlayMetadataSection::validateOverlayDir).fieldOf("directory").forGetter(OverlayMetadataSection.a::overlay)).apply(instance, OverlayMetadataSection.a::new);
        });

        public boolean isApplicable(int i) {
            return this.format.isValueInRange(i);
        }
    }
}
