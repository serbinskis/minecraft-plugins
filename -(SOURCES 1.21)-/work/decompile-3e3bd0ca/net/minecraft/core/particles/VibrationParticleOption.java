package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;

public class VibrationParticleOption implements ParticleParam {

    private static final Codec<PositionSource> SAFE_POSITION_SOURCE_CODEC = PositionSource.CODEC.validate((positionsource) -> {
        return positionsource instanceof EntityPositionSource ? DataResult.error(() -> {
            return "Entity position sources are not allowed";
        }) : DataResult.success(positionsource);
    });
    public static final MapCodec<VibrationParticleOption> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(VibrationParticleOption.SAFE_POSITION_SOURCE_CODEC.fieldOf("destination").forGetter(VibrationParticleOption::getDestination), Codec.INT.fieldOf("arrival_in_ticks").forGetter(VibrationParticleOption::getArrivalInTicks)).apply(instance, VibrationParticleOption::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, VibrationParticleOption> STREAM_CODEC = StreamCodec.composite(PositionSource.STREAM_CODEC, VibrationParticleOption::getDestination, ByteBufCodecs.VAR_INT, VibrationParticleOption::getArrivalInTicks, VibrationParticleOption::new);
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationParticleOption(PositionSource positionsource, int i) {
        this.destination = positionsource;
        this.arrivalInTicks = i;
    }

    @Override
    public Particle<VibrationParticleOption> getType() {
        return Particles.VIBRATION;
    }

    public PositionSource getDestination() {
        return this.destination;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }
}
