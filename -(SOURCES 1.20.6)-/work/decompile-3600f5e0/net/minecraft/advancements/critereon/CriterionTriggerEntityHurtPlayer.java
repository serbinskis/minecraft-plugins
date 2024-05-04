package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class CriterionTriggerEntityHurtPlayer extends CriterionTriggerAbstract<CriterionTriggerEntityHurtPlayer.a> {

    public CriterionTriggerEntityHurtPlayer() {}

    @Override
    public Codec<CriterionTriggerEntityHurtPlayer.a> codec() {
        return CriterionTriggerEntityHurtPlayer.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, DamageSource damagesource, float f, float f1, boolean flag) {
        this.trigger(entityplayer, (criteriontriggerentityhurtplayer_a) -> {
            return criteriontriggerentityhurtplayer_a.matches(entityplayer, damagesource, f, f1, flag);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionDamage> damage) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerEntityHurtPlayer.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerEntityHurtPlayer.a::player), CriterionConditionDamage.CODEC.optionalFieldOf("damage").forGetter(CriterionTriggerEntityHurtPlayer.a::damage)).apply(instance, CriterionTriggerEntityHurtPlayer.a::new);
        });

        public static Criterion<CriterionTriggerEntityHurtPlayer.a> entityHurtPlayer() {
            return CriterionTriggers.ENTITY_HURT_PLAYER.createCriterion(new CriterionTriggerEntityHurtPlayer.a(Optional.empty(), Optional.empty()));
        }

        public static Criterion<CriterionTriggerEntityHurtPlayer.a> entityHurtPlayer(CriterionConditionDamage criterionconditiondamage) {
            return CriterionTriggers.ENTITY_HURT_PLAYER.createCriterion(new CriterionTriggerEntityHurtPlayer.a(Optional.empty(), Optional.of(criterionconditiondamage)));
        }

        public static Criterion<CriterionTriggerEntityHurtPlayer.a> entityHurtPlayer(CriterionConditionDamage.a criterionconditiondamage_a) {
            return CriterionTriggers.ENTITY_HURT_PLAYER.createCriterion(new CriterionTriggerEntityHurtPlayer.a(Optional.empty(), Optional.of(criterionconditiondamage_a.build())));
        }

        public boolean matches(EntityPlayer entityplayer, DamageSource damagesource, float f, float f1, boolean flag) {
            return !this.damage.isPresent() || ((CriterionConditionDamage) this.damage.get()).matches(entityplayer, damagesource, f, f1, flag);
        }
    }
}
