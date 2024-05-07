package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public enum DataFixTypes {

    LEVEL(DataConverterTypes.LEVEL), PLAYER(DataConverterTypes.PLAYER), CHUNK(DataConverterTypes.CHUNK), HOTBAR(DataConverterTypes.HOTBAR), OPTIONS(DataConverterTypes.OPTIONS), STRUCTURE(DataConverterTypes.STRUCTURE), STATS(DataConverterTypes.STATS), SAVED_DATA_COMMAND_STORAGE(DataConverterTypes.SAVED_DATA_COMMAND_STORAGE), SAVED_DATA_FORCED_CHUNKS(DataConverterTypes.SAVED_DATA_FORCED_CHUNKS), SAVED_DATA_MAP_DATA(DataConverterTypes.SAVED_DATA_MAP_DATA), SAVED_DATA_MAP_INDEX(DataConverterTypes.SAVED_DATA_MAP_INDEX), SAVED_DATA_RAIDS(DataConverterTypes.SAVED_DATA_RAIDS), SAVED_DATA_RANDOM_SEQUENCES(DataConverterTypes.SAVED_DATA_RANDOM_SEQUENCES), SAVED_DATA_SCOREBOARD(DataConverterTypes.SAVED_DATA_SCOREBOARD), SAVED_DATA_STRUCTURE_FEATURE_INDICES(DataConverterTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES), ADVANCEMENTS(DataConverterTypes.ADVANCEMENTS), POI_CHUNK(DataConverterTypes.POI_CHUNK), WORLD_GEN_SETTINGS(DataConverterTypes.WORLD_GEN_SETTINGS), ENTITY_CHUNK(DataConverterTypes.ENTITY_CHUNK);

    public static final Set<TypeReference> TYPES_FOR_LEVEL_LIST = Set.of(DataFixTypes.LEVEL.type);
    private final TypeReference type;

    private DataFixTypes(final TypeReference typereference) {
        this.type = typereference;
    }

    static int currentVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    public <A> Codec<A> wrapCodec(final Codec<A> codec, final DataFixer datafixer, final int i) {
        return new Codec<A>() {
            public <T> DataResult<T> encode(A a0, DynamicOps<T> dynamicops, T t0) {
                return codec.encode(a0, dynamicops, t0).flatMap((object) -> {
                    return dynamicops.mergeToMap(object, dynamicops.createString("DataVersion"), dynamicops.createInt(DataFixTypes.currentVersion()));
                });
            }

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicops, T t0) {
                DataResult dataresult = dynamicops.get(t0, "DataVersion");

                Objects.requireNonNull(dynamicops);
                int j = (Integer) dataresult.flatMap(dynamicops::getNumberValue).map(Number::intValue).result().orElse(i);
                Dynamic<T> dynamic = new Dynamic(dynamicops, dynamicops.remove(t0, "DataVersion"));
                Dynamic<T> dynamic1 = DataFixTypes.this.updateToCurrentVersion(datafixer, dynamic, j);

                return codec.decode(dynamic1);
            }
        };
    }

    public <T> Dynamic<T> update(DataFixer datafixer, Dynamic<T> dynamic, int i, int j) {
        return datafixer.update(this.type, dynamic, i, j);
    }

    public <T> Dynamic<T> updateToCurrentVersion(DataFixer datafixer, Dynamic<T> dynamic, int i) {
        return this.update(datafixer, dynamic, i, currentVersion());
    }

    public NBTTagCompound update(DataFixer datafixer, NBTTagCompound nbttagcompound, int i, int j) {
        return (NBTTagCompound) this.update(datafixer, new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound), i, j).getValue();
    }

    public NBTTagCompound updateToCurrentVersion(DataFixer datafixer, NBTTagCompound nbttagcompound, int i) {
        return this.update(datafixer, nbttagcompound, i, currentVersion());
    }
}
