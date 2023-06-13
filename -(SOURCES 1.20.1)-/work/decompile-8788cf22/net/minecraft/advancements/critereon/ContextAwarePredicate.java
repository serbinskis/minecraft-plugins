package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {

    public static final ContextAwarePredicate ANY = new ContextAwarePredicate(new LootItemCondition[0]);
    private final LootItemCondition[] conditions;
    private final Predicate<LootTableInfo> compositePredicates;

    ContextAwarePredicate(LootItemCondition[] alootitemcondition) {
        this.conditions = alootitemcondition;
        this.compositePredicates = LootItemConditions.andConditions(alootitemcondition);
    }

    public static ContextAwarePredicate create(LootItemCondition... alootitemcondition) {
        return new ContextAwarePredicate(alootitemcondition);
    }

    @Nullable
    public static ContextAwarePredicate fromElement(String s, LootDeserializationContext lootdeserializationcontext, @Nullable JsonElement jsonelement, LootContextParameterSet lootcontextparameterset) {
        if (jsonelement != null && jsonelement.isJsonArray()) {
            LootItemCondition[] alootitemcondition = lootdeserializationcontext.deserializeConditions(jsonelement.getAsJsonArray(), lootdeserializationcontext.getAdvancementId() + "/" + s, lootcontextparameterset);

            return new ContextAwarePredicate(alootitemcondition);
        } else {
            return null;
        }
    }

    public boolean matches(LootTableInfo loottableinfo) {
        return this.compositePredicates.test(loottableinfo);
    }

    public JsonElement toJson(LootSerializationContext lootserializationcontext) {
        return (JsonElement) (this.conditions.length == 0 ? JsonNull.INSTANCE : lootserializationcontext.serializeConditions(this.conditions));
    }

    public static JsonElement toJson(ContextAwarePredicate[] acontextawarepredicate, LootSerializationContext lootserializationcontext) {
        if (acontextawarepredicate.length == 0) {
            return JsonNull.INSTANCE;
        } else {
            JsonArray jsonarray = new JsonArray();
            ContextAwarePredicate[] acontextawarepredicate1 = acontextawarepredicate;
            int i = acontextawarepredicate.length;

            for (int j = 0; j < i; ++j) {
                ContextAwarePredicate contextawarepredicate = acontextawarepredicate1[j];

                jsonarray.add(contextawarepredicate.toJson(lootserializationcontext));
            }

            return jsonarray;
        }
    }
}
