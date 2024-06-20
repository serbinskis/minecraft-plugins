package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
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
        this(createSequence(i, Optional.of(minecraftkey)));
    }

    public RandomSequence(long i, Optional<MinecraftKey> optional) {
        this(createSequence(i, optional));
    }

    private static XoroshiroRandomSource createSequence(long i, Optional<MinecraftKey> optional) {
        RandomSupport.a randomsupport_a = RandomSupport.upgradeSeedTo128bitUnmixed(i);

        if (optional.isPresent()) {
            randomsupport_a = randomsupport_a.xor(seedForKey((MinecraftKey) optional.get()));
        }

        return new XoroshiroRandomSource(randomsupport_a.mixed());
    }

    public static RandomSupport.a seedForKey(MinecraftKey minecraftkey) {
        return RandomSupport.seedFromHashOf(minecraftkey.toString());
    }

    public RandomSource random() {
        return this.source;
    }
}
