package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
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
    public CriterionTriggerCuredZombieVillager.a createInstance(JsonObject jsonobject, Optional<ContextAwarePredicate> optional, LootDeserializationContext lootdeserializationcontext) {
        Optional<ContextAwarePredicate> optional1 = CriterionConditionEntity.fromJson(jsonobject, "zombie", lootdeserializationcontext);
        Optional<ContextAwarePredicate> optional2 = CriterionConditionEntity.fromJson(jsonobject, "villager", lootdeserializationcontext);

        return new CriterionTriggerCuredZombieVillager.a(optional, optional1, optional2);
    }

    public void trigger(EntityPlayer entityplayer, EntityZombie entityzombie, EntityVillager entityvillager) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityzombie);
        LootTableInfo loottableinfo1 = CriterionConditionEntity.createContext(entityplayer, entityvillager);

        this.trigger(entityplayer, (criteriontriggercuredzombievillager_a) -> {
            return criteriontriggercuredzombievillager_a.matches(loottableinfo, loottableinfo1);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final Optional<ContextAwarePredicate> zombie;
        private final Optional<ContextAwarePredicate> villager;

        public a(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional1, Optional<ContextAwarePredicate> optional2) {
            super(optional);
            this.zombie = optional1;
            this.villager = optional2;
        }

        public static Criterion<CriterionTriggerCuredZombieVillager.a> curedZombieVillager() {
            return CriterionTriggers.CURED_ZOMBIE_VILLAGER.createCriterion(new CriterionTriggerCuredZombieVillager.a(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootTableInfo loottableinfo, LootTableInfo loottableinfo1) {
            return this.zombie.isPresent() && !((ContextAwarePredicate) this.zombie.get()).matches(loottableinfo) ? false : !this.villager.isPresent() || ((ContextAwarePredicate) this.villager.get()).matches(loottableinfo1);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject jsonobject = super.serializeToJson();

            this.zombie.ifPresent((contextawarepredicate) -> {
                jsonobject.add("zombie", contextawarepredicate.toJson());
            });
            this.villager.ifPresent((contextawarepredicate) -> {
                jsonobject.add("villager", contextawarepredicate.toJson());
            });
            return jsonobject;
        }
    }
}
