package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

// CraftBukkit start
import java.util.ArrayList;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemBisected;
import net.minecraft.world.level.block.BlockCake;
import net.minecraft.world.level.block.BlockDoor;
import net.minecraft.world.level.block.BlockTrapdoor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockPropertyDoubleBlockHalf;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
// CraftBukkit end

public class PlayerInteractManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    protected WorldServer level;
    protected final EntityPlayer player;
    private EnumGamemode gameModeForPlayer;
    @Nullable
    private EnumGamemode previousGameModeForPlayer;
    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPosition destroyPos;
    private int gameTicks;
    private boolean hasDelayedDestroy;
    private BlockPosition delayedDestroyPos;
    private int delayedTickStart;
    private int lastSentState;

    public PlayerInteractManager(EntityPlayer entityplayer) {
        this.gameModeForPlayer = EnumGamemode.DEFAULT_MODE;
        this.destroyPos = BlockPosition.ZERO;
        this.delayedDestroyPos = BlockPosition.ZERO;
        this.lastSentState = -1;
        this.player = entityplayer;
        this.level = entityplayer.serverLevel();
    }

    public boolean changeGameModeForPlayer(EnumGamemode enumgamemode) {
        if (enumgamemode == this.gameModeForPlayer) {
            return false;
        } else {
            // CraftBukkit start
            PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(player.getBukkitEntity(), GameMode.getByValue(enumgamemode.getId()));
            level.getCraftServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            // CraftBukkit end
            this.setGameModeForPlayer(enumgamemode, this.previousGameModeForPlayer);
            this.player.onUpdateAbilities();
            this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.UPDATE_GAME_MODE, this.player), this.player); // CraftBukkit
            this.level.updateSleepingPlayerList();
            return true;
        }
    }

    protected void setGameModeForPlayer(EnumGamemode enumgamemode, @Nullable EnumGamemode enumgamemode1) {
        this.previousGameModeForPlayer = enumgamemode1;
        this.gameModeForPlayer = enumgamemode;
        enumgamemode.updatePlayerAbilities(this.player.getAbilities());
    }

    public EnumGamemode getGameModeForPlayer() {
        return this.gameModeForPlayer;
    }

    @Nullable
    public EnumGamemode getPreviousGameModeForPlayer() {
        return this.previousGameModeForPlayer;
    }

    public boolean isSurvival() {
        return this.gameModeForPlayer.isSurvival();
    }

    public boolean isCreative() {
        return this.gameModeForPlayer.isCreative();
    }

    public void tick() {
        this.gameTicks = MinecraftServer.currentTick; // CraftBukkit;
        IBlockData iblockdata;

        if (this.hasDelayedDestroy) {
            iblockdata = this.level.getBlockState(this.delayedDestroyPos);
            if (iblockdata.isAir()) {
                this.hasDelayedDestroy = false;
            } else {
                float f = this.incrementDestroyProgress(iblockdata, this.delayedDestroyPos, this.delayedTickStart);

                if (f >= 1.0F) {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock(this.delayedDestroyPos);
                }
            }
        } else if (this.isDestroyingBlock) {
            iblockdata = this.level.getBlockState(this.destroyPos);
            if (iblockdata.isAir()) {
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
            } else {
                this.incrementDestroyProgress(iblockdata, this.destroyPos, this.destroyProgressStart);
            }
        }

    }

    private float incrementDestroyProgress(IBlockData iblockdata, BlockPosition blockposition, int i) {
        int j = this.gameTicks - i;
        float f = iblockdata.getDestroyProgress(this.player, this.player.level(), blockposition) * (float) (j + 1);
        int k = (int) (f * 10.0F);

        if (k != this.lastSentState) {
            this.level.destroyBlockProgress(this.player.getId(), blockposition, k);
            this.lastSentState = k;
        }

        return f;
    }

    private void debugLogging(BlockPosition blockposition, boolean flag, int i, String s) {}

    public void handleBlockBreakAction(BlockPosition blockposition, PacketPlayInBlockDig.EnumPlayerDigType packetplayinblockdig_enumplayerdigtype, EnumDirection enumdirection, int i, int j) {
        if (this.player.getEyePosition().distanceToSqr(Vec3D.atCenterOf(blockposition)) > PlayerConnection.MAX_INTERACTION_DISTANCE) {
            this.debugLogging(blockposition, false, j, "too far");
        } else if (blockposition.getY() >= i) {
            this.player.connection.send(new PacketPlayOutBlockChange(blockposition, this.level.getBlockState(blockposition)));
            this.debugLogging(blockposition, false, j, "too high");
        } else {
            IBlockData iblockdata;

            if (packetplayinblockdig_enumplayerdigtype == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                if (!this.level.mayInteract(this.player, blockposition)) {
                    // CraftBukkit start - fire PlayerInteractEvent
                    CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, enumdirection, this.player.getInventory().getSelected(), EnumHand.MAIN_HAND);
                    this.player.connection.send(new PacketPlayOutBlockChange(blockposition, this.level.getBlockState(blockposition)));
                    this.debugLogging(blockposition, false, j, "may not interact");
                    // Update any tile entity data for this block
                    TileEntity tileentity = level.getBlockEntity(blockposition);
                    if (tileentity != null) {
                        this.player.connection.send(tileentity.getUpdatePacket());
                    }
                    // CraftBukkit end
                    return;
                }

                // CraftBukkit start
                PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, enumdirection, this.player.getInventory().getSelected(), EnumHand.MAIN_HAND);
                if (event.isCancelled()) {
                    // Let the client know the block still exists
                    this.player.connection.send(new PacketPlayOutBlockChange(this.level, blockposition));
                    // Update any tile entity data for this block
                    TileEntity tileentity = this.level.getBlockEntity(blockposition);
                    if (tileentity != null) {
                        this.player.connection.send(tileentity.getUpdatePacket());
                    }
                    return;
                }
                // CraftBukkit end

                if (this.isCreative()) {
                    this.destroyAndAck(blockposition, j, "creative destroy");
                    return;
                }

                // Spigot start - handle debug stick left click for non-creative
                if (this.player.getMainHandItem().is(net.minecraft.world.item.Items.DEBUG_STICK)
                        && ((net.minecraft.world.item.ItemDebugStick) net.minecraft.world.item.Items.DEBUG_STICK).handleInteraction(this.player, this.level.getBlockState(blockposition), this.level, blockposition, false, this.player.getMainHandItem())) {
                    this.player.connection.send(new PacketPlayOutBlockChange(this.level, blockposition));
                    return;
                }
                // Spigot end

                if (this.player.blockActionRestricted(this.level, blockposition, this.gameModeForPlayer)) {
                    this.player.connection.send(new PacketPlayOutBlockChange(blockposition, this.level.getBlockState(blockposition)));
                    this.debugLogging(blockposition, false, j, "block action restricted");
                    return;
                }

                this.destroyProgressStart = this.gameTicks;
                float f = 1.0F;

                iblockdata = this.level.getBlockState(blockposition);
                // CraftBukkit start - Swings at air do *NOT* exist.
                if (event.useInteractedBlock() == Event.Result.DENY) {
                    // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
                    IBlockData data = this.level.getBlockState(blockposition);
                    if (data.getBlock() instanceof BlockDoor) {
                        // For some reason *BOTH* the bottom/top part have to be marked updated.
                        boolean bottom = data.getValue(BlockDoor.HALF) == BlockPropertyDoubleBlockHalf.LOWER;
                        this.player.connection.send(new PacketPlayOutBlockChange(this.level, blockposition));
                        this.player.connection.send(new PacketPlayOutBlockChange(this.level, bottom ? blockposition.above() : blockposition.below()));
                    } else if (data.getBlock() instanceof BlockTrapdoor) {
                        this.player.connection.send(new PacketPlayOutBlockChange(this.level, blockposition));
                    }
                } else if (!iblockdata.isAir()) {
                    iblockdata.attack(this.level, blockposition, this.player);
                    f = iblockdata.getDestroyProgress(this.player, this.player.level(), blockposition);
                }

                if (event.useItemInHand() == Event.Result.DENY) {
                    // If we 'insta destroyed' then the client needs to be informed.
                    if (f > 1.0f) {
                        this.player.connection.send(new PacketPlayOutBlockChange(this.level, blockposition));
                    }
                    return;
                }
                org.bukkit.event.block.BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.player, blockposition, this.player.getInventory().getSelected(), f >= 1.0f);

                if (blockEvent.isCancelled()) {
                    // Let the client know the block still exists
                    this.player.connection.send(new PacketPlayOutBlockChange(this.level, blockposition));
                    return;
                }

                if (blockEvent.getInstaBreak()) {
                    f = 2.0f;
                }
                // CraftBukkit end

                if (!iblockdata.isAir() && f >= 1.0F) {
                    this.destroyAndAck(blockposition, j, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        this.player.connection.send(new PacketPlayOutBlockChange(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                        this.debugLogging(blockposition, false, j, "abort destroying since another started (client insta mine, server disagreed)");
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = blockposition.immutable();
                    int k = (int) (f * 10.0F);

                    this.level.destroyBlockProgress(this.player.getId(), blockposition, k);
                    this.debugLogging(blockposition, true, j, "actual start of destroying");
                    this.lastSentState = k;
                }
            } else if (packetplayinblockdig_enumplayerdigtype == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                if (blockposition.equals(this.destroyPos)) {
                    int l = this.gameTicks - this.destroyProgressStart;

                    iblockdata = this.level.getBlockState(blockposition);
                    if (!iblockdata.isAir()) {
                        float f1 = iblockdata.getDestroyProgress(this.player, this.player.level(), blockposition) * (float) (l + 1);

                        if (f1 >= 0.7F) {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), blockposition, -1);
                            this.destroyAndAck(blockposition, j, "destroyed");
                            return;
                        }

                        if (!this.hasDelayedDestroy) {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = blockposition;
                            this.delayedTickStart = this.destroyProgressStart;
                        }
                    }
                }

                this.debugLogging(blockposition, true, j, "stopped destroying");
            } else if (packetplayinblockdig_enumplayerdigtype == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                this.isDestroyingBlock = false;
                if (!Objects.equals(this.destroyPos, blockposition)) {
                    PlayerInteractManager.LOGGER.debug("Mismatch in destroy block pos: {} {}", this.destroyPos, blockposition); // CraftBukkit - SPIGOT-5457 sent by client when interact event cancelled
                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.debugLogging(blockposition, true, j, "aborted mismatched destroying");
                }

                this.level.destroyBlockProgress(this.player.getId(), blockposition, -1);
                this.debugLogging(blockposition, true, j, "aborted destroying");

                CraftEventFactory.callBlockDamageAbortEvent(this.player, blockposition, this.player.getInventory().getSelected()); // CraftBukkit
            }

        }
    }

    public void destroyAndAck(BlockPosition blockposition, int i, String s) {
        if (this.destroyBlock(blockposition)) {
            this.debugLogging(blockposition, true, i, s);
        } else {
            this.player.connection.send(new PacketPlayOutBlockChange(blockposition, this.level.getBlockState(blockposition)));
            this.debugLogging(blockposition, false, i, s);
        }

    }

    public boolean destroyBlock(BlockPosition blockposition) {
        IBlockData iblockdata = this.level.getBlockState(blockposition);
        // CraftBukkit start - fire BlockBreakEvent
        org.bukkit.block.Block bblock = CraftBlock.at(level, blockposition);
        BlockBreakEvent event = null;

        if (this.player instanceof EntityPlayer) {
            // Sword + Creative mode pre-cancel
            boolean isSwordNoBreak = !this.player.getMainHandItem().getItem().canAttackBlock(iblockdata, this.level, blockposition, this.player);

            // Tell client the block is gone immediately then process events
            // Don't tell the client if its a creative sword break because its not broken!
            if (level.getBlockEntity(blockposition) == null && !isSwordNoBreak) {
                PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(blockposition, Blocks.AIR.defaultBlockState());
                this.player.connection.send(packet);
            }

            event = new BlockBreakEvent(bblock, this.player.getBukkitEntity());

            // Sword + Creative mode pre-cancel
            event.setCancelled(isSwordNoBreak);

            // Calculate default block experience
            IBlockData nmsData = this.level.getBlockState(blockposition);
            Block nmsBlock = nmsData.getBlock();

            ItemStack itemstack = this.player.getItemBySlot(EnumItemSlot.MAINHAND);

            if (nmsBlock != null && !event.isCancelled() && !this.isCreative() && this.player.hasCorrectToolForDrops(nmsBlock.defaultBlockState())) {
                event.setExpToDrop(nmsBlock.getExpDrop(nmsData, this.level, blockposition, itemstack, true));
            }

            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                if (isSwordNoBreak) {
                    return false;
                }
                // Let the client know the block still exists
                this.player.connection.send(new PacketPlayOutBlockChange(this.level, blockposition));

                // Brute force all possible updates
                for (EnumDirection dir : EnumDirection.values()) {
                    this.player.connection.send(new PacketPlayOutBlockChange(level, blockposition.relative(dir)));
                }

                // Update any tile entity data for this block
                TileEntity tileentity = this.level.getBlockEntity(blockposition);
                if (tileentity != null) {
                    this.player.connection.send(tileentity.getUpdatePacket());
                }
                return false;
            }
        }
        // CraftBukkit end

        if (false && !this.player.getMainHandItem().getItem().canAttackBlock(iblockdata, this.level, blockposition, this.player)) { // CraftBukkit - false
            return false;
        } else {
            iblockdata = this.level.getBlockState(blockposition); // CraftBukkit - update state from plugins
            if (iblockdata.isAir()) return false; // CraftBukkit - A plugin set block to air without cancelling
            TileEntity tileentity = this.level.getBlockEntity(blockposition);
            Block block = iblockdata.getBlock();

            if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
                this.level.sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
                return false;
            } else if (this.player.blockActionRestricted(this.level, blockposition, this.gameModeForPlayer)) {
                return false;
            } else {
                // CraftBukkit start
                org.bukkit.block.BlockState state = bblock.getState();
                level.captureDrops = new ArrayList<>();
                // CraftBukkit end
                block.playerWillDestroy(this.level, blockposition, iblockdata, this.player);
                boolean flag = this.level.removeBlock(blockposition, false);

                if (flag) {
                    block.destroy(this.level, blockposition, iblockdata);
                }

                if (this.isCreative()) {
                    // return true; // CraftBukkit
                } else {
                    ItemStack itemstack = this.player.getMainHandItem();
                    ItemStack itemstack1 = itemstack.copy();
                    boolean flag1 = this.player.hasCorrectToolForDrops(iblockdata);

                    itemstack.mineBlock(this.level, iblockdata, blockposition, this.player);
                    if (flag && flag1 && event.isDropItems()) { // CraftBukkit - Check if block should drop items
                        block.playerDestroy(this.level, this.player, blockposition, iblockdata, tileentity, itemstack1);
                    }

                    // return true; // CraftBukkit
                }
                // CraftBukkit start
                if (event.isDropItems()) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockDropItemEvent(bblock, state, this.player, level.captureDrops);
                }
                level.captureDrops = null;

                // Drop event experience
                if (flag && event != null) {
                    iblockdata.getBlock().popExperience(this.level, blockposition, event.getExpToDrop());
                }

                return true;
                // CraftBukkit end
            }
        }
    }

    public EnumInteractionResult useItem(EntityPlayer entityplayer, World world, ItemStack itemstack, EnumHand enumhand) {
        if (this.gameModeForPlayer == EnumGamemode.SPECTATOR) {
            return EnumInteractionResult.PASS;
        } else if (entityplayer.getCooldowns().isOnCooldown(itemstack.getItem())) {
            return EnumInteractionResult.PASS;
        } else {
            int i = itemstack.getCount();
            int j = itemstack.getDamageValue();
            InteractionResultWrapper<ItemStack> interactionresultwrapper = itemstack.use(world, entityplayer, enumhand);
            ItemStack itemstack1 = (ItemStack) interactionresultwrapper.getObject();

            if (itemstack1 == itemstack && itemstack1.getCount() == i && itemstack1.getUseDuration() <= 0 && itemstack1.getDamageValue() == j) {
                return interactionresultwrapper.getResult();
            } else if (interactionresultwrapper.getResult() == EnumInteractionResult.FAIL && itemstack1.getUseDuration() > 0 && !entityplayer.isUsingItem()) {
                return interactionresultwrapper.getResult();
            } else {
                if (itemstack != itemstack1) {
                    entityplayer.setItemInHand(enumhand, itemstack1);
                }

                if (this.isCreative() && itemstack1 != ItemStack.EMPTY) {
                    itemstack1.setCount(i);
                    if (itemstack1.isDamageableItem() && itemstack1.getDamageValue() != j) {
                        itemstack1.setDamageValue(j);
                    }
                }

                if (itemstack1.isEmpty()) {
                    entityplayer.setItemInHand(enumhand, ItemStack.EMPTY);
                }

                if (!entityplayer.isUsingItem()) {
                    entityplayer.inventoryMenu.sendAllDataToRemote();
                }

                return interactionresultwrapper.getResult();
            }
        }
    }

    // CraftBukkit start - whole method
    public boolean interactResult = false;
    public boolean firedInteract = false;
    public BlockPosition interactPosition;
    public EnumHand interactHand;
    public ItemStack interactItemStack;
    public EnumInteractionResult useItemOn(EntityPlayer entityplayer, World world, ItemStack itemstack, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
        IBlockData iblockdata = world.getBlockState(blockposition);
        EnumInteractionResult enuminteractionresult = EnumInteractionResult.PASS;
        boolean cancelledBlock = false;

        if (!iblockdata.getBlock().isEnabled(world.enabledFeatures())) {
            return EnumInteractionResult.FAIL;
        } else if (this.gameModeForPlayer == EnumGamemode.SPECTATOR) {
            ITileInventory itileinventory = iblockdata.getMenuProvider(world, blockposition);
            cancelledBlock = !(itileinventory instanceof ITileInventory);
        }

        if (entityplayer.getCooldowns().isOnCooldown(itemstack.getItem())) {
            cancelledBlock = true;
        }

        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(entityplayer, Action.RIGHT_CLICK_BLOCK, blockposition, movingobjectpositionblock.getDirection(), itemstack, cancelledBlock, enumhand, movingobjectpositionblock.getLocation());
        firedInteract = true;
        interactResult = event.useItemInHand() == Event.Result.DENY;
        interactPosition = blockposition.immutable();
        interactHand = enumhand;
        interactItemStack = itemstack.copy();

        if (event.useInteractedBlock() == Event.Result.DENY) {
            // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
            if (iblockdata.getBlock() instanceof BlockDoor) {
                boolean bottom = iblockdata.getValue(BlockDoor.HALF) == BlockPropertyDoubleBlockHalf.LOWER;
                entityplayer.connection.send(new PacketPlayOutBlockChange(world, bottom ? blockposition.above() : blockposition.below()));
            } else if (iblockdata.getBlock() instanceof BlockCake) {
                entityplayer.getBukkitEntity().sendHealthUpdate(); // SPIGOT-1341 - reset health for cake
            } else if (interactItemStack.getItem() instanceof ItemBisected) {
                // send a correcting update to the client, as it already placed the upper half of the bisected item
                entityplayer.connection.send(new PacketPlayOutBlockChange(world, blockposition.relative(movingobjectpositionblock.getDirection()).above()));

                // send a correcting update to the client for the block above as well, this because of replaceable blocks (such as grass, sea grass etc)
                entityplayer.connection.send(new PacketPlayOutBlockChange(world, blockposition.above()));
            }
            entityplayer.getBukkitEntity().updateInventory(); // SPIGOT-2867
            enuminteractionresult = (event.useItemInHand() != Event.Result.ALLOW) ? EnumInteractionResult.SUCCESS : EnumInteractionResult.PASS;
        } else if (this.gameModeForPlayer == EnumGamemode.SPECTATOR) {
            ITileInventory itileinventory = iblockdata.getMenuProvider(world, blockposition);

            if (itileinventory != null) {
                entityplayer.openMenu(itileinventory);
                return EnumInteractionResult.SUCCESS;
            } else {
                return EnumInteractionResult.PASS;
            }
        } else {
            boolean flag = !entityplayer.getMainHandItem().isEmpty() || !entityplayer.getOffhandItem().isEmpty();
            boolean flag1 = entityplayer.isSecondaryUseActive() && flag;
            ItemStack itemstack1 = itemstack.copy();

            if (!flag1) {
                enuminteractionresult = iblockdata.use(world, entityplayer, enumhand, movingobjectpositionblock);

                if (enuminteractionresult.consumesAction()) {
                    CriterionTriggers.ITEM_USED_ON_BLOCK.trigger(entityplayer, blockposition, itemstack1);
                    return enuminteractionresult;
                }
            }

            if (!itemstack.isEmpty() && enuminteractionresult != EnumInteractionResult.SUCCESS && !interactResult) { // add !interactResult SPIGOT-764
                ItemActionContext itemactioncontext = new ItemActionContext(entityplayer, enumhand, movingobjectpositionblock);
                EnumInteractionResult enuminteractionresult1;

                if (this.isCreative()) {
                    int i = itemstack.getCount();

                    enuminteractionresult1 = itemstack.useOn(itemactioncontext);
                    itemstack.setCount(i);
                } else {
                    enuminteractionresult1 = itemstack.useOn(itemactioncontext);
                }

                if (enuminteractionresult1.consumesAction()) {
                    CriterionTriggers.ITEM_USED_ON_BLOCK.trigger(entityplayer, blockposition, itemstack1);
                }

                return enuminteractionresult1;
            }
        }
        return enuminteractionresult;
        // CraftBukkit end
    }

    public void setLevel(WorldServer worldserver) {
        this.level = worldserver;
    }
}
