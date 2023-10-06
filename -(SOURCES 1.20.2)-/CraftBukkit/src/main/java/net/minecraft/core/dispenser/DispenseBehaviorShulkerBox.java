package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContextDirectional;
import net.minecraft.world.level.block.BlockDispenser;
import org.slf4j.Logger;

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

public class DispenseBehaviorShulkerBox extends DispenseBehaviorMaybe {

    private static final Logger LOGGER = LogUtils.getLogger();

    public DispenseBehaviorShulkerBox() {}

    @Override
    protected ItemStack execute(SourceBlock sourceblock, ItemStack itemstack) {
        this.setSuccess(false);
        Item item = itemstack.getItem();

        if (item instanceof ItemBlock) {
            EnumDirection enumdirection = (EnumDirection) sourceblock.state().getValue(BlockDispenser.FACING);
            BlockPosition blockposition = sourceblock.pos().relative(enumdirection);
            EnumDirection enumdirection1 = sourceblock.level().isEmptyBlock(blockposition.below()) ? enumdirection : EnumDirection.UP;

            // CraftBukkit start
            org.bukkit.block.Block bukkitBlock = CraftBlock.at(sourceblock.level(), sourceblock.pos());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);

            BlockDispenseEvent event = new BlockDispenseEvent(bukkitBlock, craftItem.clone(), new org.bukkit.util.Vector(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            if (!BlockDispenser.eventFired) {
                sourceblock.level().getCraftServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                return itemstack;
            }

            if (!event.getItem().equals(craftItem)) {
                // Chain to handler for new item
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.DISPENSER_REGISTRY.get(eventStack.getItem());
                if (idispensebehavior != IDispenseBehavior.NOOP && idispensebehavior != this) {
                    idispensebehavior.dispense(sourceblock, eventStack);
                    return itemstack;
                }
            }
            // CraftBukkit end

            try {
                this.setSuccess(((ItemBlock) item).place(new BlockActionContextDirectional(sourceblock.level(), blockposition, enumdirection, itemstack, enumdirection1)).consumesAction());
            } catch (Exception exception) {
                DispenseBehaviorShulkerBox.LOGGER.error("Error trying to place shulker box at {}", blockposition, exception);
            }
        }

        return itemstack;
    }
}
