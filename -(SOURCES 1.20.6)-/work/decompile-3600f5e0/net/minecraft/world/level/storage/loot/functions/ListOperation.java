package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;
import org.slf4j.Logger;

public interface ListOperation {

    MapCodec<ListOperation> UNLIMITED_CODEC = codec(Integer.MAX_VALUE);

    static MapCodec<ListOperation> codec(int i) {
        return ListOperation.f.CODEC.dispatchMap("mode", ListOperation::mode, (listoperation_f) -> {
            return listoperation_f.mapCodec;
        }).validate((listoperation) -> {
            if (listoperation instanceof ListOperation.d listoperation_d) {
                if (listoperation_d.size().isPresent()) {
                    int j = (Integer) listoperation_d.size().get();

                    if (j > i) {
                        return DataResult.error(() -> {
                            return "Size value too large: " + j + ", max size is " + i;
                        });
                    }
                }
            }

            return DataResult.success(listoperation);
        });
    }

    ListOperation.f mode();

    default <T> List<T> apply(List<T> list, List<T> list1) {
        return this.apply(list, list1, Integer.MAX_VALUE);
    }

    <T> List<T> apply(List<T> list, List<T> list1, int i);

    public static enum f implements INamable {

        REPLACE_ALL("replace_all", ListOperation.c.MAP_CODEC), REPLACE_SECTION("replace_section", ListOperation.d.MAP_CODEC), INSERT("insert", ListOperation.b.MAP_CODEC), APPEND("append", ListOperation.a.MAP_CODEC);

        public static final Codec<ListOperation.f> CODEC = INamable.fromEnum(ListOperation.f::values);
        private final String id;
        final MapCodec<? extends ListOperation> mapCodec;

        private f(final String s, final MapCodec mapcodec) {
            this.id = s;
            this.mapCodec = mapcodec;
        }

        public MapCodec<? extends ListOperation> mapCodec() {
            return this.mapCodec;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static record d(int offset, Optional<Integer> size) implements ListOperation {

        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ListOperation.d> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.d::offset), ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("size").forGetter(ListOperation.d::size)).apply(instance, ListOperation.d::new);
        });

        public d(int i) {
            this(i, Optional.empty());
        }

        @Override
        public ListOperation.f mode() {
            return ListOperation.f.REPLACE_SECTION;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list1, int i) {
            int j = list.size();

            if (this.offset > j) {
                ListOperation.d.LOGGER.error("Cannot replace when offset is out of bounds");
                return list;
            } else {
                Builder<T> builder = ImmutableList.builder();

                builder.addAll(list.subList(0, this.offset));
                builder.addAll(list1);
                int k = this.offset + (Integer) this.size.orElse(list1.size());

                if (k < j) {
                    builder.addAll(list.subList(k, j));
                }

                List<T> list2 = builder.build();

                if (list2.size() > i) {
                    ListOperation.d.LOGGER.error("Contents overflow in section replacement");
                    return list;
                } else {
                    return list2;
                }
            }
        }
    }

    public static record e<T>(List<T> value, ListOperation operation) {

        public static <T> Codec<ListOperation.e<T>> codec(Codec<T> codec, int i) {
            return RecordCodecBuilder.create((instance) -> {
                return instance.group(codec.sizeLimitedListOf(i).fieldOf("values").forGetter((listoperation_e) -> {
                    return listoperation_e.value;
                }), ListOperation.codec(i).forGetter((listoperation_e) -> {
                    return listoperation_e.operation;
                })).apply(instance, ListOperation.e::new);
            });
        }

        public List<T> apply(List<T> list) {
            return this.operation.apply(list, this.value);
        }
    }

    public static class a implements ListOperation {

        private static final Logger LOGGER = LogUtils.getLogger();
        public static final ListOperation.a INSTANCE = new ListOperation.a();
        public static final MapCodec<ListOperation.a> MAP_CODEC = MapCodec.unit(() -> {
            return ListOperation.a.INSTANCE;
        });

        private a() {}

        @Override
        public ListOperation.f mode() {
            return ListOperation.f.APPEND;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list1, int i) {
            if (list.size() + list1.size() > i) {
                ListOperation.a.LOGGER.error("Contents overflow in section append");
                return list;
            } else {
                return Stream.concat(list.stream(), list1.stream()).toList();
            }
        }
    }

    public static record b(int offset) implements ListOperation {

        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ListOperation.b> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.b::offset)).apply(instance, ListOperation.b::new);
        });

        @Override
        public ListOperation.f mode() {
            return ListOperation.f.INSERT;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list1, int i) {
            int j = list.size();

            if (this.offset > j) {
                ListOperation.b.LOGGER.error("Cannot insert when offset is out of bounds");
                return list;
            } else if (j + list1.size() > i) {
                ListOperation.b.LOGGER.error("Contents overflow in section insertion");
                return list;
            } else {
                Builder<T> builder = ImmutableList.builder();

                builder.addAll(list.subList(0, this.offset));
                builder.addAll(list1);
                builder.addAll(list.subList(this.offset, j));
                return builder.build();
            }
        }
    }

    public static class c implements ListOperation {

        public static final ListOperation.c INSTANCE = new ListOperation.c();
        public static final MapCodec<ListOperation.c> MAP_CODEC = MapCodec.unit(() -> {
            return ListOperation.c.INSTANCE;
        });

        private c() {}

        @Override
        public ListOperation.f mode() {
            return ListOperation.f.REPLACE_ALL;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list1, int i) {
            return list1;
        }
    }
}
