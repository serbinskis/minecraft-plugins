package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public final class PatchedDataComponentMap implements DataComponentMap {

    private final DataComponentMap prototype;
    private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
    private boolean copyOnWrite;

    public PatchedDataComponentMap(DataComponentMap datacomponentmap) {
        this(datacomponentmap, Reference2ObjectMaps.emptyMap(), true);
    }

    private PatchedDataComponentMap(DataComponentMap datacomponentmap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap, boolean flag) {
        this.prototype = datacomponentmap;
        this.patch = reference2objectmap;
        this.copyOnWrite = flag;
    }

    public static PatchedDataComponentMap fromPatch(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch) {
        if (isPatchSanitized(datacomponentmap, datacomponentpatch.map)) {
            return new PatchedDataComponentMap(datacomponentmap, datacomponentpatch.map, true);
        } else {
            PatchedDataComponentMap patcheddatacomponentmap = new PatchedDataComponentMap(datacomponentmap);

            patcheddatacomponentmap.applyPatch(datacomponentpatch);
            return patcheddatacomponentmap;
        }
    }

    private static boolean isPatchSanitized(DataComponentMap datacomponentmap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap) {
        ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(reference2objectmap).iterator();

        Object object;
        Optional optional;

        do {
            if (!objectiterator.hasNext()) {
                return true;
            }

            Entry<DataComponentType<?>, Optional<?>> entry = (Entry) objectiterator.next();

            object = datacomponentmap.get((DataComponentType) entry.getKey());
            optional = (Optional) entry.getValue();
            if (optional.isPresent() && optional.get().equals(object)) {
                return false;
            }
        } while (!optional.isEmpty() || object != null);

        return false;
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> datacomponenttype) {
        Optional<? extends T> optional = (Optional) this.patch.get(datacomponenttype);

        return optional != null ? optional.orElse((Object) null) : this.prototype.get(datacomponenttype);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> datacomponenttype, @Nullable T t0) {
        this.ensureMapOwnership();
        T t1 = this.prototype.get(datacomponenttype);
        Optional optional;

        if (Objects.equals(t0, t1)) {
            optional = (Optional) this.patch.remove(datacomponenttype);
        } else {
            optional = (Optional) this.patch.put(datacomponenttype, Optional.ofNullable(t0));
        }

        return optional != null ? optional.orElse(t1) : t1;
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> datacomponenttype) {
        this.ensureMapOwnership();
        T t0 = this.prototype.get(datacomponenttype);
        Optional optional;

        if (t0 != null) {
            optional = (Optional) this.patch.put(datacomponenttype, Optional.empty());
        } else {
            optional = (Optional) this.patch.remove(datacomponenttype);
        }

        return optional != null ? optional.orElse((Object) null) : t0;
    }

    public void applyPatch(DataComponentPatch datacomponentpatch) {
        this.ensureMapOwnership();
        ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(datacomponentpatch.map).iterator();

        while (objectiterator.hasNext()) {
            Entry<DataComponentType<?>, Optional<?>> entry = (Entry) objectiterator.next();

            this.applyPatch((DataComponentType) entry.getKey(), (Optional) entry.getValue());
        }

    }

    private void applyPatch(DataComponentType<?> datacomponenttype, Optional<?> optional) {
        Object object = this.prototype.get(datacomponenttype);

        if (optional.isPresent()) {
            if (optional.get().equals(object)) {
                this.patch.remove(datacomponenttype);
            } else {
                this.patch.put(datacomponenttype, optional);
            }
        } else if (object != null) {
            this.patch.put(datacomponenttype, Optional.empty());
        } else {
            this.patch.remove(datacomponenttype);
        }

    }

    public void restorePatch(DataComponentPatch datacomponentpatch) {
        this.ensureMapOwnership();
        this.patch.clear();
        this.patch.putAll(datacomponentpatch.map);
    }

    public void setAll(DataComponentMap datacomponentmap) {
        Iterator iterator = datacomponentmap.iterator();

        while (iterator.hasNext()) {
            TypedDataComponent<?> typeddatacomponent = (TypedDataComponent) iterator.next();

            typeddatacomponent.applyTo(this);
        }

    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.patch = new Reference2ObjectArrayMap(this.patch);
            this.copyOnWrite = false;
        }

    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        } else {
            Set<DataComponentType<?>> set = new ReferenceArraySet(this.prototype.keySet());
            ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(this.patch).iterator();

            while (objectiterator.hasNext()) {
                it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> it_unimi_dsi_fastutil_objects_reference2objectmap_entry = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry) objectiterator.next();
                Optional<?> optional = (Optional) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getValue();

                if (optional.isPresent()) {
                    set.add((DataComponentType) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getKey());
                } else {
                    set.remove(it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getKey());
                }
            }

            return set;
        }
    }

    @Override
    public Iterator<TypedDataComponent<?>> iterator() {
        if (this.patch.isEmpty()) {
            return this.prototype.iterator();
        } else {
            List<TypedDataComponent<?>> list = new ArrayList(this.patch.size() + this.prototype.size());
            ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(this.patch).iterator();

            while (objectiterator.hasNext()) {
                it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> it_unimi_dsi_fastutil_objects_reference2objectmap_entry = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry) objectiterator.next();

                if (((Optional) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getValue()).isPresent()) {
                    list.add(TypedDataComponent.createUnchecked((DataComponentType) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getKey(), ((Optional) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getValue()).get()));
                }
            }

            Iterator iterator = this.prototype.iterator();

            while (iterator.hasNext()) {
                TypedDataComponent<?> typeddatacomponent = (TypedDataComponent) iterator.next();

                if (!this.patch.containsKey(typeddatacomponent.type())) {
                    list.add(typeddatacomponent);
                }
            }

            return list.iterator();
        }
    }

    @Override
    public int size() {
        int i = this.prototype.size();
        ObjectIterator objectiterator = Reference2ObjectMaps.fastIterable(this.patch).iterator();

        while (objectiterator.hasNext()) {
            it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> it_unimi_dsi_fastutil_objects_reference2objectmap_entry = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry) objectiterator.next();
            boolean flag = ((Optional) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getValue()).isPresent();
            boolean flag1 = this.prototype.has((DataComponentType) it_unimi_dsi_fastutil_objects_reference2objectmap_entry.getKey());

            if (flag != flag1) {
                i += flag ? 1 : -1;
            }
        }

        return i;
    }

    public DataComponentPatch asPatch() {
        if (this.patch.isEmpty()) {
            return DataComponentPatch.EMPTY;
        } else {
            this.copyOnWrite = true;
            return new DataComponentPatch(this.patch);
        }
    }

    public PatchedDataComponentMap copy() {
        this.copyOnWrite = true;
        return new PatchedDataComponentMap(this.prototype, this.patch, true);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof PatchedDataComponentMap) {
                PatchedDataComponentMap patcheddatacomponentmap = (PatchedDataComponentMap) object;

                if (this.prototype.equals(patcheddatacomponentmap.prototype) && this.patch.equals(patcheddatacomponentmap.patch)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.prototype.hashCode() + this.patch.hashCode() * 31;
    }

    public String toString() {
        Stream stream = this.stream().map(TypedDataComponent::toString);

        return "{" + (String) stream.collect(Collectors.joining(", ")) + "}";
    }
}
