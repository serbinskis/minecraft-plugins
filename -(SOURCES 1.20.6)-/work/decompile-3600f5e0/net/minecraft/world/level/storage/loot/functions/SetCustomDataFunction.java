package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetCustomDataFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetCustomDataFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(MojangsonParser.LENIENT_CODEC.fieldOf("tag").forGetter((setcustomdatafunction) -> {
            return setcustomdatafunction.tag;
        })).apply(instance, SetCustomDataFunction::new);
    });
    private final NBTTagCompound tag;

    private SetCustomDataFunction(List<LootItemCondition> list, NBTTagCompound nbttagcompound) {
        super(list);
        this.tag = nbttagcompound;
    }

    @Override
    public LootItemFunctionType<SetCustomDataFunction> getType() {
        return LootItemFunctions.SET_CUSTOM_DATA;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        CustomData.update(DataComponents.CUSTOM_DATA, itemstack, (nbttagcompound) -> {
            nbttagcompound.merge(this.tag);
        });
        return itemstack;
    }

    /** @deprecated */
    @Deprecated
    public static LootItemFunctionConditional.a<?> setCustomData(NBTTagCompound nbttagcompound) {
        return simpleBuilder((list) -> {
            return new SetCustomDataFunction(list, nbttagcompound);
        });
    }
}
