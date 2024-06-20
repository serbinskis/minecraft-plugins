package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface PosRuleTestType<P extends PosRuleTest> {

    PosRuleTestType<PosRuleTestTrue> ALWAYS_TRUE_TEST = register("always_true", PosRuleTestTrue.CODEC);
    PosRuleTestType<PosRuleTestLinear> LINEAR_POS_TEST = register("linear_pos", PosRuleTestLinear.CODEC);
    PosRuleTestType<PosRuleTestAxisAlignedLinear> AXIS_ALIGNED_LINEAR_POS_TEST = register("axis_aligned_linear_pos", PosRuleTestAxisAlignedLinear.CODEC);

    MapCodec<P> codec();

    static <P extends PosRuleTest> PosRuleTestType<P> register(String s, MapCodec<P> mapcodec) {
        return (PosRuleTestType) IRegistry.register(BuiltInRegistries.POS_RULE_TEST, s, () -> {
            return mapcodec;
        });
    }
}
