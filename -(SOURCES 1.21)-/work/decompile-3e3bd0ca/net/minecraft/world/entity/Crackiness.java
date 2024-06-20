package net.minecraft.world.entity;

import net.minecraft.world.item.ItemStack;

public class Crackiness {

    public static final Crackiness GOLEM = new Crackiness(0.75F, 0.5F, 0.25F);
    public static final Crackiness WOLF_ARMOR = new Crackiness(0.95F, 0.69F, 0.32F);
    private final float fractionLow;
    private final float fractionMedium;
    private final float fractionHigh;

    private Crackiness(float f, float f1, float f2) {
        this.fractionLow = f;
        this.fractionMedium = f1;
        this.fractionHigh = f2;
    }

    public Crackiness.a byFraction(float f) {
        return f < this.fractionHigh ? Crackiness.a.HIGH : (f < this.fractionMedium ? Crackiness.a.MEDIUM : (f < this.fractionLow ? Crackiness.a.LOW : Crackiness.a.NONE));
    }

    public Crackiness.a byDamage(ItemStack itemstack) {
        return !itemstack.isDamageableItem() ? Crackiness.a.NONE : this.byDamage(itemstack.getDamageValue(), itemstack.getMaxDamage());
    }

    public Crackiness.a byDamage(int i, int j) {
        return this.byFraction((float) (j - i) / (float) j);
    }

    public static enum a {

        NONE, LOW, MEDIUM, HIGH;

        private a() {}
    }
}
