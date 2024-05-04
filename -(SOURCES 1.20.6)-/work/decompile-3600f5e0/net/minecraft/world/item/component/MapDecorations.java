package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public record MapDecorations(Map<String, MapDecorations.a> decorations) {

    public static final MapDecorations EMPTY = new MapDecorations(Map.of());
    public static final Codec<MapDecorations> CODEC = Codec.unboundedMap(Codec.STRING, MapDecorations.a.CODEC).xmap(MapDecorations::new, MapDecorations::decorations);

    public MapDecorations withDecoration(String s, MapDecorations.a mapdecorations_a) {
        return new MapDecorations(SystemUtils.copyAndPut(this.decorations, s, mapdecorations_a));
    }

    public static record a(Holder<MapDecorationType> type, double x, double z, float rotation) {

        public static final Codec<MapDecorations.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(MapDecorationType.CODEC.fieldOf("type").forGetter(MapDecorations.a::type), Codec.DOUBLE.fieldOf("x").forGetter(MapDecorations.a::x), Codec.DOUBLE.fieldOf("z").forGetter(MapDecorations.a::z), Codec.FLOAT.fieldOf("rotation").forGetter(MapDecorations.a::rotation)).apply(instance, MapDecorations.a::new);
        });
    }
}
