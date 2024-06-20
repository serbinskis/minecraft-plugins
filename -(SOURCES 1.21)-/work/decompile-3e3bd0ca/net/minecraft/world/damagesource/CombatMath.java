package net.minecraft.world.damagesource;

import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;

public class CombatMath {

    public static final float MAX_ARMOR = 20.0F;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
    public static final float MIN_ARMOR_RATIO = 0.2F;
    private static final int NUM_ARMOR_ITEMS = 4;

    public CombatMath() {}

    public static float getDamageAfterAbsorb(EntityLiving entityliving, float f, DamageSource damagesource, float f1, float f2) {
        float f3;
        label12:
        {
            float f4 = 2.0F + f2 / 4.0F;
            float f5 = MathHelper.clamp(f1 - f / f4, f1 * 0.2F, 20.0F);
            float f6 = f5 / 25.0F;
            ItemStack itemstack = damagesource.getWeaponItem();

            if (itemstack != null) {
                World world = entityliving.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    f3 = MathHelper.clamp(EnchantmentManager.modifyArmorEffectiveness(worldserver, itemstack, entityliving, damagesource, f6), 0.0F, 1.0F);
                    break label12;
                }
            }

            f3 = f6;
        }

        float f7 = 1.0F - f3;

        return f * f7;
    }

    public static float getDamageAfterMagicAbsorb(float f, float f1) {
        float f2 = MathHelper.clamp(f1, 0.0F, 20.0F);

        return f * (1.0F - f2 / 25.0F);
    }
}
