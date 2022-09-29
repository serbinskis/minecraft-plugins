package net.minecraft.core.dispenser;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.core.ISourceBlock;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;

// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

public class DispenseBehaviorItem implements IDispenseBehavior {

    // CraftBukkit start
    private boolean dropper;

    public DispenseBehaviorItem(boolean dropper) {
        this.dropper = dropper;
    }
    // CraftBukkit end

    public DispenseBehaviorItem() {}

    @Override
    public final ItemStack dispense(ISourceBlock isourceblock, ItemStack itemstack) {
        ItemStack itemstack1 = this.execute(isourceblock, itemstack);

        this.playSound(isourceblock);
        this.playAnimation(isourceblock, (EnumDirection) isourceblock.getBlockState().getValue(BlockDispenser.FACING));
        return itemstack1;
    }

    protected ItemStack execute(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = (EnumDirection) isourceblock.getBlockState().getValue(BlockDispenser.FACING);
        IPosition iposition = BlockDispenser.getDispensePosition(isourceblock);
        ItemStack itemstack1 = itemstack.split(1);

        // CraftBukkit start
        if (!spawnItem(isourceblock.getLevel(), itemstack1, 6, enumdirection, isourceblock, dropper)) {
            itemstack.grow(1);
        }
        // CraftBukkit end
        return itemstack;
    }

    // CraftBukkit start - void -> boolean return, IPosition -> ISourceBlock last argument, dropper
    public static boolean spawnItem(World world, ItemStack itemstack, int i, EnumDirection enumdirection, ISourceBlock isourceblock, boolean dropper) {
        if (itemstack.isEmpty()) return true;
        IPosition iposition = BlockDispenser.getDispensePosition(isourceblock);
        // CraftBukkit end
        double d0 = iposition.x();
        double d1 = iposition.y();
        double d2 = iposition.z();

        if (enumdirection.getAxis() == EnumDirection.EnumAxis.Y) {
            d1 -= 0.125D;
        } else {
            d1 -= 0.15625D;
        }

        EntityItem entityitem = new EntityItem(world, d0, d1, d2, itemstack);
        double d3 = world.random.nextDouble() * 0.1D + 0.2D;

        entityitem.setDeltaMovement(world.random.triangle((double) enumdirection.getStepX() * d3, 0.0172275D * (double) i), world.random.triangle(0.2D, 0.0172275D * (double) i), world.random.triangle((double) enumdirection.getStepZ() * d3, 0.0172275D * (double) i));

        // CraftBukkit start
        org.bukkit.block.Block block = world.getWorld().getBlockAt(isourceblock.getPos().getX(), isourceblock.getPos().getY(), isourceblock.getPos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), CraftVector.toBukkit(entityitem.getDeltaMovement()));
        if (!BlockDispenser.eventFired) {
            world.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            return false;
        }

        entityitem.setItem(CraftItemStack.asNMSCopy(event.getItem()));
        entityitem.setDeltaMovement(CraftVector.toNMS(event.getVelocity()));

        if (!dropper && !event.getItem().getType().equals(craftItem.getType())) {
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != IDispenseBehavior.NOOP && idispensebehavior.getClass() != DispenseBehaviorItem.class) {
                idispensebehavior.dispense(isourceblock, eventStack);
            } else {
                world.addFreshEntity(entityitem);
            }
            return false;
        }

        world.addFreshEntity(entityitem);

        return true;
        // CraftBukkit end
    }

    protected void playSound(ISourceBlock isourceblock) {
        isourceblock.getLevel().levelEvent(1000, isourceblock.getPos(), 0);
    }

    protected void playAnimation(ISourceBlock isourceblock, EnumDirection enumdirection) {
        isourceblock.getLevel().levelEvent(2000, isourceblock.getPos(), enumdirection.get3DDataValue());
    }
}
