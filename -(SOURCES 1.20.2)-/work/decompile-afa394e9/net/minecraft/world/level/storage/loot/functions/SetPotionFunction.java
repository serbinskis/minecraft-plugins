package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.PotionUtil;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemFunctionConditional {

    public static final Codec<SetPotionFunction> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(BuiltInRegistries.POTION.holderByNameCodec().fieldOf("id").forGetter((setpotionfunction) -> {
            return setpotionfunction.potion;
        })).apply(instance, SetPotionFunction::new);
    });
    private final Holder<PotionRegistry> potion;

    private SetPotionFunction(List<LootItemCondition> list, Holder<PotionRegistry> holder) {
        super(list);
        this.potion = holder;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_POTION;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        PotionUtil.setPotion(itemstack, (PotionRegistry) this.potion.value());
        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> setPotion(PotionRegistry potionregistry) {
        return simpleBuilder((list) -> {
            return new SetPotionFunction(list, potionregistry.builtInRegistryHolder());
        });
    }
}
