package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.FileUtils;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutExperience;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import net.minecraft.network.protocol.game.PacketPlayOutHeldItemSlot;
import net.minecraft.network.protocol.game.PacketPlayOutLogin;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.network.protocol.game.PacketPlayOutRecipeUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutRespawn;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutServerDifficulty;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnPosition;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateTime;
import net.minecraft.network.protocol.game.PacketPlayOutViewDistance;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.AdvancementDataPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.ServerStatisticManager;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.border.IWorldBorderListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.SavedFile;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.WorldNBTStorage;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardTeam;
import org.slf4j.Logger;

public abstract class PlayerList {

    public static final File USERBANLIST_FILE = new File("banned-players.json");
    public static final File IPBANLIST_FILE = new File("banned-ips.json");
    public static final File OPLIST_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    public static final IChatBaseComponent CHAT_FILTERED_FULL = IChatBaseComponent.translatable("chat.filtered_full");
    public static final IChatBaseComponent DUPLICATE_LOGIN_DISCONNECT_MESSAGE = IChatBaseComponent.translatable("multiplayer.disconnect.duplicate_login");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SEND_PLAYER_INFO_INTERVAL = 600;
    private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    public final List<EntityPlayer> players = Lists.newArrayList();
    private final Map<UUID, EntityPlayer> playersByUUID = Maps.newHashMap();
    private final GameProfileBanList bans;
    private final IpBanList ipBans;
    private final OpList ops;
    private final WhiteList whitelist;
    private final Map<UUID, ServerStatisticManager> stats;
    private final Map<UUID, AdvancementDataPlayer> advancements;
    public final WorldNBTStorage playerIo;
    private boolean doWhiteList;
    private final LayeredRegistryAccess<RegistryLayer> registries;
    public int maxPlayers;
    private int viewDistance;
    private int simulationDistance;
    private boolean allowCommandsForAllPlayers;
    private static final boolean ALLOW_LOGOUTIVATOR = false;
    private int sendAllPlayerInfoIn;

    public PlayerList(MinecraftServer minecraftserver, LayeredRegistryAccess<RegistryLayer> layeredregistryaccess, WorldNBTStorage worldnbtstorage, int i) {
        this.bans = new GameProfileBanList(PlayerList.USERBANLIST_FILE);
        this.ipBans = new IpBanList(PlayerList.IPBANLIST_FILE);
        this.ops = new OpList(PlayerList.OPLIST_FILE);
        this.whitelist = new WhiteList(PlayerList.WHITELIST_FILE);
        this.stats = Maps.newHashMap();
        this.advancements = Maps.newHashMap();
        this.server = minecraftserver;
        this.registries = layeredregistryaccess;
        this.maxPlayers = i;
        this.playerIo = worldnbtstorage;
    }

    public void placeNewPlayer(NetworkManager networkmanager, EntityPlayer entityplayer, CommonListenerCookie commonlistenercookie) {
        GameProfile gameprofile = entityplayer.getGameProfile();
        UserCache usercache = this.server.getProfileCache();
        Optional optional;
        String s;

        if (usercache != null) {
            optional = usercache.get(gameprofile.getId());
            s = (String) optional.map(GameProfile::getName).orElse(gameprofile.getName());
            usercache.add(gameprofile);
        } else {
            s = gameprofile.getName();
        }

        optional = this.load(entityplayer);
        ResourceKey<World> resourcekey = (ResourceKey) optional.flatMap((nbttagcompound) -> {
            DataResult dataresult = DimensionManager.parseLegacy(new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound.get("Dimension")));
            Logger logger = PlayerList.LOGGER;

            Objects.requireNonNull(logger);
            return dataresult.resultOrPartial(logger::error);
        }).orElse(World.OVERWORLD);
        WorldServer worldserver = this.server.getLevel(resourcekey);
        WorldServer worldserver1;

        if (worldserver == null) {
            PlayerList.LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", resourcekey);
            worldserver1 = this.server.overworld();
        } else {
            worldserver1 = worldserver;
        }

        entityplayer.setServerLevel(worldserver1);
        String s1 = networkmanager.getLoggableAddress(this.server.logIPs());

        PlayerList.LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{entityplayer.getName().getString(), s1, entityplayer.getId(), entityplayer.getX(), entityplayer.getY(), entityplayer.getZ()});
        WorldData worlddata = worldserver1.getLevelData();

        entityplayer.loadGameTypes((NBTTagCompound) optional.orElse((Object) null));
        PlayerConnection playerconnection = new PlayerConnection(this.server, networkmanager, entityplayer, commonlistenercookie);

        networkmanager.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess())), playerconnection);
        GameRules gamerules = worldserver1.getGameRules();
        boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        boolean flag2 = gamerules.getBoolean(GameRules.RULE_LIMITED_CRAFTING);

        playerconnection.send(new PacketPlayOutLogin(entityplayer.getId(), worlddata.isHardcore(), this.server.levelKeys(), this.getMaxPlayers(), this.viewDistance, this.simulationDistance, flag1, !flag, flag2, entityplayer.createCommonSpawnInfo(worldserver1), this.server.enforceSecureProfile()));
        playerconnection.send(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        playerconnection.send(new PacketPlayOutAbilities(entityplayer.getAbilities()));
        playerconnection.send(new PacketPlayOutHeldItemSlot(entityplayer.getInventory().selected));
        playerconnection.send(new PacketPlayOutRecipeUpdate(this.server.getRecipeManager().getOrderedRecipes()));
        this.sendPlayerPermissionLevel(entityplayer);
        entityplayer.getStats().markAllDirty();
        entityplayer.getRecipeBook().sendInitialRecipeBook(entityplayer);
        this.updateEntireScoreboard(worldserver1.getScoreboard(), entityplayer);
        this.server.invalidateStatus();
        IChatMutableComponent ichatmutablecomponent;

        if (entityplayer.getGameProfile().getName().equalsIgnoreCase(s)) {
            ichatmutablecomponent = IChatBaseComponent.translatable("multiplayer.player.joined", entityplayer.getDisplayName());
        } else {
            ichatmutablecomponent = IChatBaseComponent.translatable("multiplayer.player.joined.renamed", entityplayer.getDisplayName(), s);
        }

        this.broadcastSystemMessage(ichatmutablecomponent.withStyle(EnumChatFormat.YELLOW), false);
        playerconnection.teleport(entityplayer.getX(), entityplayer.getY(), entityplayer.getZ(), entityplayer.getYRot(), entityplayer.getXRot());
        ServerPing serverping = this.server.getStatus();

        if (serverping != null && !commonlistenercookie.transferred()) {
            entityplayer.sendServerStatus(serverping);
        }

        entityplayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(this.players));
        this.players.add(entityplayer);
        this.playersByUUID.put(entityplayer.getUUID(), entityplayer);
        this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(entityplayer)));
        this.sendLevelInfo(entityplayer, worldserver1);
        worldserver1.addNewPlayer(entityplayer);
        this.server.getCustomBossEvents().onPlayerConnect(entityplayer);
        this.sendActivePlayerEffects(entityplayer);
        if (optional.isPresent() && ((NBTTagCompound) optional.get()).contains("RootVehicle", 10)) {
            NBTTagCompound nbttagcompound = ((NBTTagCompound) optional.get()).getCompound("RootVehicle");
            Entity entity = EntityTypes.loadEntityRecursive(nbttagcompound.getCompound("Entity"), worldserver1, (entity1) -> {
                return !worldserver1.addWithUUID(entity1) ? null : entity1;
            });

            if (entity != null) {
                UUID uuid;

                if (nbttagcompound.hasUUID("Attach")) {
                    uuid = nbttagcompound.getUUID("Attach");
                } else {
                    uuid = null;
                }

                Iterator iterator;
                Entity entity1;

                if (entity.getUUID().equals(uuid)) {
                    entityplayer.startRiding(entity, true);
                } else {
                    iterator = entity.getIndirectPassengers().iterator();

                    while (iterator.hasNext()) {
                        entity1 = (Entity) iterator.next();
                        if (entity1.getUUID().equals(uuid)) {
                            entityplayer.startRiding(entity1, true);
                            break;
                        }
                    }
                }

                if (!entityplayer.isPassenger()) {
                    PlayerList.LOGGER.warn("Couldn't reattach entity to player");
                    entity.discard();
                    iterator = entity.getIndirectPassengers().iterator();

                    while (iterator.hasNext()) {
                        entity1 = (Entity) iterator.next();
                        entity1.discard();
                    }
                }
            }
        }

        entityplayer.initInventoryMenu();
    }

    public void updateEntireScoreboard(ScoreboardServer scoreboardserver, EntityPlayer entityplayer) {
        Set<ScoreboardObjective> set = Sets.newHashSet();
        Iterator iterator = scoreboardserver.getPlayerTeams().iterator();

        while (iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam) iterator.next();

            entityplayer.connection.send(PacketPlayOutScoreboardTeam.createAddOrModifyPacket(scoreboardteam, true));
        }

        DisplaySlot[] adisplayslot = DisplaySlot.values();
        int i = adisplayslot.length;

        for (int j = 0; j < i; ++j) {
            DisplaySlot displayslot = adisplayslot[j];
            ScoreboardObjective scoreboardobjective = scoreboardserver.getDisplayObjective(displayslot);

            if (scoreboardobjective != null && !set.contains(scoreboardobjective)) {
                List<Packet<?>> list = scoreboardserver.getStartTrackingPackets(scoreboardobjective);
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    Packet<?> packet = (Packet) iterator1.next();

                    entityplayer.connection.send(packet);
                }

                set.add(scoreboardobjective);
            }
        }

    }

    public void addWorldborderListener(WorldServer worldserver) {
        worldserver.getWorldBorder().addListener(new IWorldBorderListener() {
            @Override
            public void onBorderSizeSet(WorldBorder worldborder, double d0) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket(worldborder));
            }

            @Override
            public void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket(worldborder));
            }

            @Override
            public void onBorderCenterSet(WorldBorder worldborder, double d0, double d1) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket(worldborder));
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder worldborder, int i) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldborder));
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder worldborder, int i) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldborder));
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0) {}

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0) {}
        });
    }

    public Optional<NBTTagCompound> load(EntityPlayer entityplayer) {
        NBTTagCompound nbttagcompound = this.server.getWorldData().getLoadedPlayerTag();
        Optional optional;

        if (this.server.isSingleplayerOwner(entityplayer.getGameProfile()) && nbttagcompound != null) {
            optional = Optional.of(nbttagcompound);
            entityplayer.load(nbttagcompound);
            PlayerList.LOGGER.debug("loading single player");
        } else {
            optional = this.playerIo.load(entityplayer);
        }

        return optional;
    }

    protected void save(EntityPlayer entityplayer) {
        this.playerIo.save(entityplayer);
        ServerStatisticManager serverstatisticmanager = (ServerStatisticManager) this.stats.get(entityplayer.getUUID());

        if (serverstatisticmanager != null) {
            serverstatisticmanager.save();
        }

        AdvancementDataPlayer advancementdataplayer = (AdvancementDataPlayer) this.advancements.get(entityplayer.getUUID());

        if (advancementdataplayer != null) {
            advancementdataplayer.save();
        }

    }

    public void remove(EntityPlayer entityplayer) {
        WorldServer worldserver = entityplayer.serverLevel();

        entityplayer.awardStat(StatisticList.LEAVE_GAME);
        this.save(entityplayer);
        if (entityplayer.isPassenger()) {
            Entity entity = entityplayer.getRootVehicle();

            if (entity.hasExactlyOnePlayerPassenger()) {
                PlayerList.LOGGER.debug("Removing player mount");
                entityplayer.stopRiding();
                entity.getPassengersAndSelf().forEach((entity1) -> {
                    entity1.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                });
            }
        }

        entityplayer.unRide();
        worldserver.removePlayerImmediately(entityplayer, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        entityplayer.getAdvancements().stopListening();
        this.players.remove(entityplayer);
        this.server.getCustomBossEvents().onPlayerDisconnect(entityplayer);
        UUID uuid = entityplayer.getUUID();
        EntityPlayer entityplayer1 = (EntityPlayer) this.playersByUUID.get(uuid);

        if (entityplayer1 == entityplayer) {
            this.playersByUUID.remove(uuid);
            this.stats.remove(uuid);
            this.advancements.remove(uuid);
        }

        this.broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(entityplayer.getUUID())));
    }

    @Nullable
    public IChatBaseComponent canPlayerLogin(SocketAddress socketaddress, GameProfile gameprofile) {
        IChatMutableComponent ichatmutablecomponent;

        if (this.bans.isBanned(gameprofile)) {
            GameProfileBanEntry gameprofilebanentry = (GameProfileBanEntry) this.bans.get(gameprofile);

            ichatmutablecomponent = IChatBaseComponent.translatable("multiplayer.disconnect.banned.reason", gameprofilebanentry.getReason());
            if (gameprofilebanentry.getExpires() != null) {
                ichatmutablecomponent.append((IChatBaseComponent) IChatBaseComponent.translatable("multiplayer.disconnect.banned.expiration", PlayerList.BAN_DATE_FORMAT.format(gameprofilebanentry.getExpires())));
            }

            return ichatmutablecomponent;
        } else if (!this.isWhiteListed(gameprofile)) {
            return IChatBaseComponent.translatable("multiplayer.disconnect.not_whitelisted");
        } else if (this.ipBans.isBanned(socketaddress)) {
            IpBanEntry ipbanentry = this.ipBans.get(socketaddress);

            ichatmutablecomponent = IChatBaseComponent.translatable("multiplayer.disconnect.banned_ip.reason", ipbanentry.getReason());
            if (ipbanentry.getExpires() != null) {
                ichatmutablecomponent.append((IChatBaseComponent) IChatBaseComponent.translatable("multiplayer.disconnect.banned_ip.expiration", PlayerList.BAN_DATE_FORMAT.format(ipbanentry.getExpires())));
            }

            return ichatmutablecomponent;
        } else {
            return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameprofile) ? IChatBaseComponent.translatable("multiplayer.disconnect.server_full") : null;
        }
    }

    public EntityPlayer getPlayerForLogin(GameProfile gameprofile, ClientInformation clientinformation) {
        return new EntityPlayer(this.server, this.server.overworld(), gameprofile, clientinformation);
    }

    public boolean disconnectAllPlayersWithProfile(GameProfile gameprofile) {
        UUID uuid = gameprofile.getId();
        Set<EntityPlayer> set = Sets.newIdentityHashSet();
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.getUUID().equals(uuid)) {
                set.add(entityplayer);
            }
        }

        EntityPlayer entityplayer1 = (EntityPlayer) this.playersByUUID.get(gameprofile.getId());

        if (entityplayer1 != null) {
            set.add(entityplayer1);
        }

        Iterator iterator1 = set.iterator();

        while (iterator1.hasNext()) {
            EntityPlayer entityplayer2 = (EntityPlayer) iterator1.next();

            entityplayer2.connection.disconnect(PlayerList.DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
        }

        return !set.isEmpty();
    }

    public EntityPlayer respawn(EntityPlayer entityplayer, boolean flag, Entity.RemovalReason entity_removalreason) {
        this.players.remove(entityplayer);
        entityplayer.serverLevel().removePlayerImmediately(entityplayer, entity_removalreason);
        DimensionTransition dimensiontransition = entityplayer.findRespawnPositionAndUseSpawnBlock(flag, DimensionTransition.DO_NOTHING);
        WorldServer worldserver = dimensiontransition.newLevel();
        EntityPlayer entityplayer1 = new EntityPlayer(this.server, worldserver, entityplayer.getGameProfile(), entityplayer.clientInformation());

        entityplayer1.connection = entityplayer.connection;
        entityplayer1.restoreFrom(entityplayer, flag);
        entityplayer1.setId(entityplayer.getId());
        entityplayer1.setMainArm(entityplayer.getMainArm());
        if (!dimensiontransition.missingRespawnBlock()) {
            entityplayer1.copyRespawnPosition(entityplayer);
        }

        Iterator iterator = entityplayer.getTags().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            entityplayer1.addTag(s);
        }

        Vec3D vec3d = dimensiontransition.pos();

        entityplayer1.moveTo(vec3d.x, vec3d.y, vec3d.z, dimensiontransition.yRot(), dimensiontransition.xRot());
        if (dimensiontransition.missingRespawnBlock()) {
            entityplayer1.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }

        int i = flag ? 1 : 0;
        WorldServer worldserver1 = entityplayer1.serverLevel();
        WorldData worlddata = worldserver1.getLevelData();

        entityplayer1.connection.send(new PacketPlayOutRespawn(entityplayer1.createCommonSpawnInfo(worldserver1), (byte) i));
        entityplayer1.connection.teleport(entityplayer1.getX(), entityplayer1.getY(), entityplayer1.getZ(), entityplayer1.getYRot(), entityplayer1.getXRot());
        entityplayer1.connection.send(new PacketPlayOutSpawnPosition(worldserver.getSharedSpawnPos(), worldserver.getSharedSpawnAngle()));
        entityplayer1.connection.send(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer1.connection.send(new PacketPlayOutExperience(entityplayer1.experienceProgress, entityplayer1.totalExperience, entityplayer1.experienceLevel));
        this.sendActivePlayerEffects(entityplayer1);
        this.sendLevelInfo(entityplayer1, worldserver);
        this.sendPlayerPermissionLevel(entityplayer1);
        worldserver.addRespawnedPlayer(entityplayer1);
        this.players.add(entityplayer1);
        this.playersByUUID.put(entityplayer1.getUUID(), entityplayer1);
        entityplayer1.initInventoryMenu();
        entityplayer1.setHealth(entityplayer1.getHealth());
        if (!flag) {
            BlockPosition blockposition = BlockPosition.containing(dimensiontransition.pos());
            IBlockData iblockdata = worldserver.getBlockState(blockposition);

            if (iblockdata.is(Blocks.RESPAWN_ANCHOR)) {
                entityplayer1.connection.send(new PacketPlayOutNamedSoundEffect(SoundEffects.RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 1.0F, 1.0F, worldserver.getRandom().nextLong()));
            }
        }

        return entityplayer1;
    }

    public void sendActivePlayerEffects(EntityPlayer entityplayer) {
        this.sendActiveEffects(entityplayer, entityplayer.connection);
    }

    public void sendActiveEffects(EntityLiving entityliving, PlayerConnection playerconnection) {
        Iterator iterator = entityliving.getActiveEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            playerconnection.send(new PacketPlayOutEntityEffect(entityliving.getId(), mobeffect, false));
        }

    }

    public void sendPlayerPermissionLevel(EntityPlayer entityplayer) {
        GameProfile gameprofile = entityplayer.getGameProfile();
        int i = this.server.getProfilePermissions(gameprofile);

        this.sendPlayerPermissionLevel(entityplayer, i);
    }

    public void tick() {
        if (++this.sendAllPlayerInfoIn > 600) {
            this.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.a.UPDATE_LATENCY), this.players));
            this.sendAllPlayerInfoIn = 0;
        }

    }

    public void broadcastAll(Packet<?> packet) {
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            entityplayer.connection.send(packet);
        }

    }

    public void broadcastAll(Packet<?> packet, ResourceKey<World> resourcekey) {
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.level().dimension() == resourcekey) {
                entityplayer.connection.send(packet);
            }
        }

    }

    public void broadcastSystemToTeam(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        ScoreboardTeam scoreboardteam = entityhuman.getTeam();

        if (scoreboardteam != null) {
            Collection<String> collection = scoreboardteam.getPlayers();
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();
                EntityPlayer entityplayer = this.getPlayerByName(s);

                if (entityplayer != null && entityplayer != entityhuman) {
                    entityplayer.sendSystemMessage(ichatbasecomponent);
                }
            }

        }
    }

    public void broadcastSystemToAllExceptTeam(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        ScoreboardTeam scoreboardteam = entityhuman.getTeam();

        if (scoreboardteam == null) {
            this.broadcastSystemMessage(ichatbasecomponent, false);
        } else {
            for (int i = 0; i < this.players.size(); ++i) {
                EntityPlayer entityplayer = (EntityPlayer) this.players.get(i);

                if (entityplayer.getTeam() != scoreboardteam) {
                    entityplayer.sendSystemMessage(ichatbasecomponent);
                }
            }

        }
    }

    public String[] getPlayerNamesArray() {
        String[] astring = new String[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i) {
            astring[i] = ((EntityPlayer) this.players.get(i)).getGameProfile().getName();
        }

        return astring;
    }

    public GameProfileBanList getBans() {
        return this.bans;
    }

    public IpBanList getIpBans() {
        return this.ipBans;
    }

    public void op(GameProfile gameprofile) {
        this.ops.add(new OpListEntry(gameprofile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(gameprofile)));
        EntityPlayer entityplayer = this.getPlayer(gameprofile.getId());

        if (entityplayer != null) {
            this.sendPlayerPermissionLevel(entityplayer);
        }

    }

    public void deop(GameProfile gameprofile) {
        this.ops.remove((Object) gameprofile);
        EntityPlayer entityplayer = this.getPlayer(gameprofile.getId());

        if (entityplayer != null) {
            this.sendPlayerPermissionLevel(entityplayer);
        }

    }

    private void sendPlayerPermissionLevel(EntityPlayer entityplayer, int i) {
        if (entityplayer.connection != null) {
            byte b0;

            if (i <= 0) {
                b0 = 24;
            } else if (i >= 4) {
                b0 = 28;
            } else {
                b0 = (byte) (24 + i);
            }

            entityplayer.connection.send(new PacketPlayOutEntityStatus(entityplayer, b0));
        }

        this.server.getCommands().sendCommands(entityplayer);
    }

    public boolean isWhiteListed(GameProfile gameprofile) {
        return !this.doWhiteList || this.ops.contains(gameprofile) || this.whitelist.contains(gameprofile);
    }

    public boolean isOp(GameProfile gameprofile) {
        return this.ops.contains(gameprofile) || this.server.isSingleplayerOwner(gameprofile) && this.server.getWorldData().isAllowCommands() || this.allowCommandsForAllPlayers;
    }

    @Nullable
    public EntityPlayer getPlayerByName(String s) {
        int i = this.players.size();

        for (int j = 0; j < i; ++j) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(j);

            if (entityplayer.getGameProfile().getName().equalsIgnoreCase(s)) {
                return entityplayer;
            }
        }

        return null;
    }

    public void broadcast(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, double d3, ResourceKey<World> resourcekey, Packet<?> packet) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(i);

            if (entityplayer != entityhuman && entityplayer.level().dimension() == resourcekey) {
                double d4 = d0 - entityplayer.getX();
                double d5 = d1 - entityplayer.getY();
                double d6 = d2 - entityplayer.getZ();

                if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
                    entityplayer.connection.send(packet);
                }
            }
        }

    }

    public void saveAll() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.save((EntityPlayer) this.players.get(i));
        }

    }

    public WhiteList getWhiteList() {
        return this.whitelist;
    }

    public String[] getWhiteListNames() {
        return this.whitelist.getUserList();
    }

    public OpList getOps() {
        return this.ops;
    }

    public String[] getOpNames() {
        return this.ops.getUserList();
    }

    public void reloadWhiteList() {}

    public void sendLevelInfo(EntityPlayer entityplayer, WorldServer worldserver) {
        WorldBorder worldborder = this.server.overworld().getWorldBorder();

        entityplayer.connection.send(new ClientboundInitializeBorderPacket(worldborder));
        entityplayer.connection.send(new PacketPlayOutUpdateTime(worldserver.getGameTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
        entityplayer.connection.send(new PacketPlayOutSpawnPosition(worldserver.getSharedSpawnPos(), worldserver.getSharedSpawnAngle()));
        if (worldserver.isRaining()) {
            entityplayer.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.START_RAINING, 0.0F));
            entityplayer.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.RAIN_LEVEL_CHANGE, worldserver.getRainLevel(1.0F)));
            entityplayer.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.THUNDER_LEVEL_CHANGE, worldserver.getThunderLevel(1.0F)));
        }

        entityplayer.connection.send(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.LEVEL_CHUNKS_LOAD_START, 0.0F));
        this.server.tickRateManager().updateJoiningPlayer(entityplayer);
    }

    public void sendAllPlayerInfo(EntityPlayer entityplayer) {
        entityplayer.inventoryMenu.sendAllDataToRemote();
        entityplayer.resetSentInfo();
        entityplayer.connection.send(new PacketPlayOutHeldItemSlot(entityplayer.getInventory().selected));
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isUsingWhitelist() {
        return this.doWhiteList;
    }

    public void setUsingWhiteList(boolean flag) {
        this.doWhiteList = flag;
    }

    public List<EntityPlayer> getPlayersWithAddress(String s) {
        List<EntityPlayer> list = Lists.newArrayList();
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.getIpAddress().equals(s)) {
                list.add(entityplayer);
            }
        }

        return list;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getSimulationDistance() {
        return this.simulationDistance;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    @Nullable
    public NBTTagCompound getSingleplayerData() {
        return null;
    }

    public void setAllowCommandsForAllPlayers(boolean flag) {
        this.allowCommandsForAllPlayers = flag;
    }

    public void removeAll() {
        for (int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayer) this.players.get(i)).connection.disconnect((IChatBaseComponent) IChatBaseComponent.translatable("multiplayer.disconnect.server_shutdown"));
        }

    }

    public void broadcastSystemMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.broadcastSystemMessage(ichatbasecomponent, (entityplayer) -> {
            return ichatbasecomponent;
        }, flag);
    }

    public void broadcastSystemMessage(IChatBaseComponent ichatbasecomponent, Function<EntityPlayer, IChatBaseComponent> function, boolean flag) {
        this.server.sendSystemMessage(ichatbasecomponent);
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();
            IChatBaseComponent ichatbasecomponent1 = (IChatBaseComponent) function.apply(entityplayer);

            if (ichatbasecomponent1 != null) {
                entityplayer.sendSystemMessage(ichatbasecomponent1, flag);
            }
        }

    }

    public void broadcastChatMessage(PlayerChatMessage playerchatmessage, CommandListenerWrapper commandlistenerwrapper, ChatMessageType.a chatmessagetype_a) {
        Objects.requireNonNull(commandlistenerwrapper);
        this.broadcastChatMessage(playerchatmessage, commandlistenerwrapper::shouldFilterMessageTo, commandlistenerwrapper.getPlayer(), chatmessagetype_a);
    }

    public void broadcastChatMessage(PlayerChatMessage playerchatmessage, EntityPlayer entityplayer, ChatMessageType.a chatmessagetype_a) {
        Objects.requireNonNull(entityplayer);
        this.broadcastChatMessage(playerchatmessage, entityplayer::shouldFilterMessageTo, entityplayer, chatmessagetype_a);
    }

    private void broadcastChatMessage(PlayerChatMessage playerchatmessage, Predicate<EntityPlayer> predicate, @Nullable EntityPlayer entityplayer, ChatMessageType.a chatmessagetype_a) {
        boolean flag = this.verifyChatTrusted(playerchatmessage);

        this.server.logChatMessage(playerchatmessage.decoratedContent(), chatmessagetype_a, flag ? null : "Not Secure");
        OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(playerchatmessage);
        boolean flag1 = false;

        boolean flag2;

        for (Iterator iterator = this.players.iterator(); iterator.hasNext(); flag1 |= flag2 && playerchatmessage.isFullyFiltered()) {
            EntityPlayer entityplayer1 = (EntityPlayer) iterator.next();

            flag2 = predicate.test(entityplayer1);
            entityplayer1.sendChatMessage(outgoingchatmessage, flag2, chatmessagetype_a);
        }

        if (flag1 && entityplayer != null) {
            entityplayer.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }

    }

    private boolean verifyChatTrusted(PlayerChatMessage playerchatmessage) {
        return playerchatmessage.hasSignature() && !playerchatmessage.hasExpiredServer(Instant.now());
    }

    public ServerStatisticManager getPlayerStats(EntityHuman entityhuman) {
        UUID uuid = entityhuman.getUUID();
        ServerStatisticManager serverstatisticmanager = (ServerStatisticManager) this.stats.get(uuid);

        if (serverstatisticmanager == null) {
            File file = this.server.getWorldPath(SavedFile.PLAYER_STATS_DIR).toFile();
            File file1 = new File(file, String.valueOf(uuid) + ".json");

            if (!file1.exists()) {
                File file2 = new File(file, entityhuman.getName().getString() + ".json");
                Path path = file2.toPath();

                if (FileUtils.isPathNormalized(path) && FileUtils.isPathPortable(path) && path.startsWith(file.getPath()) && file2.isFile()) {
                    file2.renameTo(file1);
                }
            }

            serverstatisticmanager = new ServerStatisticManager(this.server, file1);
            this.stats.put(uuid, serverstatisticmanager);
        }

        return serverstatisticmanager;
    }

    public AdvancementDataPlayer getPlayerAdvancements(EntityPlayer entityplayer) {
        UUID uuid = entityplayer.getUUID();
        AdvancementDataPlayer advancementdataplayer = (AdvancementDataPlayer) this.advancements.get(uuid);

        if (advancementdataplayer == null) {
            Path path = this.server.getWorldPath(SavedFile.PLAYER_ADVANCEMENTS_DIR).resolve(String.valueOf(uuid) + ".json");

            advancementdataplayer = new AdvancementDataPlayer(this.server.getFixerUpper(), this, this.server.getAdvancements(), path, entityplayer);
            this.advancements.put(uuid, advancementdataplayer);
        }

        advancementdataplayer.setPlayer(entityplayer);
        return advancementdataplayer;
    }

    public void setViewDistance(int i) {
        this.viewDistance = i;
        this.broadcastAll(new PacketPlayOutViewDistance(i));
        Iterator iterator = this.server.getAllLevels().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (worldserver != null) {
                worldserver.getChunkSource().setViewDistance(i);
            }
        }

    }

    public void setSimulationDistance(int i) {
        this.simulationDistance = i;
        this.broadcastAll(new ClientboundSetSimulationDistancePacket(i));
        Iterator iterator = this.server.getAllLevels().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (worldserver != null) {
                worldserver.getChunkSource().setSimulationDistance(i);
            }
        }

    }

    public List<EntityPlayer> getPlayers() {
        return this.players;
    }

    @Nullable
    public EntityPlayer getPlayer(UUID uuid) {
        return (EntityPlayer) this.playersByUUID.get(uuid);
    }

    public boolean canBypassPlayerLimit(GameProfile gameprofile) {
        return false;
    }

    public void reloadResources() {
        Iterator iterator = this.advancements.values().iterator();

        while (iterator.hasNext()) {
            AdvancementDataPlayer advancementdataplayer = (AdvancementDataPlayer) iterator.next();

            advancementdataplayer.reload(this.server.getAdvancements());
        }

        this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
        PacketPlayOutRecipeUpdate packetplayoutrecipeupdate = new PacketPlayOutRecipeUpdate(this.server.getRecipeManager().getOrderedRecipes());
        Iterator iterator1 = this.players.iterator();

        while (iterator1.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator1.next();

            entityplayer.connection.send(packetplayoutrecipeupdate);
            entityplayer.getRecipeBook().sendInitialRecipeBook(entityplayer);
        }

    }

    public boolean isAllowCommandsForAllPlayers() {
        return this.allowCommandsForAllPlayers;
    }
}
