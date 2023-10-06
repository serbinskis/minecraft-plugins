package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {

    private final List<LootItemCondition> conditions;
    private final Predicate<LootTableInfo> compositePredicates;

    ContextAwarePredicate(List<LootItemCondition> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("ContextAwarePredicate must have at least one condition");
        } else {
            this.conditions = list;
            this.compositePredicates = LootItemConditions.andConditions(list);
        }
    }

    public static ContextAwarePredicate create(LootItemCondition... alootitemcondition) {
        return new ContextAwarePredicate(List.of(alootitemcondition));
    }

    public static Optional<Optional<ContextAwarePredicate>> fromElement(String s, LootDeserializationContext lootdeserializationcontext, @Nullable JsonElement jsonelement, LootContextParameterSet lootcontextparameterset) {
        if (jsonelement != null && jsonelement.isJsonArray()) {
            List<LootItemCondition> list = lootdeserializationcontext.deserializeConditions(jsonelement.getAsJsonArray(), lootdeserializationcontext.getAdvancementId() + "/" + s, lootcontextparameterset);

            return list.isEmpty() ? Optional.of(Optional.empty()) : Optional.of(Optional.of(new ContextAwarePredicate(list)));
        } else {
            return Optional.empty();
        }
    }

    public boolean matches(LootTableInfo loottableinfo) {
        return this.compositePredicates.test(loottableinfo);
    }

    public JsonElement toJson() {
        return (JsonElement) SystemUtils.getOrThrow(LootItemConditions.CODEC.listOf().encodeStart(JsonOps.INSTANCE, this.conditions), IllegalStateException::new);
    }

    public static JsonElement toJson(List<ContextAwarePredicate> list) {
        if (list.isEmpty()) {
            return JsonNull.INSTANCE;
        } else {
            JsonArray jsonarray = new JsonArray();
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                ContextAwarePredicate contextawarepredicate = (ContextAwarePredicate) iterator.next();

                jsonarray.add(contextawarepredicate.toJson());
            }

            return jsonarray;
        }
    }
}
