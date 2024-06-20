package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.PersistentBase;
import org.slf4j.Logger;

public class RandomSequences extends PersistentBase {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final long worldSeed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<MinecraftKey, RandomSequence> sequences = new Object2ObjectOpenHashMap();

    public static PersistentBase.a<RandomSequences> factory(long i) {
        return new PersistentBase.a<>(() -> {
            return new RandomSequences(i);
        }, (nbttagcompound, holderlookup_a) -> {
            return load(i, nbttagcompound);
        }, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    public RandomSequences(long i) {
        this.worldSeed = i;
    }

    public RandomSource get(MinecraftKey minecraftkey) {
        RandomSource randomsource = ((RandomSequence) this.sequences.computeIfAbsent(minecraftkey, this::createSequence)).random();

        return new RandomSequences.a(randomsource);
    }

    private RandomSequence createSequence(MinecraftKey minecraftkey) {
        return this.createSequence(minecraftkey, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(MinecraftKey minecraftkey, int i, boolean flag, boolean flag1) {
        long j = (flag ? this.worldSeed : 0L) ^ (long) i;

        return new RandomSequence(j, flag1 ? Optional.of(minecraftkey) : Optional.empty());
    }

    public void forAllSequences(BiConsumer<MinecraftKey, RandomSequence> biconsumer) {
        this.sequences.forEach(biconsumer);
    }

    public void setSeedDefaults(int i, boolean flag, boolean flag1) {
        this.salt = i;
        this.includeWorldSeed = flag;
        this.includeSequenceId = flag1;
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        nbttagcompound.putInt("salt", this.salt);
        nbttagcompound.putBoolean("include_world_seed", this.includeWorldSeed);
        nbttagcompound.putBoolean("include_sequence_id", this.includeSequenceId);
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();

        this.sequences.forEach((minecraftkey, randomsequence) -> {
            nbttagcompound1.put(minecraftkey.toString(), (NBTBase) RandomSequence.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, randomsequence).result().orElseThrow());
        });
        nbttagcompound.put("sequences", nbttagcompound1);
        return nbttagcompound;
    }

    private static boolean getBooleanWithDefault(NBTTagCompound nbttagcompound, String s, boolean flag) {
        return nbttagcompound.contains(s, 1) ? nbttagcompound.getBoolean(s) : flag;
    }

    public static RandomSequences load(long i, NBTTagCompound nbttagcompound) {
        RandomSequences randomsequences = new RandomSequences(i);

        randomsequences.setSeedDefaults(nbttagcompound.getInt("salt"), getBooleanWithDefault(nbttagcompound, "include_world_seed", true), getBooleanWithDefault(nbttagcompound, "include_sequence_id", true));
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("sequences");
        Set<String> set = nbttagcompound1.getAllKeys();
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            try {
                RandomSequence randomsequence = (RandomSequence) ((Pair) RandomSequence.CODEC.decode(DynamicOpsNBT.INSTANCE, nbttagcompound1.get(s)).result().get()).getFirst();

                randomsequences.sequences.put(MinecraftKey.parse(s), randomsequence);
            } catch (Exception exception) {
                RandomSequences.LOGGER.error("Failed to load random sequence {}", s, exception);
            }
        }

        return randomsequences;
    }

    public int clear() {
        int i = this.sequences.size();

        this.sequences.clear();
        return i;
    }

    public void reset(MinecraftKey minecraftkey) {
        this.sequences.put(minecraftkey, this.createSequence(minecraftkey));
    }

    public void reset(MinecraftKey minecraftkey, int i, boolean flag, boolean flag1) {
        this.sequences.put(minecraftkey, this.createSequence(minecraftkey, i, flag, flag1));
    }

    private class a implements RandomSource {

        private final RandomSource random;

        a(final RandomSource randomsource) {
            this.random = randomsource;
        }

        @Override
        public RandomSource fork() {
            RandomSequences.this.setDirty();
            return this.random.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return this.random.forkPositional();
        }

        @Override
        public void setSeed(long i) {
            RandomSequences.this.setDirty();
            this.random.setSeed(i);
        }

        @Override
        public int nextInt() {
            RandomSequences.this.setDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int i) {
            RandomSequences.this.setDirty();
            return this.random.nextInt(i);
        }

        @Override
        public long nextLong() {
            RandomSequences.this.setDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequences.this.setDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequences.this.setDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequences.this.setDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object instanceof RandomSequences.a) {
                RandomSequences.a randomsequences_a = (RandomSequences.a) object;

                return this.random.equals(randomsequences_a.random);
            } else {
                return false;
            }
        }
    }
}
