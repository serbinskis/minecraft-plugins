package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetComponentsFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetComponentsFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(DataComponentPatch.CODEC.fieldOf("components").forGetter((setcomponentsfunction) -> {
            return setcomponentsfunction.components;
        })).apply(instance, SetComponentsFunction::new);
    });
    private final DataComponentPatch components;

    private SetComponentsFunction(List<LootItemCondition> list, DataComponentPatch datacomponentpatch) {
        super(list);
        this.components = datacomponentpatch;
    }

    @Override
    public LootItemFunctionType<SetComponentsFunction> getType() {
        return LootItemFunctions.SET_COMPONENTS;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.applyComponentsAndValidate(this.components);
        return itemstack;
    }

    public static <T> LootItemFunctionConditional.a<?> setComponent(DataComponentType<T> datacomponenttype, T t0) {
        return simpleBuilder((list) -> {
            return new SetComponentsFunction(list, DataComponentPatch.builder().set(datacomponenttype, t0).build());
        });
    }
}
