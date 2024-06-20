package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect {

    public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomial::chance)).apply(instance, RemoveBinomial::new);
    });

    @Override
    public float process(int i, RandomSource randomsource, float f) {
        float f1 = this.chance.calculate(i);
        int j = 0;

        for (int k = 0; (float) k < f; ++k) {
            if (randomsource.nextFloat() < f1) {
                ++j;
            }
        }

        return f - (float) j;
    }

    @Override
    public MapCodec<RemoveBinomial> codec() {
        return RemoveBinomial.CODEC;
    }
}
