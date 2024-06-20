package net.minecraft.server.level;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.EnumChatFormat;
import net.minecraft.ReportedException;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutCamera;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutExperience;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import net.minecraft.network.protocol.game.PacketPlayOutLookAt;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.network.protocol.game.PacketPlayOutOpenBook;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindowHorse;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindowMerchant;
import net.minecraft.network.protocol.game.PacketPlayOutRemoveEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutRespawn;
import net.minecraft.network.protocol.game.PacketPlayOutServerDifficulty;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateHealth;
import net.minecraft.network.protocol.game.PacketPlayOutWindowData;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.AdvancementDataPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ITextFilter;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.stats.ServerStatisticManager;
import net.minecraft.stats.Statistic;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.EnumHand;
import net.minecraft.world.IInventory;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.IEntityAngerable;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.animal.EntityPig;
import net.minecraft.world.entity.animal.horse.EntityHorseAbstract;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.monster.EntityStrider;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerHorse;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.ICrafting;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SlotResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldown;
import net.minecraft.world.item.ItemCooldownPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemWorldMapBase;
import net.minecraft.world.item.ItemWrittenBook;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.trading.MerchantRecipeList;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.BlockFacingHorizontal;
import net.minecraft.world.level.block.BlockRespawnAnchor;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityCommand;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import org.slf4j.Logger;

public class EntityPlayer extends EntityHuman {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
    private static final int FLY_STAT_RECORDING_SPEED = 25;
    public static final double INTERACTION_DISTANCE_VERIFICATION_BUFFER = 1.0D;
    private static final AttributeModifier CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER = new AttributeModifier(MinecraftKey.withDefaultNamespace("creative_mode_block_range"), 0.5D, AttributeModifier.Operation.ADD_VALUE);
    private static final AttributeModifier CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER = new AttributeModifier(MinecraftKey.withDefaultNamespace("creative_mode_entity_range"), 2.0D, AttributeModifier.Operation.ADD_VALUE);
    public PlayerConnection connection;
    public final MinecraftServer server;
    public final PlayerInteractManager gameMode;
    private final AdvancementDataPlayer advancements;
    private final ServerStatisticManager stats;
    private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
    private int lastRecordedFoodLevel = Integer.MIN_VALUE;
    private int lastRecordedAirLevel = Integer.MIN_VALUE;
    private int lastRecordedArmor = Integer.MIN_VALUE;
    private int lastRecordedLevel = Integer.MIN_VALUE;
    private int lastRecordedExperience = Integer.MIN_VALUE;
    private float lastSentHealth = -1.0E8F;
    private int lastSentFood = -99999999;
    private boolean lastFoodSaturationZero = true;
    public int lastSentExp = -99999999;
    public int spawnInvulnerableTime = 60;
    private EnumChatVisibility chatVisibility;
    private boolean canChatColor;
    private long lastActionTime;
    @Nullable
    private Entity camera;
    public boolean isChangingDimension;
    public boolean seenCredits;
    private final RecipeBookServer recipeBook;
    @Nullable
    private Vec3D levitationStartPos;
    private int levitationStartTime;
    private boolean disconnected;
    private int requestedViewDistance;
    public String language;
    @Nullable
    private Vec3D startingToFallPosition;
    @Nullable
    private Vec3D enteredNetherPosition;
    @Nullable
    private Vec3D enteredLavaOnVehiclePosition;
    private SectionPosition lastSectionPos;
    private ChunkTrackingView chunkTrackingView;
    private ResourceKey<World> respawnDimension;
    @Nullable
    private BlockPosition respawnPosition;
    private boolean respawnForced;
    private float respawnAngle;
    private final ITextFilter textFilter;
    private boolean textFilteringEnabled;
    private boolean allowsListing;
    private boolean spawnExtraParticlesOnFall;
    private WardenSpawnTracker wardenSpawnTracker;
    @Nullable
    private BlockPosition raidOmenPosition;
    private Vec3D lastKnownClientMovement;
    private final ContainerSynchronizer containerSynchronizer;
    private final ICrafting containerListener;
    @Nullable
    private RemoteChatSession chatSession;
    @Nullable
    public final Object object;
    private int containerCounter;
    public boolean wonGame;

    public EntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, ClientInformation clientinformation) {
        super(worldserver, worldserver.getSharedSpawnPos(), worldserver.getSharedSpawnAngle(), gameprofile);
        this.chatVisibility = EnumChatVisibility.FULL;
        this.canChatColor = true;
        this.lastActionTime = SystemUtils.getMillis();
        this.recipeBook = new RecipeBookServer();
        this.requestedViewDistance = 2;
        this.language = "en_us";
        this.lastSectionPos = SectionPosition.of(0, 0, 0);
        this.chunkTrackingView = ChunkTrackingView.EMPTY;
        this.respawnDimension = World.OVERWORLD;
        this.wardenSpawnTracker = new WardenSpawnTracker(0, 0, 0);
        this.lastKnownClientMovement = Vec3D.ZERO;
        this.containerSynchronizer = new ContainerSynchronizer() {
            @Override
            public void sendInitialData(Container container, NonNullList<ItemStack> nonnulllist, ItemStack itemstack, int[] aint) {
                EntityPlayer.this.connection.send(new PacketPlayOutWindowItems(container.containerId, container.incrementStateId(), nonnulllist, itemstack));

                for (int i = 0; i < aint.length; ++i) {
                    this.broadcastDataValue(container, i, aint[i]);
                }

            }

            @Override
            public void sendSlotChange(Container container, int i, ItemStack itemstack) {
                EntityPlayer.this.connection.send(new PacketPlayOutSetSlot(container.containerId, container.incrementStateId(), i, itemstack));
            }

            @Override
            public void sendCarriedChange(Container container, ItemStack itemstack) {
                EntityPlayer.this.connection.send(new PacketPlayOutSetSlot(-1, container.incrementStateId(), -1, itemstack));
            }

            @Override
            public void sendDataChange(Container container, int i, int j) {
                this.broadcastDataValue(container, i, j);
            }

            private void broadcastDataValue(Container container, int i, int j) {
                EntityPlayer.this.connection.send(new PacketPlayOutWindowData(container.containerId, i, j));
            }
        };
        this.containerListener = new ICrafting() {
            @Override
            public void slotChanged(Container container, int i, ItemStack itemstack) {
                Slot slot = container.getSlot(i);

                if (!(slot instanceof SlotResult)) {
                    if (slot.container == EntityPlayer.this.getInventory()) {
                        CriterionTriggers.INVENTORY_CHANGED.trigger(EntityPlayer.this, EntityPlayer.this.getInventory(), itemstack);
                    }

                }
            }

            @Override
            public void dataChanged(Container container, int i, int j) {}
        };
        this.textFilter = minecraftserver.createTextFilterForPlayer(this);
        this.gameMode = minecraftserver.createGameModeForPlayer(this);
        this.server = minecraftserver;
        this.stats = minecraftserver.getPlayerList().getPlayerStats(this);
        this.advancements = minecraftserver.getPlayerList().getPlayerAdvancements(this);
        this.moveTo(this.adjustSpawnLocation(worldserver, worldserver.getSharedSpawnPos()).getBottomCenter(), 0.0F, 0.0F);
        this.updateOptions(clientinformation);
        this.object = null;
    }

    @Override
    public BlockPosition adjustSpawnLocation(WorldServer worldserver, BlockPosition blockposition) {
        AxisAlignedBB axisalignedbb = this.getDimensions(EntityPose.STANDING).makeBoundingBox(Vec3D.ZERO);
        BlockPosition blockposition1 = blockposition;

        if (worldserver.dimensionType().hasSkyLight() && worldserver.getServer().getWorldData().getGameType() != EnumGamemode.ADVENTURE) {
            int i = Math.max(0, this.server.getSpawnRadius(worldserver));
            int j = MathHelper.floor(worldserver.getWorldBorder().getDistanceToBorder((double) blockposition.getX(), (double) blockposition.getZ()));

            if (j < i) {
                i = j;
            }

            if (j <= 1) {
                i = 1;
            }

            long k = (long) (i * 2 + 1);
            long l = k * k;
            int i1 = l > 2147483647L ? Integer.MAX_VALUE : (int) l;
            int j1 = this.getCoprime(i1);
            int k1 = RandomSource.create().nextInt(i1);

            for (int l1 = 0; l1 < i1; ++l1) {
                int i2 = (k1 + j1 * l1) % i1;
                int j2 = i2 % (i * 2 + 1);
                int k2 = i2 / (i * 2 + 1);

                blockposition1 = WorldProviderNormal.getOverworldRespawnPos(worldserver, blockposition.getX() + j2 - i, blockposition.getZ() + k2 - i);
                if (blockposition1 != null && worldserver.noCollision(this, axisalignedbb.move(blockposition1.getBottomCenter()))) {
                    return blockposition1;
                }
            }

            blockposition1 = blockposition;
        }

        while (!worldserver.noCollision(this, axisalignedbb.move(blockposition1.getBottomCenter())) && blockposition1.getY() < worldserver.getMaxBuildHeight() - 1) {
            blockposition1 = blockposition1.above();
        }

        while (worldserver.noCollision(this, axisalignedbb.move(blockposition1.below().getBottomCenter())) && blockposition1.getY() > worldserver.getMinBuildHeight() + 1) {
            blockposition1 = blockposition1.below();
        }

        return blockposition1;
    }

    private int getCoprime(int i) {
        return i <= 16 ? i - 1 : 17;
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        DataResult dataresult;
        Logger logger;

        if (nbttagcompound.contains("warden_spawn_tracker", 10)) {
            dataresult = WardenSpawnTracker.CODEC.parse(new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound.get("warden_spawn_tracker")));
            logger = EntityPlayer.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((wardenspawntracker) -> {
                this.wardenSpawnTracker = wardenspawntracker;
            });
        }

        if (nbttagcompound.contains("enteredNetherPosition", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("enteredNetherPosition");

            this.enteredNetherPosition = new Vec3D(nbttagcompound1.getDouble("x"), nbttagcompound1.getDouble("y"), nbttagcompound1.getDouble("z"));
        }

        this.seenCredits = nbttagcompound.getBoolean("seenCredits");
        if (nbttagcompound.contains("recipeBook", 10)) {
            this.recipeBook.fromNbt(nbttagcompound.getCompound("recipeBook"), this.server.getRecipeManager());
        }

        if (this.isSleeping()) {
            this.stopSleeping();
        }

        if (nbttagcompound.contains("SpawnX", 99) && nbttagcompound.contains("SpawnY", 99) && nbttagcompound.contains("SpawnZ", 99)) {
            this.respawnPosition = new BlockPosition(nbttagcompound.getInt("SpawnX"), nbttagcompound.getInt("SpawnY"), nbttagcompound.getInt("SpawnZ"));
            this.respawnForced = nbttagcompound.getBoolean("SpawnForced");
            this.respawnAngle = nbttagcompound.getFloat("SpawnAngle");
            if (nbttagcompound.contains("SpawnDimension")) {
                DataResult dataresult1 = World.RESOURCE_KEY_CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound.get("SpawnDimension"));
                Logger logger1 = EntityPlayer.LOGGER;

                Objects.requireNonNull(logger1);
                this.respawnDimension = (ResourceKey) dataresult1.resultOrPartial(logger1::error).orElse(World.OVERWORLD);
            }
        }

        this.spawnExtraParticlesOnFall = nbttagcompound.getBoolean("spawn_extra_particles_on_fall");
        NBTBase nbtbase = nbttagcompound.get("raid_omen_position");

        if (nbtbase != null) {
            dataresult = BlockPosition.CODEC.parse(DynamicOpsNBT.INSTANCE, nbtbase);
            logger = EntityPlayer.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((blockposition) -> {
                this.raidOmenPosition = blockposition;
            });
        }

    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        DataResult dataresult = WardenSpawnTracker.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.wardenSpawnTracker);
        Logger logger = EntityPlayer.LOGGER;

        Objects.requireNonNull(logger);
        dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
            nbttagcompound.put("warden_spawn_tracker", nbtbase);
        });
        this.storeGameTypes(nbttagcompound);
        nbttagcompound.putBoolean("seenCredits", this.seenCredits);
        if (this.enteredNetherPosition != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            nbttagcompound1.putDouble("x", this.enteredNetherPosition.x);
            nbttagcompound1.putDouble("y", this.enteredNetherPosition.y);
            nbttagcompound1.putDouble("z", this.enteredNetherPosition.z);
            nbttagcompound.put("enteredNetherPosition", nbttagcompound1);
        }

        Entity entity = this.getRootVehicle();
        Entity entity1 = this.getVehicle();

        if (entity1 != null && entity != this && entity.hasExactlyOnePlayerPassenger()) {
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();

            entity.save(nbttagcompound3);
            nbttagcompound2.putUUID("Attach", entity1.getUUID());
            nbttagcompound2.put("Entity", nbttagcompound3);
            nbttagcompound.put("RootVehicle", nbttagcompound2);
        }

        nbttagcompound.put("recipeBook", this.recipeBook.toNbt());
        nbttagcompound.putString("Dimension", this.level().dimension().location().toString());
        if (this.respawnPosition != null) {
            nbttagcompound.putInt("SpawnX", this.respawnPosition.getX());
            nbttagcompound.putInt("SpawnY", this.respawnPosition.getY());
            nbttagcompound.putInt("SpawnZ", this.respawnPosition.getZ());
            nbttagcompound.putBoolean("SpawnForced", this.respawnForced);
            nbttagcompound.putFloat("SpawnAngle", this.respawnAngle);
            dataresult = MinecraftKey.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.respawnDimension.location());
            logger = EntityPlayer.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
                nbttagcompound.put("SpawnDimension", nbtbase);
            });
        }

        nbttagcompound.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
        if (this.raidOmenPosition != null) {
            dataresult = BlockPosition.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this.raidOmenPosition);
            logger = EntityPlayer.LOGGER;
            Objects.requireNonNull(logger);
            dataresult.resultOrPartial(logger::error).ifPresent((nbtbase) -> {
                nbttagcompound.put("raid_omen_position", nbtbase);
            });
        }

    }

    public void setExperiencePoints(int i) {
        float f = (float) this.getXpNeededForNextLevel();
        float f1 = (f - 1.0F) / f;

        this.experienceProgress = MathHelper.clamp((float) i / f, 0.0F, f1);
        this.lastSentExp = -1;
    }

    public void setExperienceLevels(int i) {
        this.experienceLevel = i;
        this.lastSentExp = -1;
    }

    @Override
    public void giveExperienceLevels(int i) {
        super.giveExperienceLevels(i);
        this.lastSentExp = -1;
    }

    @Override
    public void onEnchantmentPerformed(ItemStack itemstack, int i) {
        super.onEnchantmentPerformed(itemstack, i);
        this.lastSentExp = -1;
    }

    public void initMenu(Container container) {
        container.addSlotListener(this.containerListener);
        container.setSynchronizer(this.containerSynchronizer);
    }

    public void initInventoryMenu() {
        this.initMenu(this.inventoryMenu);
    }

    @Override
    public void onEnterCombat() {
        super.onEnterCombat();
        this.connection.send(ClientboundPlayerCombatEnterPacket.INSTANCE);
    }

    @Override
    public void onLeaveCombat() {
        super.onLeaveCombat();
        this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
    }

    @Override
    public void onInsideBlock(IBlockData iblockdata) {
        CriterionTriggers.ENTER_BLOCK.trigger(this, iblockdata);
    }

    @Override
    protected ItemCooldown createItemCooldowns() {
        return new ItemCooldownPlayer(this);
    }

    @Override
    public void tick() {
        this.gameMode.tick();
        this.wardenSpawnTracker.tick();
        --this.spawnInvulnerableTime;
        if (this.invulnerableTime > 0) {
            --this.invulnerableTime;
        }

        this.containerMenu.broadcastChanges();
        if (!this.level().isClientSide && !this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }

        Entity entity = this.getCamera();

        if (entity != this) {
            if (entity.isAlive()) {
                this.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                this.serverLevel().getChunkSource().move(this);
                if (this.wantsToStopRiding()) {
                    this.setCamera(this);
                }
            } else {
                this.setCamera(this);
            }
        }

        CriterionTriggers.TICK.trigger(this);
        if (this.levitationStartPos != null) {
            CriterionTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
        }

        this.trackStartFallingPosition();
        this.trackEnteredOrExitedLavaOnVehicle();
        this.updatePlayerAttributes();
        this.advancements.flushDirty(this);
    }

    private void updatePlayerAttributes() {
        AttributeModifiable attributemodifiable = this.getAttribute(GenericAttributes.BLOCK_INTERACTION_RANGE);

        if (attributemodifiable != null) {
            if (this.isCreative()) {
                attributemodifiable.addOrUpdateTransientModifier(EntityPlayer.CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            } else {
                attributemodifiable.removeModifier(EntityPlayer.CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            }
        }

        AttributeModifiable attributemodifiable1 = this.getAttribute(GenericAttributes.ENTITY_INTERACTION_RANGE);

        if (attributemodifiable1 != null) {
            if (this.isCreative()) {
                attributemodifiable1.addOrUpdateTransientModifier(EntityPlayer.CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            } else {
                attributemodifiable1.removeModifier(EntityPlayer.CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            }
        }

    }

    public void doTick() {
        try {
            if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
                super.tick();
            }

            for (int i = 0; i < this.getInventory().getContainerSize(); ++i) {
                ItemStack itemstack = this.getInventory().getItem(i);

                if (itemstack.getItem().isComplex()) {
                    Packet<?> packet = ((ItemWorldMapBase) itemstack.getItem()).getUpdatePacket(itemstack, this.level(), this);

                    if (packet != null) {
                        this.connection.send(packet);
                    }
                }
            }

            if (this.getHealth() != this.lastSentHealth || this.lastSentFood != this.foodData.getFoodLevel() || this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
                this.connection.send(new PacketPlayOutUpdateHealth(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
                this.lastSentHealth = this.getHealth();
                this.lastSentFood = this.foodData.getFoodLevel();
                this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
                this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
                this.updateScoreForCriteria(IScoreboardCriteria.HEALTH, MathHelper.ceil(this.lastRecordedHealthAndAbsorption));
            }

            if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
                this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
                this.updateScoreForCriteria(IScoreboardCriteria.FOOD, MathHelper.ceil((float) this.lastRecordedFoodLevel));
            }

            if (this.getAirSupply() != this.lastRecordedAirLevel) {
                this.lastRecordedAirLevel = this.getAirSupply();
                this.updateScoreForCriteria(IScoreboardCriteria.AIR, MathHelper.ceil((float) this.lastRecordedAirLevel));
            }

            if (this.getArmorValue() != this.lastRecordedArmor) {
                this.lastRecordedArmor = this.getArmorValue();
                this.updateScoreForCriteria(IScoreboardCriteria.ARMOR, MathHelper.ceil((float) this.lastRecordedArmor));
            }

            if (this.totalExperience != this.lastRecordedExperience) {
                this.lastRecordedExperience = this.totalExperience;
                this.updateScoreForCriteria(IScoreboardCriteria.EXPERIENCE, MathHelper.ceil((float) this.lastRecordedExperience));
            }

            if (this.experienceLevel != this.lastRecordedLevel) {
                this.lastRecordedLevel = this.experienceLevel;
                this.updateScoreForCriteria(IScoreboardCriteria.LEVEL, MathHelper.ceil((float) this.lastRecordedLevel));
            }

            if (this.totalExperience != this.lastSentExp) {
                this.lastSentExp = this.totalExperience;
                this.connection.send(new PacketPlayOutExperience(this.experienceProgress, this.totalExperience, this.experienceLevel));
            }

            if (this.tickCount % 20 == 0) {
                CriterionTriggers.LOCATION.trigger(this);
            }

        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking player");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Player being ticked");

            this.fillCrashReportCategory(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    @Override
    public void resetFallDistance() {
        if (this.getHealth() > 0.0F && this.startingToFallPosition != null) {
            CriterionTriggers.FALL_FROM_HEIGHT.trigger(this, this.startingToFallPosition);
        }

        this.startingToFallPosition = null;
        super.resetFallDistance();
    }

    public void trackStartFallingPosition() {
        if (this.fallDistance > 0.0F && this.startingToFallPosition == null) {
            this.startingToFallPosition = this.position();
            if (this.currentImpulseImpactPos != null && this.currentImpulseImpactPos.y <= this.startingToFallPosition.y) {
                CriterionTriggers.FALL_AFTER_EXPLOSION.trigger(this, this.currentImpulseImpactPos, this.currentExplosionCause);
            }
        }

    }

    public void trackEnteredOrExitedLavaOnVehicle() {
        if (this.getVehicle() != null && this.getVehicle().isInLava()) {
            if (this.enteredLavaOnVehiclePosition == null) {
                this.enteredLavaOnVehiclePosition = this.position();
            } else {
                CriterionTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.trigger(this, this.enteredLavaOnVehiclePosition);
            }
        }

        if (this.enteredLavaOnVehiclePosition != null && (this.getVehicle() == null || !this.getVehicle().isInLava())) {
            this.enteredLavaOnVehiclePosition = null;
        }

    }

    private void updateScoreForCriteria(IScoreboardCriteria iscoreboardcriteria, int i) {
        this.getScoreboard().forAllObjectives(iscoreboardcriteria, this, (scoreaccess) -> {
            scoreaccess.set(i);
        });
    }

    @Override
    public void die(DamageSource damagesource) {
        this.gameEvent(GameEvent.ENTITY_DIE);
        boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);

        if (flag) {
            IChatBaseComponent ichatbasecomponent = this.getCombatTracker().getDeathMessage();

            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), ichatbasecomponent), PacketSendListener.exceptionallySend(() -> {
                boolean flag1 = true;
                String s = ichatbasecomponent.getString(256);
                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable("death.attack.message_too_long", IChatBaseComponent.literal(s).withStyle(EnumChatFormat.YELLOW));
                IChatMutableComponent ichatmutablecomponent1 = IChatBaseComponent.translatable("death.attack.even_more_magic", this.getDisplayName()).withStyle((chatmodifier) -> {
                    return chatmodifier.withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, ichatmutablecomponent));
                });

                return new ClientboundPlayerCombatKillPacket(this.getId(), ichatmutablecomponent1);
            }));
            ScoreboardTeam scoreboardteam = this.getTeam();

            if (scoreboardteam != null && scoreboardteam.getDeathMessageVisibility() != ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS) {
                if (scoreboardteam.getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().broadcastSystemToTeam(this, ichatbasecomponent);
                } else if (scoreboardteam.getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, ichatbasecomponent);
                }
            } else {
                this.server.getPlayerList().broadcastSystemMessage(ichatbasecomponent, false);
            }
        } else {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
        }

        this.removeEntitiesOnShoulder();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        if (!this.isSpectator()) {
            this.dropAllDeathLoot(this.serverLevel(), damagesource);
        }

        this.getScoreboard().forAllObjectives(IScoreboardCriteria.DEATH_COUNT, this, ScoreAccess::increment);
        EntityLiving entityliving = this.getKillCredit();

        if (entityliving != null) {
            this.awardStat(StatisticList.ENTITY_KILLED_BY.get(entityliving.getType()));
            entityliving.awardKillScore(this, this.deathScore, damagesource);
            this.createWitherRose(entityliving);
        }

        this.level().broadcastEntityEvent(this, (byte) 3);
        this.awardStat(StatisticList.DEATHS);
        this.resetStat(StatisticList.CUSTOM.get(StatisticList.TIME_SINCE_DEATH));
        this.resetStat(StatisticList.CUSTOM.get(StatisticList.TIME_SINCE_REST));
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
        this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
    }

    private void tellNeutralMobsThatIDied() {
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(this.blockPosition())).inflate(32.0D, 10.0D, 32.0D);

        this.level().getEntitiesOfClass(EntityInsentient.class, axisalignedbb, IEntitySelector.NO_SPECTATORS).stream().filter((entityinsentient) -> {
            return entityinsentient instanceof IEntityAngerable;
        }).forEach((entityinsentient) -> {
            ((IEntityAngerable) entityinsentient).playerDied(this);
        });
    }

    @Override
    public void awardKillScore(Entity entity, int i, DamageSource damagesource) {
        if (entity != this) {
            super.awardKillScore(entity, i, damagesource);
            this.increaseScore(i);
            this.getScoreboard().forAllObjectives(IScoreboardCriteria.KILL_COUNT_ALL, this, ScoreAccess::increment);
            if (entity instanceof EntityHuman) {
                this.awardStat(StatisticList.PLAYER_KILLS);
                this.getScoreboard().forAllObjectives(IScoreboardCriteria.KILL_COUNT_PLAYERS, this, ScoreAccess::increment);
            } else {
                this.awardStat(StatisticList.MOB_KILLS);
            }

            this.handleTeamKill(this, entity, IScoreboardCriteria.TEAM_KILL);
            this.handleTeamKill(entity, this, IScoreboardCriteria.KILLED_BY_TEAM);
            CriterionTriggers.PLAYER_KILLED_ENTITY.trigger(this, entity, damagesource);
        }
    }

    private void handleTeamKill(ScoreHolder scoreholder, ScoreHolder scoreholder1, IScoreboardCriteria[] aiscoreboardcriteria) {
        ScoreboardTeam scoreboardteam = this.getScoreboard().getPlayersTeam(scoreholder1.getScoreboardName());

        if (scoreboardteam != null) {
            int i = scoreboardteam.getColor().getId();

            if (i >= 0 && i < aiscoreboardcriteria.length) {
                this.getScoreboard().forAllObjectives(aiscoreboardcriteria[i], scoreholder, ScoreAccess::increment);
            }
        }

    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        if (this.isInvulnerableTo(damagesource)) {
            return false;
        } else {
            boolean flag = this.server.isDedicatedServer() && this.isPvpAllowed() && damagesource.is(DamageTypeTags.IS_FALL);

            if (!flag && this.spawnInvulnerableTime > 0 && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return false;
            } else {
                Entity entity = damagesource.getEntity();

                if (entity instanceof EntityHuman) {
                    EntityHuman entityhuman = (EntityHuman) entity;

                    if (!this.canHarmPlayer(entityhuman)) {
                        return false;
                    }
                }

                if (entity instanceof EntityArrow) {
                    EntityArrow entityarrow = (EntityArrow) entity;
                    Entity entity1 = entityarrow.getOwner();

                    if (entity1 instanceof EntityHuman) {
                        EntityHuman entityhuman1 = (EntityHuman) entity1;

                        if (!this.canHarmPlayer(entityhuman1)) {
                            return false;
                        }
                    }
                }

                return super.hurt(damagesource, f);
            }
        }
    }

    @Override
    public boolean canHarmPlayer(EntityHuman entityhuman) {
        return !this.isPvpAllowed() ? false : super.canHarmPlayer(entityhuman);
    }

    private boolean isPvpAllowed() {
        return this.server.isPvpAllowed();
    }

    public DimensionTransition findRespawnPositionAndUseSpawnBlock(boolean flag, DimensionTransition.a dimensiontransition_a) {
        BlockPosition blockposition = this.getRespawnPosition();
        float f = this.getRespawnAngle();
        boolean flag1 = this.isRespawnForced();
        WorldServer worldserver = this.server.getLevel(this.getRespawnDimension());

        if (worldserver != null && blockposition != null) {
            Optional<EntityPlayer.RespawnPosAngle> optional = findRespawnAndUseSpawnBlock(worldserver, blockposition, f, flag1, flag);

            if (optional.isPresent()) {
                EntityPlayer.RespawnPosAngle entityplayer_respawnposangle = (EntityPlayer.RespawnPosAngle) optional.get();

                return new DimensionTransition(worldserver, entityplayer_respawnposangle.position(), Vec3D.ZERO, entityplayer_respawnposangle.yaw(), 0.0F, dimensiontransition_a);
            } else {
                return DimensionTransition.missingRespawnBlock(this.server.overworld(), this, dimensiontransition_a);
            }
        } else {
            return new DimensionTransition(this.server.overworld(), this, dimensiontransition_a);
        }
    }

    public static Optional<EntityPlayer.RespawnPosAngle> findRespawnAndUseSpawnBlock(WorldServer worldserver, BlockPosition blockposition, float f, boolean flag, boolean flag1) {
        IBlockData iblockdata = worldserver.getBlockState(blockposition);
        Block block = iblockdata.getBlock();

        if (block instanceof BlockRespawnAnchor && (flag || (Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) > 0) && BlockRespawnAnchor.canSetSpawn(worldserver)) {
            Optional<Vec3D> optional = BlockRespawnAnchor.findStandUpPosition(EntityTypes.PLAYER, worldserver, blockposition);

            if (!flag && !flag1 && optional.isPresent()) {
                worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockRespawnAnchor.CHARGE, (Integer) iblockdata.getValue(BlockRespawnAnchor.CHARGE) - 1), 3);
            }

            return optional.map((vec3d) -> {
                return EntityPlayer.RespawnPosAngle.of(vec3d, blockposition);
            });
        } else if (block instanceof BlockBed && BlockBed.canSetSpawn(worldserver)) {
            return BlockBed.findStandUpPosition(EntityTypes.PLAYER, worldserver, blockposition, (EnumDirection) iblockdata.getValue(BlockBed.FACING), f).map((vec3d) -> {
                return EntityPlayer.RespawnPosAngle.of(vec3d, blockposition);
            });
        } else if (!flag) {
            return Optional.empty();
        } else {
            boolean flag2 = block.isPossibleToRespawnInThis(iblockdata);
            IBlockData iblockdata1 = worldserver.getBlockState(blockposition.above());
            boolean flag3 = iblockdata1.getBlock().isPossibleToRespawnInThis(iblockdata1);

            return flag2 && flag3 ? Optional.of(new EntityPlayer.RespawnPosAngle(new Vec3D((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.1D, (double) blockposition.getZ() + 0.5D), f)) : Optional.empty();
        }
    }

    public void showEndCredits() {
        this.unRide();
        this.serverLevel().removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
        if (!this.wonGame) {
            this.wonGame = true;
            this.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.WIN_GAME, 0.0F));
            this.seenCredits = true;
        }

    }

    @Nullable
    @Override
    public Entity changeDimension(DimensionTransition dimensiontransition) {
        if (this.isRemoved()) {
            return null;
        } else {
            if (dimensiontransition.missingRespawnBlock()) {
                this.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
            }

            WorldServer worldserver = dimensiontransition.newLevel();
            WorldServer worldserver1 = this.serverLevel();
            ResourceKey<World> resourcekey = worldserver1.dimension();

            if (worldserver.dimension() == resourcekey) {
                this.connection.teleport(dimensiontransition.pos().x, dimensiontransition.pos().y, dimensiontransition.pos().z, dimensiontransition.yRot(), dimensiontransition.xRot());
                this.connection.resetPosition();
                dimensiontransition.postDimensionTransition().onTransition(this);
                return this;
            } else {
                this.isChangingDimension = true;
                WorldData worlddata = worldserver.getLevelData();

                this.connection.send(new PacketPlayOutRespawn(this.createCommonSpawnInfo(worldserver), (byte) 3));
                this.connection.send(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
                PlayerList playerlist = this.server.getPlayerList();

                playerlist.sendPlayerPermissionLevel(this);
                worldserver1.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
                this.unsetRemoved();
                worldserver1.getProfiler().push("moving");
                if (resourcekey == World.OVERWORLD && worldserver.dimension() == World.NETHER) {
                    this.enteredNetherPosition = this.position();
                }

                worldserver1.getProfiler().pop();
                worldserver1.getProfiler().push("placing");
                this.setServerLevel(worldserver);
                this.connection.teleport(dimensiontransition.pos().x, dimensiontransition.pos().y, dimensiontransition.pos().z, dimensiontransition.yRot(), dimensiontransition.xRot());
                this.connection.resetPosition();
                worldserver.addDuringTeleport(this);
                worldserver1.getProfiler().pop();
                this.triggerDimensionChangeTriggers(worldserver1);
                this.connection.send(new PacketPlayOutAbilities(this.getAbilities()));
                playerlist.sendLevelInfo(this, worldserver);
                playerlist.sendAllPlayerInfo(this);
                playerlist.sendActivePlayerEffects(this);
                dimensiontransition.postDimensionTransition().onTransition(this);
                this.lastSentExp = -1;
                this.lastSentHealth = -1.0F;
                this.lastSentFood = -1;
                return this;
            }
        }
    }

    public void triggerDimensionChangeTriggers(WorldServer worldserver) {
        ResourceKey<World> resourcekey = worldserver.dimension();
        ResourceKey<World> resourcekey1 = this.level().dimension();

        CriterionTriggers.CHANGED_DIMENSION.trigger(this, resourcekey, resourcekey1);
        if (resourcekey == World.NETHER && resourcekey1 == World.OVERWORLD && this.enteredNetherPosition != null) {
            CriterionTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
        }

        if (resourcekey1 != World.NETHER) {
            this.enteredNetherPosition = null;
        }

    }

    @Override
    public boolean broadcastToPlayer(EntityPlayer entityplayer) {
        return entityplayer.isSpectator() ? this.getCamera() == this : (this.isSpectator() ? false : super.broadcastToPlayer(entityplayer));
    }

    @Override
    public void take(Entity entity, int i) {
        super.take(entity, i);
        this.containerMenu.broadcastChanges();
    }

    @Override
    public Either<EntityHuman.EnumBedResult, Unit> startSleepInBed(BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) this.level().getBlockState(blockposition).getValue(BlockFacingHorizontal.FACING);

        if (!this.isSleeping() && this.isAlive()) {
            if (!this.level().dimensionType().natural()) {
                return Either.left(EntityHuman.EnumBedResult.NOT_POSSIBLE_HERE);
            } else if (!this.bedInRange(blockposition, enumdirection)) {
                return Either.left(EntityHuman.EnumBedResult.TOO_FAR_AWAY);
            } else if (this.bedBlocked(blockposition, enumdirection)) {
                return Either.left(EntityHuman.EnumBedResult.OBSTRUCTED);
            } else {
                this.setRespawnPosition(this.level().dimension(), blockposition, this.getYRot(), false, true);
                if (this.level().isDay()) {
                    return Either.left(EntityHuman.EnumBedResult.NOT_POSSIBLE_NOW);
                } else {
                    if (!this.isCreative()) {
                        double d0 = 8.0D;
                        double d1 = 5.0D;
                        Vec3D vec3d = Vec3D.atBottomCenterOf(blockposition);
                        List<EntityMonster> list = this.level().getEntitiesOfClass(EntityMonster.class, new AxisAlignedBB(vec3d.x() - 8.0D, vec3d.y() - 5.0D, vec3d.z() - 8.0D, vec3d.x() + 8.0D, vec3d.y() + 5.0D, vec3d.z() + 8.0D), (entitymonster) -> {
                            return entitymonster.isPreventingPlayerRest(this);
                        });

                        if (!list.isEmpty()) {
                            return Either.left(EntityHuman.EnumBedResult.NOT_SAFE);
                        }
                    }

                    Either<EntityHuman.EnumBedResult, Unit> either = super.startSleepInBed(blockposition).ifRight((unit) -> {
                        this.awardStat(StatisticList.SLEEP_IN_BED);
                        CriterionTriggers.SLEPT_IN_BED.trigger(this);
                    });

                    if (!this.serverLevel().canSleepThroughNights()) {
                        this.displayClientMessage(IChatBaseComponent.translatable("sleep.not_possible"), true);
                    }

                    ((WorldServer) this.level()).updateSleepingPlayerList();
                    return either;
                }
            }
        } else {
            return Either.left(EntityHuman.EnumBedResult.OTHER_PROBLEM);
        }
    }

    @Override
    public void startSleeping(BlockPosition blockposition) {
        this.resetStat(StatisticList.CUSTOM.get(StatisticList.TIME_SINCE_REST));
        super.startSleeping(blockposition);
    }

    private boolean bedInRange(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.isReachableBedBlock(blockposition) || this.isReachableBedBlock(blockposition.relative(enumdirection.getOpposite()));
    }

    private boolean isReachableBedBlock(BlockPosition blockposition) {
        Vec3D vec3d = Vec3D.atBottomCenterOf(blockposition);

        return Math.abs(this.getX() - vec3d.x()) <= 3.0D && Math.abs(this.getY() - vec3d.y()) <= 2.0D && Math.abs(this.getZ() - vec3d.z()) <= 3.0D;
    }

    private boolean bedBlocked(BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition blockposition1 = blockposition.above();

        return !this.freeAt(blockposition1) || !this.freeAt(blockposition1.relative(enumdirection.getOpposite()));
    }

    @Override
    public void stopSleepInBed(boolean flag, boolean flag1) {
        if (this.isSleeping()) {
            this.serverLevel().getChunkSource().broadcastAndSend(this, new PacketPlayOutAnimation(this, 2));
        }

        super.stopSleepInBed(flag, flag1);
        if (this.connection != null) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }

    }

    @Override
    public void dismountTo(double d0, double d1, double d2) {
        this.removeVehicle();
        this.setPos(d0, d1, d2);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damagesource) {
        return super.isInvulnerableTo(damagesource) || this.isChangingDimension();
    }

    @Override
    protected void checkFallDamage(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {}

    @Override
    protected void onChangedBlock(WorldServer worldserver, BlockPosition blockposition) {
        if (!this.isSpectator()) {
            super.onChangedBlock(worldserver, blockposition);
        }

    }

    public void doCheckFallDamage(double d0, double d1, double d2, boolean flag) {
        if (!this.touchingUnloadedChunk()) {
            this.checkSupportingBlock(flag, new Vec3D(d0, d1, d2));
            BlockPosition blockposition = this.getOnPosLegacy();
            IBlockData iblockdata = this.level().getBlockState(blockposition);

            if (this.spawnExtraParticlesOnFall && flag && this.fallDistance > 0.0F) {
                Vec3D vec3d = blockposition.getCenter().add(0.0D, 0.5D, 0.0D);
                int i = (int) MathHelper.clamp(50.0F * this.fallDistance, 0.0F, 200.0F);

                this.serverLevel().sendParticles(new ParticleParamBlock(Particles.BLOCK, iblockdata), vec3d.x, vec3d.y, vec3d.z, i, 0.30000001192092896D, 0.30000001192092896D, 0.30000001192092896D, 0.15000000596046448D);
                this.spawnExtraParticlesOnFall = false;
            }

            super.checkFallDamage(d1, flag, iblockdata, blockposition);
        }
    }

    @Override
    public void onExplosionHit(@Nullable Entity entity) {
        super.onExplosionHit(entity);
        this.currentImpulseImpactPos = this.position();
        this.currentExplosionCause = entity;
        this.setIgnoreFallDamageFromCurrentImpulse(entity != null && entity.getType() == EntityTypes.WIND_CHARGE);
    }

    @Override
    protected void pushEntities() {
        if (this.level().tickRateManager().runsNormally()) {
            super.pushEntities();
        }

    }

    @Override
    public void openTextEdit(TileEntitySign tileentitysign, boolean flag) {
        this.connection.send(new PacketPlayOutBlockChange(this.level(), tileentitysign.getBlockPos()));
        this.connection.send(new PacketPlayOutOpenSignEditor(tileentitysign.getBlockPos(), flag));
    }

    public void nextContainerCounter() {
        this.containerCounter = this.containerCounter % 100 + 1;
    }

    @Override
    public OptionalInt openMenu(@Nullable ITileInventory itileinventory) {
        if (itileinventory == null) {
            return OptionalInt.empty();
        } else {
            if (this.containerMenu != this.inventoryMenu) {
                this.closeContainer();
            }

            this.nextContainerCounter();
            Container container = itileinventory.createMenu(this.containerCounter, this.getInventory(), this);

            if (container == null) {
                if (this.isSpectator()) {
                    this.displayClientMessage(IChatBaseComponent.translatable("container.spectatorCantOpen").withStyle(EnumChatFormat.RED), true);
                }

                return OptionalInt.empty();
            } else {
                this.connection.send(new PacketPlayOutOpenWindow(container.containerId, container.getType(), itileinventory.getDisplayName()));
                this.initMenu(container);
                this.containerMenu = container;
                return OptionalInt.of(this.containerCounter);
            }
        }
    }

    @Override
    public void sendMerchantOffers(int i, MerchantRecipeList merchantrecipelist, int j, int k, boolean flag, boolean flag1) {
        this.connection.send(new PacketPlayOutOpenWindowMerchant(i, merchantrecipelist, j, k, flag, flag1));
    }

    @Override
    public void openHorseInventory(EntityHorseAbstract entityhorseabstract, IInventory iinventory) {
        if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
        }

        this.nextContainerCounter();
        int i = entityhorseabstract.getInventoryColumns();

        this.connection.send(new PacketPlayOutOpenWindowHorse(this.containerCounter, i, entityhorseabstract.getId()));
        this.containerMenu = new ContainerHorse(this.containerCounter, this.getInventory(), iinventory, entityhorseabstract, i);
        this.initMenu(this.containerMenu);
    }

    @Override
    public void openItemGui(ItemStack itemstack, EnumHand enumhand) {
        if (itemstack.is(Items.WRITTEN_BOOK)) {
            if (ItemWrittenBook.resolveBookComponents(itemstack, this.createCommandSourceStack(), this)) {
                this.containerMenu.broadcastChanges();
            }

            this.connection.send(new PacketPlayOutOpenBook(enumhand));
        }

    }

    @Override
    public void openCommandBlock(TileEntityCommand tileentitycommand) {
        this.connection.send(PacketPlayOutTileEntityData.create(tileentitycommand, TileEntity::saveCustomOnly));
    }

    @Override
    public void closeContainer() {
        this.connection.send(new PacketPlayOutCloseWindow(this.containerMenu.containerId));
        this.doCloseContainer();
    }

    @Override
    public void doCloseContainer() {
        this.containerMenu.removed(this);
        this.inventoryMenu.transferState(this.containerMenu);
        this.containerMenu = this.inventoryMenu;
    }

    public void setPlayerInput(float f, float f1, boolean flag, boolean flag1) {
        if (this.isPassenger()) {
            if (f >= -1.0F && f <= 1.0F) {
                this.xxa = f;
            }

            if (f1 >= -1.0F && f1 <= 1.0F) {
                this.zza = f1;
            }

            this.jumping = flag;
            this.setShiftKeyDown(flag1);
        }

    }

    @Override
    public void travel(Vec3D vec3d) {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();

        super.travel(vec3d);
        this.checkMovementStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
    }

    @Override
    public void rideTick() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();

        super.rideTick();
        this.checkRidingStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
    }

    public void checkMovementStatistics(double d0, double d1, double d2) {
        if (!this.isPassenger() && !didNotMove(d0, d1, d2)) {
            int i;

            if (this.isSwimming()) {
                i = Math.round((float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
                if (i > 0) {
                    this.awardStat(StatisticList.SWIM_ONE_CM, i);
                    this.causeFoodExhaustion(0.01F * (float) i * 0.01F);
                }
            } else if (this.isEyeInFluid(TagsFluid.WATER)) {
                i = Math.round((float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
                if (i > 0) {
                    this.awardStat(StatisticList.WALK_UNDER_WATER_ONE_CM, i);
                    this.causeFoodExhaustion(0.01F * (float) i * 0.01F);
                }
            } else if (this.isInWater()) {
                i = Math.round((float) Math.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 0) {
                    this.awardStat(StatisticList.WALK_ON_WATER_ONE_CM, i);
                    this.causeFoodExhaustion(0.01F * (float) i * 0.01F);
                }
            } else if (this.onClimbable()) {
                if (d1 > 0.0D) {
                    this.awardStat(StatisticList.CLIMB_ONE_CM, (int) Math.round(d1 * 100.0D));
                }
            } else if (this.onGround()) {
                i = Math.round((float) Math.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 0) {
                    if (this.isSprinting()) {
                        this.awardStat(StatisticList.SPRINT_ONE_CM, i);
                        this.causeFoodExhaustion(0.1F * (float) i * 0.01F);
                    } else if (this.isCrouching()) {
                        this.awardStat(StatisticList.CROUCH_ONE_CM, i);
                        this.causeFoodExhaustion(0.0F * (float) i * 0.01F);
                    } else {
                        this.awardStat(StatisticList.WALK_ONE_CM, i);
                        this.causeFoodExhaustion(0.0F * (float) i * 0.01F);
                    }
                }
            } else if (this.isFallFlying()) {
                i = Math.round((float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
                this.awardStat(StatisticList.AVIATE_ONE_CM, i);
            } else {
                i = Math.round((float) Math.sqrt(d0 * d0 + d2 * d2) * 100.0F);
                if (i > 25) {
                    this.awardStat(StatisticList.FLY_ONE_CM, i);
                }
            }

        }
    }

    private void checkRidingStatistics(double d0, double d1, double d2) {
        if (this.isPassenger() && !didNotMove(d0, d1, d2)) {
            int i = Math.round((float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
            Entity entity = this.getVehicle();

            if (entity instanceof EntityMinecartAbstract) {
                this.awardStat(StatisticList.MINECART_ONE_CM, i);
            } else if (entity instanceof EntityBoat) {
                this.awardStat(StatisticList.BOAT_ONE_CM, i);
            } else if (entity instanceof EntityPig) {
                this.awardStat(StatisticList.PIG_ONE_CM, i);
            } else if (entity instanceof EntityHorseAbstract) {
                this.awardStat(StatisticList.HORSE_ONE_CM, i);
            } else if (entity instanceof EntityStrider) {
                this.awardStat(StatisticList.STRIDER_ONE_CM, i);
            }

        }
    }

    private static boolean didNotMove(double d0, double d1, double d2) {
        return d0 == 0.0D && d1 == 0.0D && d2 == 0.0D;
    }

    @Override
    public void awardStat(Statistic<?> statistic, int i) {
        this.stats.increment(this, statistic, i);
        this.getScoreboard().forAllObjectives(statistic, this, (scoreaccess) -> {
            scoreaccess.add(i);
        });
    }

    @Override
    public void resetStat(Statistic<?> statistic) {
        this.stats.setValue(this, statistic, 0);
        this.getScoreboard().forAllObjectives(statistic, this, ScoreAccess::reset);
    }

    @Override
    public int awardRecipes(Collection<RecipeHolder<?>> collection) {
        return this.recipeBook.addRecipes(collection, this);
    }

    @Override
    public void triggerRecipeCrafted(RecipeHolder<?> recipeholder, List<ItemStack> list) {
        CriterionTriggers.RECIPE_CRAFTED.trigger(this, recipeholder.id(), list);
    }

    @Override
    public void awardRecipesByKey(List<MinecraftKey> list) {
        List<RecipeHolder<?>> list1 = (List) list.stream().flatMap((minecraftkey) -> {
            return this.server.getRecipeManager().byKey(minecraftkey).stream();
        }).collect(Collectors.toList());

        this.awardRecipes(list1);
    }

    @Override
    public int resetRecipes(Collection<RecipeHolder<?>> collection) {
        return this.recipeBook.removeRecipes(collection, this);
    }

    @Override
    public void giveExperiencePoints(int i) {
        super.giveExperiencePoints(i);
        this.lastSentExp = -1;
    }

    public void disconnect() {
        this.disconnected = true;
        this.ejectPassengers();
        if (this.isSleeping()) {
            this.stopSleepInBed(true, false);
        }

    }

    public boolean hasDisconnected() {
        return this.disconnected;
    }

    public void resetSentInfo() {
        this.lastSentHealth = -1.0E8F;
    }

    @Override
    public void displayClientMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.sendSystemMessage(ichatbasecomponent, flag);
    }

    @Override
    protected void completeUsingItem() {
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            this.connection.send(new PacketPlayOutEntityStatus(this, (byte) 9));
            super.completeUsingItem();
        }

    }

    @Override
    public void lookAt(ArgumentAnchor.Anchor argumentanchor_anchor, Vec3D vec3d) {
        super.lookAt(argumentanchor_anchor, vec3d);
        this.connection.send(new PacketPlayOutLookAt(argumentanchor_anchor, vec3d.x, vec3d.y, vec3d.z));
    }

    public void lookAt(ArgumentAnchor.Anchor argumentanchor_anchor, Entity entity, ArgumentAnchor.Anchor argumentanchor_anchor1) {
        Vec3D vec3d = argumentanchor_anchor1.apply(entity);

        super.lookAt(argumentanchor_anchor, vec3d);
        this.connection.send(new PacketPlayOutLookAt(argumentanchor_anchor, entity, argumentanchor_anchor1));
    }

    public void restoreFrom(EntityPlayer entityplayer, boolean flag) {
        this.wardenSpawnTracker = entityplayer.wardenSpawnTracker;
        this.chatSession = entityplayer.chatSession;
        this.gameMode.setGameModeForPlayer(entityplayer.gameMode.getGameModeForPlayer(), entityplayer.gameMode.getPreviousGameModeForPlayer());
        this.onUpdateAbilities();
        this.getAttributes().assignBaseValues(entityplayer.getAttributes());
        this.setHealth(this.getMaxHealth());
        if (flag) {
            this.getInventory().replaceWith(entityplayer.getInventory());
            this.setHealth(entityplayer.getHealth());
            this.foodData = entityplayer.foodData;
            Iterator iterator = entityplayer.getActiveEffects().iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                this.addEffect(new MobEffect(mobeffect));
            }

            this.experienceLevel = entityplayer.experienceLevel;
            this.totalExperience = entityplayer.totalExperience;
            this.experienceProgress = entityplayer.experienceProgress;
            this.setScore(entityplayer.getScore());
            this.portalProcess = entityplayer.portalProcess;
        } else if (this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || entityplayer.isSpectator()) {
            this.getInventory().replaceWith(entityplayer.getInventory());
            this.experienceLevel = entityplayer.experienceLevel;
            this.totalExperience = entityplayer.totalExperience;
            this.experienceProgress = entityplayer.experienceProgress;
            this.setScore(entityplayer.getScore());
        }

        this.enchantmentSeed = entityplayer.enchantmentSeed;
        this.enderChestInventory = entityplayer.enderChestInventory;
        this.getEntityData().set(EntityPlayer.DATA_PLAYER_MODE_CUSTOMISATION, (Byte) entityplayer.getEntityData().get(EntityPlayer.DATA_PLAYER_MODE_CUSTOMISATION));
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0F;
        this.lastSentFood = -1;
        this.recipeBook.copyOverData(entityplayer.recipeBook);
        this.seenCredits = entityplayer.seenCredits;
        this.enteredNetherPosition = entityplayer.enteredNetherPosition;
        this.chunkTrackingView = entityplayer.chunkTrackingView;
        this.setShoulderEntityLeft(entityplayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(entityplayer.getShoulderEntityRight());
        this.setLastDeathLocation(entityplayer.getLastDeathLocation());
    }

    @Override
    protected void onEffectAdded(MobEffect mobeffect, @Nullable Entity entity) {
        super.onEffectAdded(mobeffect, entity);
        this.connection.send(new PacketPlayOutEntityEffect(this.getId(), mobeffect, true));
        if (mobeffect.is(MobEffects.LEVITATION)) {
            this.levitationStartTime = this.tickCount;
            this.levitationStartPos = this.position();
        }

        CriterionTriggers.EFFECTS_CHANGED.trigger(this, entity);
    }

    @Override
    protected void onEffectUpdated(MobEffect mobeffect, boolean flag, @Nullable Entity entity) {
        super.onEffectUpdated(mobeffect, flag, entity);
        this.connection.send(new PacketPlayOutEntityEffect(this.getId(), mobeffect, false));
        CriterionTriggers.EFFECTS_CHANGED.trigger(this, entity);
    }

    @Override
    protected void onEffectRemoved(MobEffect mobeffect) {
        super.onEffectRemoved(mobeffect);
        this.connection.send(new PacketPlayOutRemoveEntityEffect(this.getId(), mobeffect.getEffect()));
        if (mobeffect.is(MobEffects.LEVITATION)) {
            this.levitationStartPos = null;
        }

        CriterionTriggers.EFFECTS_CHANGED.trigger(this, (Entity) null);
    }

    @Override
    public void teleportTo(double d0, double d1, double d2) {
        this.connection.teleport(d0, d1, d2, this.getYRot(), this.getXRot(), RelativeMovement.ROTATION);
    }

    @Override
    public void teleportRelative(double d0, double d1, double d2) {
        this.connection.teleport(this.getX() + d0, this.getY() + d1, this.getZ() + d2, this.getYRot(), this.getXRot(), RelativeMovement.ALL);
    }

    @Override
    public boolean teleportTo(WorldServer worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1) {
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(BlockPosition.containing(d0, d1, d2));

        worldserver.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkcoordintpair, 1, this.getId());
        this.stopRiding();
        if (this.isSleeping()) {
            this.stopSleepInBed(true, true);
        }

        if (worldserver == this.level()) {
            this.connection.teleport(d0, d1, d2, f, f1, set);
        } else {
            this.teleportTo(worldserver, d0, d1, d2, f, f1);
        }

        this.setYHeadRot(f);
        return true;
    }

    @Override
    public void moveTo(double d0, double d1, double d2) {
        super.moveTo(d0, d1, d2);
        this.connection.resetPosition();
    }

    @Override
    public void crit(Entity entity) {
        this.serverLevel().getChunkSource().broadcastAndSend(this, new PacketPlayOutAnimation(entity, 4));
    }

    @Override
    public void magicCrit(Entity entity) {
        this.serverLevel().getChunkSource().broadcastAndSend(this, new PacketPlayOutAnimation(entity, 5));
    }

    @Override
    public void onUpdateAbilities() {
        if (this.connection != null) {
            this.connection.send(new PacketPlayOutAbilities(this.getAbilities()));
            this.updateInvisibilityStatus();
        }
    }

    public WorldServer serverLevel() {
        return (WorldServer) this.level();
    }

    public boolean setGameMode(EnumGamemode enumgamemode) {
        boolean flag = this.isSpectator();

        if (!this.gameMode.changeGameModeForPlayer(enumgamemode)) {
            return false;
        } else {
            this.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.CHANGE_GAME_MODE, (float) enumgamemode.getId()));
            if (enumgamemode == EnumGamemode.SPECTATOR) {
                this.removeEntitiesOnShoulder();
                this.stopRiding();
                EnchantmentManager.stopLocationBasedEffects(this);
            } else {
                this.setCamera(this);
                if (flag) {
                    EnchantmentManager.runLocationChangedEffects(this.serverLevel(), this);
                }
            }

            this.onUpdateAbilities();
            this.updateEffectVisibility();
            return true;
        }
    }

    @Override
    public boolean isSpectator() {
        return this.gameMode.getGameModeForPlayer() == EnumGamemode.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return this.gameMode.getGameModeForPlayer() == EnumGamemode.CREATIVE;
    }

    @Override
    public void sendSystemMessage(IChatBaseComponent ichatbasecomponent) {
        this.sendSystemMessage(ichatbasecomponent, false);
    }

    public void sendSystemMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        if (this.acceptsSystemMessages(flag)) {
            this.connection.send(new ClientboundSystemChatPacket(ichatbasecomponent, flag), PacketSendListener.exceptionallySend(() -> {
                if (this.acceptsSystemMessages(false)) {
                    boolean flag1 = true;
                    String s = ichatbasecomponent.getString(256);
                    IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(s).withStyle(EnumChatFormat.YELLOW);

                    return new ClientboundSystemChatPacket(IChatBaseComponent.translatable("multiplayer.message_not_delivered", ichatmutablecomponent).withStyle(EnumChatFormat.RED), false);
                } else {
                    return null;
                }
            }));
        }
    }

    public void sendChatMessage(OutgoingChatMessage outgoingchatmessage, boolean flag, ChatMessageType.a chatmessagetype_a) {
        if (this.acceptsChatMessages()) {
            outgoingchatmessage.sendToPlayer(this, flag, chatmessagetype_a);
        }

    }

    public String getIpAddress() {
        SocketAddress socketaddress = this.connection.getRemoteAddress();

        if (socketaddress instanceof InetSocketAddress inetsocketaddress) {
            return InetAddresses.toAddrString(inetsocketaddress.getAddress());
        } else {
            return "<unknown>";
        }
    }

    public void updateOptions(ClientInformation clientinformation) {
        this.language = clientinformation.language();
        this.requestedViewDistance = clientinformation.viewDistance();
        this.chatVisibility = clientinformation.chatVisibility();
        this.canChatColor = clientinformation.chatColors();
        this.textFilteringEnabled = clientinformation.textFilteringEnabled();
        this.allowsListing = clientinformation.allowsListing();
        this.getEntityData().set(EntityPlayer.DATA_PLAYER_MODE_CUSTOMISATION, (byte) clientinformation.modelCustomisation());
        this.getEntityData().set(EntityPlayer.DATA_PLAYER_MAIN_HAND, (byte) clientinformation.mainHand().getId());
    }

    public ClientInformation clientInformation() {
        byte b0 = (Byte) this.getEntityData().get(EntityPlayer.DATA_PLAYER_MODE_CUSTOMISATION);
        EnumMainHand enummainhand = (EnumMainHand) EnumMainHand.BY_ID.apply((Byte) this.getEntityData().get(EntityPlayer.DATA_PLAYER_MAIN_HAND));

        return new ClientInformation(this.language, this.requestedViewDistance, this.chatVisibility, this.canChatColor, b0, enummainhand, this.textFilteringEnabled, this.allowsListing);
    }

    public boolean canChatInColor() {
        return this.canChatColor;
    }

    public EnumChatVisibility getChatVisibility() {
        return this.chatVisibility;
    }

    private boolean acceptsSystemMessages(boolean flag) {
        return this.chatVisibility == EnumChatVisibility.HIDDEN ? flag : true;
    }

    private boolean acceptsChatMessages() {
        return this.chatVisibility == EnumChatVisibility.FULL;
    }

    public int requestedViewDistance() {
        return this.requestedViewDistance;
    }

    public void sendServerStatus(ServerPing serverping) {
        this.connection.send(new ClientboundServerDataPacket(serverping.description(), serverping.favicon().map(ServerPing.a::iconBytes)));
    }

    @Override
    protected int getPermissionLevel() {
        return this.server.getProfilePermissions(this.getGameProfile());
    }

    public void resetLastActionTime() {
        this.lastActionTime = SystemUtils.getMillis();
    }

    public ServerStatisticManager getStats() {
        return this.stats;
    }

    public RecipeBookServer getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected void updateInvisibilityStatus() {
        if (this.isSpectator()) {
            this.removeEffectParticles();
            this.setInvisible(true);
        } else {
            super.updateInvisibilityStatus();
        }

    }

    public Entity getCamera() {
        return (Entity) (this.camera == null ? this : this.camera);
    }

    public void setCamera(@Nullable Entity entity) {
        Entity entity1 = this.getCamera();

        this.camera = (Entity) (entity == null ? this : entity);
        if (entity1 != this.camera) {
            World world = this.camera.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                this.teleportTo(worldserver, this.camera.getX(), this.camera.getY(), this.camera.getZ(), Set.of(), this.getYRot(), this.getXRot());
            }

            if (entity != null) {
                this.serverLevel().getChunkSource().move(this);
            }

            this.connection.send(new PacketPlayOutCamera(this.camera));
            this.connection.resetPosition();
        }

    }

    @Override
    protected void processPortalCooldown() {
        if (!this.isChangingDimension) {
            super.processPortalCooldown();
        }

    }

    @Override
    public void attack(Entity entity) {
        if (this.gameMode.getGameModeForPlayer() == EnumGamemode.SPECTATOR) {
            this.setCamera(entity);
        } else {
            super.attack(entity);
        }

    }

    public long getLastActionTime() {
        return this.lastActionTime;
    }

    @Nullable
    public IChatBaseComponent getTabListDisplayName() {
        return null;
    }

    @Override
    public void swing(EnumHand enumhand) {
        super.swing(enumhand);
        this.resetAttackStrengthTicker();
    }

    public boolean isChangingDimension() {
        return this.isChangingDimension;
    }

    public void hasChangedDimension() {
        this.isChangingDimension = false;
    }

    public AdvancementDataPlayer getAdvancements() {
        return this.advancements;
    }

    public void teleportTo(WorldServer worldserver, double d0, double d1, double d2, float f, float f1) {
        this.setCamera(this);
        this.stopRiding();
        if (worldserver == this.level()) {
            this.connection.teleport(d0, d1, d2, f, f1);
        } else {
            this.changeDimension(new DimensionTransition(worldserver, new Vec3D(d0, d1, d2), Vec3D.ZERO, f, f1, DimensionTransition.DO_NOTHING));
        }

    }

    @Nullable
    public BlockPosition getRespawnPosition() {
        return this.respawnPosition;
    }

    public float getRespawnAngle() {
        return this.respawnAngle;
    }

    public ResourceKey<World> getRespawnDimension() {
        return this.respawnDimension;
    }

    public boolean isRespawnForced() {
        return this.respawnForced;
    }

    public void copyRespawnPosition(EntityPlayer entityplayer) {
        this.setRespawnPosition(entityplayer.getRespawnDimension(), entityplayer.getRespawnPosition(), entityplayer.getRespawnAngle(), entityplayer.isRespawnForced(), false);
    }

    public void setRespawnPosition(ResourceKey<World> resourcekey, @Nullable BlockPosition blockposition, float f, boolean flag, boolean flag1) {
        if (blockposition != null) {
            boolean flag2 = blockposition.equals(this.respawnPosition) && resourcekey.equals(this.respawnDimension);

            if (flag1 && !flag2) {
                this.sendSystemMessage(IChatBaseComponent.translatable("block.minecraft.set_spawn"));
            }

            this.respawnPosition = blockposition;
            this.respawnDimension = resourcekey;
            this.respawnAngle = f;
            this.respawnForced = flag;
        } else {
            this.respawnPosition = null;
            this.respawnDimension = World.OVERWORLD;
            this.respawnAngle = 0.0F;
            this.respawnForced = false;
        }

    }

    public SectionPosition getLastSectionPos() {
        return this.lastSectionPos;
    }

    public void setLastSectionPos(SectionPosition sectionposition) {
        this.lastSectionPos = sectionposition;
    }

    public ChunkTrackingView getChunkTrackingView() {
        return this.chunkTrackingView;
    }

    public void setChunkTrackingView(ChunkTrackingView chunktrackingview) {
        this.chunkTrackingView = chunktrackingview;
    }

    @Override
    public void playNotifySound(SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        this.connection.send(new PacketPlayOutNamedSoundEffect(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundeffect), soundcategory, this.getX(), this.getY(), this.getZ(), f, f1, this.random.nextLong()));
    }

    @Override
    public EntityItem drop(ItemStack itemstack, boolean flag, boolean flag1) {
        EntityItem entityitem = super.drop(itemstack, flag, flag1);

        if (entityitem == null) {
            return null;
        } else {
            this.level().addFreshEntity(entityitem);
            ItemStack itemstack1 = entityitem.getItem();

            if (flag1) {
                if (!itemstack1.isEmpty()) {
                    this.awardStat(StatisticList.ITEM_DROPPED.get(itemstack1.getItem()), itemstack.getCount());
                }

                this.awardStat(StatisticList.DROP);
            }

            return entityitem;
        }
    }

    public ITextFilter getTextFilter() {
        return this.textFilter;
    }

    public void setServerLevel(WorldServer worldserver) {
        this.setLevel(worldserver);
        this.gameMode.setLevel(worldserver);
    }

    @Nullable
    private static EnumGamemode readPlayerMode(@Nullable NBTTagCompound nbttagcompound, String s) {
        return nbttagcompound != null && nbttagcompound.contains(s, 99) ? EnumGamemode.byId(nbttagcompound.getInt(s)) : null;
    }

    private EnumGamemode calculateGameModeForNewPlayer(@Nullable EnumGamemode enumgamemode) {
        EnumGamemode enumgamemode1 = this.server.getForcedGameType();

        return enumgamemode1 != null ? enumgamemode1 : (enumgamemode != null ? enumgamemode : this.server.getDefaultGameType());
    }

    public void loadGameTypes(@Nullable NBTTagCompound nbttagcompound) {
        this.gameMode.setGameModeForPlayer(this.calculateGameModeForNewPlayer(readPlayerMode(nbttagcompound, "playerGameType")), readPlayerMode(nbttagcompound, "previousPlayerGameType"));
    }

    private void storeGameTypes(NBTTagCompound nbttagcompound) {
        nbttagcompound.putInt("playerGameType", this.gameMode.getGameModeForPlayer().getId());
        EnumGamemode enumgamemode = this.gameMode.getPreviousGameModeForPlayer();

        if (enumgamemode != null) {
            nbttagcompound.putInt("previousPlayerGameType", enumgamemode.getId());
        }

    }

    @Override
    public boolean isTextFilteringEnabled() {
        return this.textFilteringEnabled;
    }

    public boolean shouldFilterMessageTo(EntityPlayer entityplayer) {
        return entityplayer == this ? false : this.textFilteringEnabled || entityplayer.textFilteringEnabled;
    }

    @Override
    public boolean mayInteract(World world, BlockPosition blockposition) {
        return super.mayInteract(world, blockposition) && world.mayInteract(this, blockposition);
    }

    @Override
    protected void updateUsingItem(ItemStack itemstack) {
        CriterionTriggers.USING_ITEM.trigger(this, itemstack);
        super.updateUsingItem(itemstack);
    }

    public boolean drop(boolean flag) {
        PlayerInventory playerinventory = this.getInventory();
        ItemStack itemstack = playerinventory.removeFromSelected(flag);

        this.containerMenu.findSlot(playerinventory, playerinventory.selected).ifPresent((i) -> {
            this.containerMenu.setRemoteSlot(i, playerinventory.getSelected());
        });
        return this.drop(itemstack, false, true) != null;
    }

    public boolean allowsListing() {
        return this.allowsListing;
    }

    @Override
    public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
        return Optional.of(this.wardenSpawnTracker);
    }

    public void setSpawnExtraParticlesOnFall(boolean flag) {
        this.spawnExtraParticlesOnFall = flag;
    }

    @Override
    public void onItemPickup(EntityItem entityitem) {
        super.onItemPickup(entityitem);
        Entity entity = entityitem.getOwner();

        if (entity != null) {
            CriterionTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, entityitem.getItem(), entity);
        }

    }

    public void setChatSession(RemoteChatSession remotechatsession) {
        this.chatSession = remotechatsession;
    }

    @Nullable
    public RemoteChatSession getChatSession() {
        return this.chatSession != null && this.chatSession.hasExpired() ? null : this.chatSession;
    }

    @Override
    public void indicateDamage(double d0, double d1) {
        this.hurtDir = (float) (MathHelper.atan2(d1, d0) * 57.2957763671875D - (double) this.getYRot());
        this.connection.send(new ClientboundHurtAnimationPacket(this));
    }

    @Override
    public boolean startRiding(Entity entity, boolean flag) {
        if (super.startRiding(entity, flag)) {
            this.setKnownMovement(Vec3D.ZERO);
            entity.positionRider(this);
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            if (entity instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) entity;

                this.server.getPlayerList().sendActiveEffects(entityliving, this.connection);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void stopRiding() {
        Entity entity = this.getVehicle();

        super.stopRiding();
        if (entity instanceof EntityLiving entityliving) {
            Iterator iterator = entityliving.getActiveEffects().iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                this.connection.send(new PacketPlayOutRemoveEntityEffect(entity.getId(), mobeffect.getEffect()));
            }
        }

    }

    public CommonPlayerSpawnInfo createCommonSpawnInfo(WorldServer worldserver) {
        return new CommonPlayerSpawnInfo(worldserver.dimensionTypeRegistration(), worldserver.dimension(), BiomeManager.obfuscateSeed(worldserver.getSeed()), this.gameMode.getGameModeForPlayer(), this.gameMode.getPreviousGameModeForPlayer(), worldserver.isDebug(), worldserver.isFlat(), this.getLastDeathLocation(), this.getPortalCooldown());
    }

    public void setRaidOmenPosition(BlockPosition blockposition) {
        this.raidOmenPosition = blockposition;
    }

    public void clearRaidOmenPosition() {
        this.raidOmenPosition = null;
    }

    @Nullable
    public BlockPosition getRaidOmenPosition() {
        return this.raidOmenPosition;
    }

    @Override
    public Vec3D getKnownMovement() {
        Entity entity = this.getVehicle();

        return entity != null && entity.getControllingPassenger() != this ? entity.getKnownMovement() : this.lastKnownClientMovement;
    }

    public void setKnownMovement(Vec3D vec3d) {
        this.lastKnownClientMovement = vec3d;
    }

    @Override
    protected float getEnchantedDamage(Entity entity, float f, DamageSource damagesource) {
        return EnchantmentManager.modifyDamage(this.serverLevel(), this.getWeaponItem(), entity, damagesource, f);
    }

    @Override
    public void onEquippedItemBroken(Item item, EnumItemSlot enumitemslot) {
        super.onEquippedItemBroken(item, enumitemslot);
        this.awardStat(StatisticList.ITEM_BROKEN.get(item));
    }

    public static record RespawnPosAngle(Vec3D position, float yaw) {

        public static EntityPlayer.RespawnPosAngle of(Vec3D vec3d, BlockPosition blockposition) {
            return new EntityPlayer.RespawnPosAngle(vec3d, calculateLookAtYaw(vec3d, blockposition));
        }

        private static float calculateLookAtYaw(Vec3D vec3d, BlockPosition blockposition) {
            Vec3D vec3d1 = Vec3D.atBottomCenterOf(blockposition).subtract(vec3d).normalize();

            return (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D - 90.0D);
        }
    }
}
