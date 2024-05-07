package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.phys.Vec3D;

public class FallAfterExplosionTrigger extends CriterionTriggerAbstract<FallAfterExplosionTrigger.a> {

    public FallAfterExplosionTrigger() {}

    @Override
    public Codec<FallAfterExplosionTrigger.a> codec() {
        return FallAfterExplosionTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Vec3D vec3d, @Nullable Entity entity) {
        Vec3D vec3d1 = entityplayer.position();
        LootTableInfo loottableinfo = entity != null ? CriterionConditionEntity.createContext(entityplayer, entity) : null;

        this.trigger(entityplayer, (fallafterexplosiontrigger_a) -> {
            return fallafterexplosiontrigger_a.matches(entityplayer.serverLevel(), vec3d, vec3d1, loottableinfo);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionLocation> startPosition, Optional<CriterionConditionDistance> distance, Optional<ContextAwarePredicate> cause) implements CriterionTriggerAbstract.a {

        public static final Codec<FallAfterExplosionTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(FallAfterExplosionTrigger.a::player), CriterionConditionLocation.CODEC.optionalFieldOf("start_position").forGetter(FallAfterExplosionTrigger.a::startPosition), CriterionConditionDistance.CODEC.optionalFieldOf("distance").forGetter(FallAfterExplosionTrigger.a::distance), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("cause").forGetter(FallAfterExplosionTrigger.a::cause)).apply(instance, FallAfterExplosionTrigger.a::new);
        });

        public static Criterion<FallAfterExplosionTrigger.a> fallAfterExplosion(CriterionConditionDistance criterionconditiondistance, CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.FALL_AFTER_EXPLOSION.createCriterion(new FallAfterExplosionTrigger.a(Optional.empty(), Optional.empty(), Optional.of(criterionconditiondistance), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a))));
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.cause(), ".cause");
        }

        public boolean matches(WorldServer worldserver, Vec3D vec3d, Vec3D vec3d1, @Nullable LootTableInfo loottableinfo) {
            return this.startPosition.isPresent() && !((CriterionConditionLocation) this.startPosition.get()).matches(worldserver, vec3d.x, vec3d.y, vec3d.z) ? false : (this.distance.isPresent() && !((CriterionConditionDistance) this.distance.get()).matches(vec3d.x, vec3d.y, vec3d.z, vec3d1.x, vec3d1.y, vec3d1.z) ? false : !this.cause.isPresent() || loottableinfo != null && ((ContextAwarePredicate) this.cause.get()).matches(loottableinfo));
        }
    }
}
