package net.minecraft.world.item.enchantment;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityLiving;

public class EnchantmentProtection extends Enchantment {

    public final EnchantmentProtection.DamageType type;

    public EnchantmentProtection(Enchantment.b enchantment_b, EnchantmentProtection.DamageType enchantmentprotection_damagetype) {
        super(enchantment_b);
        this.type = enchantmentprotection_damagetype;
    }

    @Override
    public int getDamageProtection(int i, DamageSource damagesource) {
        return damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? 0 : (this.type == EnchantmentProtection.DamageType.ALL ? i : (this.type == EnchantmentProtection.DamageType.FIRE && damagesource.is(DamageTypeTags.IS_FIRE) ? i * 2 : (this.type == EnchantmentProtection.DamageType.FALL && damagesource.is(DamageTypeTags.IS_FALL) ? i * 3 : (this.type == EnchantmentProtection.DamageType.EXPLOSION && damagesource.is(DamageTypeTags.IS_EXPLOSION) ? i * 2 : (this.type == EnchantmentProtection.DamageType.PROJECTILE && damagesource.is(DamageTypeTags.IS_PROJECTILE) ? i * 2 : 0)))));
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        if (enchantment instanceof EnchantmentProtection enchantmentprotection) {
            return this.type == enchantmentprotection.type ? false : this.type == EnchantmentProtection.DamageType.FALL || enchantmentprotection.type == EnchantmentProtection.DamageType.FALL;
        } else {
            return super.checkCompatibility(enchantment);
        }
    }

    public static int getFireAfterDampener(EntityLiving entityliving, int i) {
        int j = EnchantmentManager.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, entityliving);

        if (j > 0) {
            i -= MathHelper.floor((float) i * (float) j * 0.15F);
        }

        return i;
    }

    public static double getExplosionKnockbackAfterDampener(EntityLiving entityliving, double d0) {
        int i = EnchantmentManager.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, entityliving);

        if (i > 0) {
            d0 *= MathHelper.clamp(1.0D - (double) i * 0.15D, 0.0D, 1.0D);
        }

        return d0;
    }

    public static enum DamageType {

        ALL, FIRE, FALL, EXPLOSION, PROJECTILE;

        private DamageType() {}
    }
}
