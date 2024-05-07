package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public record LodestoneTracker(Optional<GlobalPos> target, boolean tracked) {

    public static final Codec<LodestoneTracker> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(GlobalPos.CODEC.optionalFieldOf("target").forGetter(LodestoneTracker::target), Codec.BOOL.optionalFieldOf("tracked", true).forGetter(LodestoneTracker::tracked)).apply(instance, LodestoneTracker::new);
    });
    public static final StreamCodec<ByteBuf, LodestoneTracker> STREAM_CODEC = StreamCodec.composite(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional), LodestoneTracker::target, ByteBufCodecs.BOOL, LodestoneTracker::tracked, LodestoneTracker::new);

    public LodestoneTracker tick(WorldServer worldserver) {
        if (this.tracked && !this.target.isEmpty()) {
            if (((GlobalPos) this.target.get()).dimension() != worldserver.dimension()) {
                return this;
            } else {
                BlockPosition blockposition = ((GlobalPos) this.target.get()).pos();

                return worldserver.isInWorldBounds(blockposition) && worldserver.getPoiManager().existsAtPosition(PoiTypes.LODESTONE, blockposition) ? this : new LodestoneTracker(Optional.empty(), true);
            }
        } else {
            return this;
        }
    }
}
