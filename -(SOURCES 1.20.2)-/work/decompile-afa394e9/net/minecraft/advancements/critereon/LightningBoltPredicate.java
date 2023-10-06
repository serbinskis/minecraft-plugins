package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.phys.Vec3D;

public record LightningBoltPredicate(CriterionConditionValue.IntegerRange blocksSetOnFire, Optional<CriterionConditionEntity> entityStruck) implements EntitySubPredicate {

    public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionValue.IntegerRange.CODEC, "blocks_set_on_fire", CriterionConditionValue.IntegerRange.ANY).forGetter(LightningBoltPredicate::blocksSetOnFire), ExtraCodecs.strictOptionalField(CriterionConditionEntity.CODEC, "entity_struck").forGetter(LightningBoltPredicate::entityStruck)).apply(instance, LightningBoltPredicate::new);
    });

    public static LightningBoltPredicate blockSetOnFire(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        return new LightningBoltPredicate(criterionconditionvalue_integerrange, Optional.empty());
    }

    @Override
    public EntitySubPredicate.a type() {
        return EntitySubPredicate.b.LIGHTNING;
    }

    @Override
    public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
        if (!(entity instanceof EntityLightning)) {
            return false;
        } else {
            EntityLightning entitylightning = (EntityLightning) entity;

            return this.blocksSetOnFire.matches(entitylightning.getBlocksSetOnFire()) && (this.entityStruck.isEmpty() || entitylightning.getHitEntities().anyMatch((entity1) -> {
                return ((CriterionConditionEntity) this.entityStruck.get()).matches(worldserver, vec3d, entity1);
            }));
        }
    }
}
