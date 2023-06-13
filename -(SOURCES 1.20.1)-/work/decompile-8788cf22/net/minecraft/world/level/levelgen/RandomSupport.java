package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomSupport {

    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final HashFunction MD5_128 = Hashing.md5();
    private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);

    public RandomSupport() {}

    @VisibleForTesting
    public static long mixStafford13(long i) {
        i = (i ^ i >>> 30) * -4658895280553007687L;
        i = (i ^ i >>> 27) * -7723592293110705685L;
        return i ^ i >>> 31;
    }

    public static RandomSupport.a upgradeSeedTo128bitUnmixed(long i) {
        long j = i ^ 7640891576956012809L;
        long k = j + -7046029254386353131L;

        return new RandomSupport.a(j, k);
    }

    public static RandomSupport.a upgradeSeedTo128bit(long i) {
        return upgradeSeedTo128bitUnmixed(i).mixed();
    }

    public static RandomSupport.a seedFromHashOf(String s) {
        byte[] abyte = RandomSupport.MD5_128.hashString(s, Charsets.UTF_8).asBytes();
        long i = Longs.fromBytes(abyte[0], abyte[1], abyte[2], abyte[3], abyte[4], abyte[5], abyte[6], abyte[7]);
        long j = Longs.fromBytes(abyte[8], abyte[9], abyte[10], abyte[11], abyte[12], abyte[13], abyte[14], abyte[15]);

        return new RandomSupport.a(i, j);
    }

    public static long generateUniqueSeed() {
        return RandomSupport.SEED_UNIQUIFIER.updateAndGet((i) -> {
            return i * 1181783497276652981L;
        }) ^ System.nanoTime();
    }

    public static record a(long seedLo, long seedHi) {

        public RandomSupport.a xor(long i, long j) {
            return new RandomSupport.a(this.seedLo ^ i, this.seedHi ^ j);
        }

        public RandomSupport.a xor(RandomSupport.a randomsupport_a) {
            return this.xor(randomsupport_a.seedLo, randomsupport_a.seedHi);
        }

        public RandomSupport.a mixed() {
            return new RandomSupport.a(RandomSupport.mixStafford13(this.seedLo), RandomSupport.mixStafford13(this.seedHi));
        }
    }
}
