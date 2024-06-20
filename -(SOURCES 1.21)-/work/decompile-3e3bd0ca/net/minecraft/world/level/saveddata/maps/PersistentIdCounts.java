package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.PersistentBase;

public class PersistentIdCounts extends PersistentBase {

    public static final String FILE_NAME = "idcounts";
    private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap();

    public static PersistentBase.a<PersistentIdCounts> factory() {
        return new PersistentBase.a<>(PersistentIdCounts::new, PersistentIdCounts::load, DataFixTypes.SAVED_DATA_MAP_INDEX);
    }

    public PersistentIdCounts() {
        this.usedAuxIds.defaultReturnValue(-1);
    }

    public static PersistentIdCounts load(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        PersistentIdCounts persistentidcounts = new PersistentIdCounts();
        Iterator iterator = nbttagcompound.getAllKeys().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            if (nbttagcompound.contains(s, 99)) {
                persistentidcounts.usedAuxIds.put(s, nbttagcompound.getInt(s));
            }
        }

        return persistentidcounts;
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        ObjectIterator objectiterator = this.usedAuxIds.object2IntEntrySet().iterator();

        while (objectiterator.hasNext()) {
            Entry<String> entry = (Entry) objectiterator.next();

            nbttagcompound.putInt((String) entry.getKey(), entry.getIntValue());
        }

        return nbttagcompound;
    }

    public MapId getFreeAuxValueForMap() {
        int i = this.usedAuxIds.getInt("map") + 1;

        this.usedAuxIds.put("map", i);
        this.setDirty();
        return new MapId(i);
    }
}
