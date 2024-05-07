package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;

public class PosRuleTestTrue extends PosRuleTest {

    public static final MapCodec<PosRuleTestTrue> CODEC = MapCodec.unit(() -> {
        return PosRuleTestTrue.INSTANCE;
    });
    public static final PosRuleTestTrue INSTANCE = new PosRuleTestTrue();

    private PosRuleTestTrue() {}

    @Override
    public boolean test(BlockPosition blockposition, BlockPosition blockposition1, BlockPosition blockposition2, RandomSource randomsource) {
        return true;
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.ALWAYS_TRUE_TEST;
    }
}
