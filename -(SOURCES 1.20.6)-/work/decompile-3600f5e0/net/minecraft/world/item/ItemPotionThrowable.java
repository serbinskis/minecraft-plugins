package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityPotion;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;

public class ItemPotionThrowable extends ItemPotion implements ProjectileItem {

    public ItemPotionThrowable(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (!world.isClientSide) {
            EntityPotion entitypotion = new EntityPotion(world, entityhuman);

            entitypotion.setItem(itemstack);
            entitypotion.shootFromRotation(entityhuman, entityhuman.getXRot(), entityhuman.getYRot(), -20.0F, 0.5F, 1.0F);
            world.addFreshEntity(entitypotion);
        }

        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        itemstack.consume(1, entityhuman);
        return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        EntityPotion entitypotion = new EntityPotion(world, iposition.x(), iposition.y(), iposition.z());

        entitypotion.setItem(itemstack);
        return entitypotion;
    }

    @Override
    public ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.builder().uncertainty(ProjectileItem.a.DEFAULT.uncertainty() * 0.5F).power(ProjectileItem.a.DEFAULT.power() * 1.25F).build();
    }
}
