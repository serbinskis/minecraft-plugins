package net.minecraft.world.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;

public class ItemCompass extends Item {

    public ItemCompass(Item.Info item_info) {
        super(item_info);
    }

    @Nullable
    public static GlobalPos getSpawnPosition(World world) {
        return world.dimensionType().natural() ? GlobalPos.of(world.dimension(), world.getSharedSpawnPos()) : null;
    }

    @Override
    public boolean isFoil(ItemStack itemstack) {
        return itemstack.has(DataComponents.LODESTONE_TRACKER) || super.isFoil(itemstack);
    }

    @Override
    public void inventoryTick(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {
        if (world instanceof WorldServer worldserver) {
            LodestoneTracker lodestonetracker = (LodestoneTracker) itemstack.get(DataComponents.LODESTONE_TRACKER);

            if (lodestonetracker != null) {
                LodestoneTracker lodestonetracker1 = lodestonetracker.tick(worldserver);

                if (lodestonetracker1 != lodestonetracker) {
                    itemstack.set(DataComponents.LODESTONE_TRACKER, lodestonetracker1);
                }
            }
        }

    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        World world = itemactioncontext.getLevel();

        if (!world.getBlockState(blockposition).is(Blocks.LODESTONE)) {
            return super.useOn(itemactioncontext);
        } else {
            world.playSound((EntityHuman) null, blockposition, SoundEffects.LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            EntityHuman entityhuman = itemactioncontext.getPlayer();
            ItemStack itemstack = itemactioncontext.getItemInHand();
            boolean flag = !entityhuman.hasInfiniteMaterials() && itemstack.getCount() == 1;
            LodestoneTracker lodestonetracker = new LodestoneTracker(Optional.of(GlobalPos.of(world.dimension(), blockposition)), true);

            if (flag) {
                itemstack.set(DataComponents.LODESTONE_TRACKER, lodestonetracker);
            } else {
                ItemStack itemstack1 = itemstack.transmuteCopy(Items.COMPASS, 1);

                itemstack.consume(1, entityhuman);
                itemstack1.set(DataComponents.LODESTONE_TRACKER, lodestonetracker);
                if (!entityhuman.getInventory().add(itemstack1)) {
                    entityhuman.drop(itemstack1, false);
                }
            }

            return EnumInteractionResult.sidedSuccess(world.isClientSide);
        }
    }

    @Override
    public String getDescriptionId(ItemStack itemstack) {
        return itemstack.has(DataComponents.LODESTONE_TRACKER) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemstack);
    }
}
