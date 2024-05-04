package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class DataWatcher {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ID_VALUE = 254;
    static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
    private final SyncedDataHolder entity;
    private final DataWatcher.Item<?>[] itemsById;
    private boolean isDirty;

    DataWatcher(SyncedDataHolder synceddataholder, DataWatcher.Item<?>[] adatawatcher_item) {
        this.entity = synceddataholder;
        this.itemsById = adatawatcher_item;
    }

    public static <T> DataWatcherObject<T> defineId(Class<? extends SyncedDataHolder> oclass, DataWatcherSerializer<T> datawatcherserializer) {
        if (DataWatcher.LOGGER.isDebugEnabled()) {
            try {
                Class<?> oclass1 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());

                if (!oclass1.equals(oclass)) {
                    DataWatcher.LOGGER.debug("defineId called for: {} from {}", new Object[]{oclass, oclass1, new RuntimeException()});
                }
            } catch (ClassNotFoundException classnotfoundexception) {
                ;
            }
        }

        int i = DataWatcher.ID_REGISTRY.define(oclass);

        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        } else {
            return datawatcherserializer.createAccessor(i);
        }
    }

    private <T> DataWatcher.Item<T> getItem(DataWatcherObject<T> datawatcherobject) {
        return this.itemsById[datawatcherobject.id()];
    }

    public <T> T get(DataWatcherObject<T> datawatcherobject) {
        return this.getItem(datawatcherobject).getValue();
    }

    public <T> void set(DataWatcherObject<T> datawatcherobject, T t0) {
        this.set(datawatcherobject, t0, false);
    }

    public <T> void set(DataWatcherObject<T> datawatcherobject, T t0, boolean flag) {
        DataWatcher.Item<T> datawatcher_item = this.getItem(datawatcherobject);

        if (flag || ObjectUtils.notEqual(t0, datawatcher_item.getValue())) {
            datawatcher_item.setValue(t0);
            this.entity.onSyncedDataUpdated(datawatcherobject);
            datawatcher_item.setDirty(true);
            this.isDirty = true;
        }

    }

    public boolean isDirty() {
        return this.isDirty;
    }

    @Nullable
    public List<DataWatcher.c<?>> packDirty() {
        if (!this.isDirty) {
            return null;
        } else {
            this.isDirty = false;
            List<DataWatcher.c<?>> list = new ArrayList();
            DataWatcher.Item[] adatawatcher_item = this.itemsById;
            int i = adatawatcher_item.length;

            for (int j = 0; j < i; ++j) {
                DataWatcher.Item<?> datawatcher_item = adatawatcher_item[j];

                if (datawatcher_item.isDirty()) {
                    datawatcher_item.setDirty(false);
                    list.add(datawatcher_item.value());
                }
            }

            return list;
        }
    }

    @Nullable
    public List<DataWatcher.c<?>> getNonDefaultValues() {
        List<DataWatcher.c<?>> list = null;
        DataWatcher.Item[] adatawatcher_item = this.itemsById;
        int i = adatawatcher_item.length;

        for (int j = 0; j < i; ++j) {
            DataWatcher.Item<?> datawatcher_item = adatawatcher_item[j];

            if (!datawatcher_item.isSetToDefault()) {
                if (list == null) {
                    list = new ArrayList();
                }

                list.add(datawatcher_item.value());
            }
        }

        return list;
    }

    public void assignValues(List<DataWatcher.c<?>> list) {
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            DataWatcher.c<?> datawatcher_c = (DataWatcher.c) iterator.next();
            DataWatcher.Item<?> datawatcher_item = this.itemsById[datawatcher_c.id];

            this.assignValue(datawatcher_item, datawatcher_c);
            this.entity.onSyncedDataUpdated(datawatcher_item.getAccessor());
        }

        this.entity.onSyncedDataUpdated(list);
    }

    private <T> void assignValue(DataWatcher.Item<T> datawatcher_item, DataWatcher.c<?> datawatcher_c) {
        if (!Objects.equals(datawatcher_c.serializer(), datawatcher_item.accessor.serializer())) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", datawatcher_item.accessor.id(), this.entity, datawatcher_item.value, datawatcher_item.value.getClass(), datawatcher_c.value, datawatcher_c.value.getClass()));
        } else {
            datawatcher_item.setValue(datawatcher_c.value);
        }
    }

    public static class Item<T> {

        final DataWatcherObject<T> accessor;
        T value;
        private final T initialValue;
        private boolean dirty;

        public Item(DataWatcherObject<T> datawatcherobject, T t0) {
            this.accessor = datawatcherobject;
            this.initialValue = t0;
            this.value = t0;
        }

        public DataWatcherObject<T> getAccessor() {
            return this.accessor;
        }

        public void setValue(T t0) {
            this.value = t0;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean flag) {
            this.dirty = flag;
        }

        public boolean isSetToDefault() {
            return this.initialValue.equals(this.value);
        }

        public DataWatcher.c<T> value() {
            return DataWatcher.c.create(this.accessor, this.value);
        }
    }

    public static record c<T>(int id, DataWatcherSerializer<T> serializer, T value) {

        public static <T> DataWatcher.c<T> create(DataWatcherObject<T> datawatcherobject, T t0) {
            DataWatcherSerializer<T> datawatcherserializer = datawatcherobject.serializer();

            return new DataWatcher.c<>(datawatcherobject.id(), datawatcherserializer, datawatcherserializer.copy(t0));
        }

        public void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            int i = DataWatcherRegistry.getSerializedId(this.serializer);

            if (i < 0) {
                throw new EncoderException("Unknown serializer type " + String.valueOf(this.serializer));
            } else {
                registryfriendlybytebuf.writeByte(this.id);
                registryfriendlybytebuf.writeVarInt(i);
                this.serializer.codec().encode(registryfriendlybytebuf, this.value);
            }
        }

        public static DataWatcher.c<?> read(RegistryFriendlyByteBuf registryfriendlybytebuf, int i) {
            int j = registryfriendlybytebuf.readVarInt();
            DataWatcherSerializer<?> datawatcherserializer = DataWatcherRegistry.getSerializer(j);

            if (datawatcherserializer == null) {
                throw new DecoderException("Unknown serializer type " + j);
            } else {
                return read(registryfriendlybytebuf, i, datawatcherserializer);
            }
        }

        private static <T> DataWatcher.c<T> read(RegistryFriendlyByteBuf registryfriendlybytebuf, int i, DataWatcherSerializer<T> datawatcherserializer) {
            return new DataWatcher.c<>(i, datawatcherserializer, datawatcherserializer.codec().decode(registryfriendlybytebuf));
        }
    }

    public static class a {

        private final SyncedDataHolder entity;
        private final DataWatcher.Item<?>[] itemsById;

        public a(SyncedDataHolder synceddataholder) {
            this.entity = synceddataholder;
            this.itemsById = new DataWatcher.Item[DataWatcher.ID_REGISTRY.getCount(synceddataholder.getClass())];
        }

        public <T> DataWatcher.a define(DataWatcherObject<T> datawatcherobject, T t0) {
            int i = datawatcherobject.id();

            if (i > this.itemsById.length) {
                throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + this.itemsById.length + ")");
            } else if (this.itemsById[i] != null) {
                throw new IllegalArgumentException("Duplicate id value for " + i + "!");
            } else if (DataWatcherRegistry.getSerializedId(datawatcherobject.serializer()) < 0) {
                String s = String.valueOf(datawatcherobject.serializer());

                throw new IllegalArgumentException("Unregistered serializer " + s + " for " + i + "!");
            } else {
                this.itemsById[datawatcherobject.id()] = new DataWatcher.Item<>(datawatcherobject, t0);
                return this;
            }
        }

        public DataWatcher build() {
            for (int i = 0; i < this.itemsById.length; ++i) {
                if (this.itemsById[i] == null) {
                    String s = String.valueOf(this.entity.getClass());

                    throw new IllegalStateException("Entity " + s + " has not defined synched data value " + i);
                }
            }

            return new DataWatcher(this.entity, this.itemsById);
        }
    }
}
