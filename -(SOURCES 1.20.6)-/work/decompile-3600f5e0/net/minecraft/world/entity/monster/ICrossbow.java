package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.projectile.ProjectileHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCrossbow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface ICrossbow extends IRangedEntity {

    void setChargingCrossbow(boolean flag);

    @Nullable
    EntityLiving getTarget();

    void onCrossbowAttackPerformed();

    default void performCrossbowAttack(EntityLiving entityliving, float f) {
        EnumHand enumhand = ProjectileHelper.getWeaponHoldingHand(entityliving, Items.CROSSBOW);
        ItemStack itemstack = entityliving.getItemInHand(enumhand);
        Item item = itemstack.getItem();

        if (item instanceof ItemCrossbow itemcrossbow) {
            itemcrossbow.performShooting(entityliving.level(), entityliving, enumhand, itemstack, f, (float) (14 - entityliving.level().getDifficulty().getId() * 4), this.getTarget());
        }

        this.onCrossbowAttackPerformed();
    }
}
