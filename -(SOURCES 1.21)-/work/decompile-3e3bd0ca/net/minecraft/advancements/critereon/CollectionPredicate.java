package net.minecraft.advancements.critereon;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;

public record CollectionPredicate<T, P extends Predicate<T>>(Optional<CollectionContentsPredicate<T, P>> contains, Optional<CollectionCountsPredicate<T, P>> counts, Optional<CriterionConditionValue.IntegerRange> size) implements Predicate<Iterable<T>> {

    public static <T, P extends Predicate<T>> Codec<CollectionPredicate<T, P>> codec(Codec<P> codec) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(CollectionContentsPredicate.codec(codec).optionalFieldOf("contains").forGetter(CollectionPredicate::contains), CollectionCountsPredicate.codec(codec).optionalFieldOf("count").forGetter(CollectionPredicate::counts), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("size").forGetter(CollectionPredicate::size)).apply(instance, CollectionPredicate::new);
        });
    }

    public boolean test(Iterable<T> iterable) {
        return this.contains.isPresent() && !((CollectionContentsPredicate) this.contains.get()).test(iterable) ? false : (this.counts.isPresent() && !((CollectionCountsPredicate) this.counts.get()).test(iterable) ? false : !this.size.isPresent() || ((CriterionConditionValue.IntegerRange) this.size.get()).matches(Iterables.size(iterable)));
    }
}
