package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemFunctionConditional {

    public static final MapCodec<SetInstrumentFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(TagKey.hashedCodec(Registries.INSTRUMENT).fieldOf("options").forGetter((setinstrumentfunction) -> {
            return setinstrumentfunction.options;
        })).apply(instance, SetInstrumentFunction::new);
    });
    private final TagKey<Instrument> options;

    private SetInstrumentFunction(List<LootItemCondition> list, TagKey<Instrument> tagkey) {
        super(list);
        this.options = tagkey;
    }

    @Override
    public LootItemFunctionType<SetInstrumentFunction> getType() {
        return LootItemFunctions.SET_INSTRUMENT;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        InstrumentItem.setRandom(itemstack, this.options, loottableinfo.getRandom());
        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> setInstrumentOptions(TagKey<Instrument> tagkey) {
        return simpleBuilder((list) -> {
            return new SetInstrumentFunction(list, tagkey);
        });
    }
}
