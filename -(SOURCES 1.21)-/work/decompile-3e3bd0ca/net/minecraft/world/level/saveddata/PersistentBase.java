package net.minecraft.world.level.saveddata;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;

public abstract class PersistentBase {

    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean dirty;

    public PersistentBase() {}

    public abstract NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a);

    public void setDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean flag) {
        this.dirty = flag;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void save(File file, HolderLookup.a holderlookup_a) {
        if (this.isDirty()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            nbttagcompound.put("data", this.save(new NBTTagCompound(), holderlookup_a));
            GameProfileSerializer.addCurrentDataVersion(nbttagcompound);

            try {
                NBTCompressedStreamTools.writeCompressed(nbttagcompound, file.toPath());
            } catch (IOException ioexception) {
                PersistentBase.LOGGER.error("Could not save data {}", this, ioexception);
            }

            this.setDirty(false);
        }
    }

    public static record a<T extends PersistentBase>(Supplier<T> constructor, BiFunction<NBTTagCompound, HolderLookup.a, T> deserializer, DataFixTypes type) {

    }
}
