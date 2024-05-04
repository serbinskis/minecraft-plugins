package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntityFox;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class ItemChorusFruit extends Item {

    public ItemChorusFruit(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, World world, EntityLiving entityliving) {
        ItemStack itemstack1 = super.finishUsingItem(itemstack, world, entityliving);

        if (!world.isClientSide) {
            for (int i = 0; i < 16; ++i) {
                double d0 = entityliving.getX() + (entityliving.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d1 = MathHelper.clamp(entityliving.getY() + (double) (entityliving.getRandom().nextInt(16) - 8), (double) world.getMinBuildHeight(), (double) (world.getMinBuildHeight() + ((WorldServer) world).getLogicalHeight() - 1));
                double d2 = entityliving.getZ() + (entityliving.getRandom().nextDouble() - 0.5D) * 16.0D;

                if (entityliving.isPassenger()) {
                    entityliving.stopRiding();
                }

                Vec3D vec3d = entityliving.position();

                // CraftBukkit start - handle canceled status of teleport event
                java.util.Optional<Boolean> status = entityliving.randomTeleport(d0, d1, d2, true, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);

                if (!status.isPresent()) {
                    // teleport event was canceled, no more tries
                    break;
                }

                if (status.get()) {
                    // CraftBukkit end
                    world.gameEvent((Holder) GameEvent.TELEPORT, vec3d, GameEvent.a.of((Entity) entityliving));
                    SoundEffect soundeffect;
                    SoundCategory soundcategory;

                    if (entityliving instanceof EntityFox) {
                        soundeffect = SoundEffects.FOX_TELEPORT;
                        soundcategory = SoundCategory.NEUTRAL;
                    } else {
                        soundeffect = SoundEffects.CHORUS_FRUIT_TELEPORT;
                        soundcategory = SoundCategory.PLAYERS;
                    }

                    world.playSound((EntityHuman) null, entityliving.getX(), entityliving.getY(), entityliving.getZ(), soundeffect, soundcategory);
                    entityliving.resetFallDistance();
                    break;
                }
            }

            if (entityliving instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entityliving;

                entityhuman.getCooldowns().addCooldown(this, 20);
            }
        }

        return itemstack1;
    }
}
