package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class LootItemFunctions {

    public static final BiFunction<ItemStack, LootTableInfo, ItemStack> IDENTITY = (itemstack, loottableinfo) -> {
        return itemstack;
    };
    public static final Codec<LootItemFunction> TYPED_CODEC = BuiltInRegistries.LOOT_FUNCTION_TYPE.byNameCodec().dispatch("function", LootItemFunction::getType, LootItemFunctionType::codec);
    public static final Codec<LootItemFunction> ROOT_CODEC = Codec.lazyInitialized(() -> {
        return Codec.withAlternative(LootItemFunctions.TYPED_CODEC, SequenceFunction.INLINE_CODEC);
    });
    public static final Codec<Holder<LootItemFunction>> CODEC = RegistryFileCodec.create(Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetCount> SET_COUNT = register("set_count", LootItemFunctionSetCount.CODEC);
    public static final LootItemFunctionType<SetItemFunction> SET_ITEM = register("set_item", SetItemFunction.CODEC);
    public static final LootItemFunctionType<LootEnchantLevel> ENCHANT_WITH_LEVELS = register("enchant_with_levels", LootEnchantLevel.CODEC);
    public static final LootItemFunctionType<LootItemFunctionEnchant> ENCHANT_RANDOMLY = register("enchant_randomly", LootItemFunctionEnchant.CODEC);
    public static final LootItemFunctionType<SetEnchantmentsFunction> SET_ENCHANTMENTS = register("set_enchantments", SetEnchantmentsFunction.CODEC);
    public static final LootItemFunctionType<SetCustomDataFunction> SET_CUSTOM_DATA = register("set_custom_data", SetCustomDataFunction.CODEC);
    public static final LootItemFunctionType<SetComponentsFunction> SET_COMPONENTS = register("set_components", SetComponentsFunction.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSmelt> FURNACE_SMELT = register("furnace_smelt", LootItemFunctionSmelt.CODEC);
    public static final LootItemFunctionType<EnchantedCountIncreaseFunction> ENCHANTED_COUNT_INCREASE = register("enchanted_count_increase", EnchantedCountIncreaseFunction.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetDamage> SET_DAMAGE = register("set_damage", LootItemFunctionSetDamage.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetAttribute> SET_ATTRIBUTES = register("set_attributes", LootItemFunctionSetAttribute.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetName> SET_NAME = register("set_name", LootItemFunctionSetName.CODEC);
    public static final LootItemFunctionType<LootItemFunctionExplorationMap> EXPLORATION_MAP = register("exploration_map", LootItemFunctionExplorationMap.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetStewEffect> SET_STEW_EFFECT = register("set_stew_effect", LootItemFunctionSetStewEffect.CODEC);
    public static final LootItemFunctionType<LootItemFunctionCopyName> COPY_NAME = register("copy_name", LootItemFunctionCopyName.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetContents> SET_CONTENTS = register("set_contents", LootItemFunctionSetContents.CODEC);
    public static final LootItemFunctionType<ModifyContainerContents> MODIFY_CONTENTS = register("modify_contents", ModifyContainerContents.CODEC);
    public static final LootItemFunctionType<FilteredFunction> FILTERED = register("filtered", FilteredFunction.CODEC);
    public static final LootItemFunctionType<LootItemFunctionLimitCount> LIMIT_COUNT = register("limit_count", LootItemFunctionLimitCount.CODEC);
    public static final LootItemFunctionType<LootItemFunctionApplyBonus> APPLY_BONUS = register("apply_bonus", LootItemFunctionApplyBonus.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetTable> SET_LOOT_TABLE = register("set_loot_table", LootItemFunctionSetTable.CODEC);
    public static final LootItemFunctionType<LootItemFunctionExplosionDecay> EXPLOSION_DECAY = register("explosion_decay", LootItemFunctionExplosionDecay.CODEC);
    public static final LootItemFunctionType<LootItemFunctionSetLore> SET_LORE = register("set_lore", LootItemFunctionSetLore.CODEC);
    public static final LootItemFunctionType<LootItemFunctionFillPlayerHead> FILL_PLAYER_HEAD = register("fill_player_head", LootItemFunctionFillPlayerHead.CODEC);
    public static final LootItemFunctionType<CopyCustomDataFunction> COPY_CUSTOM_DATA = register("copy_custom_data", CopyCustomDataFunction.CODEC);
    public static final LootItemFunctionType<LootItemFunctionCopyState> COPY_STATE = register("copy_state", LootItemFunctionCopyState.CODEC);
    public static final LootItemFunctionType<SetBannerPatternFunction> SET_BANNER_PATTERN = register("set_banner_pattern", SetBannerPatternFunction.CODEC);
    public static final LootItemFunctionType<SetPotionFunction> SET_POTION = register("set_potion", SetPotionFunction.CODEC);
    public static final LootItemFunctionType<SetInstrumentFunction> SET_INSTRUMENT = register("set_instrument", SetInstrumentFunction.CODEC);
    public static final LootItemFunctionType<FunctionReference> REFERENCE = register("reference", FunctionReference.CODEC);
    public static final LootItemFunctionType<SequenceFunction> SEQUENCE = register("sequence", SequenceFunction.CODEC);
    public static final LootItemFunctionType<CopyComponentsFunction> COPY_COMPONENTS = register("copy_components", CopyComponentsFunction.CODEC);
    public static final LootItemFunctionType<SetFireworksFunction> SET_FIREWORKS = register("set_fireworks", SetFireworksFunction.CODEC);
    public static final LootItemFunctionType<SetFireworkExplosionFunction> SET_FIREWORK_EXPLOSION = register("set_firework_explosion", SetFireworkExplosionFunction.CODEC);
    public static final LootItemFunctionType<SetBookCoverFunction> SET_BOOK_COVER = register("set_book_cover", SetBookCoverFunction.CODEC);
    public static final LootItemFunctionType<SetWrittenBookPagesFunction> SET_WRITTEN_BOOK_PAGES = register("set_written_book_pages", SetWrittenBookPagesFunction.CODEC);
    public static final LootItemFunctionType<SetWritableBookPagesFunction> SET_WRITABLE_BOOK_PAGES = register("set_writable_book_pages", SetWritableBookPagesFunction.CODEC);
    public static final LootItemFunctionType<ToggleTooltips> TOGGLE_TOOLTIPS = register("toggle_tooltips", ToggleTooltips.CODEC);
    public static final LootItemFunctionType<SetOminousBottleAmplifierFunction> SET_OMINOUS_BOTTLE_AMPLIFIER = register("set_ominous_bottle_amplifier", SetOminousBottleAmplifierFunction.CODEC);
    public static final LootItemFunctionType<SetCustomModelDataFunction> SET_CUSTOM_MODEL_DATA = register("set_custom_model_data", SetCustomModelDataFunction.CODEC);

    public LootItemFunctions() {}

    private static <T extends LootItemFunction> LootItemFunctionType<T> register(String s, MapCodec<T> mapcodec) {
        return (LootItemFunctionType) IRegistry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, MinecraftKey.withDefaultNamespace(s), new LootItemFunctionType<>(mapcodec));
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
