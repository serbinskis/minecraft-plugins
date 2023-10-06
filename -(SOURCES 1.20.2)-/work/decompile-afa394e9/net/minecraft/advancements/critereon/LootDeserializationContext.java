package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Iterator;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDeserializationContext {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftKey id;
    private final LootDataManager lootData;

    public LootDeserializationContext(MinecraftKey minecraftkey, LootDataManager lootdatamanager) {
        this.id = minecraftkey;
        this.lootData = lootdatamanager;
    }

    public final List<LootItemCondition> deserializeConditions(JsonArray jsonarray, String s, LootContextParameterSet lootcontextparameterset) {
        List<LootItemCondition> list = (List) SystemUtils.getOrThrow(LootItemConditions.CODEC.listOf().parse(JsonOps.INSTANCE, jsonarray), JsonParseException::new);
        LootCollector lootcollector = new LootCollector(lootcontextparameterset, this.lootData);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            LootItemCondition lootitemcondition = (LootItemCondition) iterator.next();

            lootitemcondition.validate(lootcollector);
            lootcollector.getProblems().forEach((s1, s2) -> {
                LootDeserializationContext.LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", new Object[]{s, s1, s2});
            });
        }

        return list;
    }

    public MinecraftKey getAdvancementId() {
        return this.id;
    }
}
