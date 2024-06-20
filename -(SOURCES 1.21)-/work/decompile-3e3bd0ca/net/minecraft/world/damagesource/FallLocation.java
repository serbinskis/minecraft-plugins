package net.minecraft.world.damagesource;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public record FallLocation(String id) {

    public static final FallLocation GENERIC = new FallLocation("generic");
    public static final FallLocation LADDER = new FallLocation("ladder");
    public static final FallLocation VINES = new FallLocation("vines");
    public static final FallLocation WEEPING_VINES = new FallLocation("weeping_vines");
    public static final FallLocation TWISTING_VINES = new FallLocation("twisting_vines");
    public static final FallLocation SCAFFOLDING = new FallLocation("scaffolding");
    public static final FallLocation OTHER_CLIMBABLE = new FallLocation("other_climbable");
    public static final FallLocation WATER = new FallLocation("water");

    public static FallLocation blockToFallLocation(IBlockData iblockdata) {
        return !iblockdata.is(Blocks.LADDER) && !iblockdata.is(TagsBlock.TRAPDOORS) ? (iblockdata.is(Blocks.VINE) ? FallLocation.VINES : (!iblockdata.is(Blocks.WEEPING_VINES) && !iblockdata.is(Blocks.WEEPING_VINES_PLANT) ? (!iblockdata.is(Blocks.TWISTING_VINES) && !iblockdata.is(Blocks.TWISTING_VINES_PLANT) ? (iblockdata.is(Blocks.SCAFFOLDING) ? FallLocation.SCAFFOLDING : FallLocation.OTHER_CLIMBABLE) : FallLocation.TWISTING_VINES) : FallLocation.WEEPING_VINES)) : FallLocation.LADDER;
    }

    @Nullable
    public static FallLocation getCurrentFallLocation(EntityLiving entityliving) {
        Optional<BlockPosition> optional = entityliving.getLastClimbablePos();

        if (optional.isPresent()) {
            IBlockData iblockdata = entityliving.level().getBlockState((BlockPosition) optional.get());

            return blockToFallLocation(iblockdata);
        } else {
            return entityliving.isInWater() ? FallLocation.WATER : null;
        }
    }

    public String languageKey() {
        return "death.fell.accident." + this.id;
    }
}
