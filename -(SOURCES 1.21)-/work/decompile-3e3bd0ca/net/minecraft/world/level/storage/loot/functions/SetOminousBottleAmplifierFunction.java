package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetOminousBottleAmplifierFunction extends LootItemFunctionConditional {

    static final MapCodec<SetOminousBottleAmplifierFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(NumberProviders.CODEC.fieldOf("amplifier").forGetter((setominousbottleamplifierfunction) -> {
            return setominousbottleamplifierfunction.amplifierGenerator;
        })).apply(instance, SetOminousBottleAmplifierFunction::new);
    });
    private final NumberProvider amplifierGenerator;

    private SetOminousBottleAmplifierFunction(List<LootItemCondition> list, NumberProvider numberprovider) {
        super(list);
        this.amplifierGenerator = numberprovider;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.amplifierGenerator.getReferencedContextParams();
    }

    @Override
    public LootItemFunctionType<SetOminousBottleAmplifierFunction> getType() {
        return LootItemFunctions.SET_OMINOUS_BOTTLE_AMPLIFIER;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        int i = MathHelper.clamp(this.amplifierGenerator.getInt(loottableinfo), 0, 4);

        itemstack.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, i);
        return itemstack;
    }

    public NumberProvider amplifier() {
        return this.amplifierGenerator;
    }

    public static LootItemFunctionConditional.a<?> setAmplifier(NumberProvider numberprovider) {
        return simpleBuilder((list) -> {
            return new SetOminousBottleAmplifierFunction(list, numberprovider);
        });
    }
}
