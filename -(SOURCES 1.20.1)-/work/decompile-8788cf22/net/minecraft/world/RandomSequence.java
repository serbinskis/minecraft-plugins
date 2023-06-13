package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class RandomSequence {

    public static final Codec<RandomSequence> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(XoroshiroRandomSource.CODEC.fieldOf("source").forGetter((randomsequence) -> {
            return randomsequence.source;
        })).apply(instance, RandomSequence::new);
    });
    private final XoroshiroRandomSource source;

    public RandomSequence(XoroshiroRandomSource xoroshirorandomsource) {
        this.source = xoroshirorandomsource;
    }

    public RandomSequence(long i, MinecraftKey minecraftkey) {
        this(createSequence(i, minecraftkey));
    }

    private static XoroshiroRandomSource createSequence(long i, MinecraftKey minecraftkey) {
        return new XoroshiroRandomSource(RandomSupport.upgradeSeedTo128bitUnmixed(i).xor(seedForKey(minecraftkey)).mixed());
    }

    public static RandomSupport.a seedForKey(MinecraftKey minecraftkey) {
        return RandomSupport.seedFromHashOf(minecraftkey.toString());
    }

    public RandomSource random() {
        return this.source;
    }
}
