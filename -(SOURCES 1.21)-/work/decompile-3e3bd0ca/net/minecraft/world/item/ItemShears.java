package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockGrowingTop;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemShears extends Item {

    public ItemShears(Item.Info item_info) {
        super(item_info);
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(Tool.a.minesAndDrops(List.of(Blocks.COBWEB), 15.0F), Tool.a.overrideSpeed(TagsBlock.LEAVES, 15.0F), Tool.a.overrideSpeed(TagsBlock.WOOL, 5.0F), Tool.a.overrideSpeed(List.of(Blocks.VINE, Blocks.GLOW_LICHEN), 2.0F)), 1.0F, 1);
    }

    @Override
    public boolean mineBlock(ItemStack itemstack, World world, IBlockData iblockdata, BlockPosition blockposition, EntityLiving entityliving) {
        if (!world.isClientSide && !iblockdata.is(TagsBlock.FIRE)) {
            itemstack.hurtAndBreak(1, entityliving, EnumItemSlot.MAINHAND);
        }

        return iblockdata.is(TagsBlock.LEAVES) || iblockdata.is(Blocks.COBWEB) || iblockdata.is(Blocks.SHORT_GRASS) || iblockdata.is(Blocks.FERN) || iblockdata.is(Blocks.DEAD_BUSH) || iblockdata.is(Blocks.HANGING_ROOTS) || iblockdata.is(Blocks.VINE) || iblockdata.is(Blocks.TRIPWIRE) || iblockdata.is(TagsBlock.WOOL);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);
        Block block = iblockdata.getBlock();

        if (block instanceof BlockGrowingTop blockgrowingtop) {
            if (!blockgrowingtop.isMaxAge(iblockdata)) {
                EntityHuman entityhuman = itemactioncontext.getPlayer();
                ItemStack itemstack = itemactioncontext.getItemInHand();

                if (entityhuman instanceof EntityPlayer) {
                    CriterionTriggers.ITEM_USED_ON_BLOCK.trigger((EntityPlayer) entityhuman, blockposition, itemstack);
                }

                world.playSound(entityhuman, blockposition, SoundEffects.GROWING_PLANT_CROP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                IBlockData iblockdata1 = blockgrowingtop.getMaxAgeState(iblockdata);

                world.setBlockAndUpdate(blockposition, iblockdata1);
                world.gameEvent((Holder) GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(itemactioncontext.getPlayer(), iblockdata1));
                if (entityhuman != null) {
                    itemstack.hurtAndBreak(1, entityhuman, EntityLiving.getSlotForHand(itemactioncontext.getHand()));
                }

                return EnumInteractionResult.sidedSuccess(world.isClientSide);
            }
        }

        return super.useOn(itemactioncontext);
    }
}
