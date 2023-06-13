package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.ISourceBlock;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BlockDispenser;

// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

public class DispenseBehaviorBoat extends DispenseBehaviorItem {

    private final DispenseBehaviorItem defaultDispenseItemBehavior;
    private final EntityBoat.EnumBoatType type;
    private final boolean isChestBoat;

    public DispenseBehaviorBoat(EntityBoat.EnumBoatType entityboat_enumboattype) {
        this(entityboat_enumboattype, false);
    }

    public DispenseBehaviorBoat(EntityBoat.EnumBoatType entityboat_enumboattype, boolean flag) {
        this.defaultDispenseItemBehavior = new DispenseBehaviorItem();
        this.type = entityboat_enumboattype;
        this.isChestBoat = flag;
    }

    @Override
    public ItemStack execute(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = (EnumDirection) isourceblock.getBlockState().getValue(BlockDispenser.FACING);
        WorldServer worldserver = isourceblock.getLevel();
        double d0 = 0.5625D + (double) EntityTypes.BOAT.getWidth() / 2.0D;
        double d1 = isourceblock.x() + (double) enumdirection.getStepX() * d0;
        double d2 = isourceblock.y() + (double) ((float) enumdirection.getStepY() * 1.125F);
        double d3 = isourceblock.z() + (double) enumdirection.getStepZ() * d0;
        BlockPosition blockposition = isourceblock.getPos().relative(enumdirection);
        double d4;

        if (worldserver.getFluidState(blockposition).is(TagsFluid.WATER)) {
            d4 = 1.0D;
        } else {
            if (!worldserver.getBlockState(blockposition).isAir() || !worldserver.getFluidState(blockposition.below()).is(TagsFluid.WATER)) {
                return this.defaultDispenseItemBehavior.dispense(isourceblock, itemstack);
            }

            d4 = 0.0D;
        }

        // Object object = this.isChestBoat ? new ChestBoat(worldserver, d1, d2 + d4, d3) : new EntityBoat(worldserver, d1, d2 + d4, d3);
        // CraftBukkit start
        ItemStack itemstack1 = itemstack.split(1);
        org.bukkit.block.Block block = worldserver.getWorld().getBlockAt(isourceblock.getPos().getX(), isourceblock.getPos().getY(), isourceblock.getPos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d1, d2 + d4, d3));
        if (!BlockDispenser.eventFired) {
            worldserver.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            itemstack.grow(1);
            return itemstack;
        }

        if (!event.getItem().equals(craftItem)) {
            itemstack.grow(1);
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != IDispenseBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(isourceblock, eventStack);
                return itemstack;
            }
        }

        Object object = this.isChestBoat ? new ChestBoat(worldserver, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ()) : new EntityBoat(worldserver, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
        // CraftBukkit end

        ((EntityBoat) object).setVariant(this.type);
        ((EntityBoat) object).setYRot(enumdirection.toYRot());
        if (!worldserver.addFreshEntity((Entity) object)) itemstack.grow(1); // CraftBukkit
        // itemstack.shrink(1); // CraftBukkit - handled during event processing
        return itemstack;
    }

    @Override
    protected void playSound(ISourceBlock isourceblock) {
        isourceblock.getLevel().levelEvent(1000, isourceblock.getPos(), 0);
    }
}
