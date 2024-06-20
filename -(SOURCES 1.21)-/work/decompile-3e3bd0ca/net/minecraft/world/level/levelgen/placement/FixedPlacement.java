package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.RandomSource;

public class FixedPlacement extends PlacementModifier {

    public static final MapCodec<FixedPlacement> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockPosition.CODEC.listOf().fieldOf("positions").forGetter((fixedplacement) -> {
            return fixedplacement.positions;
        })).apply(instance, FixedPlacement::new);
    });
    private final List<BlockPosition> positions;

    public static FixedPlacement of(BlockPosition... ablockposition) {
        return new FixedPlacement(List.of(ablockposition));
    }

    private FixedPlacement(List<BlockPosition> list) {
        this.positions = list;
    }

    @Override
    public Stream<BlockPosition> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPosition blockposition) {
        int i = SectionPosition.blockToSectionCoord(blockposition.getX());
        int j = SectionPosition.blockToSectionCoord(blockposition.getZ());
        boolean flag = false;
        Iterator iterator = this.positions.iterator();

        while (iterator.hasNext()) {
            BlockPosition blockposition1 = (BlockPosition) iterator.next();

            if (isSameChunk(i, j, blockposition1)) {
                flag = true;
                break;
            }
        }

        return !flag ? Stream.empty() : this.positions.stream().filter((blockposition2) -> {
            return isSameChunk(i, j, blockposition2);
        });
    }

    private static boolean isSameChunk(int i, int j, BlockPosition blockposition) {
        return i == SectionPosition.blockToSectionCoord(blockposition.getX()) && j == SectionPosition.blockToSectionCoord(blockposition.getZ());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.FIXED_PLACEMENT;
    }
}
