package net.minecraft.world.item.enchantment;

import java.util.Map.Entry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;

public class EnchantmentThorns extends Enchantment {

    private static final float CHANCE_PER_LEVEL = 0.15F;

    public EnchantmentThorns(Enchantment.b enchantment_b) {
        super(enchantment_b);
    }

    @Override
    public void doPostHurt(EntityLiving entityliving, Entity entity, int i) {
        RandomSource randomsource = entityliving.getRandom();
        Entry<EnumItemSlot, ItemStack> entry = EnchantmentManager.getRandomItemWith(Enchantments.THORNS, entityliving);

        if (shouldHit(i, randomsource)) {
            if (entity != null) {
                entity.hurt(entityliving.damageSources().thorns(entityliving), (float) getDamage(i, randomsource));
            }

            if (entry != null) {
                ((ItemStack) entry.getValue()).hurtAndBreak(2, entityliving, (EnumItemSlot) entry.getKey());
            }
        }

    }

    public static boolean shouldHit(int i, RandomSource randomsource) {
        return i <= 0 ? false : randomsource.nextFloat() < 0.15F * (float) i;
    }

    public static int getDamage(int i, RandomSource randomsource) {
        return i > 10 ? i - 10 : 1 + randomsource.nextInt(4);
    }
}
