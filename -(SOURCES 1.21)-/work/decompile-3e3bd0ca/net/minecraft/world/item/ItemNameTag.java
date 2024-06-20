package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;

public class ItemNameTag extends Item {

    public ItemNameTag(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult interactLivingEntity(ItemStack itemstack, EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) itemstack.get(DataComponents.CUSTOM_NAME);

        if (ichatbasecomponent != null && !(entityliving instanceof EntityHuman)) {
            if (!entityhuman.level().isClientSide && entityliving.isAlive()) {
                entityliving.setCustomName(ichatbasecomponent);
                if (entityliving instanceof EntityInsentient) {
                    EntityInsentient entityinsentient = (EntityInsentient) entityliving;

                    entityinsentient.setPersistenceRequired();
                }

                itemstack.shrink(1);
            }

            return EnumInteractionResult.sidedSuccess(entityhuman.level().isClientSide);
        } else {
            return EnumInteractionResult.PASS;
        }
    }
}
