package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetCustomModelDataFunction extends LootItemFunctionConditional {

    static final MapCodec<SetCustomModelDataFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(NumberProviders.CODEC.fieldOf("value").forGetter((setcustommodeldatafunction) -> {
            return setcustommodeldatafunction.valueProvider;
        })).apply(instance, SetCustomModelDataFunction::new);
    });
    private final NumberProvider valueProvider;

    private SetCustomModelDataFunction(List<LootItemCondition> list, NumberProvider numberprovider) {
        super(list);
        this.valueProvider = numberprovider;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.valueProvider.getReferencedContextParams();
    }

    @Override
    public LootItemFunctionType<SetCustomModelDataFunction> getType() {
        return LootItemFunctions.SET_CUSTOM_MODEL_DATA;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(this.valueProvider.getInt(loottableinfo)));
        return itemstack;
    }
}
