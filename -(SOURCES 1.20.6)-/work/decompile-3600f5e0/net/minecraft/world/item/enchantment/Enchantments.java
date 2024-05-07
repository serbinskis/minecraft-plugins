package net.minecraft.world.item.enchantment;

import java.util.Optional;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagsEntity;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.entity.EnumItemSlot;

public class Enchantments {

    private static final EnumItemSlot[] ARMOR_SLOTS = new EnumItemSlot[]{EnumItemSlot.HEAD, EnumItemSlot.CHEST, EnumItemSlot.LEGS, EnumItemSlot.FEET};
    public static final Enchantment PROTECTION = register("protection", new EnchantmentProtection(Enchantment.definition(TagsItem.ARMOR_ENCHANTABLE, 10, 4, Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(12, 11), 1, Enchantments.ARMOR_SLOTS), EnchantmentProtection.DamageType.ALL));
    public static final Enchantment FIRE_PROTECTION = register("fire_protection", new EnchantmentProtection(Enchantment.definition(TagsItem.ARMOR_ENCHANTABLE, 5, 4, Enchantment.dynamicCost(10, 8), Enchantment.dynamicCost(18, 8), 2, Enchantments.ARMOR_SLOTS), EnchantmentProtection.DamageType.FIRE));
    public static final Enchantment FEATHER_FALLING = register("feather_falling", new EnchantmentProtection(Enchantment.definition(TagsItem.FOOT_ARMOR_ENCHANTABLE, 5, 4, Enchantment.dynamicCost(5, 6), Enchantment.dynamicCost(11, 6), 2, Enchantments.ARMOR_SLOTS), EnchantmentProtection.DamageType.FALL));
    public static final Enchantment BLAST_PROTECTION = register("blast_protection", new EnchantmentProtection(Enchantment.definition(TagsItem.ARMOR_ENCHANTABLE, 2, 4, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(13, 8), 4, Enchantments.ARMOR_SLOTS), EnchantmentProtection.DamageType.EXPLOSION));
    public static final Enchantment PROJECTILE_PROTECTION = register("projectile_protection", new EnchantmentProtection(Enchantment.definition(TagsItem.ARMOR_ENCHANTABLE, 5, 4, Enchantment.dynamicCost(3, 6), Enchantment.dynamicCost(9, 6), 2, Enchantments.ARMOR_SLOTS), EnchantmentProtection.DamageType.PROJECTILE));
    public static final Enchantment RESPIRATION = register("respiration", new Enchantment(Enchantment.definition(TagsItem.HEAD_ARMOR_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(40, 10), 4, Enchantments.ARMOR_SLOTS)));
    public static final Enchantment AQUA_AFFINITY = register("aqua_affinity", new Enchantment(Enchantment.definition(TagsItem.HEAD_ARMOR_ENCHANTABLE, 2, 1, Enchantment.constantCost(1), Enchantment.constantCost(41), 4, Enchantments.ARMOR_SLOTS)));
    public static final Enchantment THORNS = register("thorns", new EnchantmentThorns(Enchantment.definition(TagsItem.ARMOR_ENCHANTABLE, TagsItem.CHEST_ARMOR_ENCHANTABLE, 1, 3, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 8, Enchantments.ARMOR_SLOTS)));
    public static final Enchantment DEPTH_STRIDER = register("depth_strider", new EnchantmentDepthStrider(Enchantment.definition(TagsItem.FOOT_ARMOR_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(25, 10), 4, Enchantments.ARMOR_SLOTS)));
    public static final Enchantment FROST_WALKER = register("frost_walker", new EnchantmentFrostWalker(Enchantment.definition(TagsItem.FOOT_ARMOR_ENCHANTABLE, 2, 2, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(25, 10), 4, EnumItemSlot.FEET)));
    public static final Enchantment BINDING_CURSE = register("binding_curse", new EnchantmentBinding(Enchantment.definition(TagsItem.EQUIPPABLE_ENCHANTABLE, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, Enchantments.ARMOR_SLOTS)));
    public static final Enchantment SOUL_SPEED = register("soul_speed", new EnchantmentSoulSpeed(Enchantment.definition(TagsItem.FOOT_ARMOR_ENCHANTABLE, 1, 3, Enchantment.dynamicCost(10, 10), Enchantment.dynamicCost(25, 10), 8, EnumItemSlot.FEET)));
    public static final Enchantment SWIFT_SNEAK = register("swift_sneak", new SwiftSneakEnchantment(Enchantment.definition(TagsItem.LEG_ARMOR_ENCHANTABLE, 1, 3, Enchantment.dynamicCost(25, 25), Enchantment.dynamicCost(75, 25), 8, EnumItemSlot.LEGS)));
    public static final Enchantment SHARPNESS = register("sharpness", new EnchantmentWeaponDamage(Enchantment.definition(TagsItem.SHARP_WEAPON_ENCHANTABLE, TagsItem.SWORD_ENCHANTABLE, 10, 5, Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(21, 11), 1, EnumItemSlot.MAINHAND), Optional.empty()));
    public static final Enchantment SMITE = register("smite", new EnchantmentWeaponDamage(Enchantment.definition(TagsItem.WEAPON_ENCHANTABLE, TagsItem.SWORD_ENCHANTABLE, 5, 5, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(25, 8), 2, EnumItemSlot.MAINHAND), Optional.of(TagsEntity.SENSITIVE_TO_SMITE)));
    public static final Enchantment BANE_OF_ARTHROPODS = register("bane_of_arthropods", new EnchantmentWeaponDamage(Enchantment.definition(TagsItem.WEAPON_ENCHANTABLE, TagsItem.SWORD_ENCHANTABLE, 5, 5, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(25, 8), 2, EnumItemSlot.MAINHAND), Optional.of(TagsEntity.SENSITIVE_TO_BANE_OF_ARTHROPODS)));
    public static final Enchantment KNOCKBACK = register("knockback", new Enchantment(Enchantment.definition(TagsItem.SWORD_ENCHANTABLE, 5, 2, Enchantment.dynamicCost(5, 20), Enchantment.dynamicCost(55, 20), 2, EnumItemSlot.MAINHAND)));
    public static final Enchantment FIRE_ASPECT = register("fire_aspect", new Enchantment(Enchantment.definition(TagsItem.FIRE_ASPECT_ENCHANTABLE, 2, 2, Enchantment.dynamicCost(10, 20), Enchantment.dynamicCost(60, 20), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment LOOTING = register("looting", new EnchantmentLootBonus(Enchantment.definition(TagsItem.SWORD_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment SWEEPING_EDGE = register("sweeping_edge", new Enchantment(Enchantment.definition(TagsItem.SWORD_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(5, 9), Enchantment.dynamicCost(20, 9), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment EFFICIENCY = register("efficiency", new Enchantment(Enchantment.definition(TagsItem.MINING_ENCHANTABLE, 10, 5, Enchantment.dynamicCost(1, 10), Enchantment.dynamicCost(51, 10), 1, EnumItemSlot.MAINHAND)));
    public static final Enchantment SILK_TOUCH = register("silk_touch", new EnchantmentSilkTouch(Enchantment.definition(TagsItem.MINING_LOOT_ENCHANTABLE, 1, 1, Enchantment.constantCost(15), Enchantment.constantCost(65), 8, EnumItemSlot.MAINHAND)));
    public static final Enchantment UNBREAKING = register("unbreaking", new EnchantmentDurability(Enchantment.definition(TagsItem.DURABILITY_ENCHANTABLE, 5, 3, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(55, 8), 2, EnumItemSlot.MAINHAND)));
    public static final Enchantment FORTUNE = register("fortune", new EnchantmentLootBonus(Enchantment.definition(TagsItem.MINING_LOOT_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment POWER = register("power", new Enchantment(Enchantment.definition(TagsItem.BOW_ENCHANTABLE, 10, 5, Enchantment.dynamicCost(1, 10), Enchantment.dynamicCost(16, 10), 1, EnumItemSlot.MAINHAND)));
    public static final Enchantment PUNCH = register("punch", new Enchantment(Enchantment.definition(TagsItem.BOW_ENCHANTABLE, 2, 2, Enchantment.dynamicCost(12, 20), Enchantment.dynamicCost(37, 20), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment FLAME = register("flame", new Enchantment(Enchantment.definition(TagsItem.BOW_ENCHANTABLE, 2, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment INFINITY = register("infinity", new EnchantmentInfiniteArrows(Enchantment.definition(TagsItem.BOW_ENCHANTABLE, 1, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 8, EnumItemSlot.MAINHAND)));
    public static final Enchantment LUCK_OF_THE_SEA = register("luck_of_the_sea", new EnchantmentLootBonus(Enchantment.definition(TagsItem.FISHING_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment LURE = register("lure", new Enchantment(Enchantment.definition(TagsItem.FISHING_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment LOYALTY = register("loyalty", new Enchantment(Enchantment.definition(TagsItem.TRIDENT_ENCHANTABLE, 5, 3, Enchantment.dynamicCost(12, 7), Enchantment.constantCost(50), 2, EnumItemSlot.MAINHAND)));
    public static final Enchantment IMPALING = register("impaling", new EnchantmentWeaponDamage(Enchantment.definition(TagsItem.TRIDENT_ENCHANTABLE, 2, 5, Enchantment.dynamicCost(1, 8), Enchantment.dynamicCost(21, 8), 4, EnumItemSlot.MAINHAND), Optional.of(TagsEntity.SENSITIVE_TO_IMPALING)));
    public static final Enchantment RIPTIDE = register("riptide", new EnchantmentTridentRiptide(Enchantment.definition(TagsItem.TRIDENT_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(17, 7), Enchantment.constantCost(50), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment CHANNELING = register("channeling", new Enchantment(Enchantment.definition(TagsItem.TRIDENT_ENCHANTABLE, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EnumItemSlot.MAINHAND)));
    public static final Enchantment MULTISHOT = register("multishot", new EnchantmentMultishot(Enchantment.definition(TagsItem.CROSSBOW_ENCHANTABLE, 2, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 4, EnumItemSlot.MAINHAND)));
    public static final Enchantment QUICK_CHARGE = register("quick_charge", new Enchantment(Enchantment.definition(TagsItem.CROSSBOW_ENCHANTABLE, 5, 3, Enchantment.dynamicCost(12, 20), Enchantment.constantCost(50), 2, EnumItemSlot.MAINHAND)));
    public static final Enchantment PIERCING = register("piercing", new EnchantmentPiercing(Enchantment.definition(TagsItem.CROSSBOW_ENCHANTABLE, 10, 4, Enchantment.dynamicCost(1, 10), Enchantment.constantCost(50), 1, EnumItemSlot.MAINHAND)));
    public static final Enchantment DENSITY = register("density", new DensityEnchantment());
    public static final Enchantment BREACH = register("breach", new BreachEnchantment());
    public static final Enchantment WIND_BURST = register("wind_burst", new WindBurstEnchantment());
    public static final Enchantment MENDING = register("mending", new EnchantmentMending(Enchantment.definition(TagsItem.DURABILITY_ENCHANTABLE, 2, 1, Enchantment.dynamicCost(25, 25), Enchantment.dynamicCost(75, 25), 4, EnumItemSlot.values())));
    public static final Enchantment VANISHING_CURSE = register("vanishing_curse", new EnchantmentVanishing(Enchantment.definition(TagsItem.VANISHING_ENCHANTABLE, 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EnumItemSlot.values())));

    public Enchantments() {}

    private static Enchantment register(String s, Enchantment enchantment) {
        return (Enchantment) IRegistry.register(BuiltInRegistries.ENCHANTMENT, s, enchantment);
    }
}
