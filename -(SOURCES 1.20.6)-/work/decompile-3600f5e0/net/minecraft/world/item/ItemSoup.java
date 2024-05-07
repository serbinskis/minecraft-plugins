package net.minecraft.world.item;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;

public class ItemSoup extends Item {

    public ItemSoup(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, World world, EntityLiving entityliving) {
        ItemStack itemstack1 = super.finishUsingItem(itemstack, world, entityliving);

        if (entityliving instanceof EntityHuman entityhuman) {
            if (entityhuman.hasInfiniteMaterials()) {
                return itemstack1;
            }
        }

        return new ItemStack(Items.BOWL);
    }
}
