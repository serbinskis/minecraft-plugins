package net.minecraft.util.random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface WeightedEntry {

    Weight getWeight();

    static <T> WeightedEntry.b<T> wrap(T t0, int i) {
        return new WeightedEntry.b<>(t0, Weight.of(i));
    }

    public static record b<T>(T data, Weight weight) implements WeightedEntry {

        @Override
        public Weight getWeight() {
            return this.weight;
        }

        public static <E> Codec<WeightedEntry.b<E>> codec(Codec<E> codec) {
            return RecordCodecBuilder.create((instance) -> {
                return instance.group(codec.fieldOf("data").forGetter(WeightedEntry.b::data), Weight.CODEC.fieldOf("weight").forGetter(WeightedEntry.b::weight)).apply(instance, WeightedEntry.b::new);
            });
        }
    }

    public static class a implements WeightedEntry {

        private final Weight weight;

        public a(int i) {
            this.weight = Weight.of(i);
        }

        public a(Weight weight) {
            this.weight = weight;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }
}
