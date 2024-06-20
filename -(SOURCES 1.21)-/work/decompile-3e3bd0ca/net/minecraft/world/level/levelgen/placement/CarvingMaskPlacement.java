package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.levelgen.WorldGenStage;

public class CarvingMaskPlacement extends PlacementModifier {

    public static final MapCodec<CarvingMaskPlacement> CODEC = WorldGenStage.Features.CODEC.fieldOf("step").xmap(CarvingMaskPlacement::new, (carvingmaskplacement) -> {
        return carvingmaskplacement.step;
    });
    private final WorldGenStage.Features step;

    private CarvingMaskPlacement(WorldGenStage.Features worldgenstage_features) {
        this.step = worldgenstage_features;
    }

    public static CarvingMaskPlacement forStep(WorldGenStage.Features worldgenstage_features) {
        return new CarvingMaskPlacement(worldgenstage_features);
    }

    @Override
    public Stream<BlockPosition> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPosition blockposition) {
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(blockposition);

        return placementcontext.getCarvingMask(chunkcoordintpair, this.step).stream(chunkcoordintpair);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.CARVING_MASK_PLACEMENT;
    }
}
