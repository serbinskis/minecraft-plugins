package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerCuredZombieVillager extends CriterionTriggerAbstract<CriterionTriggerCuredZombieVillager.a> {

    public CriterionTriggerCuredZombieVillager() {}

    @Override
    public Codec<CriterionTriggerCuredZombieVillager.a> codec() {
        return CriterionTriggerCuredZombieVillager.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, EntityZombie entityzombie, EntityVillager entityvillager) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityzombie);
        LootTableInfo loottableinfo1 = CriterionConditionEntity.createContext(entityplayer, entityvillager);

        this.trigger(entityplayer, (criteriontriggercuredzombievillager_a) -> {
            return criteriontriggercuredzombievillager_a.matches(loottableinfo, loottableinfo1);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> zombie, Optional<ContextAwarePredicate> villager) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerCuredZombieVillager.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CriterionTriggerCuredZombieVillager.a::player), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("zombie").forGetter(CriterionTriggerCuredZombieVillager.a::zombie), CriterionConditionEntity.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(CriterionTriggerCuredZombieVillager.a::villager)).apply(instance, CriterionTriggerCuredZombieVillager.a::new);
        });

        public static Criterion<CriterionTriggerCuredZombieVillager.a> curedZombieVillager() {
            return CriterionTriggers.CURED_ZOMBIE_VILLAGER.createCriterion(new CriterionTriggerCuredZombieVillager.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootTableInfo loottableinfo, LootTableInfo loottableinfo1) {
            return this.zombie.isPresent() && !((ContextAwarePredicate) this.zombie.get()).matches(loottableinfo) ? false : !this.villager.isPresent() || ((ContextAwarePredicate) this.villager.get()).matches(loottableinfo1);
        }

        @Override
        public void validate(CriterionValidator criterionvalidator) {
            CriterionTriggerAbstract.a.super.validate(criterionvalidator);
            criterionvalidator.validateEntity(this.zombie, ".zombie");
            criterionvalidator.validateEntity(this.villager, ".villager");
        }
    }
}
