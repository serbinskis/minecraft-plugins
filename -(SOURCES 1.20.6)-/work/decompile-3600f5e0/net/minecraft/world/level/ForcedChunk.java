package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.PersistentBase;

public class ForcedChunk extends PersistentBase {

    public static final String FILE_ID = "chunks";
    private static final String TAG_FORCED = "Forced";
    private final LongSet chunks;

    public static PersistentBase.a<ForcedChunk> factory() {
        return new PersistentBase.a<>(ForcedChunk::new, ForcedChunk::load, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    }

    private ForcedChunk(LongSet longset) {
        this.chunks = longset;
    }

    public ForcedChunk() {
        this(new LongOpenHashSet());
    }

    public static ForcedChunk load(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        return new ForcedChunk(new LongOpenHashSet(nbttagcompound.getLongArray("Forced")));
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        nbttagcompound.putLongArray("Forced", this.chunks.toLongArray());
        return nbttagcompound;
    }

    public LongSet getChunks() {
        return this.chunks;
    }
}
