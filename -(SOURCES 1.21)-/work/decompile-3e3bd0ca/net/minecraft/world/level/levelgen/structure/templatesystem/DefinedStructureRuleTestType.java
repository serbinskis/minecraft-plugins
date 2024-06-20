package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface DefinedStructureRuleTestType<P extends DefinedStructureRuleTest> {

    DefinedStructureRuleTestType<DefinedStructureTestTrue> ALWAYS_TRUE_TEST = register("always_true", DefinedStructureTestTrue.CODEC);
    DefinedStructureRuleTestType<DefinedStructureTestBlock> BLOCK_TEST = register("block_match", DefinedStructureTestBlock.CODEC);
    DefinedStructureRuleTestType<DefinedStructureTestBlockState> BLOCKSTATE_TEST = register("blockstate_match", DefinedStructureTestBlockState.CODEC);
    DefinedStructureRuleTestType<DefinedStructureTestTag> TAG_TEST = register("tag_match", DefinedStructureTestTag.CODEC);
    DefinedStructureRuleTestType<DefinedStructureTestRandomBlock> RANDOM_BLOCK_TEST = register("random_block_match", DefinedStructureTestRandomBlock.CODEC);
    DefinedStructureRuleTestType<DefinedStructureTestRandomBlockState> RANDOM_BLOCKSTATE_TEST = register("random_blockstate_match", DefinedStructureTestRandomBlockState.CODEC);

    MapCodec<P> codec();

    static <P extends DefinedStructureRuleTest> DefinedStructureRuleTestType<P> register(String s, MapCodec<P> mapcodec) {
        return (DefinedStructureRuleTestType) IRegistry.register(BuiltInRegistries.RULE_TEST, s, () -> {
            return mapcodec;
        });
    }
}
