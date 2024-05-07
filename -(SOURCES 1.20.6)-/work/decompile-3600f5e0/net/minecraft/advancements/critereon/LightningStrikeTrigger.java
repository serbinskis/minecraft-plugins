package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class LightningStrikeTrigger extends CriterionTriggerAbstract<LightningStrikeTrigger.a> {

    public LightningStrikeTrigger() {}

    @Override
    public Codec<LightningStrikeTrigger.a> codec() {
        return LightningStrikeTrigger.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, EntityLightning entitylightning, List<Entity> list) {
        List<LootTableInfo> list1 = (List) list.stream().map((entity) -> {
            return CriterionConditionEntity.createContext(entityplayer, entity);
        }).collect(Collectors.toList());
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entitylightning);

        this.trigger(entityplayer, (lightningstriketrigger_a) -> {
            return lightningstriketrigger_a.matches(loottableinfo, list1);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> lightning, Optional<ContextAwarePredicate> bystander) implements CriterionTriggerAbstract.a {

        public static final Codec<LightningStrikeTrigger.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LightningStrikeTrigger.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("lightning").forGetter(LightningStrikeTrigger.a::lightning), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("bystander").forGetter(LightningStrikeTrigger.a::bystander)).apply(instance, LightningStrikeTrigger.a::new);
        });

        public static Criterion<LightningStrikeTrigger.a> lightningStrike(Optional<CriterionConditionEntity> optional, Optional<CriterionConditionEntity> optional1) {
            return CriterionTriggers.LIGHTNING_STRIKE.createCriterion(new LightningStrikeTrigger.a(Optional.empty(), CriterionConditionEntity.wrap(optional), CriterionConditionEntity.wrap(optional1)));
        }

        public boolean matches(LootTableInfo loottableinfo, List<LootTableInfo> list) {
            if (this.lightning.isPresent() && !((ContextAwarePredicate) this.lightning.get()).matches(loottableinfo)) {
                return false;
            } else {
                if (this.bystander.isPresent()) {
                    Stream stream = list.stream();
                    ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) this.bystander.get();

                    Objects.requireNonNull(contextawarepredicate);
                    if (stream.noneMatch(contextawarepredicate::matches)) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.lightning, ".lightning");
            criterionvalidator.validateEntity(this.bystander, ".bystander");
        }
    }
}
