package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.phys.Vec3D;

public record SlimePredicate(CriterionConditionValue.IntegerRange size) implements EntitySubPredicate {

    public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("size", CriterionConditionValue.IntegerRange.ANY).forGetter(SlimePredicate::size)).apply(instance, SlimePredicate::new);
    });

    public static SlimePredicate sized(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        return new SlimePredicate(criterionconditionvalue_integerrange);
    }

    @Override
    public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
        if (entity instanceof EntitySlime entityslime) {
            return this.size.matches(entityslime.getSize());
        } else {
            return false;
        }
    }

    @Override
    public MapCodec<SlimePredicate> codec() {
        return EntitySubPredicates.SLIME;
    }
}
