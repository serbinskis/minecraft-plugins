package net.minecraft.world.level.gameevent;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class BlockPositionSource implements PositionSource {

    public static final MapCodec<BlockPositionSource> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockPosition.CODEC.fieldOf("pos").forGetter((blockpositionsource) -> {
            return blockpositionsource.pos;
        })).apply(instance, BlockPositionSource::new);
    });
    public static final StreamCodec<ByteBuf, BlockPositionSource> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, (blockpositionsource) -> {
        return blockpositionsource.pos;
    }, BlockPositionSource::new);
    private final BlockPosition pos;

    public BlockPositionSource(BlockPosition blockposition) {
        this.pos = blockposition;
    }

    @Override
    public Optional<Vec3D> getPosition(World world) {
        return Optional.of(Vec3D.atCenterOf(this.pos));
    }

    @Override
    public PositionSourceType<BlockPositionSource> getType() {
        return PositionSourceType.BLOCK;
    }

    public static class a implements PositionSourceType<BlockPositionSource> {

        public a() {}

        @Override
        public MapCodec<BlockPositionSource> codec() {
            return BlockPositionSource.CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, BlockPositionSource> streamCodec() {
            return BlockPositionSource.STREAM_CODEC;
        }
    }
}
