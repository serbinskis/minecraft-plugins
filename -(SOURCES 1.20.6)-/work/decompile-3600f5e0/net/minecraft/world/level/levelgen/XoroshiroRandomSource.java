package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;

public class XoroshiroRandomSource implements RandomSource {

    private static final float FLOAT_UNIT = 5.9604645E-8F;
    private static final double DOUBLE_UNIT = 1.1102230246251565E-16D;
    public static final Codec<XoroshiroRandomSource> CODEC = Xoroshiro128PlusPlus.CODEC.xmap((xoroshiro128plusplus) -> {
        return new XoroshiroRandomSource(xoroshiro128plusplus);
    }, (xoroshirorandomsource) -> {
        return xoroshirorandomsource.randomNumberGenerator;
    });
    private Xoroshiro128PlusPlus randomNumberGenerator;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public XoroshiroRandomSource(long i) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(i));
    }

    public XoroshiroRandomSource(RandomSupport.a randomsupport_a) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(randomsupport_a);
    }

    public XoroshiroRandomSource(long i, long j) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(i, j);
    }

    private XoroshiroRandomSource(Xoroshiro128PlusPlus xoroshiro128plusplus) {
        this.randomNumberGenerator = xoroshiro128plusplus;
    }

    @Override
    public RandomSource fork() {
        return new XoroshiroRandomSource(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new XoroshiroRandomSource.a(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
    }

    @Override
    public void setSeed(long i) {
        this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(i));
        this.gaussianSource.reset();
    }

    @Override
    public int nextInt() {
        return (int) this.randomNumberGenerator.nextLong();
    }

    @Override
    public int nextInt(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        } else {
            long j = Integer.toUnsignedLong(this.nextInt());
            long k = j * (long) i;
            long l = k & 4294967295L;

            if (l < (long) i) {
                for (int i1 = Integer.remainderUnsigned(~i + 1, i); l < (long) i1; l = k & 4294967295L) {
                    j = Integer.toUnsignedLong(this.nextInt());
                    k = j * (long) i;
                }
            }

            long j1 = k >> 32;

            return (int) j1;
        }
    }

    @Override
    public long nextLong() {
        return this.randomNumberGenerator.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return (this.randomNumberGenerator.nextLong() & 1L) != 0L;
    }

    @Override
    public float nextFloat() {
        return (float) this.nextBits(24) * 5.9604645E-8F;
    }

    @Override
    public double nextDouble() {
        return (double) this.nextBits(53) * 1.1102230246251565E-16D;
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }

    @Override
    public void consumeCount(int i) {
        for (int j = 0; j < i; ++j) {
            this.randomNumberGenerator.nextLong();
        }

    }

    private long nextBits(int i) {
        return this.randomNumberGenerator.nextLong() >>> 64 - i;
    }

    public static class a implements PositionalRandomFactory {

        private final long seedLo;
        private final long seedHi;

        public a(long i, long j) {
            this.seedLo = i;
            this.seedHi = j;
        }

        @Override
        public RandomSource at(int i, int j, int k) {
            long l = MathHelper.getSeed(i, j, k);
            long i1 = l ^ this.seedLo;

            return new XoroshiroRandomSource(i1, this.seedHi);
        }

        @Override
        public RandomSource fromHashOf(String s) {
            RandomSupport.a randomsupport_a = RandomSupport.seedFromHashOf(s);

            return new XoroshiroRandomSource(randomsupport_a.xor(this.seedLo, this.seedHi));
        }

        @VisibleForTesting
        @Override
        public void parityConfigString(StringBuilder stringbuilder) {
            stringbuilder.append("seedLo: ").append(this.seedLo).append(", seedHi: ").append(this.seedHi);
        }
    }
}
