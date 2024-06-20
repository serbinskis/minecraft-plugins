package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.world.level.block.state.IBlockData;

/** @deprecated */
@Deprecated
public class SolidPredicate extends StateTestingPredicate {

    public static final MapCodec<SolidPredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return stateTestingCodec(instance).apply(instance, SolidPredicate::new);
    });

    public SolidPredicate(BaseBlockPosition baseblockposition) {
        super(baseblockposition);
    }

    @Override
    protected boolean test(IBlockData iblockdata) {
        return iblockdata.isSolid();
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.SOLID;
    }
}
