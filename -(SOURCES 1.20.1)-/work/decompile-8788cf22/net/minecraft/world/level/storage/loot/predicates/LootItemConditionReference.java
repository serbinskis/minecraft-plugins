package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootSerializer;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import org.slf4j.Logger;

public class LootItemConditionReference implements LootItemCondition {

    private static final Logger LOGGER = LogUtils.getLogger();
    final MinecraftKey name;

    LootItemConditionReference(MinecraftKey minecraftkey) {
        this.name = minecraftkey;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.REFERENCE;
    }

    @Override
    public void validate(LootCollector lootcollector) {
        LootDataId<LootItemCondition> lootdataid = new LootDataId<>(LootDataType.PREDICATE, this.name);

        if (lootcollector.hasVisitedElement(lootdataid)) {
            lootcollector.reportProblem("Condition " + this.name + " is recursively called");
        } else {
            LootItemCondition.super.validate(lootcollector);
            lootcollector.resolver().getElementOptional(lootdataid).ifPresentOrElse((lootitemcondition) -> {
                lootitemcondition.validate(lootcollector.enterElement(".{" + this.name + "}", lootdataid));
            }, () -> {
                lootcollector.reportProblem("Unknown condition table called " + this.name);
            });
        }
    }

    public boolean test(LootTableInfo loottableinfo) {
        LootItemCondition lootitemcondition = (LootItemCondition) loottableinfo.getResolver().getElement(LootDataType.PREDICATE, this.name);

        if (lootitemcondition == null) {
            LootItemConditionReference.LOGGER.warn("Tried using unknown condition table called {}", this.name);
            return false;
        } else {
            LootTableInfo.c<?> loottableinfo_c = LootTableInfo.createVisitedEntry(lootitemcondition);

            if (loottableinfo.pushVisitedElement(loottableinfo_c)) {
                boolean flag;

                try {
                    flag = lootitemcondition.test(loottableinfo);
                } finally {
                    loottableinfo.popVisitedElement(loottableinfo_c);
                }

                return flag;
            } else {
                LootItemConditionReference.LOGGER.warn("Detected infinite loop in loot tables");
                return false;
            }
        }
    }

    public static LootItemCondition.a conditionReference(MinecraftKey minecraftkey) {
        return () -> {
            return new LootItemConditionReference(minecraftkey);
        };
    }

    public static class a implements LootSerializer<LootItemConditionReference> {

        public a() {}

        public void serialize(JsonObject jsonobject, LootItemConditionReference lootitemconditionreference, JsonSerializationContext jsonserializationcontext) {
            jsonobject.addProperty("name", lootitemconditionreference.name.toString());
        }

        @Override
        public LootItemConditionReference deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
            MinecraftKey minecraftkey = new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "name"));

            return new LootItemConditionReference(minecraftkey);
        }
    }
}
