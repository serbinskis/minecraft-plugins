package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ISaddleable;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemSaddle extends Item {

    public ItemSaddle(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult interactLivingEntity(ItemStack itemstack, EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        if (entityliving instanceof ISaddleable isaddleable) {
            if (entityliving.isAlive() && !isaddleable.isSaddled() && isaddleable.isSaddleable()) {
                if (!entityhuman.level().isClientSide) {
                    isaddleable.equipSaddle(itemstack.split(1), SoundCategory.NEUTRAL);
                    entityliving.level().gameEvent((Entity) entityliving, (Holder) GameEvent.EQUIP, entityliving.position());
                }

                return EnumInteractionResult.sidedSuccess(entityhuman.level().isClientSide);
            }
        }

        return EnumInteractionResult.PASS;
    }
}
