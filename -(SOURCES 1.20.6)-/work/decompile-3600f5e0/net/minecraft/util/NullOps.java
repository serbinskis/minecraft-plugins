package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NullOps implements DynamicOps<Unit> {

    public static final NullOps INSTANCE = new NullOps();

    private NullOps() {}

    public <U> U convertTo(DynamicOps<U> dynamicops, Unit unit) {
        return dynamicops.empty();
    }

    public Unit empty() {
        return Unit.INSTANCE;
    }

    public Unit emptyMap() {
        return Unit.INSTANCE;
    }

    public Unit emptyList() {
        return Unit.INSTANCE;
    }

    public Unit createNumeric(Number number) {
        return Unit.INSTANCE;
    }

    public Unit createByte(byte b0) {
        return Unit.INSTANCE;
    }

    public Unit createShort(short short0) {
        return Unit.INSTANCE;
    }

    public Unit createInt(int i) {
        return Unit.INSTANCE;
    }

    public Unit createLong(long i) {
        return Unit.INSTANCE;
    }

    public Unit createFloat(float f) {
        return Unit.INSTANCE;
    }

    public Unit createDouble(double d0) {
        return Unit.INSTANCE;
    }

    public Unit createBoolean(boolean flag) {
        return Unit.INSTANCE;
    }

    public Unit createString(String s) {
        return Unit.INSTANCE;
    }

    public DataResult<Number> getNumberValue(Unit unit) {
        return DataResult.error(() -> {
            return "Not a number";
        });
    }

    public DataResult<Boolean> getBooleanValue(Unit unit) {
        return DataResult.error(() -> {
            return "Not a boolean";
        });
    }

    public DataResult<String> getStringValue(Unit unit) {
        return DataResult.error(() -> {
            return "Not a string";
        });
    }

    public DataResult<Unit> mergeToList(Unit unit, Unit unit1) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToList(Unit unit, List<Unit> list) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToMap(Unit unit, Unit unit1, Unit unit2) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToMap(Unit unit, Map<Unit, Unit> map) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToMap(Unit unit, MapLike<Unit> maplike) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Stream<Pair<Unit, Unit>>> getMapValues(Unit unit) {
        return DataResult.error(() -> {
            return "Not a map";
        });
    }

    public DataResult<Consumer<BiConsumer<Unit, Unit>>> getMapEntries(Unit unit) {
        return DataResult.error(() -> {
            return "Not a map";
        });
    }

    public DataResult<MapLike<Unit>> getMap(Unit unit) {
        return DataResult.error(() -> {
            return "Not a map";
        });
    }

    public DataResult<Stream<Unit>> getStream(Unit unit) {
        return DataResult.error(() -> {
            return "Not a list";
        });
    }

    public DataResult<Consumer<Consumer<Unit>>> getList(Unit unit) {
        return DataResult.error(() -> {
            return "Not a list";
        });
    }

    public DataResult<ByteBuffer> getByteBuffer(Unit unit) {
        return DataResult.error(() -> {
            return "Not a byte list";
        });
    }

    public DataResult<IntStream> getIntStream(Unit unit) {
        return DataResult.error(() -> {
            return "Not an int list";
        });
    }

    public DataResult<LongStream> getLongStream(Unit unit) {
        return DataResult.error(() -> {
            return "Not a long list";
        });
    }

    public Unit createMap(Stream<Pair<Unit, Unit>> stream) {
        return Unit.INSTANCE;
    }

    public Unit createMap(Map<Unit, Unit> map) {
        return Unit.INSTANCE;
    }

    public Unit createList(Stream<Unit> stream) {
        return Unit.INSTANCE;
    }

    public Unit createByteList(ByteBuffer bytebuffer) {
        return Unit.INSTANCE;
    }

    public Unit createIntList(IntStream intstream) {
        return Unit.INSTANCE;
    }

    public Unit createLongList(LongStream longstream) {
        return Unit.INSTANCE;
    }

    public Unit remove(Unit unit, String s) {
        return unit;
    }

    public RecordBuilder<Unit> mapBuilder() {
        return new NullOps.a(this);
    }

    public String toString() {
        return "Null";
    }

    private static final class a extends AbstractUniversalBuilder<Unit, Unit> {

        public a(DynamicOps<Unit> dynamicops) {
            super(dynamicops);
        }

        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        protected Unit append(Unit unit, Unit unit1, Unit unit2) {
            return unit2;
        }

        protected DataResult<Unit> build(Unit unit, Unit unit1) {
            return DataResult.success(unit1);
        }
    }
}
