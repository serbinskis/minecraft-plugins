package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<FunctionReference> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(MinecraftKey.CODEC.fieldOf("name").forGetter((functionreference) -> {
            return functionreference.name;
        })).apply(instance, FunctionReference::new);
    });
    private final MinecraftKey name;

    private FunctionReference(List<LootItemCondition> list, MinecraftKey minecraftkey) {
        super(list);
        this.name = minecraftkey;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(LootCollector lootcollector) {
        LootDataId<LootItemFunction> lootdataid = new LootDataId<>(LootDataType.MODIFIER, this.name);

        if (lootcollector.hasVisitedElement(lootdataid)) {
            lootcollector.reportProblem("Function " + this.name + " is recursively called");
        } else {
            super.validate(lootcollector);
            lootcollector.resolver().getElementOptional(lootdataid).ifPresentOrElse((lootitemfunction) -> {
                lootitemfunction.validate(lootcollector.enterElement(".{" + this.name + "}", lootdataid));
            }, () -> {
                lootcollector.reportProblem("Unknown function table called " + this.name);
            });
        }
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        LootItemFunction lootitemfunction = (LootItemFunction) loottableinfo.getResolver().getElement(LootDataType.MODIFIER, this.name);

        if (lootitemfunction == null) {
            FunctionReference.LOGGER.warn("Unknown function: {}", this.name);
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

    public static LootItemFunctionConditional.a<?> functionReference(MinecraftKey minecraftkey) {
        return simpleBuilder((list) -> {
            return new FunctionReference(list, minecraftkey);
        });
    }
}
