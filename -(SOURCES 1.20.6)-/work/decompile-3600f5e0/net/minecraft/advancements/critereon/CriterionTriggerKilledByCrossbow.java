package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerKilledByCrossbow extends CriterionTriggerAbstract<CriterionTriggerKilledByCrossbow.a> {

    public CriterionTriggerKilledByCrossbow() {}

    @Override
    public Codec<CriterionTriggerKilledByCrossbow.a> codec() {
        return CriterionTriggerKilledByCrossbow.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, Collection<Entity> collection) {
        List<LootTableInfo> list = Lists.newArrayList();
        Set<EntityTypes<?>> set = Sets.newHashSet();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            set.add(entity.getType());
            list.add(CriterionConditionEntity.createContext(entityplayer, entity));
        }

        this.trigger(entityplayer, (criteriontriggerkilledbycrossbow_a) -> {
            return criteriontriggerkilledbycrossbow_a.matches(list, set.size());
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, CriterionConditionValue.IntegerRange uniqueEntityTypes) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerKilledByCrossbow.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerKilledByCrossbow.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(CriterionTriggerKilledByCrossbow.a::victims), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("unique_entity_types", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerKilledByCrossbow.a::uniqueEntityTypes)).apply(instance, CriterionTriggerKilledByCrossbow.a::new);
        });

        public static Criterion<CriterionTriggerKilledByCrossbow.a> crossbowKilled(CriterionConditionEntity.a... acriterionconditionentity_a) {
            return CriterionTriggers.KILLED_BY_CROSSBOW.createCriterion(new CriterionTriggerKilledByCrossbow.a(Optional.empty(), CriterionConditionEntity.wrap(acriterionconditionentity_a), CriterionConditionValue.IntegerRange.ANY));
        }

        public static Criterion<CriterionTriggerKilledByCrossbow.a> crossbowKilled(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.KILLED_BY_CROSSBOW.createCriterion(new CriterionTriggerKilledByCrossbow.a(Optional.empty(), List.of(), criterionconditionvalue_integerrange));
        }

        public boolean matches(Collection<LootTableInfo> collection, int i) {
            if (!this.victims.isEmpty()) {
                List<LootTableInfo> list = Lists.newArrayList(collection);
                Iterator iterator = this.victims.iterator();

                while (iterator.hasNext()) {
                    ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) iterator.next();
                    boolean flag = false;
                    Iterator<LootTableInfo> iterator1 = list.iterator();

                    while (iterator1.hasNext()) {
                        LootTableInfo loottableinfo = (LootTableInfo) iterator1.next();

                        if (contextawarepredicate.matches(loottableinfo)) {
                            iterator1.remove();
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }
            }

            return this.uniqueEntityTypes.matches(i);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntities(this.victims, ".victims");
        }
    }
}
