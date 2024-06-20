package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.Passthrough;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;

public class DefinedStructureProcessorPredicates {

    public static final Passthrough DEFAULT_BLOCK_ENTITY_MODIFIER = Passthrough.INSTANCE;
    public static final Codec<DefinedStructureProcessorPredicates> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(DefinedStructureRuleTest.CODEC.fieldOf("input_predicate").forGetter((definedstructureprocessorpredicates) -> {
            return definedstructureprocessorpredicates.inputPredicate;
        }), DefinedStructureRuleTest.CODEC.fieldOf("location_predicate").forGetter((definedstructureprocessorpredicates) -> {
            return definedstructureprocessorpredicates.locPredicate;
        }), PosRuleTest.CODEC.lenientOptionalFieldOf("position_predicate", PosRuleTestTrue.INSTANCE).forGetter((definedstructureprocessorpredicates) -> {
            return definedstructureprocessorpredicates.posPredicate;
        }), IBlockData.CODEC.fieldOf("output_state").forGetter((definedstructureprocessorpredicates) -> {
            return definedstructureprocessorpredicates.outputState;
        }), RuleBlockEntityModifier.CODEC.lenientOptionalFieldOf("block_entity_modifier", DefinedStructureProcessorPredicates.DEFAULT_BLOCK_ENTITY_MODIFIER).forGetter((definedstructureprocessorpredicates) -> {
            return definedstructureprocessorpredicates.blockEntityModifier;
        })).apply(instance, DefinedStructureProcessorPredicates::new);
    });
    private final DefinedStructureRuleTest inputPredicate;
    private final DefinedStructureRuleTest locPredicate;
    private final PosRuleTest posPredicate;
    private final IBlockData outputState;
    private final RuleBlockEntityModifier blockEntityModifier;

    public DefinedStructureProcessorPredicates(DefinedStructureRuleTest definedstructureruletest, DefinedStructureRuleTest definedstructureruletest1, IBlockData iblockdata) {
        this(definedstructureruletest, definedstructureruletest1, PosRuleTestTrue.INSTANCE, iblockdata);
    }

    public DefinedStructureProcessorPredicates(DefinedStructureRuleTest definedstructureruletest, DefinedStructureRuleTest definedstructureruletest1, PosRuleTest posruletest, IBlockData iblockdata) {
        this(definedstructureruletest, definedstructureruletest1, posruletest, iblockdata, DefinedStructureProcessorPredicates.DEFAULT_BLOCK_ENTITY_MODIFIER);
    }

    public DefinedStructureProcessorPredicates(DefinedStructureRuleTest definedstructureruletest, DefinedStructureRuleTest definedstructureruletest1, PosRuleTest posruletest, IBlockData iblockdata, RuleBlockEntityModifier ruleblockentitymodifier) {
        this.inputPredicate = definedstructureruletest;
        this.locPredicate = definedstructureruletest1;
        this.posPredicate = posruletest;
        this.outputState = iblockdata;
        this.blockEntityModifier = ruleblockentitymodifier;
    }

    public boolean test(IBlockData iblockdata, IBlockData iblockdata1, BlockPosition blockposition, BlockPosition blockposition1, BlockPosition blockposition2, RandomSource randomsource) {
        return this.inputPredicate.test(iblockdata, randomsource) && this.locPredicate.test(iblockdata1, randomsource) && this.posPredicate.test(blockposition, blockposition1, blockposition2, randomsource);
    }

    public IBlockData getOutputState() {
        return this.outputState;
    }

    @Nullable
    public NBTTagCompound getOutputTag(RandomSource randomsource, @Nullable NBTTagCompound nbttagcompound) {
        return this.blockEntityModifier.apply(randomsource, nbttagcompound);
    }
}
