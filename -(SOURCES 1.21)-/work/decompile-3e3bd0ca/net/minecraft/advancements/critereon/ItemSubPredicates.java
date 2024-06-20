package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemSubPredicates {

    public static final ItemSubPredicate.a<ItemDamagePredicate> DAMAGE = register("damage", ItemDamagePredicate.CODEC);
    public static final ItemSubPredicate.a<ItemEnchantmentsPredicate.a> ENCHANTMENTS = register("enchantments", ItemEnchantmentsPredicate.a.CODEC);
    public static final ItemSubPredicate.a<ItemEnchantmentsPredicate.b> STORED_ENCHANTMENTS = register("stored_enchantments", ItemEnchantmentsPredicate.b.CODEC);
    public static final ItemSubPredicate.a<ItemPotionsPredicate> POTIONS = register("potion_contents", ItemPotionsPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemCustomDataPredicate> CUSTOM_DATA = register("custom_data", ItemCustomDataPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemContainerPredicate> CONTAINER = register("container", ItemContainerPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemBundlePredicate> BUNDLE_CONTENTS = register("bundle_contents", ItemBundlePredicate.CODEC);
    public static final ItemSubPredicate.a<ItemFireworkExplosionPredicate> FIREWORK_EXPLOSION = register("firework_explosion", ItemFireworkExplosionPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemFireworksPredicate> FIREWORKS = register("fireworks", ItemFireworksPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemWritableBookPredicate> WRITABLE_BOOK = register("writable_book_content", ItemWritableBookPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemWrittenBookPredicate> WRITTEN_BOOK = register("written_book_content", ItemWrittenBookPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemAttributeModifiersPredicate> ATTRIBUTE_MODIFIERS = register("attribute_modifiers", ItemAttributeModifiersPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemTrimPredicate> ARMOR_TRIM = register("trim", ItemTrimPredicate.CODEC);
    public static final ItemSubPredicate.a<ItemJukeboxPlayablePredicate> JUKEBOX_PLAYABLE = register("jukebox_playable", ItemJukeboxPlayablePredicate.CODEC);

    public ItemSubPredicates() {}

    private static <T extends ItemSubPredicate> ItemSubPredicate.a<T> register(String s, Codec<T> codec) {
        return (ItemSubPredicate.a) IRegistry.register(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, s, new ItemSubPredicate.a<>(codec));
    }

    public static ItemSubPredicate.a<?> bootstrap(IRegistry<ItemSubPredicate.a<?>> iregistry) {
        return ItemSubPredicates.DAMAGE;
    }
}
