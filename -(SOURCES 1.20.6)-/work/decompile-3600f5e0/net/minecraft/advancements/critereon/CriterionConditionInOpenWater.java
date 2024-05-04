package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.phys.Vec3D;

public record CriterionConditionInOpenWater(Optional<Boolean> inOpenWater) implements EntitySubPredicate {

    public static final CriterionConditionInOpenWater ANY = new CriterionConditionInOpenWater(Optional.empty());
    public static final MapCodec<CriterionConditionInOpenWater> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("in_open_water").forGetter(CriterionConditionInOpenWater::inOpenWater)).apply(instance, CriterionConditionInOpenWater::new);
    });

    public static CriterionConditionInOpenWater inOpenWater(boolean flag) {
        return new CriterionConditionInOpenWater(Optional.of(flag));
    }

    @Override
    public MapCodec<CriterionConditionInOpenWater> codec() {
        return EntitySubPredicates.FISHING_HOOK;
    }

    @Override
    public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
        if (this.inOpenWater.isEmpty()) {
            return true;
        } else if (entity instanceof EntityFishingHook) {
            EntityFishingHook entityfishinghook = (EntityFishingHook) entity;

            return (Boolean) this.inOpenWater.get() == entityfishinghook.isOpenWaterFishing();
        } else {
            return false;
        }
    }
}
