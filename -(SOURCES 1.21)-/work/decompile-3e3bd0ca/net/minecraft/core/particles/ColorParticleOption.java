package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ColorUtil;
import net.minecraft.util.ExtraCodecs;

public class ColorParticleOption implements ParticleParam {

    private final Particle<ColorParticleOption> type;
    private final int color;

    public static MapCodec<ColorParticleOption> codec(Particle<ColorParticleOption> particle) {
        return ExtraCodecs.ARGB_COLOR_CODEC.xmap((integer) -> {
            return new ColorParticleOption(particle, integer);
        }, (colorparticleoption) -> {
            return colorparticleoption.color;
        }).fieldOf("color");
    }

    public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(Particle<ColorParticleOption> particle) {
        return ByteBufCodecs.INT.map((integer) -> {
            return new ColorParticleOption(particle, integer);
        }, (colorparticleoption) -> {
            return colorparticleoption.color;
        });
    }

    private ColorParticleOption(Particle<ColorParticleOption> particle, int i) {
        this.type = particle;
        this.color = i;
    }

    @Override
    public Particle<ColorParticleOption> getType() {
        return this.type;
    }

    public float getRed() {
        return (float) ColorUtil.b.red(this.color) / 255.0F;
    }

    public float getGreen() {
        return (float) ColorUtil.b.green(this.color) / 255.0F;
    }

    public float getBlue() {
        return (float) ColorUtil.b.blue(this.color) / 255.0F;
    }

    public float getAlpha() {
        return (float) ColorUtil.b.alpha(this.color) / 255.0F;
    }

    public static ColorParticleOption create(Particle<ColorParticleOption> particle, int i) {
        return new ColorParticleOption(particle, i);
    }

    public static ColorParticleOption create(Particle<ColorParticleOption> particle, float f, float f1, float f2) {
        return create(particle, ColorUtil.b.colorFromFloat(1.0F, f, f1, f2));
    }
}
