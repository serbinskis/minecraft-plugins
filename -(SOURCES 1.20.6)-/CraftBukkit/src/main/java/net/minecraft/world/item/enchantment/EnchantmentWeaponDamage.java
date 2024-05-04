package net.minecraft.world.item.enchantment;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;

public class EnchantmentWeaponDamage extends Enchantment {

    private final Optional<TagKey<EntityTypes<?>>> targets;

    public EnchantmentWeaponDamage(Enchantment.b enchantment_b, Optional<TagKey<EntityTypes<?>>> optional) {
        super(enchantment_b);
        this.targets = optional;
    }

    @Override
    public float getDamageBonus(int i, @Nullable EntityTypes<?> entitytypes) {
        return this.targets.isEmpty() ? 1.0F + (float) Math.max(0, i - 1) * 0.5F : (entitytypes != null && entitytypes.is((TagKey) this.targets.get()) ? (float) i * 2.5F : 0.0F);
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return !(enchantment instanceof EnchantmentWeaponDamage);
    }

    @Override
    public void doPostAttack(EntityLiving entityliving, Entity entity, int i) {
        if (this.targets.isPresent() && entity instanceof EntityLiving entityliving1) {
            if (this.targets.get() == TagsEntity.SENSITIVE_TO_BANE_OF_ARTHROPODS && i > 0 && entityliving1.getType().is((TagKey) this.targets.get())) {
                int j = 20 + entityliving.getRandom().nextInt(10 * i);

                entityliving1.addEffect(new MobEffect(MobEffects.MOVEMENT_SLOWDOWN, j, 3), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.ATTACK); // CraftBukkit
            }
        }

    }
}
