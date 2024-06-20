package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.phys.shapes.VoxelShapes;

record UnobstructedPredicate(BaseBlockPosition offset) implements BlockPredicate {

    public static MapCodec<UnobstructedPredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BaseBlockPosition.CODEC.optionalFieldOf("offset", BaseBlockPosition.ZERO).forGetter(UnobstructedPredicate::offset)).apply(instance, UnobstructedPredicate::new);
    });

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.UNOBSTRUCTED;
    }

    public boolean test(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        return generatoraccessseed.isUnobstructed((Entity) null, VoxelShapes.block().move((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ()));
    }
}
