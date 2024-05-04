package net.minecraft.world.item.enchantment;

import net.minecraft.core.particles.Particles;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.World;

public class WindBurstEnchantment extends Enchantment {

    public WindBurstEnchantment() {
        super(Enchantment.definition(TagsItem.MACE_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, FeatureFlagSet.of(FeatureFlags.UPDATE_1_21), EnumItemSlot.MAINHAND));
    }

    @Override
    public void doPostItemStackHurt(EntityLiving entityliving, Entity entity, int i) {
        float f = 0.25F + 0.25F * (float) i;

        entityliving.level().explode((Entity) null, (DamageSource) null, new WindBurstEnchantment.a(f), entityliving.getX(), entityliving.getY(), entityliving.getZ(), 3.5F, false, World.a.BLOW, Particles.GUST_EMITTER_SMALL, Particles.GUST_EMITTER_LARGE, SoundEffects.WIND_CHARGE_BURST);
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    private static final class a extends AbstractWindCharge.a {

        private final float knockBackPower;

        public a(float f) {
            this.knockBackPower = f;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            boolean flag;
            label17:
            {
                if (entity instanceof EntityHuman entityhuman) {
                    if (entityhuman.getAbilities().flying) {
                        flag = true;
                        break label17;
                    }
                }

                flag = false;
            }

            boolean flag1 = flag;

            return !flag1 ? this.knockBackPower : 0.0F;
        }
    }
}
