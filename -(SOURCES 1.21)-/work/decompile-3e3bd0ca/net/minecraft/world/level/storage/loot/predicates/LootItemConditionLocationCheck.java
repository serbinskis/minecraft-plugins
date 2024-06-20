package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.CriterionConditionLocation;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;

public record LootItemConditionLocationCheck(Optional<CriterionConditionLocation> predicate, BlockPosition offset) implements LootItemCondition {

    private static final MapCodec<BlockPosition> OFFSET_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.INT.optionalFieldOf("offsetX", 0).forGetter(BaseBlockPosition::getX), Codec.INT.optionalFieldOf("offsetY", 0).forGetter(BaseBlockPosition::getY), Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(BaseBlockPosition::getZ)).apply(instance, BlockPosition::new);
    });
    public static final MapCodec<LootItemConditionLocationCheck> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(CriterionConditionLocation.CODEC.optionalFieldOf("predicate").forGetter(LootItemConditionLocationCheck::predicate), LootItemConditionLocationCheck.OFFSET_CODEC.forGetter(LootItemConditionLocationCheck::offset)).apply(instance, LootItemConditionLocationCheck::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.LOCATION_CHECK;
    }

    public boolean test(LootTableInfo loottableinfo) {
        Vec3D vec3d = (Vec3D) loottableinfo.getParamOrNull(LootContextParameters.ORIGIN);

        return vec3d != null && (this.predicate.isEmpty() || ((CriterionConditionLocation) this.predicate.get()).matches(loottableinfo.getLevel(), vec3d.x() + (double) this.offset.getX(), vec3d.y() + (double) this.offset.getY(), vec3d.z() + (double) this.offset.getZ()));
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return Set.of(LootContextParameters.ORIGIN);
    }

    public static LootItemCondition.a checkLocation(CriterionConditionLocation.a criterionconditionlocation_a) {
        return () -> {
            return new LootItemConditionLocationCheck(Optional.of(criterionconditionlocation_a.build()), BlockPosition.ZERO);
        };
    }

    public static LootItemCondition.a checkLocation(CriterionConditionLocation.a criterionconditionlocation_a, BlockPosition blockposition) {
        return () -> {
            return new LootItemConditionLocationCheck(Optional.of(criterionconditionlocation_a.build()), blockposition);
        };
    }
}
