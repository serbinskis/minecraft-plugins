package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<FunctionReference> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("name").forGetter((functionreference) -> {
            return functionreference.name;
        })).apply(instance, FunctionReference::new);
    });
    private final ResourceKey<LootItemFunction> name;

    private FunctionReference(List<LootItemCondition> list, ResourceKey<LootItemFunction> resourcekey) {
        super(list);
        this.name = resourcekey;
    }

    @Override
    public LootItemFunctionType<FunctionReference> getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(LootCollector lootcollector) {
        if (!lootcollector.allowsReferences()) {
            lootcollector.reportProblem("Uses reference to " + String.valueOf(this.name.location()) + ", but references are not allowed");
        } else if (lootcollector.hasVisitedElement(this.name)) {
            lootcollector.reportProblem("Function " + String.valueOf(this.name.location()) + " is recursively called");
        } else {
            super.validate(lootcollector);
            lootcollector.resolver().get(Registries.ITEM_MODIFIER, this.name).ifPresentOrElse((holder_c) -> {
                ((LootItemFunction) holder_c.value()).validate(lootcollector.enterElement(".{" + String.valueOf(this.name.location()) + "}", this.name));
            }, () -> {
                lootcollector.reportProblem("Unknown function table called " + String.valueOf(this.name.location()));
            });
        }
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        LootItemFunction lootitemfunction = (LootItemFunction) loottableinfo.getResolver().get(Registries.ITEM_MODIFIER, this.name).map(Holder::value).orElse((Object) null);

        if (lootitemfunction == null) {
            FunctionReference.LOGGER.warn("Unknown function: {}", this.name.location());
            return itemstack;
        } else {
            LootTableInfo.c<?> loottableinfo_c = LootTableInfo.createVisitedEntry(lootitemfunction);

            if (loottableinfo.pushVisitedElement(loottableinfo_c)) {
                ItemStack itemstack1;

                try {
                    itemstack1 = (ItemStack) lootitemfunction.apply(itemstack, loottableinfo);
                } finally {
                    loottableinfo.popVisitedElement(loottableinfo_c);
                }

                return itemstack1;
            } else {
                FunctionReference.LOGGER.warn("Detected infinite loop in loot tables");
                return itemstack;
            }
        }
    }

    public static LootItemFunctionConditional.a<?> functionReference(ResourceKey<LootItemFunction> resourcekey) {
        return simpleBuilder((list) -> {
            return new FunctionReference(list, resourcekey);
        });
    }
}
