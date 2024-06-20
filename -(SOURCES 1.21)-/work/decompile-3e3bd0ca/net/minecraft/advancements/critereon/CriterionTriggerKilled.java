package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerKilled extends CriterionTriggerAbstract<CriterionTriggerKilled.a> {

    public CriterionTriggerKilled() {}

    @Override
    public Codec<CriterionTriggerKilled.a> codec() {
        return CriterionTriggerKilled.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, DamageSource damagesource) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerkilled_a) -> {
            return criteriontriggerkilled_a.matches(entityplayer, loottableinfo, damagesource);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate, Optional<CriterionConditionDamageSource> killingBlow) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerKilled.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerKilled.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(CriterionTriggerKilled.a::entityPredicate), CriterionConditionDamageSource.CODEC.optionalFieldOf("killing_blow").forGetter(CriterionTriggerKilled.a::killingBlow)).apply(instance, CriterionTriggerKilled.a::new);
        });

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity() {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(Optional<CriterionConditionEntity> optional, Optional<CriterionConditionDamageSource> optional1) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), optional1));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(CriterionConditionEntity.a criterionconditionentity_a, Optional<CriterionConditionDamageSource> optional) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), optional));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(Optional<CriterionConditionEntity> optional, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntity(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.PLAYER_KILLED_ENTITY.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public static Criterion<CriterionTriggerKilled.a> playerKilledEntityNearSculkCatalyst() {
            return CriterionTriggers.KILL_MOB_NEAR_SCULK_CATALYST.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(CriterionConditionEntity.a criterionconditionentity_a) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer() {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(Optional<CriterionConditionEntity> optional, Optional<CriterionConditionDamageSource> optional1) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), optional1));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(CriterionConditionEntity.a criterionconditionentity_a, Optional<CriterionConditionDamageSource> optional) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), optional));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(Optional<CriterionConditionEntity> optional, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), CriterionConditionEntity.wrap(optional), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public static Criterion<CriterionTriggerKilled.a> entityKilledPlayer(CriterionConditionEntity.a criterionconditionentity_a, CriterionConditionDamageSource.a criterionconditiondamagesource_a) {
            return CriterionTriggers.ENTITY_KILLED_PLAYER.createCriterion(new CriterionTriggerKilled.a(Optional.empty(), Optional.of(CriterionConditionEntity.wrap(criterionconditionentity_a)), Optional.of(criterionconditiondamagesource_a.build())));
        }

        public boolean matches(EntityPlayer entityplayer, LootTableInfo loottableinfo, DamageSource damagesource) {
            return this.killingBlow.isPresent() && !((CriterionConditionDamageSource) this.killingBlow.get()).matches(entityplayer, damagesource) ? false : this.entityPredicate.isEmpty() || ((ContextAwarePredicate) this.entityPredicate.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.entityPredicate, ".entity");
        }
    }
}
