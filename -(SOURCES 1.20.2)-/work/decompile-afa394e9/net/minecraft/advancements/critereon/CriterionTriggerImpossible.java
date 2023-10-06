package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.AdvancementDataPlayer;

public class CriterionTriggerImpossible implements CriterionTrigger<CriterionTriggerImpossible.a> {

    public CriterionTriggerImpossible() {}

    @Override
    public void addPlayerListener(AdvancementDataPlayer advancementdataplayer, CriterionTrigger.a<CriterionTriggerImpossible.a> criteriontrigger_a) {}

    @Override
    public void removePlayerListener(AdvancementDataPlayer advancementdataplayer, CriterionTrigger.a<CriterionTriggerImpossible.a> criteriontrigger_a) {}

    @Override
    public void removePlayerListeners(AdvancementDataPlayer advancementdataplayer) {}

    @Override
    public CriterionTriggerImpossible.a createInstance(JsonObject jsonobject, LootDeserializationContext lootdeserializationcontext) {
        return new CriterionTriggerImpossible.a();
    }

    public static class a implements CriterionInstance {

        public a() {}

        @Override
        public JsonObject serializeToJson() {
            return new JsonObject();
        }
    }
}
