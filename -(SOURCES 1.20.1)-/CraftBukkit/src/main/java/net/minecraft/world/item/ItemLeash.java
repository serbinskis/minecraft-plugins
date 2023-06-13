package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.decoration.EntityLeash;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;

// CraftBukkit start
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.event.hanging.HangingPlaceEvent;
// CraftBukkit end

public class ItemLeash extends Item {

    public ItemLeash(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (iblockdata.is(TagsBlock.FENCES)) {
            EntityHuman entityhuman = itemactioncontext.getPlayer();

            if (!world.isClientSide && entityhuman != null) {
                bindPlayerMobs(entityhuman, world, blockposition, itemactioncontext.getHand()); // CraftBukkit - Pass hand
            }

            return EnumInteractionResult.sidedSuccess(world.isClientSide);
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    public static EnumInteractionResult bindPlayerMobs(EntityHuman entityhuman, World world, BlockPosition blockposition, net.minecraft.world.EnumHand enumhand) { // CraftBukkit - Add EnumHand
        EntityLeash entityleash = null;
        boolean flag = false;
        double d0 = 7.0D;
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();
        List<EntityInsentient> list = world.getEntitiesOfClass(EntityInsentient.class, new AxisAlignedBB((double) i - 7.0D, (double) j - 7.0D, (double) k - 7.0D, (double) i + 7.0D, (double) j + 7.0D, (double) k + 7.0D));
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityInsentient entityinsentient = (EntityInsentient) iterator.next();

            if (entityinsentient.getLeashHolder() == entityhuman) {
                if (entityleash == null) {
                    entityleash = EntityLeash.getOrCreateKnot(world, blockposition);

                    // CraftBukkit start - fire HangingPlaceEvent
                    org.bukkit.inventory.EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);
                    HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) entityleash.getBukkitEntity(), entityhuman != null ? (org.bukkit.entity.Player) entityhuman.getBukkitEntity() : null, world.getWorld().getBlockAt(i, j, k), org.bukkit.block.BlockFace.SELF, hand);
                    world.getCraftServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        entityleash.discard();
                        return EnumInteractionResult.PASS;
                    }
                    // CraftBukkit end
                    entityleash.playPlacementSound();
                }

                // CraftBukkit start
                if (org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, entityleash, entityhuman, enumhand).isCancelled()) {
                    continue;
                }
                // CraftBukkit end

                entityinsentient.setLeashedTo(entityleash, true);
                flag = true;
            }
        }

        if (flag) {
            world.gameEvent(GameEvent.BLOCK_ATTACH, blockposition, GameEvent.a.of((Entity) entityhuman));
        }

        return flag ? EnumInteractionResult.SUCCESS : EnumInteractionResult.PASS;
    }

    // CraftBukkit start
    public static EnumInteractionResult bindPlayerMobs(EntityHuman entityhuman, World world, BlockPosition blockposition) {
        return bindPlayerMobs(entityhuman, world, blockposition, net.minecraft.world.EnumHand.MAIN_HAND);
    }
    // CraftBukkit end
}
