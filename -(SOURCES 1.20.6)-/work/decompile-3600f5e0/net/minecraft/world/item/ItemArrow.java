package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityTippedArrow;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;

public class ItemArrow extends Item implements ProjectileItem {

    public ItemArrow(Item.Info item_info) {
        super(item_info);
    }

    public EntityArrow createArrow(World world, ItemStack itemstack, EntityLiving entityliving) {
        return new EntityTippedArrow(world, entityliving, itemstack.copyWithCount(1));
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(world, iposition.x(), iposition.y(), iposition.z(), itemstack.copyWithCount(1));

        entitytippedarrow.pickup = EntityArrow.PickupStatus.ALLOWED;
        return entitytippedarrow;
    }
}
