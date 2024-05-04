package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import org.slf4j.Logger;

public record LootItemConditionReference(ResourceKey<LootItemCondition> name) implements LootItemCondition {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<LootItemConditionReference> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.PREDICATE).fieldOf("name").forGetter(LootItemConditionReference::name)).apply(instance, LootItemConditionReference::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.REFERENCE;
    }

    @Override
    public void validate(LootCollector lootcollector) {
        if (lootcollector.hasVisitedElement(this.name)) {
            lootcollector.reportProblem("Condition " + String.valueOf(this.name.location()) + " is recursively called");
        } else {
            LootItemCondition.super.validate(lootcollector);
            lootcollector.resolver().get(Registries.PREDICATE, this.name).ifPresentOrElse((holder_c) -> {
                ((LootItemCondition) holder_c.value()).validate(lootcollector.enterElement(".{" + String.valueOf(this.name.location()) + "}", this.name));
            }, () -> {
                lootcollector.reportProblem("Unknown condition table called " + String.valueOf(this.name.location()));
            });
        }
    }

    public boolean test(LootTableInfo loottableinfo) {
        LootItemCondition lootitemcondition = (LootItemCondition) loottableinfo.getResolver().get(Registries.PREDICATE, this.name).map(Holder.c::value).orElse((Object) null);

        if (lootitemcondition == null) {
            LootItemConditionReference.LOGGER.warn("Tried using unknown condition table called {}", this.name.location());
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

    public static LootItemCondition.a conditionReference(ResourceKey<LootItemCondition> resourcekey) {
        return () -> {
            return new LootItemConditionReference(resourcekey);
        };
    }
}
