package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface StructurePlacementType<SP extends StructurePlacement> {

    StructurePlacementType<RandomSpreadStructurePlacement> RANDOM_SPREAD = register("random_spread", RandomSpreadStructurePlacement.CODEC);
    StructurePlacementType<ConcentricRingsStructurePlacement> CONCENTRIC_RINGS = register("concentric_rings", ConcentricRingsStructurePlacement.CODEC);

    MapCodec<SP> codec();

    private static <SP extends StructurePlacement> StructurePlacementType<SP> register(String s, MapCodec<SP> mapcodec) {
        return (StructurePlacementType) IRegistry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, s, () -> {
            return mapcodec;
        });
    }
}
