package net.minecraft.core.dispenser;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlock;
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
    public final ItemStack dispense(SourceBlock sourceblock, ItemStack itemstack) {
        ItemStack itemstack1 = this.execute(sourceblock, itemstack);

        this.playSound(sourceblock);
        this.playAnimation(sourceblock, (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING));
        return itemstack1;
    }

    protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
        IPosition iposition = BlockDispenser.getDispensePosition(sourceblock);
        ItemStack itemstack1 = itemstack.split(1);

        // CraftBukkit start
        if (!spawnItem(sourceblock.level(), itemstack1, 6, enumdirection, sourceblock, dropper)) {
            itemstack.grow(1);
        }
        // CraftBukkit end
        return itemstack;
    }

    public static void spawnItem(World world, ItemStack itemstack, int i, EnumDirection enumdirection, IPosition iposition) {
        // CraftBukkit start
        EntityItem entityitem = prepareItem(world, itemstack, i, enumdirection, iposition);
        world.addFreshEntity(entityitem);
    }

    private static EntityItem prepareItem(World world, ItemStack itemstack, int i, EnumDirection enumdirection, IPosition iposition) {
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
        return entityitem;
    }

    // CraftBukkit - void -> boolean return, IPosition -> ISourceBlock last argument, dropper
    public static boolean spawnItem(World world, ItemStack itemstack, int i, EnumDirection enumdirection, SourceBlock sourceblock, boolean dropper) {
        if (itemstack.isEmpty()) return true;
        IPosition iposition = BlockDispenser.getDispensePosition(sourceblock);
        EntityItem entityitem = prepareItem(world, itemstack, i, enumdirection, iposition);

        org.bukkit.block.Block block = CraftBlock.at(world, sourceblock.pos());
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
                idispensebehavior.dispense(sourceblock, eventStack);
            } else {
                world.addFreshEntity(entityitem);
            }
            return false;
        }

        world.addFreshEntity(entityitem);

        return true;
        // CraftBukkit end
    }

    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(1000, sourceblock.pos(), 0);
    }

    protected void playAnimation(SourceBlock sourceblock, EnumDirection enumdirection) {
        sourceblock.level().levelEvent(2000, sourceblock.pos(), enumdirection.get3DDataValue());
    }
}
