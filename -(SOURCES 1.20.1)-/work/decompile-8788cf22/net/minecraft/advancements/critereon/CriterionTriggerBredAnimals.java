package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class CriterionTriggerBredAnimals extends CriterionTriggerAbstract<CriterionTriggerBredAnimals.a> {

    static final MinecraftKey ID = new MinecraftKey("bred_animals");

    public CriterionTriggerBredAnimals() {}

    @Override
    public MinecraftKey getId() {
        return CriterionTriggerBredAnimals.ID;
    }

    @Override
    public CriterionTriggerBredAnimals.a createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, LootDeserializationContext lootdeserializationcontext) {
        ContextAwarePredicate contextawarepredicate1 = CriterionConditionEntity.fromJson(jsonobject, "parent", lootdeserializationcontext);
        ContextAwarePredicate contextawarepredicate2 = CriterionConditionEntity.fromJson(jsonobject, "partner", lootdeserializationcontext);
        ContextAwarePredicate contextawarepredicate3 = CriterionConditionEntity.fromJson(jsonobject, "child", lootdeserializationcontext);

        return new CriterionTriggerBredAnimals.a(contextawarepredicate, contextawarepredicate1, contextawarepredicate2, contextawarepredicate3);
    }

    public void trigger(EntityPlayer entityplayer, EntityAnimal entityanimal, EntityAnimal entityanimal1, @Nullable EntityAgeable entityageable) {
        LootTableInfo loottableinfo = CriterionConditionEntity.createContext(entityplayer, entityanimal);
        LootTableInfo loottableinfo1 = CriterionConditionEntity.createContext(entityplayer, entityanimal1);
        LootTableInfo loottableinfo2 = entityageable != null ? CriterionConditionEntity.createContext(entityplayer, entityageable) : null;

        this.trigger(entityplayer, (criteriontriggerbredanimals_a) -> {
            return criteriontriggerbredanimals_a.matches(loottableinfo, loottableinfo1, loottableinfo2);
        });
    }

    public static class a extends CriterionInstanceAbstract {

        private final ContextAwarePredicate parent;
        private final ContextAwarePredicate partner;
        private final ContextAwarePredicate child;

        public a(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, ContextAwarePredicate contextawarepredicate2, ContextAwarePredicate contextawarepredicate3) {
            super(CriterionTriggerBredAnimals.ID, contextawarepredicate);
            this.parent = contextawarepredicate1;
            this.partner = contextawarepredicate2;
            this.child = contextawarepredicate3;
        }

        public static CriterionTriggerBredAnimals.a bredAnimals() {
            return new CriterionTriggerBredAnimals.a(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
        }

        public static CriterionTriggerBredAnimals.a bredAnimals(CriterionConditionEntity.a criterionconditionentity_a) {
            return new CriterionTriggerBredAnimals.a(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, CriterionConditionEntity.wrap(criterionconditionentity_a.build()));
        }

        public static CriterionTriggerBredAnimals.a bredAnimals(CriterionConditionEntity criterionconditionentity, CriterionConditionEntity criterionconditionentity1, CriterionConditionEntity criterionconditionentity2) {
            return new CriterionTriggerBredAnimals.a(ContextAwarePredicate.ANY, CriterionConditionEntity.wrap(criterionconditionentity), CriterionConditionEntity.wrap(criterionconditionentity1), CriterionConditionEntity.wrap(criterionconditionentity2));
        }

        public boolean matches(LootTableInfo loottableinfo, LootTableInfo loottableinfo1, @Nullable LootTableInfo loottableinfo2) {
            return this.child != ContextAwarePredicate.ANY && (loottableinfo2 == null || !this.child.matches(loottableinfo2)) ? false : this.parent.matches(loottableinfo) && this.partner.matches(loottableinfo1) || this.parent.matches(loottableinfo1) && this.partner.matches(loottableinfo);
        }

        @Override
        public JsonObject serializeToJson(LootSerializationContext lootserializationcontext) {
            JsonObject jsonobject = super.serializeToJson(lootserializationcontext);

            jsonobject.add("parent", this.parent.toJson(lootserializationcontext));
            jsonobject.add("partner", this.partner.toJson(lootserializationcontext));
            jsonobject.add("child", this.child.toJson(lootserializationcontext));
            return jsonobject;
        }
    }
}
