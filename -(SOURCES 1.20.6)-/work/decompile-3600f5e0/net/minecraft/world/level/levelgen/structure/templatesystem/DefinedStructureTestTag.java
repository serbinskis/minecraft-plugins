package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class DefinedStructureTestTag extends DefinedStructureRuleTest {

    public static final MapCodec<DefinedStructureTestTag> CODEC = TagKey.codec(Registries.BLOCK).fieldOf("tag").xmap(DefinedStructureTestTag::new, (definedstructuretesttag) -> {
        return definedstructuretesttag.tag;
    });
    private final TagKey<Block> tag;

    public DefinedStructureTestTag(TagKey<Block> tagkey) {
        this.tag = tagkey;
    }

    @Override
    public boolean test(IBlockData iblockdata, RandomSource randomsource) {
        return iblockdata.is(this.tag);
    }

    @Override
    protected DefinedStructureRuleTestType<?> getType() {
        return DefinedStructureRuleTestType.TAG_TEST;
    }
}
