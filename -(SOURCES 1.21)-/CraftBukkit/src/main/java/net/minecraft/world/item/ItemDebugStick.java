// mc-dev import
package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;

public class ItemDebugStick extends Item {

    public ItemDebugStick(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public boolean canAttackBlock(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman) {
        if (!world.isClientSide) {
            this.handleInteraction(entityhuman, iblockdata, world, blockposition, false, entityhuman.getItemInHand(EnumHand.MAIN_HAND));
        }

        return false;
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        EntityHuman entityhuman = itemactioncontext.getPlayer();
        World world = itemactioncontext.getLevel();

        if (!world.isClientSide && entityhuman != null) {
            BlockPosition blockposition = itemactioncontext.getClickedPos();

            if (!this.handleInteraction(entityhuman, world.getBlockState(blockposition), world, blockposition, true, itemactioncontext.getItemInHand())) {
                return EnumInteractionResult.FAIL;
            }
        }

        return EnumInteractionResult.sidedSuccess(world.isClientSide);
    }

    public boolean handleInteraction(EntityHuman entityhuman, IBlockData iblockdata, GeneratorAccess generatoraccess, BlockPosition blockposition, boolean flag, ItemStack itemstack) {
        if (!entityhuman.canUseGameMasterBlocks()) {
            return false;
        } else {
            Holder<Block> holder = iblockdata.getBlockHolder();
            BlockStateList<Block, IBlockData> blockstatelist = ((Block) holder.value()).getStateDefinition();
            Collection<IBlockState<?>> collection = blockstatelist.getProperties();

            if (collection.isEmpty()) {
                message(entityhuman, IChatBaseComponent.translatable(this.getDescriptionId() + ".empty", holder.getRegisteredName()));
                return false;
            } else {
                DebugStickState debugstickstate = (DebugStickState) itemstack.get(DataComponents.DEBUG_STICK_STATE);

                if (debugstickstate == null) {
                    return false;
                } else {
                    IBlockState<?> iblockstate = (IBlockState) debugstickstate.properties().get(holder);

                    if (flag) {
                        if (iblockstate == null) {
                            iblockstate = (IBlockState) collection.iterator().next();
                        }

                        IBlockData iblockdata1 = cycleState(iblockdata, iblockstate, entityhuman.isSecondaryUseActive());

                        generatoraccess.setBlock(blockposition, iblockdata1, 18);
                        message(entityhuman, IChatBaseComponent.translatable(this.getDescriptionId() + ".update", iblockstate.getName(), getNameHelper(iblockdata1, iblockstate)));
                    } else {
                        iblockstate = (IBlockState) getRelative(collection, iblockstate, entityhuman.isSecondaryUseActive());
                        itemstack.set(DataComponents.DEBUG_STICK_STATE, debugstickstate.withProperty(holder, iblockstate));
                        message(entityhuman, IChatBaseComponent.translatable(this.getDescriptionId() + ".select", iblockstate.getName(), getNameHelper(iblockdata, iblockstate)));
                    }

                    return true;
                }
            }
        }
    }

    private static <T extends Comparable<T>> IBlockData cycleState(IBlockData iblockdata, IBlockState<T> iblockstate, boolean flag) {
        return (IBlockData) iblockdata.setValue(iblockstate, getRelative(iblockstate.getPossibleValues(), iblockdata.getValue(iblockstate), flag)); // CraftBukkit - decompile error
    }

    private static <T> T getRelative(Iterable<T> iterable, @Nullable T t0, boolean flag) {
        return flag ? SystemUtils.findPreviousInIterable(iterable, t0) : SystemUtils.findNextInIterable(iterable, t0);
    }

    private static void message(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        ((EntityPlayer) entityhuman).sendSystemMessage(ichatbasecomponent, true);
    }

    private static <T extends Comparable<T>> String getNameHelper(IBlockData iblockdata, IBlockState<T> iblockstate) {
        return iblockstate.getName(iblockdata.getValue(iblockstate));
    }
}
