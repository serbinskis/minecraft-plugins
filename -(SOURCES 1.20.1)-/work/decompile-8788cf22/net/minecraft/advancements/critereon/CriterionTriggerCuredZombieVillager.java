package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerCuredZombieVillager extends CriterionTriggerAbstract<CriterionTriggerCuredZombieVillager.a> {

    static final MinecraftKey ID = new MinecraftKey("cured_zombie_villager");

    public CriterionTriggerCuredZombieVillager() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerCuredZombieVillager.ID;
    }

    @Override
    public CriterionTriggerCuredZombieVillager.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "zombie", lootdeserializationcontext);
        ContextAwarePredicate contextawarepredicate2 = CriterionConditionEntity.fromJson(jsonobject, "villager", lootdeserializationcontext);

        return new CriterionTriggerCuredZombieVillager.a(contextawarepredicate, contextawarepredicate1, contextawarepredicate2);
    }

    public void trigger(EntityPlayer entityplayer, EntityZombie entityzombie, EntityVillager entityvillager) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityzombie);
        LootTableInfo loottableinfo1 = CriterionConditionEntity.createContext(entityplayer, entityvillager);

        this.trigger(entityplayer, (criteriontriggercuredzombievillager_a) -> {
            return criteriontriggercuredzombievillager_a.matches(loottableinfo, loottableinfo1);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final ContextAwarePredicate zombie;
        private final ContextAwarePredicate villager;

        public a(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, ContextAwarePredicate contextawarepredicate2) {
            super(CriterionTriggerCuredZombieVillager.ID, contextawarepredicate);
            this.zombie = contextawarepredicate1;
            this.villager = contextawarepredicate2;
        }

        public static CriterionTriggerCuredZombieVillager.a curedZombieVillager() {
            return new CriterionTriggerCuredZombieVillager.a(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
        }

        public boolean matches(LootTableInfo loottableinfo, LootTableInfo loottableinfo1) {
            return !this.zombie.matches(loottableinfo) ? false : this.villager.matches(loottableinfo1);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("zombie", this.zombie.toJson(lootserializationcontext));
            jsonobject.add("villager", this.villager.toJson(lootserializationcontext));
            return jsonobject;
        }
    }
}
