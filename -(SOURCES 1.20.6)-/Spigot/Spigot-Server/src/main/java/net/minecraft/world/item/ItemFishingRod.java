package net.minecraft.world.item;

import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

// CraftBukkit start
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
// CraftBukkit end

public class ItemFishingRod extends Item {

    public ItemFishingRod(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        int i;

        if (entityhuman.fishing != null) {
            if (!world.isClientSide) {
                i = entityhuman.fishing.retrieve(itemstack);
                itemstack.hurtAndBreak(i, entityhuman, EntityLiving.getSlotForHand(enumhand));
            }

            world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            entityhuman.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            // world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!world.isClientSide) {
                i = EnchantmentManager.getFishingSpeedBonus(itemstack);
                int j = EnchantmentManager.getFishingLuckBonus(itemstack);

                // CraftBukkit start
                EntityFishingHook entityfishinghook = new EntityFishingHook(entityhuman, world, j, i);
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), null, (org.bukkit.entity.FishHook) entityfishinghook.getBukkitEntity(), CraftEquipmentSlot.getHand(enumhand), PlayerFishEvent.State.FISHING);
                world.getCraftServer().getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    entityhuman.fishing = null;
                    return InteractionResultWrapper.pass(itemstack);
                }
                world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
                world.addFreshEntity(entityfishinghook);
                // CraftBukkit end
            }

            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            entityhuman.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
