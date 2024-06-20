package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;

public record GlobalPos(ResourceKey<World> dimension, BlockPosition pos) {

    public static final MapCodec<GlobalPos> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(World.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), BlockPosition.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)).apply(instance, GlobalPos::of);
    });
    public static final Codec<GlobalPos> CODEC = GlobalPos.MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, GlobalPos> STREAM_CODEC = StreamCodec.composite(ResourceKey.streamCodec(Registries.DIMENSION), GlobalPos::dimension, BlockPosition.STREAM_CODEC, GlobalPos::pos, GlobalPos::of);

    public static GlobalPos of(ResourceKey<World> resourcekey, BlockPosition blockposition) {
        return new GlobalPos(resourcekey, blockposition);
    }

    public String toString() {
        String s = String.valueOf(this.dimension);

        return s + " " + String.valueOf(this.pos);
    }
}
