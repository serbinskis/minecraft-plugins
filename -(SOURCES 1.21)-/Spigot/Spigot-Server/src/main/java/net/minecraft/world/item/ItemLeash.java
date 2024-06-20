package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.EntityLeash;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;

// CraftBukkit start
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.block.CraftBlock;
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
        List<Leashable> list = leashableInArea(world, blockposition, (leashable) -> {
            return leashable.getLeashHolder() == entityhuman;
        });

        Leashable leashable;

        for (Iterator iterator = list.iterator(); iterator.hasNext();) { // CraftBukkit - handle setLeashedTo at end of loop
            leashable = (Leashable) iterator.next();
            if (entityleash == null) {
                entityleash = EntityLeash.getOrCreateKnot(world, blockposition);

                // CraftBukkit start - fire HangingPlaceEvent
                org.bukkit.inventory.EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);
                HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) entityleash.getBukkitEntity(), entityhuman != null ? (org.bukkit.entity.Player) entityhuman.getBukkitEntity() : null, CraftBlock.at(world, blockposition), org.bukkit.block.BlockFace.SELF, hand);
                world.getCraftServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    entityleash.discard(null); // CraftBukkit - add Bukkit remove cause
                    return EnumInteractionResult.PASS;
                }
                // CraftBukkit end
                entityleash.playPlacementSound();
            }

            // CraftBukkit start
            if (leashable instanceof Entity leashed) {
                if (org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerLeashEntityEvent(leashed, entityleash, entityhuman, enumhand).isCancelled()) {
                    iterator.remove();
                    continue;
                }
            }

            leashable.setLeashedTo(entityleash, true);
            // CraftBukkit end
        }

        if (!list.isEmpty()) {
            world.gameEvent((Holder) GameEvent.BLOCK_ATTACH, blockposition, GameEvent.a.of((Entity) entityhuman));
            return EnumInteractionResult.SUCCESS;
        } else {
            // CraftBukkit start- remove leash if we do not leash any entity because of the cancelled event
            if (entityleash != null) {
                entityleash.discard(null);
            }
            // CraftBukkit end
            return EnumInteractionResult.PASS;
        }
    }

    // CraftBukkit start
    public static EnumInteractionResult bindPlayerMobs(EntityHuman entityhuman, World world, BlockPosition blockposition) {
        return bindPlayerMobs(entityhuman, world, blockposition, net.minecraft.world.EnumHand.MAIN_HAND);
    }
    // CraftBukkit end

    public static List<Leashable> leashableInArea(World world, BlockPosition blockposition, Predicate<Leashable> predicate) {
        double d0 = 7.0D;
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();
        AxisAlignedBB axisalignedbb = new AxisAlignedBB((double) i - 7.0D, (double) j - 7.0D, (double) k - 7.0D, (double) i + 7.0D, (double) j + 7.0D, (double) k + 7.0D);
        Stream stream = world.getEntitiesOfClass(Entity.class, axisalignedbb, (entity) -> {
            boolean flag;

            if (entity instanceof Leashable leashable) {
                if (predicate.test(leashable)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }).stream();

        Objects.requireNonNull(Leashable.class);
        return stream.map(Leashable.class::cast).toList();
    }
}
