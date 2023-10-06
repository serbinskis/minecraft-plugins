package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class LootItemFunctions {

    public static final BiFunction<ItemStack, LootTableInfo, ItemStack> IDENTITY = (itemstack, loottableinfo) -> {
        return itemstack;
    };
    private static final Codec<LootItemFunction> TYPED_CODEC = BuiltInRegistries.LOOT_FUNCTION_TYPE.byNameCodec().dispatch("function", LootItemFunction::getType, LootItemFunctionType::codec);
    public static final Codec<LootItemFunction> CODEC = ExtraCodecs.lazyInitializedCodec(() -> {
        return ExtraCodecs.withAlternative(LootItemFunctions.TYPED_CODEC, SequenceFunction.INLINE_CODEC);
    });
    public static final LootItemFunctionType SET_COUNT = register("set_count", LootItemFunctionSetCount.CODEC);
    public static final LootItemFunctionType ENCHANT_WITH_LEVELS = register("enchant_with_levels", LootEnchantLevel.CODEC);
    public static final LootItemFunctionType ENCHANT_RANDOMLY = register("enchant_randomly", LootItemFunctionEnchant.CODEC);
    public static final LootItemFunctionType SET_ENCHANTMENTS = register("set_enchantments", SetEnchantmentsFunction.CODEC);
    public static final LootItemFunctionType SET_NBT = register("set_nbt", LootItemFunctionSetTag.CODEC);
    public static final LootItemFunctionType FURNACE_SMELT = register("furnace_smelt", LootItemFunctionSmelt.CODEC);
    public static final LootItemFunctionType LOOTING_ENCHANT = register("looting_enchant", LootEnchantFunction.CODEC);
    public static final LootItemFunctionType SET_DAMAGE = register("set_damage", LootItemFunctionSetDamage.CODEC);
    public static final LootItemFunctionType SET_ATTRIBUTES = register("set_attributes", LootItemFunctionSetAttribute.CODEC);
    public static final LootItemFunctionType SET_NAME = register("set_name", LootItemFunctionSetName.CODEC);
    public static final LootItemFunctionType EXPLORATION_MAP = register("exploration_map", LootItemFunctionExplorationMap.CODEC);
    public static final LootItemFunctionType SET_STEW_EFFECT = register("set_stew_effect", LootItemFunctionSetStewEffect.CODEC);
    public static final LootItemFunctionType COPY_NAME = register("copy_name", LootItemFunctionCopyName.CODEC);
    public static final LootItemFunctionType SET_CONTENTS = register("set_contents", LootItemFunctionSetContents.CODEC);
    public static final LootItemFunctionType LIMIT_COUNT = register("limit_count", LootItemFunctionLimitCount.CODEC);
    public static final LootItemFunctionType APPLY_BONUS = register("apply_bonus", LootItemFunctionApplyBonus.CODEC);
    public static final LootItemFunctionType SET_LOOT_TABLE = register("set_loot_table", LootItemFunctionSetTable.CODEC);
    public static final LootItemFunctionType EXPLOSION_DECAY = register("explosion_decay", LootItemFunctionExplosionDecay.CODEC);
    public static final LootItemFunctionType SET_LORE = register("set_lore", LootItemFunctionSetLore.CODEC);
    public static final LootItemFunctionType FILL_PLAYER_HEAD = register("fill_player_head", LootItemFunctionFillPlayerHead.CODEC);
    public static final LootItemFunctionType COPY_NBT = register("copy_nbt", LootItemFunctionCopyNBT.CODEC);
    public static final LootItemFunctionType COPY_STATE = register("copy_state", LootItemFunctionCopyState.CODEC);
    public static final LootItemFunctionType SET_BANNER_PATTERN = register("set_banner_pattern", SetBannerPatternFunction.CODEC);
    public static final LootItemFunctionType SET_POTION = register("set_potion", SetPotionFunction.CODEC);
    public static final LootItemFunctionType SET_INSTRUMENT = register("set_instrument", SetInstrumentFunction.CODEC);
    public static final LootItemFunctionType REFERENCE = register("reference", FunctionReference.CODEC);
    public static final LootItemFunctionType SEQUENCE = register("sequence", SequenceFunction.CODEC);

    public LootItemFunctions() {}

    private static LootItemFunctionType register(String s, Codec<? extends LootItemFunction> codec) {
        return (LootItemFunctionType) IRegistry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, new MinecraftKey(s), new LootItemFunctionType(codec));
    }

    public static BiFunction<ItemStack, LootTableInfo, ItemStack> compose(List<? extends BiFunction<ItemStack, LootTableInfo, ItemStack>> list) {
        List<BiFunction<ItemStack, LootTableInfo, ItemStack>> list1 = List.copyOf(list);
        BiFunction bifunction;

        switch (list1.size()) {
            case 0:
                bifunction = LootItemFunctions.IDENTITY;
                break;
            case 1:
                bifunction = (BiFunction) list1.get(0);
                break;
            case 2:
                BiFunction<ItemStack, LootTableInfo, ItemStack> bifunction1 = (BiFunction) list1.get(0);
                BiFunction<ItemStack, LootTableInfo, ItemStack> bifunction2 = (BiFunction) list1.get(1);

                bifunction = (itemstack, loottableinfo) -> {
                    return (ItemStack) bifunction2.apply((ItemStack) bifunction1.apply(itemstack, loottableinfo), loottableinfo);
                };
                break;
            default:
                bifunction = (itemstack, loottableinfo) -> {
                    BiFunction bifunction3;

                    for (Iterator iterator = list1.iterator(); iterator.hasNext(); itemstack = (ItemStack) bifunction3.apply(itemstack, loottableinfo)) {
                        bifunction3 = (BiFunction) iterator.next();
                    }

                    return itemstack;
                };
        }

        return bifunction;
    }
}
