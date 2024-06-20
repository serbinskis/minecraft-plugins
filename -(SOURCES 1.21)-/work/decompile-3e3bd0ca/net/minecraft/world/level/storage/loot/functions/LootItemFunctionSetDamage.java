package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.slf4j.Logger;

public class LootItemFunctionSetDamage extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<LootItemFunctionSetDamage> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(NumberProviders.CODEC.fieldOf("damage").forGetter((lootitemfunctionsetdamage) -> {
            return lootitemfunctionsetdamage.damage;
        }), Codec.BOOL.fieldOf("add").orElse(false).forGetter((lootitemfunctionsetdamage) -> {
            return lootitemfunctionsetdamage.add;
        }))).apply(instance, LootItemFunctionSetDamage::new);
    });
    private final NumberProvider damage;
    private final boolean add;

    private LootItemFunctionSetDamage(List<LootItemCondition> list, NumberProvider numberprovider, boolean flag) {
        super(list);
        this.damage = numberprovider;
        this.add = flag;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetDamage> getType() {
        return LootItemFunctions.SET_DAMAGE;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.damage.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.isDamageableItem()) {
            int i = itemstack.getMaxDamage();
            float f = this.add ? 1.0F - (float) itemstack.getDamageValue() / (float) i : 0.0F;
            float f1 = 1.0F - MathHelper.clamp(this.damage.getFloat(loottableinfo) + f, 0.0F, 1.0F);

            itemstack.setDamageValue(MathHelper.floor(f1 * (float) i));
        } else {
            LootItemFunctionSetDamage.LOGGER.warn("Couldn't set damage of loot item {}", itemstack);
        }

        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> setDamage(NumberProvider numberprovider) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetDamage(list, numberprovider, false);
        });
    }

    public static LootItemFunctionConditional.a<?> setDamage(NumberProvider numberprovider, boolean flag) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetDamage(list, numberprovider, flag);
        });
    }
}
