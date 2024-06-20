package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerChanneledLightning extends CriterionTriggerAbstract<CriterionTriggerChanneledLightning.a> {

    public CriterionTriggerChanneledLightning() {}

    @Override
    public Codec<CriterionTriggerChanneledLightning.a> codec() {
        return CriterionTriggerChanneledLightning.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Collection<? extends Entity> collection) {
        List<LootTableInfo> list = (List) collection.stream().map((entity) -> {
            return CriterionConditionEntity.createContext(entityplayer, entity);
        }).collect(Collectors.toList());

        this.trigger(entityplayer, (criteriontriggerchanneledlightning_a) -> {
            return criteriontriggerchanneledlightning_a.matches(list);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerChanneledLightning.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerChanneledLightning.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(CriterionTriggerChanneledLightning.a::victims)).apply(instance, CriterionTriggerChanneledLightning.a::new);
        });

        public static Criterion<CriterionTriggerChanneledLightning.a> channeledLightning(CriterionConditionEntity.a... acriterionconditionentity_a) {
            return CriterionTriggers.CHANNELED_LIGHTNING.createCriterion(new CriterionTriggerChanneledLightning.a(Optional.empty(), CriterionConditionEntity.wrap(acriterionconditionentity_a)));
        }

        public boolean matches(Collection<? extends LootTableInfo> collection) {
            Iterator iterator = this.victims.iterator();

            while (iterator.hasNext()) {
                ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) iterator.next();
                boolean flag = false;
                Iterator iterator1 = collection.iterator();

                while (true) {
                    if (iterator1.hasNext()) {
                        LootTableInfo loottableinfo = (LootTableInfo) iterator1.next();

                        if (!contextawarepredicate.matches(loottableinfo)) {
                            continue;
                        }

                        flag = true;
                    }

                    if (!flag) {
                        return false;
                    }
                    break;
                }
            }

            return true;
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntities(this.victims, ".victims");
        }
    }
}
