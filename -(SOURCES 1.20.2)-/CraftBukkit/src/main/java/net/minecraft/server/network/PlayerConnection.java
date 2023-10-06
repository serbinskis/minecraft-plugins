package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenMessagesValidator;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PlayerConnectionUtils;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketPlayInAbilities;
import net.minecraft.network.protocol.game.PacketPlayInAdvancements;
import net.minecraft.network.protocol.game.PacketPlayInArmAnimation;
import net.minecraft.network.protocol.game.PacketPlayInAutoRecipe;
import net.minecraft.network.protocol.game.PacketPlayInBEdit;
import net.minecraft.network.protocol.game.PacketPlayInBeacon;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayInBlockPlace;
import net.minecraft.network.protocol.game.PacketPlayInBoatMove;
import net.minecraft.network.protocol.game.PacketPlayInChat;
import net.minecraft.network.protocol.game.PacketPlayInClientCommand;
import net.minecraft.network.protocol.game.PacketPlayInCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayInDifficultyChange;
import net.minecraft.network.protocol.game.PacketPlayInDifficultyLock;
import net.minecraft.network.protocol.game.PacketPlayInEnchantItem;
import net.minecraft.network.protocol.game.PacketPlayInEntityAction;
import net.minecraft.network.protocol.game.PacketPlayInEntityNBTQuery;
import net.minecraft.network.protocol.game.PacketPlayInFlying;
import net.minecraft.network.protocol.game.PacketPlayInHeldItemSlot;
import net.minecraft.network.protocol.game.PacketPlayInItemName;
import net.minecraft.network.protocol.game.PacketPlayInJigsawGenerate;
import net.minecraft.network.protocol.game.PacketPlayInPickItem;
import net.minecraft.network.protocol.game.PacketPlayInRecipeDisplayed;
import net.minecraft.network.protocol.game.PacketPlayInRecipeSettings;
import net.minecraft.network.protocol.game.PacketPlayInSetCommandBlock;
import net.minecraft.network.protocol.game.PacketPlayInSetCommandMinecart;
import net.minecraft.network.protocol.game.PacketPlayInSetCreativeSlot;
import net.minecraft.network.protocol.game.PacketPlayInSetJigsaw;
import net.minecraft.network.protocol.game.PacketPlayInSpectate;
import net.minecraft.network.protocol.game.PacketPlayInSteerVehicle;
import net.minecraft.network.protocol.game.PacketPlayInStruct;
import net.minecraft.network.protocol.game.PacketPlayInTabComplete;
import net.minecraft.network.protocol.game.PacketPlayInTeleportAccept;
import net.minecraft.network.protocol.game.PacketPlayInTileNBTQuery;
import net.minecraft.network.protocol.game.PacketPlayInTrSel;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayInUseItem;
import net.minecraft.network.protocol.game.PacketPlayInVehicleMove;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutHeldItemSlot;
import net.minecraft.network.protocol.game.PacketPlayOutNBTQuery;
import net.minecraft.network.protocol.game.PacketPlayOutPosition;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.protocol.game.PacketPlayOutTabComplete;
import net.minecraft.network.protocol.game.PacketPlayOutVehicleMove;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.status.PacketStatusInPing;
import net.minecraft.network.protocol.status.PacketStatusOutPong;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.util.FutureChain;
import net.minecraft.util.MathHelper;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.UtilColor;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.IJumpable;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.ContainerBeacon;
import net.minecraft.world.inventory.ContainerMerchant;
import net.minecraft.world.inventory.ContainerRecipeBook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemBucket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.CommandBlockListenerAbstract;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockCommand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityCommand;
import net.minecraft.world.level.block.entity.TileEntityJigsaw;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;
import org.slf4j.Logger;

// CraftBukkit start
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.phys.MovingObjectPosition;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.SmithingInventory;
// CraftBukkit end

public class PlayerConnection extends ServerCommonPacketListenerImpl implements PacketListenerPlayIn, ServerPlayerConnection, TickablePacketListener {

    static final Logger LOGGER = LogUtils.getLogger();
    public static final double MAX_INTERACTION_DISTANCE = MathHelper.square(6.0D);
    private static final int NO_BLOCK_UPDATES_TO_ACK = -1;
    private static final int TRACKED_MESSAGE_DISCONNECT_THRESHOLD = 4096;
    private static final IChatBaseComponent CHAT_VALIDATION_FAILED = IChatBaseComponent.translatable("multiplayer.disconnect.chat_validation_failed");
    public EntityPlayer player;
    public final PlayerChunkSender chunkSender;
    private int tickCount;
    private int ackBlockChangesUpTo = -1;
    // CraftBukkit start - multithreaded fields
    private final AtomicInteger chatSpamTickCount = new AtomicInteger();
    // CraftBukkit end
    private int dropSpamTickCount;
    private double firstGoodX;
    private double firstGoodY;
    private double firstGoodZ;
    private double lastGoodX;
    private double lastGoodY;
    private double lastGoodZ;
    @Nullable
    private Entity lastVehicle;
    private double vehicleFirstGoodX;
    private double vehicleFirstGoodY;
    private double vehicleFirstGoodZ;
    private double vehicleLastGoodX;
    private double vehicleLastGoodY;
    private double vehicleLastGoodZ;
    @Nullable
    private Vec3D awaitingPositionFromClient;
    private int awaitingTeleport;
    private int awaitingTeleportTime;
    private boolean clientIsFloating;
    private int aboveGroundTickCount;
    private boolean clientVehicleIsFloating;
    private int aboveGroundVehicleTickCount;
    private int receivedMovePacketCount;
    private int knownMovePacketCount;
    private final AtomicReference<Instant> lastChatTimeStamp;
    @Nullable
    private RemoteChatSession chatSession;
    private SignedMessageChain.b signedMessageDecoder;
    private final LastSeenMessagesValidator lastSeenMessages;
    private final MessageSignatureCache messageSignatureCache;
    private final FutureChain chatMessageChain;
    private boolean waitingForSwitchToConfig;

    public PlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer, CommonListenerCookie commonlistenercookie) {
        super(minecraftserver, networkmanager, commonlistenercookie, entityplayer); // CraftBukkit
        this.lastChatTimeStamp = new AtomicReference(Instant.EPOCH);
        this.lastSeenMessages = new LastSeenMessagesValidator(20);
        this.messageSignatureCache = MessageSignatureCache.createDefault();
        this.chunkSender = new PlayerChunkSender(networkmanager.isMemoryConnection());
        networkmanager.setListener(this);
        this.player = entityplayer;
        entityplayer.connection = this;
        entityplayer.getTextFilter().join();
        this.signedMessageDecoder = minecraftserver.enforceSecureProfile() ? SignedMessageChain.b.REJECT_ALL : SignedMessageChain.b.unsigned(entityplayer.getUUID());
        this.chatMessageChain = new FutureChain(minecraftserver.chatExecutor); // CraftBukkit - async chat
    }

    // CraftBukkit start - add fields
    private int lastTick = MinecraftServer.currentTick;
    private int allowedPlayerTicks = 1;
    private int lastDropTick = MinecraftServer.currentTick;
    private int lastBookTick  = MinecraftServer.currentTick;
    private int dropCount = 0;

    // Get position of last block hit for BlockDamageLevel.STOPPED
    private double lastPosX = Double.MAX_VALUE;
    private double lastPosY = Double.MAX_VALUE;
    private double lastPosZ = Double.MAX_VALUE;
    private float lastPitch = Float.MAX_VALUE;
    private float lastYaw = Float.MAX_VALUE;
    private boolean justTeleported = false;
    // CraftBukkit end

    @Override
    public void tick() {
        if (this.ackBlockChangesUpTo > -1) {
            this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
            this.ackBlockChangesUpTo = -1;
        }

        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        this.player.doTick();
        this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger() && !this.player.isDeadOrDying()) {
            if (++this.aboveGroundTickCount > 80) {
                PlayerConnection.LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
                this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.flying"));
                return;
            }
        } else {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }

        this.lastVehicle = this.player.getRootVehicle();
        if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
            this.vehicleFirstGoodX = this.lastVehicle.getX();
            this.vehicleFirstGoodY = this.lastVehicle.getY();
            this.vehicleFirstGoodZ = this.lastVehicle.getZ();
            this.vehicleLastGoodX = this.lastVehicle.getX();
            this.vehicleLastGoodY = this.lastVehicle.getY();
            this.vehicleLastGoodZ = this.lastVehicle.getZ();
            if (this.clientVehicleIsFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
                if (++this.aboveGroundVehicleTickCount > 80) {
                    PlayerConnection.LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName().getString());
                    this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.flying"));
                    return;
                }
            } else {
                this.clientVehicleIsFloating = false;
                this.aboveGroundVehicleTickCount = 0;
            }
        } else {
            this.lastVehicle = null;
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        }

        this.keepConnectionAlive();
        // CraftBukkit start
        for (int spam; (spam = this.chatSpamTickCount.get()) > 0 && !chatSpamTickCount.compareAndSet(spam, spam - 1); ) ;
        /* Use thread-safe field access instead
        if (this.chatSpamTickCount > 0) {
            --this.chatSpamTickCount;
        }
        */
        // CraftBukkit end

        if (this.dropSpamTickCount > 0) {
            --this.dropSpamTickCount;
        }

        if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && SystemUtils.getMillis() - this.player.getLastActionTime() > (long) this.server.getPlayerIdleTimeout() * 1000L * 60L) {
            this.player.resetLastActionTime(); // CraftBukkit - SPIGOT-854
            this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.idling"));
        }

    }

    public void resetPosition() {
        this.firstGoodX = this.player.getX();
        this.firstGoodY = this.player.getY();
        this.firstGoodZ = this.player.getZ();
        this.lastGoodX = this.player.getX();
        this.lastGoodY = this.player.getY();
        this.lastGoodZ = this.player.getZ();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected() && !this.waitingForSwitchToConfig;
    }

    @Override
    public boolean shouldHandleMessage(Packet<?> packet) {
        return super.shouldHandleMessage(packet) ? true : this.waitingForSwitchToConfig && this.connection.isConnected() && packet instanceof ServerboundConfigurationAcknowledgedPacket;
    }

    @Override
    protected GameProfile playerProfile() {
        return this.player.getGameProfile();
    }

    private <T, R> CompletableFuture<R> filterTextPacket(T t0, BiFunction<ITextFilter, T, CompletableFuture<R>> bifunction) {
        return ((CompletableFuture) bifunction.apply(this.player.getTextFilter(), t0)).thenApply((object) -> {
            if (!this.isAcceptingMessages()) {
                PlayerConnection.LOGGER.debug("Ignoring packet due to disconnection");
                throw new CancellationException("disconnected");
            } else {
                return object;
            }
        });
    }

    private CompletableFuture<FilteredText> filterTextPacket(String s) {
        return this.filterTextPacket(s, ITextFilter::processStreamMessage);
    }

    private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> list) {
        return this.filterTextPacket(list, ITextFilter::processMessageBundle);
    }

    @Override
    public void handlePlayerInput(PacketPlayInSteerVehicle packetplayinsteervehicle) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinsteervehicle, this, this.player.serverLevel());
        this.player.setPlayerInput(packetplayinsteervehicle.getXxa(), packetplayinsteervehicle.getZza(), packetplayinsteervehicle.isJumping(), packetplayinsteervehicle.isShiftKeyDown());
    }

    private static boolean containsInvalidValues(double d0, double d1, double d2, float f, float f1) {
        return Double.isNaN(d0) || Double.isNaN(d1) || Double.isNaN(d2) || !Floats.isFinite(f1) || !Floats.isFinite(f);
    }

    private static double clampHorizontal(double d0) {
        return MathHelper.clamp(d0, -3.0E7D, 3.0E7D);
    }

    private static double clampVertical(double d0) {
        return MathHelper.clamp(d0, -2.0E7D, 2.0E7D);
    }

    @Override
    public void handleMoveVehicle(PacketPlayInVehicleMove packetplayinvehiclemove) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinvehiclemove, this, this.player.serverLevel());
        if (containsInvalidValues(packetplayinvehiclemove.getX(), packetplayinvehiclemove.getY(), packetplayinvehiclemove.getZ(), packetplayinvehiclemove.getYRot(), packetplayinvehiclemove.getXRot())) {
            this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            Entity entity = this.player.getRootVehicle();

            if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
                WorldServer worldserver = this.player.serverLevel();
                double d0 = entity.getX();
                double d1 = entity.getY();
                double d2 = entity.getZ();
                double d3 = clampHorizontal(packetplayinvehiclemove.getX());
                double d4 = clampVertical(packetplayinvehiclemove.getY());
                double d5 = clampHorizontal(packetplayinvehiclemove.getZ());
                float f = MathHelper.wrapDegrees(packetplayinvehiclemove.getYRot());
                float f1 = MathHelper.wrapDegrees(packetplayinvehiclemove.getXRot());
                double d6 = d3 - this.vehicleFirstGoodX;
                double d7 = d4 - this.vehicleFirstGoodY;
                double d8 = d5 - this.vehicleFirstGoodZ;
                double d9 = entity.getDeltaMovement().lengthSqr();
                double d10 = d6 * d6 + d7 * d7 + d8 * d8;


                // CraftBukkit start - handle custom speeds and skipped ticks
                this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                this.lastTick = (int) (System.currentTimeMillis() / 50);

                ++this.receivedMovePacketCount;
                int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                if (i > Math.max(this.allowedPlayerTicks, 5)) {
                    PlayerConnection.LOGGER.debug(this.player.getScoreboardName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                    i = 1;
                }

                if (d10 > 0) {
                    allowedPlayerTicks -= 1;
                } else {
                    allowedPlayerTicks = 20;
                }
                double speed;
                if (player.getAbilities().flying) {
                    speed = player.getAbilities().flyingSpeed * 20f;
                } else {
                    speed = player.getAbilities().walkingSpeed * 10f;
                }
                speed *= 2f; // TODO: Get the speed of the vehicle instead of the player

                if (d10 - d9 > Math.max(100.0D, Math.pow((double) (10.0F * (float) i * speed), 2)) && !this.isSingleplayerOwner()) {
                // CraftBukkit end
                    PlayerConnection.LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{entity.getName().getString(), this.player.getName().getString(), d6, d7, d8});
                    this.send(new PacketPlayOutVehicleMove(entity));
                    return;
                }

                boolean flag = worldserver.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));

                d6 = d3 - this.vehicleLastGoodX;
                d7 = d4 - this.vehicleLastGoodY - 1.0E-6D;
                d8 = d5 - this.vehicleLastGoodZ;
                boolean flag1 = entity.verticalCollisionBelow;

                if (entity instanceof EntityLiving) {
                    EntityLiving entityliving = (EntityLiving) entity;

                    if (entityliving.onClimbable()) {
                        entityliving.resetFallDistance();
                    }
                }

                entity.move(EnumMoveType.PLAYER, new Vec3D(d6, d7, d8));
                double d11 = d7;

                d6 = d3 - entity.getX();
                d7 = d4 - entity.getY();
                if (d7 > -0.5D || d7 < 0.5D) {
                    d7 = 0.0D;
                }

                d8 = d5 - entity.getZ();
                d10 = d6 * d6 + d7 * d7 + d8 * d8;
                boolean flag2 = false;

                if (d10 > 0.0625D) {
                    flag2 = true;
                    PlayerConnection.LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", new Object[]{entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10)});
                }

                entity.absMoveTo(d3, d4, d5, f, f1);
                player.absMoveTo(d3, d4, d5, this.player.getYRot(), this.player.getXRot()); // CraftBukkit
                boolean flag3 = worldserver.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));

                if (flag && (flag2 || !flag3)) {
                    entity.absMoveTo(d0, d1, d2, f, f1);
                    player.absMoveTo(d0, d1, d2, this.player.getYRot(), this.player.getXRot()); // CraftBukkit
                    this.send(new PacketPlayOutVehicleMove(entity));
                    return;
                }

                // CraftBukkit start - fire PlayerMoveEvent
                Player player = this.getCraftPlayer();
                Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get the Players previous Event location.
                Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

                // If the packet contains movement information then we update the To location with the correct XYZ.
                to.setX(packetplayinvehiclemove.getX());
                to.setY(packetplayinvehiclemove.getY());
                to.setZ(packetplayinvehiclemove.getZ());


                // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
                to.setYaw(packetplayinvehiclemove.getYRot());
                to.setPitch(packetplayinvehiclemove.getXRot());

                // Prevent 40 event-calls for less than a single pixel of movement >.>
                double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.isImmobile()) {
                    this.lastPosX = to.getX();
                    this.lastPosY = to.getY();
                    this.lastPosZ = to.getZ();
                    this.lastYaw = to.getYaw();
                    this.lastPitch = to.getPitch();

                    // Skip the first time we do this
                    if (from.getX() != Double.MAX_VALUE) {
                        Location oldTo = to.clone();
                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                        this.cserver.getPluginManager().callEvent(event);

                        // If the event is cancelled we move the player back to their old location.
                        if (event.isCancelled()) {
                            teleport(from);
                            return;
                        }

                        // If a Plugin has changed the To destination then we teleport the Player
                        // there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                        // We only do this if the Event was not cancelled.
                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                            this.player.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            return;
                        }

                        // Check to see if the Players Location has some how changed during the call of the event.
                        // This can happen due to a plugin teleporting the player instead of using .setTo()
                        if (!from.equals(this.getCraftPlayer().getLocation()) && this.justTeleported) {
                            this.justTeleported = false;
                            return;
                        }
                    }
                }
                // CraftBukkit end

                this.player.serverLevel().getChunkSource().move(this.player);
                this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
                this.clientVehicleIsFloating = d11 >= -0.03125D && !flag1 && !this.server.isFlightAllowed() && !entity.isNoGravity() && this.noBlocksAround(entity);
                this.vehicleLastGoodX = entity.getX();
                this.vehicleLastGoodY = entity.getY();
                this.vehicleLastGoodZ = entity.getZ();
            }

        }
    }

    private boolean noBlocksAround(Entity entity) {
        return entity.level().getBlockStates(entity.getBoundingBox().inflate(0.0625D).expandTowards(0.0D, -0.55D, 0.0D)).allMatch(BlockBase.BlockData::isAir);
    }

    @Override
    public void handleAcceptTeleportPacket(PacketPlayInTeleportAccept packetplayinteleportaccept) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinteleportaccept, this, this.player.serverLevel());
        if (packetplayinteleportaccept.getId() == this.awaitingTeleport) {
            if (this.awaitingPositionFromClient == null) {
                this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.invalid_player_movement"));
                return;
            }

            this.player.absMoveTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
            this.lastGoodX = this.awaitingPositionFromClient.x;
            this.lastGoodY = this.awaitingPositionFromClient.y;
            this.lastGoodZ = this.awaitingPositionFromClient.z;
            if (this.player.isChangingDimension()) {
                this.player.hasChangedDimension();
            }

            this.awaitingPositionFromClient = null;
            this.player.serverLevel().getChunkSource().move(this.player); // CraftBukkit
        }

    }

    @Override
    public void handleRecipeBookSeenRecipePacket(PacketPlayInRecipeDisplayed packetplayinrecipedisplayed) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinrecipedisplayed, this, this.player.serverLevel());
        Optional<? extends RecipeHolder<?>> optional = this.server.getRecipeManager().byKey(packetplayinrecipedisplayed.getRecipe()); // CraftBukkit - decompile error
        RecipeBookServer recipebookserver = this.player.getRecipeBook();

        Objects.requireNonNull(recipebookserver);
        optional.ifPresent(recipebookserver::removeHighlight);
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(PacketPlayInRecipeSettings packetplayinrecipesettings) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinrecipesettings, this, this.player.serverLevel());
        this.player.getRecipeBook().setBookSetting(packetplayinrecipesettings.getBookType(), packetplayinrecipesettings.isOpen(), packetplayinrecipesettings.isFiltering());
    }

    @Override
    public void handleSeenAdvancements(PacketPlayInAdvancements packetplayinadvancements) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinadvancements, this, this.player.serverLevel());
        if (packetplayinadvancements.getAction() == PacketPlayInAdvancements.Status.OPENED_TAB) {
            MinecraftKey minecraftkey = (MinecraftKey) Objects.requireNonNull(packetplayinadvancements.getTab());
            AdvancementHolder advancementholder = this.server.getAdvancements().get(minecraftkey);

            if (advancementholder != null) {
                this.player.getAdvancements().setSelectedTab(advancementholder);
            }
        }

    }

    @Override
    public void handleCustomCommandSuggestions(PacketPlayInTabComplete packetplayintabcomplete) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayintabcomplete, this, this.player.serverLevel());
        // CraftBukkit start
        if (chatSpamTickCount.addAndGet(1) > 500 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
            this.disconnect(IChatBaseComponent.translatable("disconnect.spam"));
            return;
        }
        // CraftBukkit end
        StringReader stringreader = new StringReader(packetplayintabcomplete.getCommand());

        if (stringreader.canRead() && stringreader.peek() == '/') {
            stringreader.skip();
        }

        ParseResults<CommandListenerWrapper> parseresults = this.server.getCommands().getDispatcher().parse(stringreader, this.player.createCommandSourceStack());

        this.server.getCommands().getDispatcher().getCompletionSuggestions(parseresults).thenAccept((suggestions) -> {
            if (suggestions.isEmpty()) return; // CraftBukkit - don't send through empty suggestions - prevents [<args>] from showing for plugins with nothing more to offer
            this.send(new PacketPlayOutTabComplete(packetplayintabcomplete.getId(), suggestions));
        });
    }

    @Override
    public void handleSetCommandBlock(PacketPlayInSetCommandBlock packetplayinsetcommandblock) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinsetcommandblock, this, this.player.serverLevel());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(IChatBaseComponent.translatable("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(IChatBaseComponent.translatable("advMode.notAllowed"));
        } else {
            CommandBlockListenerAbstract commandblocklistenerabstract = null;
            TileEntityCommand tileentitycommand = null;
            BlockPosition blockposition = packetplayinsetcommandblock.getPos();
            TileEntity tileentity = this.player.level().getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityCommand) {
                tileentitycommand = (TileEntityCommand) tileentity;
                commandblocklistenerabstract = tileentitycommand.getCommandBlock();
            }

            String s = packetplayinsetcommandblock.getCommand();
            boolean flag = packetplayinsetcommandblock.isTrackOutput();

            if (commandblocklistenerabstract != null) {
                TileEntityCommand.Type tileentitycommand_type = tileentitycommand.getMode();
                IBlockData iblockdata = this.player.level().getBlockState(blockposition);
                EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockCommand.FACING);
                IBlockData iblockdata1;

                switch (packetplayinsetcommandblock.getMode()) {
                    case SEQUENCE:
                        iblockdata1 = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                        break;
                    case AUTO:
                        iblockdata1 = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                        break;
                    case REDSTONE:
                    default:
                        iblockdata1 = Blocks.COMMAND_BLOCK.defaultBlockState();
                }

                IBlockData iblockdata2 = (IBlockData) ((IBlockData) iblockdata1.setValue(BlockCommand.FACING, enumdirection)).setValue(BlockCommand.CONDITIONAL, packetplayinsetcommandblock.isConditional());

                if (iblockdata2 != iblockdata) {
                    this.player.level().setBlock(blockposition, iblockdata2, 2);
                    tileentity.setBlockState(iblockdata2);
                    this.player.level().getChunkAt(blockposition).setBlockEntity(tileentity);
                }

                commandblocklistenerabstract.setCommand(s);
                commandblocklistenerabstract.setTrackOutput(flag);
                if (!flag) {
                    commandblocklistenerabstract.setLastOutput((IChatBaseComponent) null);
                }

                tileentitycommand.setAutomatic(packetplayinsetcommandblock.isAutomatic());
                if (tileentitycommand_type != packetplayinsetcommandblock.getMode()) {
                    tileentitycommand.onModeSwitch();
                }

                commandblocklistenerabstract.onUpdated();
                if (!UtilColor.isNullOrEmpty(s)) {
                    this.player.sendSystemMessage(IChatBaseComponent.translatable("advMode.setCommand.success", s));
                }
            }

        }
    }

    @Override
    public void handleSetCommandMinecart(PacketPlayInSetCommandMinecart packetplayinsetcommandminecart) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinsetcommandminecart, this, this.player.serverLevel());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(IChatBaseComponent.translatable("advMode.notEnabled"));
        } else if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(IChatBaseComponent.translatable("advMode.notAllowed"));
        } else {
            CommandBlockListenerAbstract commandblocklistenerabstract = packetplayinsetcommandminecart.getCommandBlock(this.player.level());

            if (commandblocklistenerabstract != null) {
                commandblocklistenerabstract.setCommand(packetplayinsetcommandminecart.getCommand());
                commandblocklistenerabstract.setTrackOutput(packetplayinsetcommandminecart.isTrackOutput());
                if (!packetplayinsetcommandminecart.isTrackOutput()) {
                    commandblocklistenerabstract.setLastOutput((IChatBaseComponent) null);
                }

                commandblocklistenerabstract.onUpdated();
                this.player.sendSystemMessage(IChatBaseComponent.translatable("advMode.setCommand.success", packetplayinsetcommandminecart.getCommand()));
            }

        }
    }

    @Override
    public void handlePickItem(PacketPlayInPickItem packetplayinpickitem) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinpickitem, this, this.player.serverLevel());
        this.player.getInventory().pickSlot(packetplayinpickitem.getSlot());
        this.player.connection.send(new PacketPlayOutSetSlot(-2, 0, this.player.getInventory().selected, this.player.getInventory().getItem(this.player.getInventory().selected)));
        this.player.connection.send(new PacketPlayOutSetSlot(-2, 0, packetplayinpickitem.getSlot(), this.player.getInventory().getItem(packetplayinpickitem.getSlot())));
        this.player.connection.send(new PacketPlayOutHeldItemSlot(this.player.getInventory().selected));
    }

    @Override
    public void handleRenameItem(PacketPlayInItemName packetplayinitemname) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinitemname, this, this.player.serverLevel());
        Container container = this.player.containerMenu;

        if (container instanceof ContainerAnvil) {
            ContainerAnvil containeranvil = (ContainerAnvil) container;

            if (!containeranvil.stillValid(this.player)) {
                PlayerConnection.LOGGER.debug("Player {} interacted with invalid menu {}", this.player, containeranvil);
                return;
            }

            containeranvil.setItemName(packetplayinitemname.getName());
        }

    }

    @Override
    public void handleSetBeaconPacket(PacketPlayInBeacon packetplayinbeacon) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinbeacon, this, this.player.serverLevel());
        Container container = this.player.containerMenu;

        if (container instanceof ContainerBeacon) {
            ContainerBeacon containerbeacon = (ContainerBeacon) container;

            if (!this.player.containerMenu.stillValid(this.player)) {
                PlayerConnection.LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
                return;
            }

            containerbeacon.updateEffects(packetplayinbeacon.getPrimary(), packetplayinbeacon.getSecondary());
        }

    }

    @Override
    public void handleSetStructureBlock(PacketPlayInStruct packetplayinstruct) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinstruct, this, this.player.serverLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPosition blockposition = packetplayinstruct.getPos();
            IBlockData iblockdata = this.player.level().getBlockState(blockposition);
            TileEntity tileentity = this.player.level().getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityStructure) {
                TileEntityStructure tileentitystructure = (TileEntityStructure) tileentity;

                tileentitystructure.setMode(packetplayinstruct.getMode());
                tileentitystructure.setStructureName(packetplayinstruct.getName());
                tileentitystructure.setStructurePos(packetplayinstruct.getOffset());
                tileentitystructure.setStructureSize(packetplayinstruct.getSize());
                tileentitystructure.setMirror(packetplayinstruct.getMirror());
                tileentitystructure.setRotation(packetplayinstruct.getRotation());
                tileentitystructure.setMetaData(packetplayinstruct.getData());
                tileentitystructure.setIgnoreEntities(packetplayinstruct.isIgnoreEntities());
                tileentitystructure.setShowAir(packetplayinstruct.isShowAir());
                tileentitystructure.setShowBoundingBox(packetplayinstruct.isShowBoundingBox());
                tileentitystructure.setIntegrity(packetplayinstruct.getIntegrity());
                tileentitystructure.setSeed(packetplayinstruct.getSeed());
                if (tileentitystructure.hasStructureName()) {
                    String s = tileentitystructure.getStructureName();

                    if (packetplayinstruct.getUpdateType() == TileEntityStructure.UpdateType.SAVE_AREA) {
                        if (tileentitystructure.saveStructure()) {
                            this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.save_success", s), false);
                        } else {
                            this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.save_failure", s), false);
                        }
                    } else if (packetplayinstruct.getUpdateType() == TileEntityStructure.UpdateType.LOAD_AREA) {
                        if (!tileentitystructure.isStructureLoadable()) {
                            this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.load_not_found", s), false);
                        } else if (tileentitystructure.loadStructure(this.player.serverLevel())) {
                            this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.load_success", s), false);
                        } else {
                            this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.load_prepare", s), false);
                        }
                    } else if (packetplayinstruct.getUpdateType() == TileEntityStructure.UpdateType.SCAN_AREA) {
                        if (tileentitystructure.detectSize()) {
                            this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.size_success", s), false);
                        } else {
                            this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.size_failure"), false);
                        }
                    }
                } else {
                    this.player.displayClientMessage(IChatBaseComponent.translatable("structure_block.invalid_structure_name", packetplayinstruct.getName()), false);
                }

                tileentitystructure.setChanged();
                this.player.level().sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
            }

        }
    }

    @Override
    public void handleSetJigsawBlock(PacketPlayInSetJigsaw packetplayinsetjigsaw) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinsetjigsaw, this, this.player.serverLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPosition blockposition = packetplayinsetjigsaw.getPos();
            IBlockData iblockdata = this.player.level().getBlockState(blockposition);
            TileEntity tileentity = this.player.level().getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityJigsaw) {
                TileEntityJigsaw tileentityjigsaw = (TileEntityJigsaw) tileentity;

                tileentityjigsaw.setName(packetplayinsetjigsaw.getName());
                tileentityjigsaw.setTarget(packetplayinsetjigsaw.getTarget());
                tileentityjigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, packetplayinsetjigsaw.getPool()));
                tileentityjigsaw.setFinalState(packetplayinsetjigsaw.getFinalState());
                tileentityjigsaw.setJoint(packetplayinsetjigsaw.getJoint());
                tileentityjigsaw.setChanged();
                this.player.level().sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
            }

        }
    }

    @Override
    public void handleJigsawGenerate(PacketPlayInJigsawGenerate packetplayinjigsawgenerate) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinjigsawgenerate, this, this.player.serverLevel());
        if (this.player.canUseGameMasterBlocks()) {
            BlockPosition blockposition = packetplayinjigsawgenerate.getPos();
            TileEntity tileentity = this.player.level().getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityJigsaw) {
                TileEntityJigsaw tileentityjigsaw = (TileEntityJigsaw) tileentity;

                tileentityjigsaw.generate(this.player.serverLevel(), packetplayinjigsawgenerate.levels(), packetplayinjigsawgenerate.keepJigsaws());
            }

        }
    }

    @Override
    public void handleSelectTrade(PacketPlayInTrSel packetplayintrsel) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayintrsel, this, this.player.serverLevel());
        int i = packetplayintrsel.getItem();
        Container container = this.player.containerMenu;

        if (container instanceof ContainerMerchant) {
            ContainerMerchant containermerchant = (ContainerMerchant) container;
            // CraftBukkit start
            final org.bukkit.event.inventory.TradeSelectEvent tradeSelectEvent = CraftEventFactory.callTradeSelectEvent(this.player, i, containermerchant);
            if (tradeSelectEvent.isCancelled()) {
                this.player.getBukkitEntity().updateInventory();
                return;
            }
            // CraftBukkit end

            if (!containermerchant.stillValid(this.player)) {
                PlayerConnection.LOGGER.debug("Player {} interacted with invalid menu {}", this.player, containermerchant);
                return;
            }

            containermerchant.setSelectionHint(i);
            containermerchant.tryMoveItems(i);
        }

    }

    @Override
    public void handleEditBook(PacketPlayInBEdit packetplayinbedit) {
        // CraftBukkit start
        if (this.lastBookTick + 20 > MinecraftServer.currentTick) {
            this.disconnect("Book edited too quickly!");
            return;
        }
        this.lastBookTick = MinecraftServer.currentTick;
        // CraftBukkit end
        int i = packetplayinbedit.getSlot();

        if (PlayerInventory.isHotbarSlot(i) || i == 40) {
            List<String> list = Lists.newArrayList();
            Optional<String> optional = packetplayinbedit.getTitle();

            Objects.requireNonNull(list);
            optional.ifPresent(list::add);
            Stream<String> stream = packetplayinbedit.getPages().stream().limit(100L); // CraftBukkit - decompile error

            Objects.requireNonNull(list);
            stream.forEach(list::add);
            Consumer<List<FilteredText>> consumer = optional.isPresent() ? (list1) -> {
                this.signBook((FilteredText) list1.get(0), list1.subList(1, list1.size()), i);
            } : (list1) -> {
                this.updateBookContents(list1, i);
            };

            this.filterTextPacket((List) list).thenAcceptAsync(consumer, this.server);
        }
    }

    private void updateBookContents(List<FilteredText> list, int i) {
        ItemStack itemstack = this.player.getInventory().getItem(i);

        if (itemstack.is(Items.WRITABLE_BOOK)) {
            this.updateBookPages(list, UnaryOperator.identity(), itemstack.copy(), i, itemstack); // CraftBukkit
        }
    }

    private void signBook(FilteredText filteredtext, List<FilteredText> list, int i) {
        ItemStack itemstack = this.player.getInventory().getItem(i);

        if (itemstack.is(Items.WRITABLE_BOOK)) {
            ItemStack itemstack1 = new ItemStack(Items.WRITTEN_BOOK);
            NBTTagCompound nbttagcompound = itemstack.getTag();

            if (nbttagcompound != null) {
                itemstack1.setTag(nbttagcompound.copy());
            }

            itemstack1.addTagElement("author", NBTTagString.valueOf(this.player.getName().getString()));
            if (this.player.isTextFilteringEnabled()) {
                itemstack1.addTagElement("title", NBTTagString.valueOf(filteredtext.filteredOrEmpty()));
            } else {
                itemstack1.addTagElement("filtered_title", NBTTagString.valueOf(filteredtext.filteredOrEmpty()));
                itemstack1.addTagElement("title", NBTTagString.valueOf(filteredtext.raw()));
            }

            this.updateBookPages(list, (s) -> {
                return IChatBaseComponent.ChatSerializer.toJson(IChatBaseComponent.literal(s));
            }, itemstack1, i, itemstack); // CraftBukkit
            this.player.getInventory().setItem(i, itemstack); // CraftBukkit - event factory updates the hand book
        }
    }

    private void updateBookPages(List<FilteredText> list, UnaryOperator<String> unaryoperator, ItemStack itemstack, int slot, ItemStack handItem) { // CraftBukkit
        NBTTagList nbttaglist = new NBTTagList();

        if (this.player.isTextFilteringEnabled()) {
            Stream<NBTTagString> stream = list.stream().map((filteredtext) -> { // CraftBukkit - decompile error
                return NBTTagString.valueOf((String) unaryoperator.apply(filteredtext.filteredOrEmpty()));
            });

            Objects.requireNonNull(nbttaglist);
            stream.forEach(nbttaglist::add);
        } else {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            int i = 0;

            for (int j = list.size(); i < j; ++i) {
                FilteredText filteredtext = (FilteredText) list.get(i);
                String s = filteredtext.raw();

                nbttaglist.add(NBTTagString.valueOf((String) unaryoperator.apply(s)));
                if (filteredtext.isFiltered()) {
                    nbttagcompound.putString(String.valueOf(i), (String) unaryoperator.apply(filteredtext.filteredOrEmpty()));
                }
            }

            if (!nbttagcompound.isEmpty()) {
                itemstack.addTagElement("filtered_pages", nbttagcompound);
            }
        }

        itemstack.addTagElement("pages", nbttaglist);
        CraftEventFactory.handleEditBookEvent(player, slot, handItem, itemstack); // CraftBukkit
    }

    @Override
    public void handleEntityTagQuery(PacketPlayInEntityNBTQuery packetplayinentitynbtquery) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinentitynbtquery, this, this.player.serverLevel());
        if (this.player.hasPermissions(2)) {
            Entity entity = this.player.level().getEntity(packetplayinentitynbtquery.getEntityId());

            if (entity != null) {
                NBTTagCompound nbttagcompound = entity.saveWithoutId(new NBTTagCompound());

                this.player.connection.send(new PacketPlayOutNBTQuery(packetplayinentitynbtquery.getTransactionId(), nbttagcompound));
            }

        }
    }

    @Override
    public void handleBlockEntityTagQuery(PacketPlayInTileNBTQuery packetplayintilenbtquery) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayintilenbtquery, this, this.player.serverLevel());
        if (this.player.hasPermissions(2)) {
            TileEntity tileentity = this.player.level().getBlockEntity(packetplayintilenbtquery.getPos());
            NBTTagCompound nbttagcompound = tileentity != null ? tileentity.saveWithoutMetadata() : null;

            this.player.connection.send(new PacketPlayOutNBTQuery(packetplayintilenbtquery.getTransactionId(), nbttagcompound));
        }
    }

    @Override
    public void handleMovePlayer(PacketPlayInFlying packetplayinflying) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinflying, this, this.player.serverLevel());
        if (containsInvalidValues(packetplayinflying.getX(0.0D), packetplayinflying.getY(0.0D), packetplayinflying.getZ(0.0D), packetplayinflying.getYRot(0.0F), packetplayinflying.getXRot(0.0F))) {
            this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.invalid_player_movement"));
        } else {
            WorldServer worldserver = this.player.serverLevel();

            if (!this.player.wonGame && !this.player.isImmobile()) { // CraftBukkit
                if (this.tickCount == 0) {
                    this.resetPosition();
                }

                if (this.awaitingPositionFromClient != null) {
                    if (this.tickCount - this.awaitingTeleportTime > 20) {
                        this.awaitingTeleportTime = this.tickCount;
                        this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
                    }
                    this.allowedPlayerTicks = 20; // CraftBukkit
                } else {
                    this.awaitingTeleportTime = this.tickCount;
                    double d0 = clampHorizontal(packetplayinflying.getX(this.player.getX()));
                    double d1 = clampVertical(packetplayinflying.getY(this.player.getY()));
                    double d2 = clampHorizontal(packetplayinflying.getZ(this.player.getZ()));
                    float f = MathHelper.wrapDegrees(packetplayinflying.getYRot(this.player.getYRot()));
                    float f1 = MathHelper.wrapDegrees(packetplayinflying.getXRot(this.player.getXRot()));

                    if (this.player.isPassenger()) {
                        this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        this.player.serverLevel().getChunkSource().move(this.player);
                        this.allowedPlayerTicks = 20; // CraftBukkit
                    } else {
                        // CraftBukkit - Make sure the move is valid but then reset it for plugins to modify
                        double prevX = player.getX();
                        double prevY = player.getY();
                        double prevZ = player.getZ();
                        float prevYaw = player.getYRot();
                        float prevPitch = player.getXRot();
                        // CraftBukkit end
                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = d0 - this.firstGoodX;
                        double d7 = d1 - this.firstGoodY;
                        double d8 = d2 - this.firstGoodZ;
                        double d9 = this.player.getDeltaMovement().lengthSqr();
                        double d10 = d6 * d6 + d7 * d7 + d8 * d8;

                        if (this.player.isSleeping()) {
                            if (d10 > 1.0D) {
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }

                        } else {
                            ++this.receivedMovePacketCount;
                            int i = this.receivedMovePacketCount - this.knownMovePacketCount;

                            // CraftBukkit start - handle custom speeds and skipped ticks
                            this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                            this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                            this.lastTick = (int) (System.currentTimeMillis() / 50);

                            if (i > Math.max(this.allowedPlayerTicks, 5)) {
                                PlayerConnection.LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                                i = 1;
                            }

                            if (packetplayinflying.hasRot || d10 > 0) {
                                allowedPlayerTicks -= 1;
                            } else {
                                allowedPlayerTicks = 20;
                            }
                            double speed;
                            if (player.getAbilities().flying) {
                                speed = player.getAbilities().flyingSpeed * 20f;
                            } else {
                                speed = player.getAbilities().walkingSpeed * 10f;
                            }

                            if (!this.player.isChangingDimension() && (!this.player.level().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                                float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;

                                if (d10 - d9 > Math.max(f2, Math.pow((double) (10.0F * (float) i * speed), 2)) && !this.isSingleplayerOwner()) {
                                // CraftBukkit end
                                    PlayerConnection.LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.player.getName().getString(), d6, d7, d8});
                                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                    return;
                                }
                            }

                            AxisAlignedBB axisalignedbb = this.player.getBoundingBox();

                            d6 = d0 - this.lastGoodX;
                            d7 = d1 - this.lastGoodY;
                            d8 = d2 - this.lastGoodZ;
                            boolean flag = d7 > 0.0D;

                            if (this.player.onGround() && !packetplayinflying.isOnGround() && flag) {
                                this.player.jumpFromGround();
                            }

                            boolean flag1 = this.player.verticalCollisionBelow;

                            this.player.move(EnumMoveType.PLAYER, new Vec3D(d6, d7, d8));
                            this.player.onGround = packetplayinflying.isOnGround(); // CraftBukkit - SPIGOT-5810, SPIGOT-5835, SPIGOT-6828: reset by this.player.move
                            double d11 = d7;

                            d6 = d0 - this.player.getX();
                            d7 = d1 - this.player.getY();
                            if (d7 > -0.5D || d7 < 0.5D) {
                                d7 = 0.0D;
                            }

                            d8 = d2 - this.player.getZ();
                            d10 = d6 * d6 + d7 * d7 + d8 * d8;
                            boolean flag2 = false;

                            if (!this.player.isChangingDimension() && d10 > 0.0625D && !this.player.isSleeping() && !this.player.gameMode.isCreative() && this.player.gameMode.getGameModeForPlayer() != EnumGamemode.SPECTATOR) {
                                flag2 = true;
                                PlayerConnection.LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                            }

                            if (!this.player.noPhysics && !this.player.isSleeping() && (flag2 && worldserver.noCollision(this.player, axisalignedbb) || this.isPlayerCollidingWithAnythingNew(worldserver, axisalignedbb, d0, d1, d2))) {
                                this.internalTeleport(d3, d4, d5, f, f1, Collections.emptySet()); // CraftBukkit - SPIGOT-1807: Don't call teleport event, when the client thinks the player is falling, because the chunks are not loaded on the client yet.
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, packetplayinflying.isOnGround());
                            } else {
                                // CraftBukkit start - fire PlayerMoveEvent
                                // Reset to old location first
                                this.player.absMoveTo(prevX, prevY, prevZ, prevYaw, prevPitch);

                                Player player = this.getCraftPlayer();
                                Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get the Players previous Event location.
                                Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

                                // If the packet contains movement information then we update the To location with the correct XYZ.
                                if (packetplayinflying.hasPos) {
                                    to.setX(packetplayinflying.x);
                                    to.setY(packetplayinflying.y);
                                    to.setZ(packetplayinflying.z);
                                }

                                // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
                                if (packetplayinflying.hasRot) {
                                    to.setYaw(packetplayinflying.yRot);
                                    to.setPitch(packetplayinflying.xRot);
                                }

                                // Prevent 40 event-calls for less than a single pixel of movement >.>
                                double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                                if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.isImmobile()) {
                                    this.lastPosX = to.getX();
                                    this.lastPosY = to.getY();
                                    this.lastPosZ = to.getZ();
                                    this.lastYaw = to.getYaw();
                                    this.lastPitch = to.getPitch();

                                    // Skip the first time we do this
                                    if (from.getX() != Double.MAX_VALUE) {
                                        Location oldTo = to.clone();
                                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                                        this.cserver.getPluginManager().callEvent(event);

                                        // If the event is cancelled we move the player back to their old location.
                                        if (event.isCancelled()) {
                                            teleport(from);
                                            return;
                                        }

                                        // If a Plugin has changed the To destination then we teleport the Player
                                        // there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                                        // We only do this if the Event was not cancelled.
                                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                                            this.player.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                            return;
                                        }

                                        // Check to see if the Players Location has some how changed during the call of the event.
                                        // This can happen due to a plugin teleporting the player instead of using .setTo()
                                        if (!from.equals(this.getCraftPlayer().getLocation()) && this.justTeleported) {
                                            this.justTeleported = false;
                                            return;
                                        }
                                    }
                                }
                                // CraftBukkit end
                                this.player.absMoveTo(d0, d1, d2, f, f1);
                                this.clientIsFloating = d11 >= -0.03125D && !flag1 && this.player.gameMode.getGameModeForPlayer() != EnumGamemode.SPECTATOR && !this.server.isFlightAllowed() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.isFallFlying() && !this.player.isAutoSpinAttack() && this.noBlocksAround(this.player);
                                this.player.serverLevel().getChunkSource().move(this.player);
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, packetplayinflying.isOnGround());
                                this.player.setOnGroundWithKnownMovement(packetplayinflying.isOnGround(), new Vec3D(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5));
                                if (flag) {
                                    this.player.resetFallDistance();
                                }

                                this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.lastGoodX = this.player.getX();
                                this.lastGoodY = this.player.getY();
                                this.lastGoodZ = this.player.getZ();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isPlayerCollidingWithAnythingNew(IWorldReader iworldreader, AxisAlignedBB axisalignedbb, double d0, double d1, double d2) {
        AxisAlignedBB axisalignedbb1 = this.player.getBoundingBox().move(d0 - this.player.getX(), d1 - this.player.getY(), d2 - this.player.getZ());
        Iterable<VoxelShape> iterable = iworldreader.getCollisions(this.player, axisalignedbb1.deflate(9.999999747378752E-6D));
        VoxelShape voxelshape = VoxelShapes.create(axisalignedbb.deflate(9.999999747378752E-6D));
        Iterator iterator = iterable.iterator();

        VoxelShape voxelshape1;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            voxelshape1 = (VoxelShape) iterator.next();
        } while (VoxelShapes.joinIsNotEmpty(voxelshape1, voxelshape, OperatorBoolean.AND));

        return true;
    }

    // CraftBukkit start - Delegate to teleport(Location)
    public void teleport(double d0, double d1, double d2, float f, float f1) {
        this.teleport(d0, d1, d2, f, f1, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    public void teleport(double d0, double d1, double d2, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
        this.teleport(d0, d1, d2, f, f1, Collections.emptySet(), cause);
    }

    public void teleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set) {
        this.teleport(d0, d1, d2, f, f1, set, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    public boolean teleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set, PlayerTeleportEvent.TeleportCause cause) { // CraftBukkit - Return event status
        Player player = this.getCraftPlayer();
        Location from = player.getLocation();

        double x = d0;
        double y = d1;
        double z = d2;
        float yaw = f;
        float pitch = f1;

        Location to = new Location(this.getCraftPlayer().getWorld(), x, y, z, yaw, pitch);
        // SPIGOT-5171: Triggered on join
        if (from.equals(to)) {
            this.internalTeleport(d0, d1, d2, f, f1, set);
            return false; // CraftBukkit - Return event status
        }

        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), cause);
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled() || !to.equals(event.getTo())) {
            set.clear(); // Can't relative teleport
            to = event.isCancelled() ? event.getFrom() : event.getTo();
            d0 = to.getX();
            d1 = to.getY();
            d2 = to.getZ();
            f = to.getYaw();
            f1 = to.getPitch();
        }

        this.internalTeleport(d0, d1, d2, f, f1, set);
        return event.isCancelled(); // CraftBukkit - Return event status
    }

    public void teleport(Location dest) {
        internalTeleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch(), Collections.emptySet());
    }

    private void internalTeleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set) {
        // CraftBukkit start
        if (Float.isNaN(f)) {
            f = 0;
        }
        if (Float.isNaN(f1)) {
            f1 = 0;
        }

        this.justTeleported = true;
        // CraftBukkit end
        double d3 = set.contains(RelativeMovement.X) ? this.player.getX() : 0.0D;
        double d4 = set.contains(RelativeMovement.Y) ? this.player.getY() : 0.0D;
        double d5 = set.contains(RelativeMovement.Z) ? this.player.getZ() : 0.0D;
        float f2 = set.contains(RelativeMovement.Y_ROT) ? this.player.getYRot() : 0.0F;
        float f3 = set.contains(RelativeMovement.X_ROT) ? this.player.getXRot() : 0.0F;

        this.awaitingPositionFromClient = new Vec3D(d0, d1, d2);
        if (++this.awaitingTeleport == Integer.MAX_VALUE) {
            this.awaitingTeleport = 0;
        }

        // CraftBukkit start - update last location
        this.lastPosX = this.awaitingPositionFromClient.x;
        this.lastPosY = this.awaitingPositionFromClient.y;
        this.lastPosZ = this.awaitingPositionFromClient.z;
        this.lastYaw = f;
        this.lastPitch = f1;
        // CraftBukkit end

        this.awaitingTeleportTime = this.tickCount;
        this.player.absMoveTo(d0, d1, d2, f, f1);
        this.player.connection.send(new PacketPlayOutPosition(d0 - d3, d1 - d4, d2 - d5, f - f2, f1 - f3, set, this.awaitingTeleport));
    }

    @Override
    public void handlePlayerAction(PacketPlayInBlockDig packetplayinblockdig) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinblockdig, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        BlockPosition blockposition = packetplayinblockdig.getPos();

        this.player.resetLastActionTime();
        PacketPlayInBlockDig.EnumPlayerDigType packetplayinblockdig_enumplayerdigtype = packetplayinblockdig.getAction();

        switch (packetplayinblockdig_enumplayerdigtype) {
            case SWAP_ITEM_WITH_OFFHAND:
                if (!this.player.isSpectator()) {
                    ItemStack itemstack = this.player.getItemInHand(EnumHand.OFF_HAND);

                    // CraftBukkit start - inspiration taken from DispenserRegistry (See SpigotCraft#394)
                    CraftItemStack mainHand = CraftItemStack.asCraftMirror(itemstack);
                    CraftItemStack offHand = CraftItemStack.asCraftMirror(this.player.getItemInHand(EnumHand.MAIN_HAND));
                    PlayerSwapHandItemsEvent swapItemsEvent = new PlayerSwapHandItemsEvent(getCraftPlayer(), mainHand.clone(), offHand.clone());
                    this.cserver.getPluginManager().callEvent(swapItemsEvent);
                    if (swapItemsEvent.isCancelled()) {
                        return;
                    }
                    if (swapItemsEvent.getOffHandItem().equals(offHand)) {
                        this.player.setItemInHand(EnumHand.OFF_HAND, this.player.getItemInHand(EnumHand.MAIN_HAND));
                    } else {
                        this.player.setItemInHand(EnumHand.OFF_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getOffHandItem()));
                    }
                    if (swapItemsEvent.getMainHandItem().equals(mainHand)) {
                        this.player.setItemInHand(EnumHand.MAIN_HAND, itemstack);
                    } else {
                        this.player.setItemInHand(EnumHand.MAIN_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getMainHandItem()));
                    }
                    // CraftBukkit end
                    this.player.stopUsingItem();
                }

                return;
            case DROP_ITEM:
                if (!this.player.isSpectator()) {
                    // limit how quickly items can be dropped
                    // If the ticks aren't the same then the count starts from 0 and we update the lastDropTick.
                    if (this.lastDropTick != MinecraftServer.currentTick) {
                        this.dropCount = 0;
                        this.lastDropTick = MinecraftServer.currentTick;
                    } else {
                        // Else we increment the drop count and check the amount.
                        this.dropCount++;
                        if (this.dropCount >= 20) {
                            LOGGER.warn(this.player.getScoreboardName() + " dropped their items too quickly!");
                            this.disconnect("You dropped your items too quickly (Hacking?)");
                            return;
                        }
                    }
                    // CraftBukkit end
                    this.player.drop(false);
                }

                return;
            case DROP_ALL_ITEMS:
                if (!this.player.isSpectator()) {
                    this.player.drop(true);
                }

                return;
            case RELEASE_USE_ITEM:
                this.player.releaseUsingItem();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                this.player.gameMode.handleBlockBreakAction(blockposition, packetplayinblockdig_enumplayerdigtype, packetplayinblockdig.getDirection(), this.player.level().getMaxBuildHeight(), packetplayinblockdig.getSequence());
                this.player.connection.ackBlockChangesUpTo(packetplayinblockdig.getSequence());
                return;
            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    private static boolean wasBlockPlacementAttempt(EntityPlayer entityplayer, ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            return false;
        } else {
            Item item = itemstack.getItem();

            return (item instanceof ItemBlock || item instanceof ItemBucket) && !entityplayer.getCooldowns().isOnCooldown(item);
        }
    }

    @Override
    public void handleUseItemOn(PacketPlayInUseItem packetplayinuseitem) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinuseitem, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        this.player.connection.ackBlockChangesUpTo(packetplayinuseitem.getSequence());
        WorldServer worldserver = this.player.serverLevel();
        EnumHand enumhand = packetplayinuseitem.getHand();
        ItemStack itemstack = this.player.getItemInHand(enumhand);

        if (itemstack.isItemEnabled(worldserver.enabledFeatures())) {
            MovingObjectPositionBlock movingobjectpositionblock = packetplayinuseitem.getHitResult();
            Vec3D vec3d = movingobjectpositionblock.getLocation();
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
            Vec3D vec3d1 = Vec3D.atCenterOf(blockposition);

            if (this.player.getEyePosition().distanceToSqr(vec3d1) <= PlayerConnection.MAX_INTERACTION_DISTANCE) {
                Vec3D vec3d2 = vec3d.subtract(vec3d1);
                double d0 = 1.0000001D;

                if (Math.abs(vec3d2.x()) < 1.0000001D && Math.abs(vec3d2.y()) < 1.0000001D && Math.abs(vec3d2.z()) < 1.0000001D) {
                    EnumDirection enumdirection = movingobjectpositionblock.getDirection();

                    this.player.resetLastActionTime();
                    int i = this.player.level().getMaxBuildHeight();

                    if (blockposition.getY() < i) {
                        if (this.awaitingPositionFromClient == null && this.player.distanceToSqr((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D) < 64.0D && worldserver.mayInteract(this.player, blockposition)) {
                            this.player.stopUsingItem(); // CraftBukkit - SPIGOT-4706
                            EnumInteractionResult enuminteractionresult = this.player.gameMode.useItemOn(this.player, worldserver, itemstack, enumhand, movingobjectpositionblock);

                            if (enumdirection == EnumDirection.UP && !enuminteractionresult.consumesAction() && blockposition.getY() >= i - 1 && wasBlockPlacementAttempt(this.player, itemstack)) {
                                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable("build.tooHigh", i - 1).withStyle(EnumChatFormat.RED);

                                this.player.sendSystemMessage(ichatmutablecomponent, true);
                            } else if (enuminteractionresult.shouldSwing()) {
                                this.player.swing(enumhand, true);
                            }
                        }
                    } else {
                        IChatMutableComponent ichatmutablecomponent1 = IChatBaseComponent.translatable("build.tooHigh", i - 1).withStyle(EnumChatFormat.RED);

                        this.player.sendSystemMessage(ichatmutablecomponent1, true);
                    }

                    this.player.connection.send(new PacketPlayOutBlockChange(worldserver, blockposition));
                    this.player.connection.send(new PacketPlayOutBlockChange(worldserver, blockposition.relative(enumdirection)));
                } else {
                    PlayerConnection.LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", new Object[]{this.player.getGameProfile().getName(), vec3d, blockposition});
                }
            }
        }
    }

    @Override
    public void handleUseItem(PacketPlayInBlockPlace packetplayinblockplace) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinblockplace, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        this.ackBlockChangesUpTo(packetplayinblockplace.getSequence());
        WorldServer worldserver = this.player.serverLevel();
        EnumHand enumhand = packetplayinblockplace.getHand();
        ItemStack itemstack = this.player.getItemInHand(enumhand);

        this.player.resetLastActionTime();
        if (!itemstack.isEmpty() && itemstack.isItemEnabled(worldserver.enabledFeatures())) {
            // CraftBukkit start
            // Raytrace to look for 'rogue armswings'
            float f1 = this.player.getXRot();
            float f2 = this.player.getYRot();
            double d0 = this.player.getX();
            double d1 = this.player.getY() + (double) this.player.getEyeHeight();
            double d2 = this.player.getZ();
            Vec3D vec3d = new Vec3D(d0, d1, d2);

            float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
            float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = player.gameMode.getGameModeForPlayer()== EnumGamemode.CREATIVE ? 5.0D : 4.5D;
            Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
            MovingObjectPosition movingobjectposition = this.player.level().clip(new RayTrace(vec3d, vec3d1, RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, player));

            boolean cancelled;
            if (movingobjectposition == null || movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack, enumhand);
                cancelled = event.useItemInHand() == Event.Result.DENY;
            } else {
                MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock) movingobjectposition;
                if (player.gameMode.firedInteract && player.gameMode.interactPosition.equals(movingobjectpositionblock.getBlockPos()) && player.gameMode.interactHand == enumhand && ItemStack.isSameItemSameTags(player.gameMode.interactItemStack, itemstack)) {
                    cancelled = player.gameMode.interactResult;
                } else {
                    org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getDirection(), itemstack, true, enumhand, movingobjectpositionblock.getLocation());
                    cancelled = event.useItemInHand() == Event.Result.DENY;
                }
                player.gameMode.firedInteract = false;
            }

            if (cancelled) {
                this.player.getBukkitEntity().updateInventory(); // SPIGOT-2524
                return;
            }
            itemstack = this.player.getItemInHand(enumhand); // Update in case it was changed in the event
            if (itemstack.isEmpty()) {
                return;
            }
            // CraftBukkit end
            EnumInteractionResult enuminteractionresult = this.player.gameMode.useItem(this.player, worldserver, itemstack, enumhand);

            if (enuminteractionresult.shouldSwing()) {
                this.player.swing(enumhand, true);
            }

        }
    }

    @Override
    public void handleTeleportToEntityPacket(PacketPlayInSpectate packetplayinspectate) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinspectate, this, this.player.serverLevel());
        if (this.player.isSpectator()) {
            Iterator iterator = this.server.getAllLevels().iterator();

            while (iterator.hasNext()) {
                WorldServer worldserver = (WorldServer) iterator.next();
                Entity entity = packetplayinspectate.getEntity(worldserver);

                if (entity != null) {
                    this.player.teleportTo(worldserver, entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.SPECTATE); // CraftBukkit
                    return;
                }
            }
        }

    }

    @Override
    public void handlePaddleBoat(PacketPlayInBoatMove packetplayinboatmove) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinboatmove, this, this.player.serverLevel());
        Entity entity = this.player.getControlledVehicle();

        if (entity instanceof EntityBoat) {
            EntityBoat entityboat = (EntityBoat) entity;

            entityboat.setPaddleState(packetplayinboatmove.getLeft(), packetplayinboatmove.getRight());
        }

    }

    @Override
    public void onDisconnect(IChatBaseComponent ichatbasecomponent) {
        // CraftBukkit start - Rarely it would send a disconnect line twice
        if (this.processedDisconnect) {
            return;
        } else {
            this.processedDisconnect = true;
        }
        // CraftBukkit end
        PlayerConnection.LOGGER.info("{} lost connection: {}", this.player.getName().getString(), ichatbasecomponent.getString());
        this.removePlayerFromWorld();
        super.onDisconnect(ichatbasecomponent);
    }

    private void removePlayerFromWorld() {
        this.chatMessageChain.close();
        // CraftBukkit start - Replace vanilla quit message handling with our own.
        /*
        this.server.invalidateStatus();
        this.server.getPlayerList().broadcastSystemMessage(IChatBaseComponent.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(EnumChatFormat.YELLOW), false);
        */

        this.player.disconnect();
        String quitMessage = this.server.getPlayerList().remove(this.player);
        if ((quitMessage != null) && (quitMessage.length() > 0)) {
            this.server.getPlayerList().broadcastMessage(CraftChatMessage.fromString(quitMessage));
        }
        // CraftBukkit end
        this.player.getTextFilter().leave();
    }

    public void ackBlockChangesUpTo(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        } else {
            this.ackBlockChangesUpTo = Math.max(i, this.ackBlockChangesUpTo);
        }
    }

    @Override
    public void handleSetCarriedItem(PacketPlayInHeldItemSlot packetplayinhelditemslot) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinhelditemslot, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        if (packetplayinhelditemslot.getSlot() >= 0 && packetplayinhelditemslot.getSlot() < PlayerInventory.getSelectionSize()) {
            PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getCraftPlayer(), this.player.getInventory().selected, packetplayinhelditemslot.getSlot());
            this.cserver.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                this.send(new PacketPlayOutHeldItemSlot(this.player.getInventory().selected));
                this.player.resetLastActionTime();
                return;
            }
            // CraftBukkit end
            if (this.player.getInventory().selected != packetplayinhelditemslot.getSlot() && this.player.getUsedItemHand() == EnumHand.MAIN_HAND) {
                this.player.stopUsingItem();
            }

            this.player.getInventory().selected = packetplayinhelditemslot.getSlot();
            this.player.resetLastActionTime();
        } else {
            PlayerConnection.LOGGER.warn("{} tried to set an invalid carried item", this.player.getName().getString());
            this.disconnect("Invalid hotbar selection (Hacking?)"); // CraftBukkit
        }
    }

    @Override
    public void handleChat(PacketPlayInChat packetplayinchat) {
        // CraftBukkit start - async chat
        // SPIGOT-3638
        if (this.server.isStopped()) {
            return;
        }
        // CraftBukkit end
        if (isChatMessageIllegal(packetplayinchat.message())) {
            this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.illegal_characters"));
        } else {
            Optional<LastSeenMessages> optional = this.tryHandleChat(packetplayinchat.message(), packetplayinchat.timeStamp(), packetplayinchat.lastSeenMessages());

            if (optional.isPresent()) {
                // this.server.submit(() -> { // CraftBukkit - async chat
                    PlayerChatMessage playerchatmessage;

                    try {
                        playerchatmessage = this.getSignedMessage(packetplayinchat, (LastSeenMessages) optional.get());
                    } catch (SignedMessageChain.a signedmessagechain_a) {
                        this.handleMessageDecodeFailure(signedmessagechain_a);
                        return;
                    }

                    CompletableFuture<FilteredText> completablefuture = this.filterTextPacket(playerchatmessage.signedContent());
                    IChatBaseComponent ichatbasecomponent = this.server.getChatDecorator().decorate(this.player, playerchatmessage.decoratedContent());

                    this.chatMessageChain.append((executor) -> {
                        return completablefuture.thenAcceptAsync((filteredtext) -> {
                            PlayerChatMessage playerchatmessage1 = playerchatmessage.withUnsignedContent(ichatbasecomponent).filter(filteredtext.mask());

                            this.broadcastChatMessage(playerchatmessage1);
                        }, this.server.chatExecutor); // CraftBukkit - async chat
                    });
                // }); // CraftBukkit - async chat
            }

        }
    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket serverboundchatcommandpacket) {
        if (isChatMessageIllegal(serverboundchatcommandpacket.command())) {
            this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.illegal_characters"));
        } else {
            Optional<LastSeenMessages> optional = this.tryHandleChat(serverboundchatcommandpacket.command(), serverboundchatcommandpacket.timeStamp(), serverboundchatcommandpacket.lastSeenMessages());

            if (optional.isPresent()) {
                this.server.submit(() -> {
                    // CraftBukkit start - SPIGOT-7346: Prevent disconnected players from executing commands
                    if (player.hasDisconnected()) {
                        return;
                    }
                    // CraftBukkit end

                    this.performChatCommand(serverboundchatcommandpacket, (LastSeenMessages) optional.get());
                    this.detectRateSpam();
                });
            }

        }
    }

    private void performChatCommand(ServerboundChatCommandPacket serverboundchatcommandpacket, LastSeenMessages lastseenmessages) {
        // CraftBukkit start
        String command = "/" + serverboundchatcommandpacket.command();
        PlayerConnection.LOGGER.info(this.player.getScoreboardName() + " issued server command: " + command);

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(getCraftPlayer(), command, new LazyPlayerSet(server));
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }
        command = event.getMessage().substring(1);

        ParseResults parseresults = this.parseCommand(command);
        // CraftBukkit end

        Map map;

        try {
            map = (serverboundchatcommandpacket.command().equals(command)) ? this.collectSignedArguments(serverboundchatcommandpacket, SignableCommand.of(parseresults), lastseenmessages) : Collections.emptyMap(); // CraftBukkit
        } catch (SignedMessageChain.a signedmessagechain_a) {
            this.handleMessageDecodeFailure(signedmessagechain_a);
            return;
        }

        CommandSigningContext.a commandsigningcontext_a = new CommandSigningContext.a(map);

        parseresults = CommandDispatcher.<CommandListenerWrapper>mapSource(parseresults, (commandlistenerwrapper) -> { // CraftBukkit - decompile error
            return commandlistenerwrapper.withSigningContext(commandsigningcontext_a, this.chatMessageChain);
        });
        this.server.getCommands().performCommand(parseresults, command); // CraftBukkit
    }

    private void handleMessageDecodeFailure(SignedMessageChain.a signedmessagechain_a) {
        if (signedmessagechain_a.shouldDisconnect()) {
            this.disconnect(signedmessagechain_a.getComponent());
        } else {
            this.player.sendSystemMessage(signedmessagechain_a.getComponent().copy().withStyle(EnumChatFormat.RED));
        }

    }

    private Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandPacket serverboundchatcommandpacket, SignableCommand<?> signablecommand, LastSeenMessages lastseenmessages) throws SignedMessageChain.a {
        Map<String, PlayerChatMessage> map = new Object2ObjectOpenHashMap();
        Iterator iterator = signablecommand.arguments().iterator();

        while (iterator.hasNext()) {
            SignableCommand.a<?> signablecommand_a = (SignableCommand.a) iterator.next();
            MessageSignature messagesignature = serverboundchatcommandpacket.argumentSignatures().get(signablecommand_a.name());
            SignedMessageBody signedmessagebody = new SignedMessageBody(signablecommand_a.value(), serverboundchatcommandpacket.timeStamp(), serverboundchatcommandpacket.salt(), lastseenmessages);

            map.put(signablecommand_a.name(), this.signedMessageDecoder.unpack(messagesignature, signedmessagebody));
        }

        return map;
    }

    private ParseResults<CommandListenerWrapper> parseCommand(String s) {
        com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> com_mojang_brigadier_commanddispatcher = this.server.getCommands().getDispatcher();

        return com_mojang_brigadier_commanddispatcher.parse(s, this.player.createCommandSourceStack());
    }

    private Optional<LastSeenMessages> tryHandleChat(String s, Instant instant, LastSeenMessages.b lastseenmessages_b) {
        if (!this.updateChatOrder(instant)) {
            PlayerConnection.LOGGER.warn("{} sent out-of-order chat: '{}'", this.player.getName().getString(), s);
            this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.out_of_order_chat"));
            return Optional.empty();
        } else {
            Optional<LastSeenMessages> optional = this.unpackAndApplyLastSeen(lastseenmessages_b);

            if (this.player.isRemoved() || this.player.getChatVisibility() == EnumChatVisibility.HIDDEN) { // CraftBukkit - dead men tell no tales
                this.send(new ClientboundSystemChatPacket(IChatBaseComponent.translatable("chat.disabled.options").withStyle(EnumChatFormat.RED), false));
                return Optional.empty();
            } else {
                this.player.resetLastActionTime();
                return optional;
            }
        }
    }

    private Optional<LastSeenMessages> unpackAndApplyLastSeen(LastSeenMessages.b lastseenmessages_b) {
        LastSeenMessagesValidator lastseenmessagesvalidator = this.lastSeenMessages;

        synchronized (this.lastSeenMessages) {
            Optional<LastSeenMessages> optional = this.lastSeenMessages.applyUpdate(lastseenmessages_b);

            if (optional.isEmpty()) {
                PlayerConnection.LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
                this.disconnect(PlayerConnection.CHAT_VALIDATION_FAILED);
            }

            return optional;
        }
    }

    private boolean updateChatOrder(Instant instant) {
        Instant instant1;

        do {
            instant1 = (Instant) this.lastChatTimeStamp.get();
            if (instant.isBefore(instant1)) {
                return false;
            }
        } while (!this.lastChatTimeStamp.compareAndSet(instant1, instant));

        return true;
    }

    private static boolean isChatMessageIllegal(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (!SharedConstants.isAllowedChatCharacter(s.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    // CraftBukkit start - add method
    public void chat(String s, PlayerChatMessage original, boolean async) {
        if (s.isEmpty() || this.player.getChatVisibility() == EnumChatVisibility.HIDDEN) {
            return;
        }
        OutgoingChatMessage outgoing = OutgoingChatMessage.create(original);

        if (!async && s.startsWith("/")) {
            this.handleCommand(s);
        } else if (this.player.getChatVisibility() == EnumChatVisibility.SYSTEM) {
            // Do nothing, this is coming from a plugin
        } else {
            Player player = this.getCraftPlayer();
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(server));
            String originalFormat = event.getFormat(), originalMessage = event.getMessage();
            this.cserver.getPluginManager().callEvent(event);

            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                // Evil plugins still listening to deprecated event
                final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
                queueEvent.setCancelled(event.isCancelled());
                Waitable waitable = new Waitable() {
                    @Override
                    protected Object evaluate() {
                        org.bukkit.Bukkit.getPluginManager().callEvent(queueEvent);

                        if (queueEvent.isCancelled()) {
                            return null;
                        }

                        String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                        if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                            if (originalFormat.equals(queueEvent.getFormat()) && originalMessage.equals(queueEvent.getMessage()) && queueEvent.getPlayer().getName().equalsIgnoreCase(queueEvent.getPlayer().getDisplayName())) {
                                PlayerConnection.this.server.getPlayerList().broadcastChatMessage(original, PlayerConnection.this.player, ChatMessageType.bind(ChatMessageType.CHAT, (Entity) PlayerConnection.this.player));
                                return null;
                            }

                            for (EntityPlayer recipient : server.getPlayerList().players) {
                                recipient.getBukkitEntity().sendMessage(PlayerConnection.this.player.getUUID(), message);
                            }
                        } else {
                            for (Player player : queueEvent.getRecipients()) {
                                player.sendMessage(PlayerConnection.this.player.getUUID(), message);
                            }
                        }
                        PlayerConnection.this.server.console.sendMessage(message);

                        return null;
                    }};
                if (async) {
                    server.processQueue.add(waitable);
                } else {
                    waitable.run();
                }
                try {
                    waitable.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                } catch (ExecutionException e) {
                    throw new RuntimeException("Exception processing chat event", e.getCause());
                }
            } else {
                if (event.isCancelled()) {
                    return;
                }

                s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                    if (originalFormat.equals(event.getFormat()) && originalMessage.equals(event.getMessage()) && event.getPlayer().getName().equalsIgnoreCase(event.getPlayer().getDisplayName())) {
                        PlayerConnection.this.server.getPlayerList().broadcastChatMessage(original, PlayerConnection.this.player, ChatMessageType.bind(ChatMessageType.CHAT, (Entity) PlayerConnection.this.player));
                        return;
                    }

                    for (EntityPlayer recipient : server.getPlayerList().players) {
                        recipient.getBukkitEntity().sendMessage(PlayerConnection.this.player.getUUID(), s);
                    }
                } else {
                    for (Player recipient : event.getRecipients()) {
                        recipient.sendMessage(PlayerConnection.this.player.getUUID(), s);
                    }
                }
                server.console.sendMessage(s);
            }
        }
    }

    private void handleCommand(String s) {
        this.LOGGER.info(this.player.getScoreboardName() + " issued server command: " + s);

        CraftPlayer player = this.getCraftPlayer();

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s, new LazyPlayerSet(server));
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        try {
            if (this.cserver.dispatchCommand(event.getPlayer(), event.getMessage().substring(1))) {
                return;
            }
        } catch (org.bukkit.command.CommandException ex) {
            player.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(PlayerConnection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            return;
        }
    }
    // CraftBukkit end

    private PlayerChatMessage getSignedMessage(PacketPlayInChat packetplayinchat, LastSeenMessages lastseenmessages) throws SignedMessageChain.a {
        SignedMessageBody signedmessagebody = new SignedMessageBody(packetplayinchat.message(), packetplayinchat.timeStamp(), packetplayinchat.salt(), lastseenmessages);

        return this.signedMessageDecoder.unpack(packetplayinchat.signature(), signedmessagebody);
    }

    private void broadcastChatMessage(PlayerChatMessage playerchatmessage) {
        // CraftBukkit start
        String s = playerchatmessage.signedContent();
        if (s.isEmpty()) {
            LOGGER.warn(this.player.getScoreboardName() + " tried to send an empty message");
        } else if (getCraftPlayer().isConversing()) {
            final String conversationInput = s;
            this.server.processQueue.add(new Runnable() {
                @Override
                public void run() {
                    getCraftPlayer().acceptConversationInput(conversationInput);
                }
            });
        } else if (this.player.getChatVisibility() == EnumChatVisibility.SYSTEM) { // Re-add "Command Only" flag check
            this.send(new ClientboundSystemChatPacket(IChatBaseComponent.translatable("chat.cannotSend").withStyle(EnumChatFormat.RED), false));
        } else {
            this.chat(s, playerchatmessage, true);
        }
        // this.server.getPlayerList().broadcastChatMessage(playerchatmessage, this.player, ChatMessageType.bind(ChatMessageType.CHAT, (Entity) this.player));
        // CraftBukkit end
        this.detectRateSpam();
    }

    private void detectRateSpam() {
        // CraftBukkit start - replaced with thread safe throttle
        // this.chatSpamTickCount += 20;
        if (this.chatSpamTickCount.addAndGet(20) > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
            // CraftBukkit end
            this.disconnect(IChatBaseComponent.translatable("disconnect.spam"));
        }

    }

    @Override
    public void handleChatAck(ServerboundChatAckPacket serverboundchatackpacket) {
        LastSeenMessagesValidator lastseenmessagesvalidator = this.lastSeenMessages;

        synchronized (this.lastSeenMessages) {
            if (!this.lastSeenMessages.applyOffset(serverboundchatackpacket.offset())) {
                PlayerConnection.LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
                this.disconnect(PlayerConnection.CHAT_VALIDATION_FAILED);
            }

        }
    }

    @Override
    public void handleAnimate(PacketPlayInArmAnimation packetplayinarmanimation) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinarmanimation, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        this.player.resetLastActionTime();
        // CraftBukkit start - Raytrace to look for 'rogue armswings'
        float f1 = this.player.getXRot();
        float f2 = this.player.getYRot();
        double d0 = this.player.getX();
        double d1 = this.player.getY() + (double) this.player.getEyeHeight();
        double d2 = this.player.getZ();
        Location origin = new Location(this.player.level().getWorld(), d0, d1, d2, f2, f1);

        double d3 = player.gameMode.getGameModeForPlayer() == EnumGamemode.CREATIVE ? 5.0D : 4.5D;
        // SPIGOT-5607: Only call interact event if no block or entity is being clicked. Use bukkit ray trace method, because it handles blocks and entities at the same time
        // SPIGOT-7429: Make sure to call PlayerInteractEvent for spectators and non-pickable entities
        org.bukkit.util.RayTraceResult result = this.player.level().getWorld().rayTrace(origin, origin.getDirection(), d3, org.bukkit.FluidCollisionMode.NEVER, false, 0.1, entity -> {
            Entity handle = ((CraftEntity) entity).getHandle();
            return entity != this.player.getBukkitEntity() && this.player.getBukkitEntity().canSee(entity) && !handle.isSpectator() && handle.isPickable() && !handle.isPassengerOfSameVehicle(player);
        });
        if (result == null) {
            CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.getInventory().getSelected(), EnumHand.MAIN_HAND);
        }

        // Arm swing animation
        PlayerAnimationEvent event = new PlayerAnimationEvent(this.getCraftPlayer(), (packetplayinarmanimation.getHand() == EnumHand.MAIN_HAND) ? PlayerAnimationType.ARM_SWING : PlayerAnimationType.OFF_ARM_SWING);
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;
        // CraftBukkit end
        this.player.swing(packetplayinarmanimation.getHand());
    }

    @Override
    public void handlePlayerCommand(PacketPlayInEntityAction packetplayinentityaction) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinentityaction, this, this.player.serverLevel());
        // CraftBukkit start
        if (this.player.isRemoved()) return;
        switch (packetplayinentityaction.getAction()) {
            case PRESS_SHIFT_KEY:
            case RELEASE_SHIFT_KEY:
                PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getCraftPlayer(), packetplayinentityaction.getAction() == PacketPlayInEntityAction.EnumPlayerAction.PRESS_SHIFT_KEY);
                this.cserver.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }
                break;
            case START_SPRINTING:
            case STOP_SPRINTING:
                PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getCraftPlayer(), packetplayinentityaction.getAction() == PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING);
                this.cserver.getPluginManager().callEvent(e2);

                if (e2.isCancelled()) {
                    return;
                }
                break;
        }
        // CraftBukkit end
        this.player.resetLastActionTime();
        Entity entity;
        IJumpable ijumpable;

        switch (packetplayinentityaction.getAction()) {
            case PRESS_SHIFT_KEY:
                this.player.setShiftKeyDown(true);
                break;
            case RELEASE_SHIFT_KEY:
                this.player.setShiftKeyDown(false);
                break;
            case START_SPRINTING:
                this.player.setSprinting(true);
                break;
            case STOP_SPRINTING:
                this.player.setSprinting(false);
                break;
            case STOP_SLEEPING:
                if (this.player.isSleeping()) {
                    this.player.stopSleepInBed(false, true);
                    this.awaitingPositionFromClient = this.player.position();
                }
                break;
            case START_RIDING_JUMP:
                entity = this.player.getControlledVehicle();
                if (entity instanceof IJumpable) {
                    ijumpable = (IJumpable) entity;
                    int i = packetplayinentityaction.getData();

                    if (ijumpable.canJump() && i > 0) {
                        ijumpable.handleStartJump(i);
                    }
                }
                break;
            case STOP_RIDING_JUMP:
                entity = this.player.getControlledVehicle();
                if (entity instanceof IJumpable) {
                    ijumpable = (IJumpable) entity;
                    ijumpable.handleStopJump();
                }
                break;
            case OPEN_INVENTORY:
                entity = this.player.getVehicle();
                if (entity instanceof HasCustomInventoryScreen) {
                    HasCustomInventoryScreen hascustominventoryscreen = (HasCustomInventoryScreen) entity;

                    hascustominventoryscreen.openCustomInventoryScreen(this.player);
                }
                break;
            case START_FALL_FLYING:
                if (!this.player.tryToStartFallFlying()) {
                    this.player.stopFallFlying();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid client command!");
        }

    }

    public void addPendingMessage(PlayerChatMessage playerchatmessage) {
        MessageSignature messagesignature = playerchatmessage.signature();

        if (messagesignature != null) {
            this.messageSignatureCache.push(playerchatmessage);
            LastSeenMessagesValidator lastseenmessagesvalidator = this.lastSeenMessages;
            int i;

            synchronized (this.lastSeenMessages) {
                this.lastSeenMessages.addPending(messagesignature);
                i = this.lastSeenMessages.trackedMessagesCount();
            }

            if (i > 4096) {
                this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.too_many_pending_chats"));
            }

        }
    }

    public void sendPlayerChatMessage(PlayerChatMessage playerchatmessage, ChatMessageType.a chatmessagetype_a) {
        // CraftBukkit start - SPIGOT-7262: if hidden we have to send as disguised message. Query whether we should send at all (but changing this may not be expected).
        if (!getCraftPlayer().canSee(playerchatmessage.link().sender())) {
            sendDisguisedChatMessage(playerchatmessage.decoratedContent(), chatmessagetype_a);
            return;
        }
        // CraftBukkit end
        this.send(new ClientboundPlayerChatPacket(playerchatmessage.link().sender(), playerchatmessage.link().index(), playerchatmessage.signature(), playerchatmessage.signedBody().pack(this.messageSignatureCache), playerchatmessage.unsignedContent(), playerchatmessage.filterMask(), chatmessagetype_a.toNetwork(this.player.level().registryAccess())));
        this.addPendingMessage(playerchatmessage);
    }

    public void sendDisguisedChatMessage(IChatBaseComponent ichatbasecomponent, ChatMessageType.a chatmessagetype_a) {
        this.send(new ClientboundDisguisedChatPacket(ichatbasecomponent, chatmessagetype_a.toNetwork(this.player.level().registryAccess())));
    }

    public SocketAddress getRemoteAddress() {
        return this.connection.getRemoteAddress();
    }

    public void switchToConfig() {
        this.waitingForSwitchToConfig = true;
        this.removePlayerFromWorld();
        this.send(new ClientboundStartConfigurationPacket());
    }

    @Override
    public void handlePingRequest(PacketStatusInPing packetstatusinping) {
        this.connection.send(new PacketStatusOutPong(packetstatusinping.getTime()));
    }

    @Override
    public void handleInteract(PacketPlayInUseEntity packetplayinuseentity) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinuseentity, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        final WorldServer worldserver = this.player.serverLevel();
        final Entity entity = packetplayinuseentity.getTarget(worldserver);

        this.player.resetLastActionTime();
        this.player.setShiftKeyDown(packetplayinuseentity.isUsingSecondaryAction());
        if (entity != null) {
            if (!worldserver.getWorldBorder().isWithinBounds(entity.blockPosition())) {
                return;
            }

            AxisAlignedBB axisalignedbb = entity.getBoundingBox();

            if (axisalignedbb.distanceToSqr(this.player.getEyePosition()) < PlayerConnection.MAX_INTERACTION_DISTANCE) {
                packetplayinuseentity.dispatch(new PacketPlayInUseEntity.c() {
                    private void performInteraction(EnumHand enumhand, PlayerConnection.a playerconnection_a, PlayerInteractEntityEvent event) { // CraftBukkit
                        ItemStack itemstack = PlayerConnection.this.player.getItemInHand(enumhand);

                        if (itemstack.isItemEnabled(worldserver.enabledFeatures())) {
                            ItemStack itemstack1 = itemstack.copy();
                            // CraftBukkit start
                            ItemStack itemInHand = PlayerConnection.this.player.getItemInHand(enumhand);
                            boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.LEAD && entity instanceof EntityInsentient;
                            Item origItem = player.getInventory().getSelected() == null ? null : player.getInventory().getSelected().getItem();

                            cserver.getPluginManager().callEvent(event);

                            // Entity in bucket - SPIGOT-4048 and SPIGOT-6859a
                            if ((entity instanceof Bucketable && entity instanceof EntityLiving && origItem != null && origItem.asItem() == Items.WATER_BUCKET) && (event.isCancelled() || player.getInventory().getSelected() == null || player.getInventory().getSelected().getItem() != origItem)) {
                                send(new PacketPlayOutSpawnEntity(entity));
                                player.containerMenu.sendAllDataToRemote();
                            }

                            if (triggerLeashUpdate && (event.isCancelled() || player.getInventory().getSelected() == null || player.getInventory().getSelected().getItem() != origItem)) {
                                // Refresh the current leash state
                                send(new PacketPlayOutAttachEntity(entity, ((EntityInsentient) entity).getLeashHolder()));
                            }

                            if (event.isCancelled() || player.getInventory().getSelected() == null || player.getInventory().getSelected().getItem() != origItem) {
                                // Refresh the current entity metadata
                                entity.getEntityData().refresh(player);
                                // SPIGOT-7136 - Allays
                                if (entity instanceof Allay) {
                                    send(new PacketPlayOutEntityEquipment(entity.getId(), Arrays.stream(EnumItemSlot.values()).map((slot) -> Pair.of(slot, ((EntityLiving) entity).getItemBySlot(slot).copy())).collect(Collectors.toList())));
                                    player.containerMenu.sendAllDataToRemote();
                                }
                            }

                            if (event.isCancelled()) {
                                return;
                            }
                            // CraftBukkit end
                            EnumInteractionResult enuminteractionresult = playerconnection_a.run(PlayerConnection.this.player, entity, enumhand);

                            // CraftBukkit start
                            if (!itemInHand.isEmpty() && itemInHand.getCount() <= -1) {
                                player.containerMenu.sendAllDataToRemote();
                            }
                            // CraftBukkit end

                            if (enuminteractionresult.consumesAction()) {
                                CriterionTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(PlayerConnection.this.player, itemstack1, entity);
                                if (enuminteractionresult.shouldSwing()) {
                                    PlayerConnection.this.player.swing(enumhand, true);
                                }
                            }

                        }
                    }

                    @Override
                    public void onInteraction(EnumHand enumhand) {
                        this.performInteraction(enumhand, EntityHuman::interactOn, new PlayerInteractEntityEvent(getCraftPlayer(), entity.getBukkitEntity(), (enumhand == EnumHand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND)); // CraftBukkit
                    }

                    @Override
                    public void onInteraction(EnumHand enumhand, Vec3D vec3d) {
                        this.performInteraction(enumhand, (entityplayer, entity1, enumhand1) -> {
                            return entity1.interactAt(entityplayer, vec3d, enumhand1);
                        }, new PlayerInteractAtEntityEvent(getCraftPlayer(), entity.getBukkitEntity(), new org.bukkit.util.Vector(vec3d.x, vec3d.y, vec3d.z), (enumhand == EnumHand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND)); // CraftBukkit
                    }

                    @Override
                    public void onAttack() {
                        // CraftBukkit
                        if (!(entity instanceof EntityItem) && !(entity instanceof EntityExperienceOrb) && !(entity instanceof EntityArrow) && (entity != PlayerConnection.this.player || player.isSpectator())) {
                            ItemStack itemstack = PlayerConnection.this.player.getItemInHand(EnumHand.MAIN_HAND);

                            if (itemstack.isItemEnabled(worldserver.enabledFeatures())) {
                                PlayerConnection.this.player.attack(entity);
                                // CraftBukkit start
                                if (!itemstack.isEmpty() && itemstack.getCount() <= -1) {
                                    player.containerMenu.sendAllDataToRemote();
                                }
                                // CraftBukkit end
                            }
                        } else {
                            PlayerConnection.this.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                            PlayerConnection.LOGGER.warn("Player {} tried to attack an invalid entity", PlayerConnection.this.player.getName().getString());
                        }
                    }
                });
            }
        }

    }

    @Override
    public void handleClientCommand(PacketPlayInClientCommand packetplayinclientcommand) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinclientcommand, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        PacketPlayInClientCommand.EnumClientCommand packetplayinclientcommand_enumclientcommand = packetplayinclientcommand.getAction();

        switch (packetplayinclientcommand_enumclientcommand) {
            case PERFORM_RESPAWN:
                if (this.player.wonGame) {
                    this.player.wonGame = false;
                    this.player = this.server.getPlayerList().respawn(this.player, true, RespawnReason.END_PORTAL);
                    CriterionTriggers.CHANGED_DIMENSION.trigger(this.player, World.END, World.OVERWORLD);
                } else {
                    if (this.player.getHealth() > 0.0F) {
                        return;
                    }

                    this.player = this.server.getPlayerList().respawn(this.player, false, RespawnReason.DEATH);
                    if (this.server.isHardcore()) {
                        this.player.setGameMode(EnumGamemode.SPECTATOR);
                        ((GameRules.GameRuleBoolean) this.player.level().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS)).set(false, this.server);
                    }
                }
                break;
            case REQUEST_STATS:
                this.player.getStats().sendStats(this.player);
        }

    }

    @Override
    public void handleContainerClose(PacketPlayInCloseWindow packetplayinclosewindow) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinclosewindow, this, this.player.serverLevel());

        if (this.player.isImmobile()) return; // CraftBukkit
        CraftEventFactory.handleInventoryCloseEvent(this.player); // CraftBukkit

        this.player.doCloseContainer();
    }

    @Override
    public void handleContainerClick(PacketPlayInWindowClick packetplayinwindowclick) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinwindowclick, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId == packetplayinwindowclick.getContainerId() && this.player.containerMenu.stillValid(this.player)) { // CraftBukkit
            boolean cancelled = this.player.isSpectator(); // CraftBukkit - see below if
            if (false/*this.player.isSpectator()*/) { // CraftBukkit
                this.player.containerMenu.sendAllDataToRemote();
            } else if (!this.player.containerMenu.stillValid(this.player)) {
                PlayerConnection.LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            } else {
                int i = packetplayinwindowclick.getSlotNum();

                if (!this.player.containerMenu.isValidSlotIndex(i)) {
                    PlayerConnection.LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", new Object[]{this.player.getName(), i, this.player.containerMenu.slots.size()});
                } else {
                    boolean flag = packetplayinwindowclick.getStateId() != this.player.containerMenu.getStateId();

                    this.player.containerMenu.suppressRemoteUpdates();
                    // CraftBukkit start - Call InventoryClickEvent
                    if (packetplayinwindowclick.getSlotNum() < -1 && packetplayinwindowclick.getSlotNum() != -999) {
                        return;
                    }

                    InventoryView inventory = this.player.containerMenu.getBukkitView();
                    SlotType type = inventory.getSlotType(packetplayinwindowclick.getSlotNum());

                    InventoryClickEvent event;
                    ClickType click = ClickType.UNKNOWN;
                    InventoryAction action = InventoryAction.UNKNOWN;

                    ItemStack itemstack = ItemStack.EMPTY;

                    switch (packetplayinwindowclick.getClickType()) {
                        case PICKUP:
                            if (packetplayinwindowclick.getButtonNum() == 0) {
                                click = ClickType.LEFT;
                            } else if (packetplayinwindowclick.getButtonNum() == 1) {
                                click = ClickType.RIGHT;
                            }
                            if (packetplayinwindowclick.getButtonNum() == 0 || packetplayinwindowclick.getButtonNum() == 1) {
                                action = InventoryAction.NOTHING; // Don't want to repeat ourselves
                                if (packetplayinwindowclick.getSlotNum() == -999) {
                                    if (!player.containerMenu.getCarried().isEmpty()) {
                                        action = packetplayinwindowclick.getButtonNum() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                                    }
                                } else if (packetplayinwindowclick.getSlotNum() < 0)  {
                                    action = InventoryAction.NOTHING;
                                } else {
                                    Slot slot = this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum());
                                    if (slot != null) {
                                        ItemStack clickedItem = slot.getItem();
                                        ItemStack cursor = player.containerMenu.getCarried();
                                        if (clickedItem.isEmpty()) {
                                            if (!cursor.isEmpty()) {
                                                action = packetplayinwindowclick.getButtonNum() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                            }
                                        } else if (slot.mayPickup(player)) {
                                            if (cursor.isEmpty()) {
                                                action = packetplayinwindowclick.getButtonNum() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                            } else if (slot.mayPlace(cursor)) {
                                                if (ItemStack.isSameItemSameTags(clickedItem, cursor)) {
                                                    int toPlace = packetplayinwindowclick.getButtonNum() == 0 ? cursor.getCount() : 1;
                                                    toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.getCount());
                                                    toPlace = Math.min(toPlace, slot.container.getMaxStackSize() - clickedItem.getCount());
                                                    if (toPlace == 1) {
                                                        action = InventoryAction.PLACE_ONE;
                                                    } else if (toPlace == cursor.getCount()) {
                                                        action = InventoryAction.PLACE_ALL;
                                                    } else if (toPlace < 0) {
                                                        action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE; // this happens with oversized stacks
                                                    } else if (toPlace != 0) {
                                                        action = InventoryAction.PLACE_SOME;
                                                    }
                                                } else if (cursor.getCount() <= slot.getMaxStackSize()) {
                                                    action = InventoryAction.SWAP_WITH_CURSOR;
                                                }
                                            } else if (ItemStack.isSameItemSameTags(cursor, clickedItem)) {
                                                if (clickedItem.getCount() >= 0) {
                                                    if (clickedItem.getCount() + cursor.getCount() <= cursor.getMaxStackSize()) {
                                                        // As of 1.5, this is result slots only
                                                        action = InventoryAction.PICKUP_ALL;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        // TODO check on updates
                        case QUICK_MOVE:
                            if (packetplayinwindowclick.getButtonNum() == 0) {
                                click = ClickType.SHIFT_LEFT;
                            } else if (packetplayinwindowclick.getButtonNum() == 1) {
                                click = ClickType.SHIFT_RIGHT;
                            }
                            if (packetplayinwindowclick.getButtonNum() == 0 || packetplayinwindowclick.getButtonNum() == 1) {
                                if (packetplayinwindowclick.getSlotNum() < 0) {
                                    action = InventoryAction.NOTHING;
                                } else {
                                    Slot slot = this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum());
                                    if (slot != null && slot.mayPickup(this.player) && slot.hasItem()) {
                                        action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                }
                            }
                            break;
                        case SWAP:
                            if ((packetplayinwindowclick.getButtonNum() >= 0 && packetplayinwindowclick.getButtonNum() < 9) || packetplayinwindowclick.getButtonNum() == 40) {
                                click = (packetplayinwindowclick.getButtonNum() == 40) ? ClickType.SWAP_OFFHAND : ClickType.NUMBER_KEY;
                                Slot clickedSlot = this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum());
                                if (clickedSlot.mayPickup(player)) {
                                    ItemStack hotbar = this.player.getInventory().getItem(packetplayinwindowclick.getButtonNum());
                                    boolean canCleanSwap = hotbar.isEmpty() || (clickedSlot.container == player.getInventory() && clickedSlot.mayPlace(hotbar)); // the slot will accept the hotbar item
                                    if (clickedSlot.hasItem()) {
                                        if (canCleanSwap) {
                                            action = InventoryAction.HOTBAR_SWAP;
                                        } else {
                                            action = InventoryAction.HOTBAR_MOVE_AND_READD;
                                        }
                                    } else if (!clickedSlot.hasItem() && !hotbar.isEmpty() && clickedSlot.mayPlace(hotbar)) {
                                        action = InventoryAction.HOTBAR_SWAP;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                } else {
                                    action = InventoryAction.NOTHING;
                                }
                            }
                            break;
                        case CLONE:
                            if (packetplayinwindowclick.getButtonNum() == 2) {
                                click = ClickType.MIDDLE;
                                if (packetplayinwindowclick.getSlotNum() < 0) {
                                    action = InventoryAction.NOTHING;
                                } else {
                                    Slot slot = this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum());
                                    if (slot != null && slot.hasItem() && player.getAbilities().instabuild && player.containerMenu.getCarried().isEmpty()) {
                                        action = InventoryAction.CLONE_STACK;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                }
                            } else {
                                click = ClickType.UNKNOWN;
                                action = InventoryAction.UNKNOWN;
                            }
                            break;
                        case THROW:
                            if (packetplayinwindowclick.getSlotNum() >= 0) {
                                if (packetplayinwindowclick.getButtonNum() == 0) {
                                    click = ClickType.DROP;
                                    Slot slot = this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum());
                                    if (slot != null && slot.hasItem() && slot.mayPickup(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.byBlock(Blocks.AIR)) {
                                        action = InventoryAction.DROP_ONE_SLOT;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                } else if (packetplayinwindowclick.getButtonNum() == 1) {
                                    click = ClickType.CONTROL_DROP;
                                    Slot slot = this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum());
                                    if (slot != null && slot.hasItem() && slot.mayPickup(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.byBlock(Blocks.AIR)) {
                                        action = InventoryAction.DROP_ALL_SLOT;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                }
                            } else {
                                // Sane default (because this happens when they are holding nothing. Don't ask why.)
                                click = ClickType.LEFT;
                                if (packetplayinwindowclick.getButtonNum() == 1) {
                                    click = ClickType.RIGHT;
                                }
                                action = InventoryAction.NOTHING;
                            }
                            break;
                        case QUICK_CRAFT:
                            this.player.containerMenu.clicked(packetplayinwindowclick.getSlotNum(), packetplayinwindowclick.getButtonNum(), packetplayinwindowclick.getClickType(), this.player);
                            break;
                        case PICKUP_ALL:
                            click = ClickType.DOUBLE_CLICK;
                            action = InventoryAction.NOTHING;
                            if (packetplayinwindowclick.getSlotNum() >= 0 && !this.player.containerMenu.getCarried().isEmpty()) {
                                ItemStack cursor = this.player.containerMenu.getCarried();
                                action = InventoryAction.NOTHING;
                                // Quick check for if we have any of the item
                                if (inventory.getTopInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem())) || inventory.getBottomInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem()))) {
                                    action = InventoryAction.COLLECT_TO_CURSOR;
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    if (packetplayinwindowclick.getClickType() != InventoryClickType.QUICK_CRAFT) {
                        if (click == ClickType.NUMBER_KEY) {
                            event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.getSlotNum(), click, action, packetplayinwindowclick.getButtonNum());
                        } else {
                            event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.getSlotNum(), click, action);
                        }

                        org.bukkit.inventory.Inventory top = inventory.getTopInventory();
                        if (packetplayinwindowclick.getSlotNum() == 0 && top instanceof CraftingInventory) {
                            org.bukkit.inventory.Recipe recipe = ((CraftingInventory) top).getRecipe();
                            if (recipe != null) {
                                if (click == ClickType.NUMBER_KEY) {
                                    event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.getSlotNum(), click, action, packetplayinwindowclick.getButtonNum());
                                } else {
                                    event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.getSlotNum(), click, action);
                                }
                            }
                        }

                        if (packetplayinwindowclick.getSlotNum() == 3 && top instanceof SmithingInventory) {
                            org.bukkit.inventory.ItemStack result = ((SmithingInventory) top).getResult();
                            if (result != null) {
                                if (click == ClickType.NUMBER_KEY) {
                                    event = new SmithItemEvent(inventory, type, packetplayinwindowclick.getSlotNum(), click, action, packetplayinwindowclick.getButtonNum());
                                } else {
                                    event = new SmithItemEvent(inventory, type, packetplayinwindowclick.getSlotNum(), click, action);
                                }
                            }
                        }

                        event.setCancelled(cancelled);
                        Container oldContainer = this.player.containerMenu; // SPIGOT-1224
                        cserver.getPluginManager().callEvent(event);
                        if (this.player.containerMenu != oldContainer) {
                            return;
                        }

                        switch (event.getResult()) {
                            case ALLOW:
                            case DEFAULT:
                                this.player.containerMenu.clicked(i, packetplayinwindowclick.getButtonNum(), packetplayinwindowclick.getClickType(), this.player);
                                break;
                            case DENY:
                                /* Needs enum constructor in InventoryAction
                                if (action.modifiesOtherSlots()) {

                                } else {
                                    if (action.modifiesCursor()) {
                                        this.player.playerConnection.sendPacket(new Packet103SetSlot(-1, -1, this.player.inventory.getCarried()));
                                    }
                                    if (action.modifiesClicked()) {
                                        this.player.playerConnection.sendPacket(new Packet103SetSlot(this.player.activeContainer.windowId, packet102windowclick.slot, this.player.activeContainer.getSlot(packet102windowclick.slot).getItem()));
                                    }
                                }*/
                                switch (action) {
                                    // Modified other slots
                                    case PICKUP_ALL:
                                    case MOVE_TO_OTHER_INVENTORY:
                                    case HOTBAR_MOVE_AND_READD:
                                    case HOTBAR_SWAP:
                                    case COLLECT_TO_CURSOR:
                                    case UNKNOWN:
                                        this.player.containerMenu.sendAllDataToRemote();
                                        break;
                                    // Modified cursor and clicked
                                    case PICKUP_SOME:
                                    case PICKUP_HALF:
                                    case PICKUP_ONE:
                                    case PLACE_ALL:
                                    case PLACE_SOME:
                                    case PLACE_ONE:
                                    case SWAP_WITH_CURSOR:
                                        this.player.connection.send(new PacketPlayOutSetSlot(-1, -1, this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getCarried()));
                                        this.player.connection.send(new PacketPlayOutSetSlot(this.player.containerMenu.containerId, this.player.inventoryMenu.incrementStateId(), packetplayinwindowclick.getSlotNum(), this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum()).getItem()));
                                        break;
                                    // Modified clicked only
                                    case DROP_ALL_SLOT:
                                    case DROP_ONE_SLOT:
                                        this.player.connection.send(new PacketPlayOutSetSlot(this.player.containerMenu.containerId, this.player.inventoryMenu.incrementStateId(), packetplayinwindowclick.getSlotNum(), this.player.containerMenu.getSlot(packetplayinwindowclick.getSlotNum()).getItem()));
                                        break;
                                    // Modified cursor only
                                    case DROP_ALL_CURSOR:
                                    case DROP_ONE_CURSOR:
                                    case CLONE_STACK:
                                        this.player.connection.send(new PacketPlayOutSetSlot(-1, -1, this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getCarried()));
                                        break;
                                    // Nothing
                                    case NOTHING:
                                        break;
                                }
                        }

                        if (event instanceof CraftItemEvent || event instanceof SmithItemEvent) {
                            // Need to update the inventory on crafting to
                            // correctly support custom recipes
                            player.containerMenu.sendAllDataToRemote();
                        }
                    }
                    // CraftBukkit end
                    ObjectIterator objectiterator = Int2ObjectMaps.fastIterable(packetplayinwindowclick.getChangedSlots()).iterator();

                    while (objectiterator.hasNext()) {
                        Entry<ItemStack> entry = (Entry) objectiterator.next();

                        this.player.containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), (ItemStack) entry.getValue());
                    }

                    this.player.containerMenu.setRemoteCarried(packetplayinwindowclick.getCarriedItem());
                    this.player.containerMenu.resumeRemoteUpdates();
                    if (flag) {
                        this.player.containerMenu.broadcastFullState();
                    } else {
                        this.player.containerMenu.broadcastChanges();
                    }

                }
            }
        }
    }

    @Override
    public void handlePlaceRecipe(PacketPlayInAutoRecipe packetplayinautorecipe) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinautorecipe, this, this.player.serverLevel());
        this.player.resetLastActionTime();
        if (!this.player.isSpectator() && this.player.containerMenu.containerId == packetplayinautorecipe.getContainerId() && this.player.containerMenu instanceof ContainerRecipeBook) {
            if (!this.player.containerMenu.stillValid(this.player)) {
                PlayerConnection.LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            } else {
                // CraftBukkit start - implement PlayerRecipeBookClickEvent
                org.bukkit.inventory.Recipe recipe = this.cserver.getRecipe(CraftNamespacedKey.fromMinecraft(packetplayinautorecipe.getRecipe()));
                if (recipe == null) {
                    return;
                }
                org.bukkit.event.player.PlayerRecipeBookClickEvent event = CraftEventFactory.callRecipeBookClickEvent(this.player, recipe, packetplayinautorecipe.isShiftDown());

                // Cast to keyed should be safe as the recipe will never be a MerchantRecipe.
                this.server.getRecipeManager().byKey(CraftNamespacedKey.toMinecraft(((org.bukkit.Keyed) event.getRecipe()).getKey())).ifPresent((recipeholder) -> {
                    ((ContainerRecipeBook) this.player.containerMenu).handlePlacement(event.isShiftClick(), recipeholder, this.player);
                });
                // CraftBukkit end
            }
        }
    }

    @Override
    public void handleContainerButtonClick(PacketPlayInEnchantItem packetplayinenchantitem) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinenchantitem, this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId == packetplayinenchantitem.getContainerId() && !this.player.isSpectator()) {
            if (!this.player.containerMenu.stillValid(this.player)) {
                PlayerConnection.LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            } else {
                boolean flag = this.player.containerMenu.clickMenuButton(this.player, packetplayinenchantitem.getButtonId());

                if (flag) {
                    this.player.containerMenu.broadcastChanges();
                }

            }
        }
    }

    @Override
    public void handleSetCreativeModeSlot(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinsetcreativeslot, this, this.player.serverLevel());
        if (this.player.gameMode.isCreative()) {
            boolean flag = packetplayinsetcreativeslot.getSlotNum() < 0;
            ItemStack itemstack = packetplayinsetcreativeslot.getItem();

            if (!itemstack.isItemEnabled(this.player.level().enabledFeatures())) {
                return;
            }

            NBTTagCompound nbttagcompound = ItemBlock.getBlockEntityData(itemstack);

            if (!itemstack.isEmpty() && nbttagcompound != null && nbttagcompound.contains("x") && nbttagcompound.contains("y") && nbttagcompound.contains("z")) {
                BlockPosition blockposition = TileEntity.getPosFromTag(nbttagcompound);

                if (this.player.level().isLoaded(blockposition)) {
                    TileEntity tileentity = this.player.level().getBlockEntity(blockposition);

                    if (tileentity != null) {
                        tileentity.saveToItem(itemstack);
                    }
                }
            }

            boolean flag1 = packetplayinsetcreativeslot.getSlotNum() >= 1 && packetplayinsetcreativeslot.getSlotNum() <= 45;
            boolean flag2 = itemstack.isEmpty() || itemstack.getDamageValue() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();
            if (flag || (flag1 && !ItemStack.matches(this.player.inventoryMenu.getSlot(packetplayinsetcreativeslot.getSlotNum()).getItem(), packetplayinsetcreativeslot.getItem()))) { // Insist on valid slot
                // CraftBukkit start - Call click event
                InventoryView inventory = this.player.inventoryMenu.getBukkitView();
                org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(packetplayinsetcreativeslot.getItem());

                SlotType type = SlotType.QUICKBAR;
                if (flag) {
                    type = SlotType.OUTSIDE;
                } else if (packetplayinsetcreativeslot.getSlotNum() < 36) {
                    if (packetplayinsetcreativeslot.getSlotNum() >= 5 && packetplayinsetcreativeslot.getSlotNum() < 9) {
                        type = SlotType.ARMOR;
                    } else {
                        type = SlotType.CONTAINER;
                    }
                }
                InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type, flag ? -999 : packetplayinsetcreativeslot.getSlotNum(), item);
                cserver.getPluginManager().callEvent(event);

                itemstack = CraftItemStack.asNMSCopy(event.getCursor());

                switch (event.getResult()) {
                case ALLOW:
                    // Plugin cleared the id / stacksize checks
                    flag2 = true;
                    break;
                case DEFAULT:
                    break;
                case DENY:
                    // Reset the slot
                    if (packetplayinsetcreativeslot.getSlotNum() >= 0) {
                        this.player.connection.send(new PacketPlayOutSetSlot(this.player.inventoryMenu.containerId, this.player.inventoryMenu.incrementStateId(), packetplayinsetcreativeslot.getSlotNum(), this.player.inventoryMenu.getSlot(packetplayinsetcreativeslot.getSlotNum()).getItem()));
                        this.player.connection.send(new PacketPlayOutSetSlot(-1, this.player.inventoryMenu.incrementStateId(), -1, ItemStack.EMPTY));
                    }
                    return;
                }
            }
            // CraftBukkit end

            if (flag1 && flag2) {
                this.player.inventoryMenu.getSlot(packetplayinsetcreativeslot.getSlotNum()).setByPlayer(itemstack);
                this.player.inventoryMenu.broadcastChanges();
            } else if (flag && flag2 && this.dropSpamTickCount < 200) {
                this.dropSpamTickCount += 20;
                this.player.drop(itemstack, true);
            }
        }

    }

    @Override
    public void handleSignUpdate(PacketPlayInUpdateSign packetplayinupdatesign) {
        List<String> list = (List) Stream.of(packetplayinupdatesign.getLines()).map(EnumChatFormat::stripFormatting).collect(Collectors.toList());

        this.filterTextPacket(list).thenAcceptAsync((list1) -> {
            this.updateSignText(packetplayinupdatesign, list1);
        }, this.server);
    }

    private void updateSignText(PacketPlayInUpdateSign packetplayinupdatesign, List<FilteredText> list) {
        if (this.player.isImmobile()) return; // CraftBukkit
        this.player.resetLastActionTime();
        WorldServer worldserver = this.player.serverLevel();
        BlockPosition blockposition = packetplayinupdatesign.getPos();

        if (worldserver.hasChunkAt(blockposition)) {
            TileEntity tileentity = worldserver.getBlockEntity(blockposition);

            if (!(tileentity instanceof TileEntitySign)) {
                return;
            }

            TileEntitySign tileentitysign = (TileEntitySign) tileentity;

            tileentitysign.updateSignText(this.player, packetplayinupdatesign.isFrontText(), list);
        }

    }

    @Override
    public void handlePlayerAbilities(PacketPlayInAbilities packetplayinabilities) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayinabilities, this, this.player.serverLevel());
        // CraftBukkit start
        if (this.player.getAbilities().mayfly && this.player.getAbilities().flying != packetplayinabilities.isFlying()) {
            PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(this.player.getBukkitEntity(), packetplayinabilities.isFlying());
            this.cserver.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.player.getAbilities().flying = packetplayinabilities.isFlying(); // Actually set the player's flying status
            } else {
                this.player.onUpdateAbilities(); // Tell the player their ability was reverted
            }
        }
        // CraftBukkit end
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket serverboundclientinformationpacket) {
        PlayerConnectionUtils.ensureRunningOnSameThread(serverboundclientinformationpacket, this, this.player.serverLevel());
        this.player.updateOptions(serverboundclientinformationpacket.information());
    }

    @Override
    public void handleChangeDifficulty(PacketPlayInDifficultyChange packetplayindifficultychange) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayindifficultychange, this, this.player.serverLevel());
        if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
            this.server.setDifficulty(packetplayindifficultychange.getDifficulty(), false);
        }
    }

    @Override
    public void handleLockDifficulty(PacketPlayInDifficultyLock packetplayindifficultylock) {
        PlayerConnectionUtils.ensureRunningOnSameThread(packetplayindifficultylock, this, this.player.serverLevel());
        if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
            this.server.setDifficultyLocked(packetplayindifficultylock.isLocked());
        }
    }

    @Override
    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundchatsessionupdatepacket) {
        PlayerConnectionUtils.ensureRunningOnSameThread(serverboundchatsessionupdatepacket, this, this.player.serverLevel());
        RemoteChatSession.a remotechatsession_a = serverboundchatsessionupdatepacket.chatSession();
        ProfilePublicKey.a profilepublickey_a = this.chatSession != null ? this.chatSession.profilePublicKey().data() : null;
        ProfilePublicKey.a profilepublickey_a1 = remotechatsession_a.profilePublicKey();

        if (!Objects.equals(profilepublickey_a, profilepublickey_a1)) {
            if (profilepublickey_a != null && profilepublickey_a1.expiresAt().isBefore(profilepublickey_a.expiresAt())) {
                this.disconnect(ProfilePublicKey.EXPIRED_PROFILE_PUBLIC_KEY);
            } else {
                try {
                    SignatureValidator signaturevalidator = this.server.getProfileKeySignatureValidator();

                    if (signaturevalidator == null) {
                        PlayerConnection.LOGGER.warn("Ignoring chat session from {} due to missing Services public key", this.player.getGameProfile().getName());
                        return;
                    }

                    this.resetPlayerChatState(remotechatsession_a.validate(this.player.getGameProfile(), signaturevalidator));
                } catch (ProfilePublicKey.b profilepublickey_b) {
                    PlayerConnection.LOGGER.error("Failed to validate profile key: {}", profilepublickey_b.getMessage());
                    this.disconnect(profilepublickey_b.getComponent());
                }

            }
        }
    }

    @Override
    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket serverboundconfigurationacknowledgedpacket) {
        if (!this.waitingForSwitchToConfig) {
            throw new IllegalStateException("Client acknowledged config, but none was requested");
        } else {
            this.connection.setListener(new ServerConfigurationPacketListenerImpl(this.server, this.connection, this.createCookie(this.player.clientInformation()), this.player)); // CraftBukkit
        }
    }

    @Override
    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket serverboundchunkbatchreceivedpacket) {
        PlayerConnectionUtils.ensureRunningOnSameThread(serverboundchunkbatchreceivedpacket, this, this.player.serverLevel());
        this.chunkSender.onChunkBatchReceivedByClient(serverboundchunkbatchreceivedpacket.desiredChunksPerTick());
    }

    private void resetPlayerChatState(RemoteChatSession remotechatsession) {
        this.chatSession = remotechatsession;
        this.signedMessageDecoder = remotechatsession.createMessageDecoder(this.player.getUUID());
        this.chatMessageChain.append((executor) -> {
            this.player.setChatSession(remotechatsession);
            this.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.a.INITIALIZE_CHAT), List.of(this.player)));
            return CompletableFuture.completedFuture((Object) null);
        });
    }

    @Override
    public EntityPlayer getPlayer() {
        return this.player;
    }

    @FunctionalInterface
    private interface a {

        EnumInteractionResult run(EntityPlayer entityplayer, Entity entity, EnumHand enumhand);
    }
}
