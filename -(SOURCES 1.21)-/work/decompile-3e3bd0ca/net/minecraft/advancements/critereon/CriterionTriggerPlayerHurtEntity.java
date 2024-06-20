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

public class CriterionTriggerPlayerHurtEntity extends CriterionTriggerAbstract<CriterionTriggerPlayerHurtEntity.a> {

    public CriterionTriggerPlayerHurtEntity() {}

    @Override
    public Codec<CriterionTriggerPlayerHurtEntity.a> codec() {
        return CriterionTriggerPlayerHurtEntity.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Entity entity, DamageSource damagesource, float f, float f1, boolean flag) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entity);

        this.trigger(entityplayer, (criteriontriggerplayerhurtentity_a) -> {
            return criteriontriggerplayerhurtentity_a.matches(entityplayer, loottableinfo, damagesource, f, f1, flag);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionDamage> damage, Optional<ContextAwarePredicate> entity) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerPlayerHurtEntity.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerPlayerHurtEntity.a::player), CriterionConditionDamage.CODEC.optionalFieldOf("damage").forGetter(CriterionTriggerPlayerHurtEntity.a::damage), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(CriterionTriggerPlayerHurtEntity.a::entity)).apply(instance, CriterionTriggerPlayerHurtEntity.a::new);
        });

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity() {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntityWithDamage(Optional<CriterionConditionDamage> optional) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), optional, Optional.empty()));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntityWithDamage(CriterionConditionDamage.a criterionconditiondamage_a) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.of(criterionconditiondamage_a.build()), Optional.empty()));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity(Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.empty(), CriterionConditionEntity.wrap(optional)));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity(Optional<CriterionConditionDamage> optional, Optional<CriterionConditionEntity> optional1) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), optional, CriterionConditionEntity.wrap(optional1)));
        }

        public static Criterion<CriterionTriggerPlayerHurtEntity.a> playerHurtEntity(CriterionConditionDamage.a criterionconditiondamage_a, Optional<CriterionConditionEntity> optional) {
            return CriterionTriggers.PLAYER_HURT_ENTITY.createCriterion(new CriterionTriggerPlayerHurtEntity.a(Optional.empty(), Optional.of(criterionconditiondamage_a.build()), CriterionConditionEntity.wrap(optional)));
        }

        public boolean matches(EntityPlayer entityplayer, LootTableInfo loottableinfo, DamageSource damagesource, float f, float f1, boolean flag) {
            return this.damage.isPresent() && !((CriterionConditionDamage) this.damage.get()).matches(entityplayer, damagesource, f, f1, flag) ? false : !this.entity.isPresent() || ((ContextAwarePredicate) this.entity.get()).matches(loottableinfo);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.entity, ".entity");
        }
    }
}
