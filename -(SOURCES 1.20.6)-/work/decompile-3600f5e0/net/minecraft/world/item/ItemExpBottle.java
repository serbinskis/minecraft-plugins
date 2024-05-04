package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityThrownExpBottle;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;

public class ItemExpBottle extends Item implements ProjectileItem {

    public ItemExpBottle(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.EXPERIENCE_BOTTLE_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClientSide) {
            EntityThrownExpBottle entitythrownexpbottle = new EntityThrownExpBottle(world, entityhuman);

            entitythrownexpbottle.setItem(itemstack);
            entitythrownexpbottle.shootFromRotation(entityhuman, entityhuman.getXRot(), entityhuman.getYRot(), -20.0F, 0.7F, 1.0F);
            world.addFreshEntity(entitythrownexpbottle);
        }

        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        itemstack.consume(1, entityhuman);
        return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        EntityThrownExpBottle entitythrownexpbottle = new EntityThrownExpBottle(world, iposition.x(), iposition.y(), iposition.z());

        entitythrownexpbottle.setItem(itemstack);
        return entitythrownexpbottle;
    }

    @Override
    public ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.builder().uncertainty(ProjectileItem.a.DEFAULT.uncertainty() * 0.5F).power(ProjectileItem.a.DEFAULT.power() * 1.25F).build();
    }
}
