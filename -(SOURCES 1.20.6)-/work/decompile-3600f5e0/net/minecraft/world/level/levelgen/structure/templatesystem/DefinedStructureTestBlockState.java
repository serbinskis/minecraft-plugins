package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.IBlockData;

public class DefinedStructureTestBlockState extends DefinedStructureRuleTest {

    public static final MapCodec<DefinedStructureTestBlockState> CODEC = IBlockData.CODEC.fieldOf("block_state").xmap(DefinedStructureTestBlockState::new, (definedstructuretestblockstate) -> {
        return definedstructuretestblockstate.blockState;
    });
    private final IBlockData blockState;

    public DefinedStructureTestBlockState(IBlockData iblockdata) {
        this.blockState = iblockdata;
    }

    @Override
    public boolean test(IBlockData iblockdata, RandomSource randomsource) {
        return iblockdata == this.blockState;
    }

    @Override
    protected DefinedStructureRuleTestType<?> getType() {
        return DefinedStructureRuleTestType.BLOCKSTATE_TEST;
    }
}
