package net.minecraft.world.effect;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
// CraftBukkit end

class SaturationMobEffect extends InstantMobEffect {

    protected SaturationMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public void applyEffectTick(EntityLiving entityliving, int i) {
        super.applyEffectTick(entityliving, i);
        if (!entityliving.level().isClientSide && entityliving instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) entityliving;

            // CraftBukkit start
            int oldFoodLevel = entityhuman.getFoodData().foodLevel;
            org.bukkit.event.entity.FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, i + 1 + oldFoodLevel);
            if (!event.isCancelled()) {
                entityhuman.getFoodData().eat(event.getFoodLevel() - oldFoodLevel, 1.0F);
            }

            ((CraftPlayer) entityhuman.getBukkitEntity()).sendHealthUpdate();
            // CraftBukkit end
        }

    }
}
