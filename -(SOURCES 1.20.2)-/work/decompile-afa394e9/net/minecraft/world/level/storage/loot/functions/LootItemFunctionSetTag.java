package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionSetTag extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionSetTag> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(MojangsonParser.AS_CODEC.fieldOf("tag").forGetter((lootitemfunctionsettag) -> {
            return lootitemfunctionsettag.tag;
        })).apply(instance, LootItemFunctionSetTag::new);
    });
    private final NBTTagCompound tag;

    private LootItemFunctionSetTag(List<LootItemCondition> list, NBTTagCompound nbttagcompound) {
        super(list);
        this.tag = nbttagcompound;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_NBT;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.getOrCreateTag().merge(this.tag);
        return itemstack;
    }

    /** @deprecated */
    @Deprecated
    public static LootItemFunctionConditional.a<?> setTag(NBTTagCompound nbttagcompound) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetTag(list, nbttagcompound);
        });
    }
}
