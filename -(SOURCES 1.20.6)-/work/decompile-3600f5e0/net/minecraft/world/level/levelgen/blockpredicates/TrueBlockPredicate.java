package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.GeneratorAccessSeed;

class TrueBlockPredicate implements BlockPredicate {

    public static TrueBlockPredicate INSTANCE = new TrueBlockPredicate();
    public static final MapCodec<TrueBlockPredicate> CODEC = MapCodec.unit(() -> {
        return TrueBlockPredicate.INSTANCE;
    });

    private TrueBlockPredicate() {}

    public boolean test(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        return true;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.TRUE;
    }
}
