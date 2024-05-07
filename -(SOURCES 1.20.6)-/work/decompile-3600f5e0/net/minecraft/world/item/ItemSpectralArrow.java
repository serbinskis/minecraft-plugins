package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntitySpectralArrow;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;

public class ItemSpectralArrow extends ItemArrow {

    public ItemSpectralArrow(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EntityArrow createArrow(World world, ItemStack itemstack, EntityLiving entityliving) {
        return new EntitySpectralArrow(world, entityliving, itemstack.copyWithCount(1));
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        EntitySpectralArrow entityspectralarrow = new EntitySpectralArrow(world, iposition.x(), iposition.y(), iposition.z(), itemstack.copyWithCount(1));

        entityspectralarrow.pickup = EntityArrow.PickupStatus.ALLOWED;
        return entityspectralarrow;
    }
}
