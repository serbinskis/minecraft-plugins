package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.GeneratorAccessSeed;

class NotPredicate implements BlockPredicate {

    public static final MapCodec<NotPredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter((notpredicate) -> {
            return notpredicate.predicate;
        })).apply(instance, NotPredicate::new);
    });
    private final BlockPredicate predicate;

    public NotPredicate(BlockPredicate blockpredicate) {
        this.predicate = blockpredicate;
    }

    public boolean test(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        return !this.predicate.test(generatoraccessseed, blockposition);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.NOT;
    }
}
