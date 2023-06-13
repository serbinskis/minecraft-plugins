package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.PersistentBase;
import org.slf4j.Logger;

public class RandomSequences extends PersistentBase {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final long seed;
    private final Map<MinecraftKey, RandomSequence> sequences = new Object2ObjectOpenHashMap();

    public RandomSequences(long i) {
        this.seed = i;
    }

    public RandomSource get(MinecraftKey minecraftkey) {
        final RandomSource randomsource = ((RandomSequence) this.sequences.computeIfAbsent(minecraftkey, (minecraftkey1) -> {
            return new RandomSequence(this.seed, minecraftkey1);
        })).random();

        return new RandomSource() {
            @Override
            public RandomSource fork() {
                RandomSequences.this.setDirty();
                return randomsource.fork();
            }

            @Override
            public PositionalRandomFactory forkPositional() {
                RandomSequences.this.setDirty();
                return randomsource.forkPositional();
            }

            @Override
            public void setSeed(long i) {
                RandomSequences.this.setDirty();
                randomsource.setSeed(i);
            }

            @Override
            public int nextInt() {
                RandomSequences.this.setDirty();
                return randomsource.nextInt();
            }

            @Override
            public int nextInt(int i) {
                RandomSequences.this.setDirty();
                return randomsource.nextInt(i);
            }

            @Override
            public long nextLong() {
                RandomSequences.this.setDirty();
                return randomsource.nextLong();
            }

            @Override
            public boolean nextBoolean() {
                RandomSequences.this.setDirty();
                return randomsource.nextBoolean();
            }

            @Override
            public float nextFloat() {
                RandomSequences.this.setDirty();
                return randomsource.nextFloat();
            }

            @Override
            public double nextDouble() {
                RandomSequences.this.setDirty();
                return randomsource.nextDouble();
            }

            @Override
            public double nextGaussian() {
                RandomSequences.this.setDirty();
                return randomsource.nextGaussian();
            }
        };
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        this.sequences.forEach((minecraftkey, randomsequence) -> {
            nbttagcompound.put(minecraftkey.toString(), (NBTBase) RandomSequence.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, randomsequence).result().orElseThrow());
        });
        return nbttagcompound;
    }

    public static RandomSequences load(long i, NBTTagCompound nbttagcompound) {
        RandomSequences randomsequences = new RandomSequences(i);
        Set<String> set = nbttagcompound.getAllKeys();
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            try {
                RandomSequence randomsequence = (RandomSequence) ((Pair) RandomSequence.CODEC.decode(DynamicOpsNBT.INSTANCE, nbttagcompound.get(s)).result().get()).getFirst();

                randomsequences.sequences.put(new MinecraftKey(s), randomsequence);
            } catch (Exception exception) {
                RandomSequences.LOGGER.error("Failed to load random sequence {}", s, exception);
            }
        }

        return randomsequences;
    }
}
