package net.minecraft.world.damagesource;

import net.minecraft.util.MathHelper;
import net.minecraft.world.item.enchantment.EnchantmentManager;

public class CombatMath {

    public static final float MAX_ARMOR = 20.0F;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
    public static final float MIN_ARMOR_RATIO = 0.2F;
    private static final int NUM_ARMOR_ITEMS = 4;

    public CombatMath() {}

    public static float getDamageAfterAbsorb(float f, DamageSource damagesource, float f1, float f2) {
        float f3 = 2.0F + f2 / 4.0F;
        float f4 = MathHelper.clamp(f1 - f / f3, f1 * 0.2F, 20.0F);
        float f5 = f4 / 25.0F;
        float f6 = EnchantmentManager.calculateArmorBreach(damagesource.getEntity(), f5);
        float f7 = 1.0F - f6;

        return f * f7;
    }

    public static float getDamageAfterMagicAbsorb(float f, float f1) {
        float f2 = MathHelper.clamp(f1, 0.0F, 20.0F);

        return f * (1.0F - f2 / 25.0F);
    }
}
