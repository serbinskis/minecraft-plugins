package net.minecraft.core.dispenser;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;

// CraftBukkit start
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

public abstract class DispenseBehaviorProjectile extends DispenseBehaviorItem {

    public DispenseBehaviorProjectile() {}

    @Override
    public ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        WorldServer worldserver = sourceblock.level();
        IPosition iposition = BlockDispenser.getDispensePosition(sourceblock);
        EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
        IProjectile iprojectile = this.getProjectile(worldserver, iposition, itemstack);

        // CraftBukkit start
        // iprojectile.shoot((double) enumdirection.getStepX(), (double) ((float) enumdirection.getStepY() + 0.1F), (double) enumdirection.getStepZ(), this.getPower(), this.getUncertainty());
        ItemStack itemstack1 = itemstack.split(1);
        org.bukkit.block.Block block = CraftBlock.at(worldserver, sourceblock.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector((double) enumdirection.getStepX(), (double) ((float) enumdirection.getStepY() + 0.1F), (double) enumdirection.getStepZ()));
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
                idispensebehavior.dispense(sourceblock, eventStack);
                return itemstack;
            }
        }

        iprojectile.shoot(event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), this.getPower(), this.getUncertainty());
        ((Entity) iprojectile).projectileSource = new org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource(sourceblock.blockEntity());
        // CraftBukkit end
        worldserver.addFreshEntity(iprojectile);
        // itemstack.shrink(1); // CraftBukkit - Handled during event processing
        return itemstack;
    }

    @Override
    protected void playSound(SourceBlock sourceblock) {
        sourceblock.level().levelEvent(1002, sourceblock.pos(), 0);
    }

    protected abstract IProjectile getProjectile(World world, IPosition iposition, ItemStack itemstack);

    protected float getUncertainty() {
        return 6.0F;
    }

    protected float getPower() {
        return 1.1F;
    }
}
