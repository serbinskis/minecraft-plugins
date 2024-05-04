package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityEgg;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;

public class ItemEgg extends Item implements ProjectileItem {

    public ItemEgg(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        // world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)); // CraftBukkit - moved down
        if (!world.isClientSide) {
            EntityEgg entityegg = new EntityEgg(world, entityhuman);

            entityegg.setItem(itemstack);
            entityegg.shootFromRotation(entityhuman, entityhuman.getXRot(), entityhuman.getYRot(), 0.0F, 1.5F, 1.0F);
            // CraftBukkit start
            if (!world.addFreshEntity(entityegg)) {
                if (entityhuman instanceof net.minecraft.server.level.EntityPlayer) {
                    ((net.minecraft.server.level.EntityPlayer) entityhuman).getBukkitEntity().updateInventory();
                }
                return InteractionResultWrapper.fail(itemstack);
            }
            // CraftBukkit end
        }
        world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        itemstack.consume(1, entityhuman);
        return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        EntityEgg entityegg = new EntityEgg(world, iposition.x(), iposition.y(), iposition.z());

        entityegg.setItem(itemstack);
        return entityegg;
    }
}
