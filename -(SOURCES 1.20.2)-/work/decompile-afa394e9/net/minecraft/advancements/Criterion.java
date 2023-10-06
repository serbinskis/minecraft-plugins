package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import net.minecraft.advancements.critereon.LootDeserializationContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ChatDeserializer;

public record Criterion<T extends CriterionInstance> (CriterionTrigger<T> trigger, T triggerInstance) {

    public static Criterion<?> criterionFromJson(JsonObject jsonobject, LootDeserializationContext lootdeserializationcontext) {
        MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "trigger"));
        CriterionTrigger<?> criteriontrigger = CriterionTriggers.getCriterion(minecraftkey);

        if (criteriontrigger == null) {
            throw new JsonSyntaxException("Invalid criterion trigger: " + minecraftkey);
        } else {
            return criterionFromJson(jsonobject, lootdeserializationcontext, criteriontrigger);
        }
    }

    private static <T extends CriterionInstance> Criterion<T> criterionFromJson(JsonObject jsonobject, LootDeserializationContext lootdeserializationcontext, CriterionTrigger<T> criteriontrigger) {
        T t0 = criteriontrigger.createInstance(ChatDeserializer.getAsJsonObject(jsonobject, "conditions", new JsonObject()), lootdeserializationcontext);

        return new Criterion<>(criteriontrigger, t0);
    }

    public static Map<String, Criterion<?>> criteriaFromJson(JsonObject jsonobject, LootDeserializationContext lootdeserializationcontext) {
        Map<String, Criterion<?>> map = Maps.newHashMap();
        Iterator iterator = jsonobject.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, JsonElement> entry = (Entry) iterator.next();

            map.put((String) entry.getKey(), criterionFromJson(ChatDeserializer.convertToJsonObject((JsonElement) entry.getValue(), "criterion"), lootdeserializationcontext));
        }

        return map;
    }

    public JsonElement serializeToJson() {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("trigger", ((MinecraftKey) Objects.requireNonNull(CriterionTriggers.getId(this.trigger), "Unregistered trigger")).toString());
        JsonObject jsonobject1 = this.triggerInstance.serializeToJson();

        if (jsonobject1.size() != 0) {
            jsonobject.add("conditions", jsonobject1);
        }

        return jsonobject;
    }
}
