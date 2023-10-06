package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntitySheep;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntitySign;

import org.bukkit.event.entity.SheepDyeWoolEvent; // CraftBukkit

public class ItemDye extends Item implements SignApplicator {

    private static final Map<EnumColor, ItemDye> ITEM_BY_COLOR = Maps.newEnumMap(EnumColor.class);
    private final EnumColor dyeColor;

    public ItemDye(EnumColor enumcolor, Item.Info item_info) {
        super(item_info);
        this.dyeColor = enumcolor;
        ItemDye.ITEM_BY_COLOR.put(enumcolor, this);
    }

    @Override
    public EnumInteractionResult interactLivingEntity(ItemStack itemstack, EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        if (entityliving instanceof EntitySheep) {
            EntitySheep entitysheep = (EntitySheep) entityliving;

            if (entitysheep.isAlive() && !entitysheep.isSheared() && entitysheep.getColor() != this.dyeColor) {
                entitysheep.level().playSound(entityhuman, (Entity) entitysheep, SoundEffects.DYE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (!entityhuman.level().isClientSide) {
                    // CraftBukkit start
                    byte bColor = (byte) this.dyeColor.getId();
                    SheepDyeWoolEvent event = new SheepDyeWoolEvent((org.bukkit.entity.Sheep) entitysheep.getBukkitEntity(), org.bukkit.DyeColor.getByWoolData(bColor), (org.bukkit.entity.Player) entityhuman.getBukkitEntity());
                    entitysheep.level().getCraftServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        return EnumInteractionResult.PASS;
                    }

                    entitysheep.setColor(EnumColor.byId((byte) event.getColor().getWoolData()));
                    // CraftBukkit end
                    itemstack.shrink(1);
                }

                return EnumInteractionResult.sidedSuccess(entityhuman.level().isClientSide);
            }
        }

        return EnumInteractionResult.PASS;
    }

    public EnumColor getDyeColor() {
        return this.dyeColor;
    }

    public static ItemDye byColor(EnumColor enumcolor) {
        return (ItemDye) ItemDye.ITEM_BY_COLOR.get(enumcolor);
    }

    @Override
    public boolean tryApplyToSign(World world, TileEntitySign tileentitysign, boolean flag, EntityHuman entityhuman) {
        if (tileentitysign.updateText((signtext) -> {
            return signtext.setColor(this.getDyeColor());
        }, flag)) {
            world.playSound((EntityHuman) null, tileentitysign.getBlockPos(), SoundEffects.DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}
