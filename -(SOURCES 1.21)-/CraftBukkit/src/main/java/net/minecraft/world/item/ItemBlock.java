package net.minecraft.world.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockShulkerBox;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

// CraftBukkit start
import net.minecraft.server.level.WorldServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
// CraftBukkit end

public class ItemBlock extends Item {

    /** @deprecated */
    @Deprecated
    private final Block block;

    public ItemBlock(Block block, Item.Info item_info) {
        super(item_info);
        this.block = block;
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        EnumInteractionResult enuminteractionresult = this.place(new BlockActionContext(itemactioncontext));

        if (!enuminteractionresult.consumesAction() && itemactioncontext.getItemInHand().has(DataComponents.FOOD)) {
            EnumInteractionResult enuminteractionresult1 = super.use(itemactioncontext.getLevel(), itemactioncontext.getPlayer(), itemactioncontext.getHand()).getResult();

            return enuminteractionresult1 == EnumInteractionResult.CONSUME ? EnumInteractionResult.CONSUME_PARTIAL : enuminteractionresult1;
        } else {
            return enuminteractionresult;
        }
    }

    public EnumInteractionResult place(BlockActionContext blockactioncontext) {
        if (!this.getBlock().isEnabled(blockactioncontext.getLevel().enabledFeatures())) {
            return EnumInteractionResult.FAIL;
        } else if (!blockactioncontext.canPlace()) {
            return EnumInteractionResult.FAIL;
        } else {
            BlockActionContext blockactioncontext1 = this.updatePlacementContext(blockactioncontext);

            if (blockactioncontext1 == null) {
                return EnumInteractionResult.FAIL;
            } else {
                IBlockData iblockdata = this.getPlacementState(blockactioncontext1);
                // CraftBukkit start - special case for handling block placement with water lilies and snow buckets
                org.bukkit.block.BlockState blockstate = null;
                if (this instanceof PlaceOnWaterBlockItem || this instanceof SolidBucketItem) {
                    blockstate = org.bukkit.craftbukkit.block.CraftBlockStates.getBlockState(blockactioncontext1.getLevel(), blockactioncontext1.getClickedPos());
                }
                // CraftBukkit end

                if (iblockdata == null) {
                    return EnumInteractionResult.FAIL;
                } else if (!this.placeBlock(blockactioncontext1, iblockdata)) {
                    return EnumInteractionResult.FAIL;
                } else {
                    BlockPosition blockposition = blockactioncontext1.getClickedPos();
                    World world = blockactioncontext1.getLevel();
                    EntityHuman entityhuman = blockactioncontext1.getPlayer();
                    ItemStack itemstack = blockactioncontext1.getItemInHand();
                    IBlockData iblockdata1 = world.getBlockState(blockposition);

                    if (iblockdata1.is(iblockdata.getBlock())) {
                        iblockdata1 = this.updateBlockStateFromTag(blockposition, world, itemstack, iblockdata1);
                        this.updateCustomBlockEntityTag(blockposition, world, entityhuman, itemstack, iblockdata1);
                        updateBlockEntityComponents(world, blockposition, itemstack);
                        iblockdata1.getBlock().setPlacedBy(world, blockposition, iblockdata1, entityhuman, itemstack);
                        // CraftBukkit start
                        if (blockstate != null) {
                            org.bukkit.event.block.BlockPlaceEvent placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent((WorldServer) world, entityhuman, blockactioncontext1.getHand(), blockstate, blockposition.getX(), blockposition.getY(), blockposition.getZ());
                            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                                blockstate.update(true, false);

                                if (this instanceof SolidBucketItem) {
                                    ((EntityPlayer) entityhuman).getBukkitEntity().updateInventory(); // SPIGOT-4541
                                }
                                return EnumInteractionResult.FAIL;
                            }
                        }
                        // CraftBukkit end
                        if (entityhuman instanceof EntityPlayer) {
                            CriterionTriggers.PLACED_BLOCK.trigger((EntityPlayer) entityhuman, blockposition, itemstack);
                        }
                    }

                    SoundEffectType soundeffecttype = iblockdata1.getSoundType();

                    // world.playSound(entityhuman, blockposition, this.getPlaceSound(iblockdata1), SoundCategory.BLOCKS, (soundeffecttype.getVolume() + 1.0F) / 2.0F, soundeffecttype.getPitch() * 0.8F);
                    world.gameEvent((Holder) GameEvent.BLOCK_PLACE, blockposition, GameEvent.a.of(entityhuman, iblockdata1));
                    itemstack.consume(1, entityhuman);
                    return EnumInteractionResult.sidedSuccess(world.isClientSide);
                }
            }
        }
    }

    protected SoundEffect getPlaceSound(IBlockData iblockdata) {
        return iblockdata.getSoundType().getPlaceSound();
    }

    @Nullable
    public BlockActionContext updatePlacementContext(BlockActionContext blockactioncontext) {
        return blockactioncontext;
    }

    private static void updateBlockEntityComponents(World world, BlockPosition blockposition, ItemStack itemstack) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity != null) {
            tileentity.applyComponentsFromItemStack(itemstack);
            tileentity.setChanged();
        }

    }

    protected boolean updateCustomBlockEntityTag(BlockPosition blockposition, World world, @Nullable EntityHuman entityhuman, ItemStack itemstack, IBlockData iblockdata) {
        return updateCustomBlockEntityTag(world, entityhuman, blockposition, itemstack);
    }

    @Nullable
    protected IBlockData getPlacementState(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = this.getBlock().getStateForPlacement(blockactioncontext);

        return iblockdata != null && this.canPlace(blockactioncontext, iblockdata) ? iblockdata : null;
    }

    private IBlockData updateBlockStateFromTag(BlockPosition blockposition, World world, ItemStack itemstack, IBlockData iblockdata) {
        BlockItemStateProperties blockitemstateproperties = (BlockItemStateProperties) itemstack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);

        if (blockitemstateproperties.isEmpty()) {
            return iblockdata;
        } else {
            IBlockData iblockdata1 = blockitemstateproperties.apply(iblockdata);

            if (iblockdata1 != iblockdata) {
                world.setBlock(blockposition, iblockdata1, 2);
            }

            return iblockdata1;
        }
    }

    protected boolean canPlace(BlockActionContext blockactioncontext, IBlockData iblockdata) {
        EntityHuman entityhuman = blockactioncontext.getPlayer();
        VoxelShapeCollision voxelshapecollision = entityhuman == null ? VoxelShapeCollision.empty() : VoxelShapeCollision.of(entityhuman);
        // CraftBukkit start - store default return
        boolean defaultReturn = (!this.mustSurvive() || iblockdata.canSurvive(blockactioncontext.getLevel(), blockactioncontext.getClickedPos())) && blockactioncontext.getLevel().isUnobstructed(iblockdata, blockactioncontext.getClickedPos(), voxelshapecollision);
        org.bukkit.entity.Player player = (blockactioncontext.getPlayer() instanceof EntityPlayer) ? (org.bukkit.entity.Player) blockactioncontext.getPlayer().getBukkitEntity() : null;

        BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at(blockactioncontext.getLevel(), blockactioncontext.getClickedPos()), player, CraftBlockData.fromData(iblockdata), defaultReturn);
        blockactioncontext.getLevel().getCraftServer().getPluginManager().callEvent(event);

        return event.isBuildable();
        // CraftBukkit end
    }

    protected boolean mustSurvive() {
        return true;
    }

    protected boolean placeBlock(BlockActionContext blockactioncontext, IBlockData iblockdata) {
        return blockactioncontext.getLevel().setBlock(blockactioncontext.getClickedPos(), iblockdata, 11);
    }

    public static boolean updateCustomBlockEntityTag(World world, @Nullable EntityHuman entityhuman, BlockPosition blockposition, ItemStack itemstack) {
        MinecraftServer minecraftserver = world.getServer();

        if (minecraftserver == null) {
            return false;
        } else {
            CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);

            if (!customdata.isEmpty()) {
                TileEntity tileentity = world.getBlockEntity(blockposition);

                if (tileentity != null) {
                    if (!world.isClientSide && tileentity.onlyOpCanSetNbt() && (entityhuman == null || !entityhuman.canUseGameMasterBlocks())) {
                        return false;
                    }

                    return customdata.loadInto(tileentity, world.registryAccess());
                }
            }

            return false;
        }
    }

    @Override
    public String getDescriptionId() {
        return this.getBlock().getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        this.getBlock().appendHoverText(itemstack, item_b, list, tooltipflag);
    }

    public Block getBlock() {
        return this.block;
    }

    public void registerBlocks(Map<Block, Item> map, Item item) {
        map.put(this.getBlock(), item);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(this.getBlock() instanceof BlockShulkerBox);
    }

    @Override
    public void onDestroyed(EntityItem entityitem) {
        ItemContainerContents itemcontainercontents = (ItemContainerContents) entityitem.getItem().set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        if (itemcontainercontents != null) {
            ItemLiquidUtil.onContainerDestroyed(entityitem, itemcontainercontents.nonEmptyItemsCopy());
        }

    }

    public static void setBlockEntityData(ItemStack itemstack, TileEntityTypes<?> tileentitytypes, NBTTagCompound nbttagcompound) {
        nbttagcompound.remove("id");
        if (nbttagcompound.isEmpty()) {
            itemstack.remove(DataComponents.BLOCK_ENTITY_DATA);
        } else {
            TileEntity.addEntityType(nbttagcompound, tileentitytypes);
            itemstack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbttagcompound));
        }

    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.getBlock().requiredFeatures();
    }
}
