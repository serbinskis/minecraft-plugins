package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.item.crafting.RecipeItemStack;

public class EnumArmorMaterial {

    public static final Holder<ArmorMaterial> LEATHER = register("leather", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 1);
        enummap.put(ItemArmor.a.LEGGINGS, 2);
        enummap.put(ItemArmor.a.CHESTPLATE, 3);
        enummap.put(ItemArmor.a.HELMET, 1);
        enummap.put(ItemArmor.a.BODY, 3);
    }), 15, SoundEffects.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> {
        return RecipeItemStack.of(Items.LEATHER);
    }, List.of(new ArmorMaterial.a(MinecraftKey.withDefaultNamespace("leather"), "", true), new ArmorMaterial.a(MinecraftKey.withDefaultNamespace("leather"), "_overlay", false)));
    public static final Holder<ArmorMaterial> CHAIN = register("chainmail", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 1);
        enummap.put(ItemArmor.a.LEGGINGS, 4);
        enummap.put(ItemArmor.a.CHESTPLATE, 5);
        enummap.put(ItemArmor.a.HELMET, 2);
        enummap.put(ItemArmor.a.BODY, 4);
    }), 12, SoundEffects.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> {
        return RecipeItemStack.of(Items.IRON_INGOT);
    });
    public static final Holder<ArmorMaterial> IRON = register("iron", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 2);
        enummap.put(ItemArmor.a.LEGGINGS, 5);
        enummap.put(ItemArmor.a.CHESTPLATE, 6);
        enummap.put(ItemArmor.a.HELMET, 2);
        enummap.put(ItemArmor.a.BODY, 5);
    }), 9, SoundEffects.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> {
        return RecipeItemStack.of(Items.IRON_INGOT);
    });
    public static final Holder<ArmorMaterial> GOLD = register("gold", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 1);
        enummap.put(ItemArmor.a.LEGGINGS, 3);
        enummap.put(ItemArmor.a.CHESTPLATE, 5);
        enummap.put(ItemArmor.a.HELMET, 2);
        enummap.put(ItemArmor.a.BODY, 7);
    }), 25, SoundEffects.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> {
        return RecipeItemStack.of(Items.GOLD_INGOT);
    });
    public static final Holder<ArmorMaterial> DIAMOND = register("diamond", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 3);
        enummap.put(ItemArmor.a.LEGGINGS, 6);
        enummap.put(ItemArmor.a.CHESTPLATE, 8);
        enummap.put(ItemArmor.a.HELMET, 3);
        enummap.put(ItemArmor.a.BODY, 11);
    }), 10, SoundEffects.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> {
        return RecipeItemStack.of(Items.DIAMOND);
    });
    public static final Holder<ArmorMaterial> TURTLE = register("turtle", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 2);
        enummap.put(ItemArmor.a.LEGGINGS, 5);
        enummap.put(ItemArmor.a.CHESTPLATE, 6);
        enummap.put(ItemArmor.a.HELMET, 2);
        enummap.put(ItemArmor.a.BODY, 5);
    }), 9, SoundEffects.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> {
        return RecipeItemStack.of(Items.TURTLE_SCUTE);
    });
    public static final Holder<ArmorMaterial> NETHERITE = register("netherite", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 3);
        enummap.put(ItemArmor.a.LEGGINGS, 6);
        enummap.put(ItemArmor.a.CHESTPLATE, 8);
        enummap.put(ItemArmor.a.HELMET, 3);
        enummap.put(ItemArmor.a.BODY, 11);
    }), 15, SoundEffects.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> {
        return RecipeItemStack.of(Items.NETHERITE_INGOT);
    });
    public static final Holder<ArmorMaterial> ARMADILLO = register("armadillo", (EnumMap) SystemUtils.make(new EnumMap(ItemArmor.a.class), (enummap) -> {
        enummap.put(ItemArmor.a.BOOTS, 3);
        enummap.put(ItemArmor.a.LEGGINGS, 6);
        enummap.put(ItemArmor.a.CHESTPLATE, 8);
        enummap.put(ItemArmor.a.HELMET, 3);
        enummap.put(ItemArmor.a.BODY, 11);
    }), 10, SoundEffects.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, () -> {
        return RecipeItemStack.of(Items.ARMADILLO_SCUTE);
    });

    public EnumArmorMaterial() {}

    public static Holder<ArmorMaterial> bootstrap(IRegistry<ArmorMaterial> iregistry) {
        return EnumArmorMaterial.LEATHER;
    }

    private static Holder<ArmorMaterial> register(String s, EnumMap<ItemArmor.a, Integer> enummap, int i, Holder<SoundEffect> holder, float f, float f1, Supplier<RecipeItemStack> supplier) {
        List<ArmorMaterial.a> list = List.of(new ArmorMaterial.a(MinecraftKey.withDefaultNamespace(s)));

        return register(s, enummap, i, holder, f, f1, supplier, list);
    }

    private static Holder<ArmorMaterial> register(String s, EnumMap<ItemArmor.a, Integer> enummap, int i, Holder<SoundEffect> holder, float f, float f1, Supplier<RecipeItemStack> supplier, List<ArmorMaterial.a> list) {
        EnumMap<ItemArmor.a, Integer> enummap1 = new EnumMap(ItemArmor.a.class);
        ItemArmor.a[] aitemarmor_a = ItemArmor.a.values();
        int j = aitemarmor_a.length;

        for (int k = 0; k < j; ++k) {
            ItemArmor.a itemarmor_a = aitemarmor_a[k];

            enummap1.put(itemarmor_a, (Integer) enummap.get(itemarmor_a));
        }

        return IRegistry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, MinecraftKey.withDefaultNamespace(s), new ArmorMaterial(enummap1, i, holder, supplier, list, f, f1));
    }
}
