package me.wobbychip.smptweaks.utils;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ReflectionUtils {
	public static EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS;
	public static int LIVING_ENTITY_FLAG_IS_USING = 1;
	public static DefaultedRegistry<Potion> POTION = BuiltInRegistries.POTION;
	public static String version;
	public static Class<?> CraftServer;
	public static Class<?> CraftEntity;
	public static Class<?> CraftHumanEntity;
	public static Class<?> CraftPlayer;
	public static Class<?> CraftVillager;
	public static Class<?> CraftInventory;
	public static Class<?> CraftItemStack;
	public static Class<?> CraftWorld;
	public static Class<?> CraftMagicNumbers;

	public static Field Entity_bukkitEntity;
	public static Field EntityPlayer_playerConnection;
	public static Field EntityPlayer_chatVisibility;
	public static Field MinecraftServer_levels;
	public static Field CustomFunctionData_ticking;
	public static Field CustomFunctionData_postReload;
	public static Field RegistryMaterials_frozen;
	public static Field RegistryMaterials_nextId;
	public static Field BlockPhysicsEvent_changed;
	public static Field PotionSplashEvent_affectedEntities;
	public static Field AreaEffectCloudApplyEvent_affectedEntities;
	public static Field MinecraftServer_registries;
	public static Field Level_dimensionTypeRegistration;
	public static Field Level_dimensionTypeId;
	public static Field Holder_owner;
	public static Field Holder_tags;
	public static Field Holder_key;
	public static Field Holder_value;
	public static Method EntityVillager_startTrading_Or_updateSpecialPrices;
	public static Method IRegistry_keySet;
	public static Method Potions_register;
	public static Method RegistryMaterials_getHolder;
	public static Method PotionBrewer_register;

	static {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		CraftServer = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".CraftServer", true));
		CraftEntity = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftEntity", true));
		CraftHumanEntity = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity", true));
		CraftPlayer = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer", true));
		CraftVillager = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftVillager", true));
		CraftInventory = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftInventory", true));
		CraftItemStack = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack", true));
		CraftWorld = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".CraftWorld", true));
		CraftMagicNumbers = Objects.requireNonNull(loadClass("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers", true));

		//Fuck it I am not interested in updating nms every time
		//So I will just search fields and methods by their types and arguments

		DATA_LIVING_ENTITY_FLAGS = (EntityDataAccessor<Byte>) Objects.requireNonNull(getValue(getField(LivingEntity.class, EntityDataAccessor.class, Byte.class, true), null));
		setRegistryMap(POTION, new HashMap<>());

		MinecraftServer_levels = Objects.requireNonNull(getField(MinecraftServer.class, Map.class, null, true));
		CustomFunctionData_ticking = Objects.requireNonNull(getField(ServerFunctionManager.class, List.class, null, true));
		CustomFunctionData_postReload = Objects.requireNonNull(getField(ServerFunctionManager.class, boolean.class, null, true));

		BlockPhysicsEvent_changed = Objects.requireNonNull(getField(BlockPhysicsEvent.class, BlockData.class, null, true));
		PotionSplashEvent_affectedEntities = Objects.requireNonNull(getField(PotionSplashEvent.class, Map.class, null, true));
		AreaEffectCloudApplyEvent_affectedEntities = Objects.requireNonNull(getField(AreaEffectCloudApplyEvent.class, List.class, null, true));

		Entity_bukkitEntity = Objects.requireNonNull(getField(net.minecraft.world.entity.Entity.class, CraftEntity, null, true));
		EntityPlayer_playerConnection = Objects.requireNonNull(getField(ServerPlayer.class, ServerGamePacketListenerImpl.class, null, true));
		EntityPlayer_chatVisibility = Objects.requireNonNull(getField(ServerPlayer.class, ChatVisiblity.class, null, true));
		RegistryMaterials_frozen = Objects.requireNonNull(getField(MappedRegistry.class, boolean.class, null, true));
		RegistryMaterials_nextId = Objects.requireNonNull(getField(MappedRegistry.class, int.class, null, true));
		EntityVillager_startTrading_Or_updateSpecialPrices = Objects.requireNonNull(findMethod(false, Modifier.PRIVATE, net.minecraft.world.entity.npc.Villager.class, Void.TYPE, null, net.minecraft.world.entity.player.Player.class));
		IRegistry_keySet = Objects.requireNonNull(findMethod(true, null, Registry.class, Set.class, ResourceLocation.class));
		Potions_register = Objects.requireNonNull(findMethod(false, Modifier.PRIVATE, Potions.class, Potion.class, null, String.class, Potion.class));
		RegistryMaterials_getHolder = Objects.requireNonNull(findMethod(true, null, Registry.class, Optional.class, null, int.class));
		PotionBrewer_register = Objects.requireNonNull(findMethod(false, null, PotionBrewing.class, Void.TYPE, null, Potion.class, Item.class, Potion.class));
		MinecraftServer_registries = Objects.requireNonNull(getField(MinecraftServer.class, LayeredRegistryAccess.class, RegistryLayer.class, true));
		Level_dimensionTypeRegistration = Objects.requireNonNull(getField(Level.class, Holder.class, DimensionType.class, true));
		Level_dimensionTypeId = Objects.requireNonNull(getField(Level.class, ResourceKey.class, DimensionType.class, true));
		Holder_owner = Objects.requireNonNull(getField(Holder.Reference.class, HolderOwner.class, null, true));
		Holder_tags = Objects.requireNonNull(getField(Holder.Reference.class, Set.class, null, true));
		Holder_key = Objects.requireNonNull(getField(Holder.Reference.class, ResourceKey.class, null, true));
		Holder_value = Objects.requireNonNull(getField(Holder.Reference.class, Object.class, null, true));
	}

	public static Class<?> loadClass(String arg0, boolean verbose) {
		try {
			return Class.forName(arg0);
		} catch (ClassNotFoundException e) {
			if (verbose) { e.printStackTrace(); }
			return null;
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
		try {
			Method method = clazz.getDeclaredMethod(name, parameters);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	//isAccessible - Is method accessible, if not use getDeclaredMethods() and then make method accessible
	//modifier - check modifier of methods, if null then ignored
	//clazz - Class in which search
	//rType - Return type, if null then any is ok
	//gType - Generic type of return type, if null then ignored
	//parameters - Method argument classes

	public static Method findMethod(boolean isAccessible, Integer modifier, Class<?> clazz, Class<?> rType, Class<?> gType, Class<?>... parameters) {
		for (Method method : !isAccessible ? clazz.getDeclaredMethods() : clazz.getMethods()) {
			if (method.getParameterCount() != parameters.length) { continue; }
			if ((rType != null) && !method.getReturnType().equals(rType)) { continue; }

			if ((modifier != null) && ((modifier == Modifier.STATIC) && !Modifier.isStatic(method.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PUBLIC) && !Modifier.isPublic(method.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PRIVATE) && !Modifier.isPrivate(method.getModifiers()))) { continue; }
			if ((modifier != null) && ((modifier == Modifier.PROTECTED) && !Modifier.isProtected(method.getModifiers()))) { continue; }

			if (gType != null) {
				Type genericType = method.getGenericReturnType();
				if (!(genericType instanceof ParameterizedType)) { continue; }
				Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
				if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
				if (!argTypes[0].equals(gType)) { continue; }
			}

			boolean match = true;
			for (int i = 0; i < parameters.length; i++) {
				match = method.getParameterTypes()[i].equals(parameters[i]);
				if (!match) { break; }
			}

			if (!match) { continue; }
			if (!isAccessible) { method.setAccessible(true); }
			return method;
		}

		return null;
	}

	public static Field getField(Class<?> clazz, Class<?> fType, Class<?> gType, boolean isPrivate) {
		return getField(clazz, fType, gType, null, null, isPrivate);
	}

	public static Field getField(Class<?> clazz, Class<?> fType, Class<?> gType, Object parent, Object value, boolean isPrivate) {
		for (Field field : isPrivate ? clazz.getDeclaredFields() : clazz.getFields()) {
			if (!field.getType().equals(fType)) { continue; }

			if (gType != null) {
				Type genericType = field.getGenericType();
				if (!(genericType instanceof ParameterizedType)) { continue; }
				Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
				if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
				if (!argTypes[0].equals(gType)) { continue; }
			}

			if ((parent != null) && (value != null)) {
				Object pvalue = getValue(field, parent);
				if ((pvalue == null) || !pvalue.equals(value)) { continue; }
			}

			field.setAccessible(true);
			return field;
		}

		return null;
	}

	public static Object getValue(Field field, Object obj) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setValue(Field field, Object target, Object obj) {
		try {
			field.setAccessible(true);
			field.set(target, obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static Object newInstance(boolean verbose, boolean isPrivate, Class<?> clazz, Class<?>[] parameters, Object[] args) {
		try {
			Constructor<?> constructor = isPrivate ? clazz.getDeclaredConstructor(parameters) : clazz.getConstructor(parameters);
			return constructor.newInstance(args);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			if (verbose) { e.printStackTrace(); }
			return null;
		}
	}

	public static net.minecraft.server.level.ServerPlayer getEntityPlayer(Player player) {
		try {
			Object craftPlayer = CraftPlayer.cast(player);
			return (net.minecraft.server.level.ServerPlayer) player.getClass().getDeclaredMethod("getHandle").invoke(craftPlayer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ServerLevel getWorld(World world) {
		try {
			Object craftWorld = CraftWorld.cast(world);
			return (ServerLevel) world.getClass().getDeclaredMethod("getHandle").invoke(craftWorld);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.player.Player getEntityHuman(HumanEntity humanEntity) {
		try {
			Object craftHumanEntity = CraftHumanEntity.cast(humanEntity);
			return (net.minecraft.world.entity.player.Player) humanEntity.getClass().getDeclaredMethod("getHandle").invoke(craftHumanEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.npc.Villager getEntityVillager(Villager villager) {
		try {
			Object craftEntity = CraftVillager.cast(villager);
			return (net.minecraft.world.entity.npc.Villager) villager.getClass().getDeclaredMethod("getHandle").invoke(craftEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.Entity getEntity(Entity entity) {
		try {
			Object craftEntity = CraftEntity.cast(entity);
			return (net.minecraft.world.entity.Entity) entity.getClass().getDeclaredMethod("getHandle").invoke(craftEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static org.bukkit.entity.Entity getBukkitEntity(net.minecraft.world.entity.Entity entity) {
		try {
			Object bukkitEntity = Entity_bukkitEntity.get(entity);

			if (bukkitEntity == null) {
				Method method = CraftEntity.getDeclaredMethod("getEntity", CraftServer, net.minecraft.world.entity.Entity.class);
				bukkitEntity = method.invoke(null, CraftServer.cast(Bukkit.getServer()), entity);
				Entity_bukkitEntity.set(entity, bukkitEntity);
			}

			return (org.bukkit.entity.Entity) bukkitEntity;
		} catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.item.ItemStack asNMSCopy(ItemStack itemStack) {
		try {
			Method method = CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
			return (net.minecraft.world.item.ItemStack) method.invoke(method, itemStack);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ItemStack asBukkitCopy(net.minecraft.world.item.ItemStack itemStack) {
		try {
			Method method = CraftItemStack.getDeclaredMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
			return (ItemStack) method.invoke(method, itemStack);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ItemStack asBukkitMirror(net.minecraft.world.item.ItemStack itemStack) {
		try {
			Method method = CraftItemStack.getDeclaredMethod("asCraftMirror", net.minecraft.world.item.ItemStack.class);
			return (ItemStack) method.invoke(method, itemStack);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public static Item getItem(ItemStack itemStack) {
		try {
			Method method = CraftMagicNumbers.getDeclaredMethod("getItem", Material.class, short.class);
			return (Item) method.invoke(method, itemStack.getType(), itemStack.getDurability());
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Block getBlock(Material block) {
		if (!block.isBlock()) { return null; }
		return ((BlockItem) getItem(new ItemStack(block))).getBlock();
	}

	public static int getBlockId(Material block) {
		return Block.getId(getBlock(block).defaultBlockState());
	}

	public static World getRespawnWorld(Player player) {
		return (World) getServer().getLevel(getEntityPlayer(player).getRespawnDimension()).getWorld();
	}

	//Get block destroy time per tick which is based on player
	public static float getPlayerDestroyTime(Player player, Material block) {
		return getEntityHuman(player).getDestroySpeed(getBlock(block).defaultBlockState());
	}

	//Get raw block destroy time which is not based on player
	public static float getBlockDestroyTime(Material block) {
		return getBlock(block).defaultBlockState().destroySpeed;
	}

	public static void popResource(Location location, ItemStack item) {
		try {
			BlockPos position = new BlockPos((int) location.getX(), (int) location.getY(), (int) location.getZ());
			Block.popResource(getWorld(location.getWorld()), position, asNMSCopy(item));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public static void setInvulnerableTicks(Player player, int ticks) {
		getEntityPlayer(player).spawnInvulnerableTime = ticks;
	}

	public static Abilities getPlayerAbilities(Player player) {
		return getEntityPlayer(player).getAbilities();
	}

	public static void sendPacket(Player player, Packet<?> packet) {
		getEntityPlayer(player).connection.send(packet);
	}

	public static void handlePacket(Player player, Packet<?> packet) {
		try {
			Object connection = EntityPlayer_playerConnection.get(getEntityPlayer(player));
			Method handle = findMethod(true, null, connection.getClass(), Void.TYPE, null, packet.getClass());
			handle.invoke(connection, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static MinecraftServer getServer() {
		return MinecraftServer.getServer();
	}

	public static Collection<Channel> getConnections() {
		HashMap<Connection, Channel> connections = new HashMap<>();
		ServerConnectionListener serverConnection = getServer().getConnection();

		for (Connection manager : serverConnection.getConnections()) {
			connections.put(manager, manager.channel);
		}

		return connections.values();
	}

	//Doesn't work in creative, it sets an actual item
	public static void setGhostItem(Player player, ItemStack item, int slot) {
		if (slot < 9) {
			slot += 36;
		} else if (slot > 39) {
			slot += 5;
		} else if (slot > 35) {
			slot = 8 - (slot - 36);
		}

		sendPacket(player, new ClientboundContainerSetSlotPacket(0, 0, slot, asNMSCopy(item)));
	}

	public static ServerFunctionManager getFunctionManager() {
		return MinecraftServer.getServer().getFunctions();
	}

	public static void setInstantBuild(Player player, boolean instantbuild, boolean clientSide, boolean serverSide) {
		Abilities abilities = getPlayerAbilities(player);

		if (clientSide) {
			boolean temp = abilities.instabuild;
			abilities.instabuild = instantbuild;
			sendPacket(player, new ClientboundPlayerAbilitiesPacket(abilities));
			abilities.instabuild = temp;
		}

		if (serverSide) {
			abilities.instabuild = instantbuild;
		}
	}

	public static void setChatVisibility(Player player, String visibility) {
		try {
			for (ChatVisiblity chat : ChatVisiblity.values()) {
				if (!chat.getKey().equalsIgnoreCase(visibility)) { continue; }
				EntityPlayer_chatVisibility.set(getEntityPlayer(player), chat);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public static String getChatVisibility(Player player) {
		return getEntityPlayer(player).getChatVisibility().getKey();
	}

	//The most useless shit I ever made
	public static boolean isUsingItem(Player player) {
		Byte entityFlags = (Byte) getEntityPlayer(player).getEntityData().get(DATA_LIVING_ENTITY_FLAGS);
		return (entityFlags & LIVING_ENTITY_FLAG_IS_USING) > 0;
	}

	public static void shootBow(Player player, ItemStack bow, int ticks) {
		net.minecraft.world.item.ItemStack item = asNMSCopy(bow);
		ServerLevel world = getWorld(player.getWorld());
		ServerPlayer entityPlayer = getEntityPlayer(player);
		item.releaseUsing(world, entityPlayer, (72000 - ticks));
	}

	public static Player addFakePlayer(Location location, UUID uuid, boolean addPlayer, boolean hideOnline, boolean hideWorld) {
		if (uuid == null) { uuid = UUID.randomUUID(); }

		MinecraftServer server = getServer();
		ServerLevel world = (ServerLevel) getWorld(location.getWorld());
		ServerPlayer entityPlayer = new ServerPlayer(server, world, new GameProfile(uuid, " ".repeat(5)), ClientInformation.createDefault());

		Connection networkManager = new Connection(PacketFlow.SERVERBOUND);
		EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ChannelHandler[] { networkManager });
		embeddedChannel.attr(Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(ConnectionProtocol.PLAY.codec(PacketFlow.SERVERBOUND));

		CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(new GameProfile(uuid, " ".repeat(5)));
		ServerGamePacketListenerImpl connection = new ServerGamePacketListenerImpl(server, networkManager, entityPlayer, commonListenerCookie) {};
		Player player = (Player) getBukkitEntity(entityPlayer);

		entityPlayer.connection = connection;
		if (addPlayer) { world.addNewPlayer(entityPlayer); }

		//Will hide from Bukkit.getOnlinePlayers()
		if (addPlayer && hideOnline) {
			List<ServerPlayer> players = server.getPlayerList().players;
			players.removeIf(e -> ((Player) getBukkitEntity(e)).getUniqueId().equals(player.getUniqueId()));
		}

		//Will hide from World.getPlayers(), but also will prevent mob spawning
		if (addPlayer && hideWorld) {
			world.players().removeIf(e -> ((Player) getBukkitEntity(e)).getUniqueId().equals(player.getUniqueId()));
		}

		player.setSleepingIgnored(true);
		return player;
	}

	public static void removeFakePlayer(Player player) {
		ServerPlayer entityPlayer = getEntityPlayer(player);
		ServerLevel world = (ServerLevel) getWorld(player.getWorld());
		world.removePlayerImmediately(entityPlayer, RemovalReason.DISCARDED);
	}

	//This needed to update player chunks and make them tickable
	public static void updateFakePlayer(Player player) {
		ServerLevel world = (ServerLevel) getWorld(player.getWorld());
		world.getChunkSource().move(getEntityPlayer(player));
	}

	//Open trading screen of a villager
	public static void startTrading(Player player, Villager villager) {
		try {
			net.minecraft.world.entity.npc.Villager entityVillager = getEntityVillager(villager);
			ServerPlayer entityPlayer = getEntityPlayer(player);
			EntityVillager_startTrading_Or_updateSpecialPrices.invoke(entityVillager, entityPlayer);
			player.openMerchant(villager, false);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void selectTrade(Player player, int slot) {
		handlePacket(player, new ServerboundSelectTradePacket(slot));
	}

	//Simulate shift+click on specific slot on opened inventory
	public static void quickMoveStack(Player player, int slot) {
		ServerPlayer entityPlayer = getEntityPlayer(player);
		entityPlayer.containerMenu.quickMoveStack(entityPlayer, slot);
	}

	//Gets player's reputation from villager (all types)
	public static int getPlayerReputation(Villager villager, Player player) {
		return getEntityVillager(villager).getPlayerReputation(getEntityPlayer(player));
	}

	public static boolean registerBrewRecipe(Object base, Material ingredient, Object result) {
		return registerBrewRecipe((Potion) base, ingredient, (Potion) result);
	}

	public static boolean registerBrewRecipe(Potion base, Material ingredient, Potion result) {
		try {
			Item potionIngredient = getItem(new ItemStack(ingredient));
			PotionBrewer_register.invoke(PotionBrewer_register, base, potionIngredient, result);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Potion getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		if (potionType == PotionType.UNCRAFTABLE) { return null; }

		ItemStack item = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
		item.setItemMeta(potionMeta);

		net.minecraft.world.item.ItemStack nmsItem = asNMSCopy(item);
		if (nmsItem == null) { return null; }
		return PotionUtils.getPotion(nmsItem);
	}

	public static String getPotionRegistryName(Object potion) {
		return getPotionRegistryName((Potion) potion);
	}

	public static String getPotionRegistryName(Potion potion) {
		return potion.getName("");
	}

	public static String getPotionTag(ItemStack item) {
		CompoundTag tag = asNMSCopy(item).getTag();
		return (tag != null) ? tag.getString("Potion").replace("minecraft:", "") : "";
	}

	public static ItemStack setPotionTag(ItemStack item, String name) {
		net.minecraft.world.item.ItemStack nmsItem = asNMSCopy(item);
		CompoundTag tag = (CompoundTag) nmsItem.getOrCreateTag();
		tag.putString("Potion", name);
		nmsItem.setTag(tag);
		return asBukkitMirror(nmsItem);
	}

	public static void setRegistryFrozen(Object registry, boolean frozen) {
		try {
			RegistryMaterials_frozen.set(registry, frozen);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void setRegistryMap(Object registry, Object hashMap) {
		Field[] fields = MappedRegistry.class.getDeclaredFields();

		for (Field field : fields) {
			if (!field.getType().equals(Map.class)) { continue; }
			if (getValue(field, POTION) != null) { continue; }
			setValue(field, POTION, hashMap); break;
		}
	}

	public static int getRegistrySize(Registry<?> registry) {
		try {
			return ((Set<ResourceLocation>) IRegistry_keySet.invoke(registry)).size();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static Potion registerInstantPotion(String name) {
		try {
			setRegistryFrozen(POTION, false);
			Potion potionRegistry = new Potion(new MobEffectInstance[0]);
			potionRegistry = (Potion) Potions_register.invoke(Potions_register, name, potionRegistry);
			setRegistryFrozen(POTION, true);
			fixHolder(POTION, potionRegistry);
			return potionRegistry;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	//FUCK MOJANG DEVELOPERS, how the fuck do you forget to set value of a holder
	//when registering a new mapping, how brain-damaged you must be.
	public static void fixHolder(Object registry, Object value) {
		try {
			int currentId = RegistryMaterials_nextId.getInt(registry)-1;
			Optional<Holder<?>> holder = (Optional<Holder<?>>) RegistryMaterials_getHolder.invoke(registry, currentId);
			Holder.Reference<?> holder_c = (Holder.Reference<?>) holder.get();

			for (Field field : Holder.Reference.class.getDeclaredFields()) {
				field.setAccessible(true);
				if (field.get(holder_c) == null) { field.set(holder_c, value); }
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	//MIX_ID is required to prevent weird visual bug when client side and server side breaking are overlapping
	//Making two animations replace each other at the same time
	public static void destroyBlockProgress(org.bukkit.block.Block block, int progress, int mix_id) {
		for (Player player : block.getWorld().getPlayers()) {
			double d0 = (double) block.getX() - player.getLocation().getX();
			double d1 = (double) block.getY() - player.getLocation().getY();
			double d2 = (double) block.getZ() - player.getLocation().getZ();

			if (d0 * d0 + d1 * d1 + d2 * d2 >= 1024.0D) { continue; }
			BlockPos location = new BlockPos(block.getX(), block.getY(), block.getZ());
			sendPacket(player, new ClientboundBlockDestructionPacket(player.getEntityId()+mix_id, location, progress));
		}
	}

	public static void destroyBlock(Player player, org.bukkit.block.Block block) {
		destroyBlockProgress(block, -1, 0);
		BlockPos location = new BlockPos(block.getX(), block.getY(), block.getZ());
		int id = getBlockId(block.getType());
		sendPacket(player, new ClientboundLevelEventPacket(2001, location, id, false));
		player.breakBlock(block);
	}

	public static void dropBlockItem(org.bukkit.block.Block block, @Nullable Player player, ItemStack itemStack) {
		double x = block.getLocation().getX() + 0.5;
		double y = block.getLocation().getY() + 0.5;
		double z = block.getLocation().getZ() + 0.5;

		double f = (Utils.ITEM_HEIGHT / 2.0F);
		double d0 = x + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET);
		double d1 = y + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET) - f;
		double d2 = z + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET);

		ItemEntity entityItem = new ItemEntity(getWorld(block.getWorld()), d0, d1, d2, asNMSCopy(itemStack));
		getBukkitEntity(entityItem).setVelocity(new Vector(Math.random()*0.2F-0.1F, 0.2F, Math.random()*0.2F-0.1F));
		List<org.bukkit.entity.Item> items = new ArrayList<>(Arrays.asList((org.bukkit.entity.Item) getBukkitEntity(entityItem)));

		if (player != null) {
			BlockDropItemEvent dropEvent = new BlockDropItemEvent(block, block.getState(), player, items);
			Bukkit.getServer().getPluginManager().callEvent(dropEvent);
			if (dropEvent.isCancelled()) { return; } else { items = dropEvent.getItems(); }
		}

		for (org.bukkit.entity.Item drop : items) {
			block.getWorld().spawn(drop.getLocation(), org.bukkit.entity.Item.class, (item) -> {
				item.setItemStack(drop.getItemStack());
				item.setVelocity(drop.getVelocity());
			});
		}
	}

	public static Object getSetLevels(Object data) throws IllegalAccessException {
		Object levels = MinecraftServer_levels.get(MinecraftServer.getServer());
		MinecraftServer_levels.set(MinecraftServer.getServer(), data);
		return levels;
	}

	public static Object getSetCustomFunctionDataTicking(Object data) throws IllegalAccessException {
		Object functionManager = MinecraftServer.getServer().getFunctions();
		Object ticking = CustomFunctionData_ticking.get(functionManager);
		CustomFunctionData_ticking.set(functionManager, data);
		return ticking;
	}

	public static Object getSetCustomFunctionPostReload(Object data) throws IllegalAccessException {
		Object functionManager = MinecraftServer.getServer().getFunctions();
		Object postReload = CustomFunctionData_postReload.get(functionManager);
		CustomFunctionData_postReload.set(functionManager, data);
		return postReload;
	}

	public static <T> T getItemNbt(ItemStack itemStack, List<String> location) {
		if (location.size() == 0) { return null; }
		net.minecraft.world.item.ItemStack item = asNMSCopy(itemStack);
		if (!item.hasTag()) { return null; }
		CompoundTag tag = item.getTag();

		while ((location.size() > 0) && (tag.getTagType(location.get(0)) == Tag.TAG_COMPOUND)) {
			tag.getCompound(location.get(0));
			location.remove(0);
		}

		if (location.size() == 0) { return (T) tag; }
		if (tag.getTagType(location.get(0)) == Tag.TAG_BYTE) { return (T) Boolean.valueOf(tag.getBoolean(location.get(0))); }
		if (tag.getTagType(location.get(0)) == Tag.TAG_INT) { return (T) Integer.valueOf(tag.getInt(location.get(0))); }
		if (tag.getTagType(location.get(0)) == Tag.TAG_STRING) { return (T) tag.getString(location.get(0)); }

		return null;
	}

	public static void setBlockNbt(org.bukkit.block.Block block, String name, Object value, boolean applyPhysics) {
		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());

		if (serverLevel == null) { return; }
		BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
		if (blockEntity == null) { return; }
		CompoundTag tag = blockEntity.saveWithoutMetadata();

		tag.remove(name);
		if (value.getClass().equals(Boolean.class)) { tag.putBoolean(name, (Boolean) value); }
		if (value.getClass().equals(Integer.class)) { tag.putInt(name, (Integer) value); }
		if (value.getClass().equals(String.class)) { tag.putString(name, (String) value); }

		blockEntity.load(tag); //Loading NBT doesn't trigger physics update of block itself
		if (applyPhysics) { blockEntity.setChanged(); } //This only updates block around (AND IT DOESN'T WORK, FUCK YOU MOJANG)
		if (applyPhysics) { NeighborUpdater.executeUpdate(serverLevel, blockEntity.getBlockState(), blockPos, blockEntity.getBlockState().getBlock(), blockPos, true); }
	}

	public static <T> T getBlockNbt(org.bukkit.block.Block block, String name) {
		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());

		if (serverLevel == null) { return null; }
		BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
		if (blockEntity == null) { return null; }
		CompoundTag tag = blockEntity.saveWithoutMetadata();

		if (tag.getTagType(name) == Tag.TAG_BYTE) { return (T) Boolean.valueOf(tag.getBoolean(name)); }
		if (tag.getTagType(name) == Tag.TAG_INT) { return (T) Integer.valueOf(tag.getInt(name)); }
		if (tag.getTagType(name) == Tag.TAG_STRING) { return (T) tag.getString(name); }

		return null;
	}

	public static void forceUpdateBlock(org.bukkit.block.Block block) {
		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);
		NeighborUpdater.executeUpdate(serverLevel, blockState, blockPos, blockState.getBlock(), blockPos, true);
	}

	public static void forceUpdateNeighbors(org.bukkit.block.Block block, int distance, @Nullable Material type, @Nullable Material exclude) {
		for (org.bukkit.block.Block nblock: Utils.getNearbyBlocks(block.getLocation(), type, distance)) {
			if ((exclude != null) && (nblock.getType() == exclude)) { continue; }
			if (!nblock.getLocation().equals(block.getLocation())) { forceUpdateBlock(nblock); }
		}
	}

	public static void updateNeighborsInFront(org.bukkit.block.Block block) {
		BlockPos block_pos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel block_world = getWorld(block.getWorld());
		BlockState block_sate = block_world.getBlockState(block_pos);
		net.minecraft.world.level.block.ComparatorBlock block_nms = (net.minecraft.world.level.block.ComparatorBlock) block_sate.getBlock();

		Direction enumdirection = block_sate.getValue(DiodeBlock.FACING);
		BlockPos blockposition1 = block_pos.relative(enumdirection.getOpposite());

		block_world.neighborChanged(blockposition1, block_nms, block_pos);
		block_world.updateNeighborsAtExceptFromFacing(blockposition1, block_nms, enumdirection);
	}

	public static int getComparatorOutputSignal(org.bukkit.block.Block block, @Nullable BlockData blockData, int power) {
		int j = getAlternateSignal(block);
		if (blockData == null) { blockData = block.getBlockData(); }
		return ((((Comparator) blockData).getMode() == Comparator.Mode.SUBTRACT)) ? Math.max(power-j, 0) : power;
	}

	//This is useless and will only set power for 1 tick, because in next tick it will calculate power signal again
	public static void setComparatorPower(org.bukkit.block.Block block, int power, boolean applyPhysics) {
		org.bukkit.block.data.type.Comparator comparator = ((Comparator) block.getBlockData());
		comparator.setPowered(power > 0);
		block.setBlockData(comparator, false);
		setBlockNbt(block, "OutputSignal", power, applyPhysics);
	}

	public static boolean isRedstoneConductor(org.bukkit.block.Block block) {
		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);
		return blockState.isRedstoneConductor(serverLevel, blockPos);
	}

	//This is used to get signal that is going inside comparator
	public static int getAlternateSignal(org.bukkit.block.Block block) {
		if (block.getType() != Material.COMPARATOR) { return 0; }

		BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
		BlockState state = blockEntity.getBlockState();
		SignalGetter world = blockEntity.getLevel();

		Direction enumdirection = state.getValue(DiodeBlock.FACING);
		Direction enumdirection1 = enumdirection.getClockWise();
		Direction enumdirection2 = enumdirection.getCounterClockWise();
		boolean flag = false;

		return Math.max(world.getControlInputSignal(pos.relative(enumdirection1), enumdirection1, flag), world.getControlInputSignal(pos.relative(enumdirection2), enumdirection2, flag));
	}

	public static void unpackLoot(org.bukkit.block.Block block) {
		if (!(block.getState() instanceof org.bukkit.block.Container)) { return; }
		if (getBlockNbt(block, "LootTable") == null) { return; }

		ServerLevel serverLevel = getWorld(block.getLocation().getWorld());
		if (serverLevel == null) { return; }
		BlockEntity blockEntity = serverLevel.getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
		if (blockEntity == null) { return; }

		if (!(blockEntity instanceof RandomizableContainerBlockEntity lootableBlock)) { return; }
		lootableBlock.unpackLootTable(null);
	}

	public static BlockData getChangedBlockData(BlockPhysicsEvent event) {
		return (BlockData) getValue(BlockPhysicsEvent_changed, event);
	}

	public static Map<org.bukkit.entity.LivingEntity, Double> getAffectedEntities(PotionSplashEvent event) {
		return (Map<org.bukkit.entity.LivingEntity, Double>) getValue(PotionSplashEvent_affectedEntities, event);
	}

	public static List<org.bukkit.entity.LivingEntity> getAffectedEntities(AreaEffectCloudApplyEvent event) {
		return (List<org.bukkit.entity.LivingEntity>) getValue(AreaEffectCloudApplyEvent_affectedEntities, event);
	}

	//ItemStack to remove can be null which means it will not take any items at all from source inventory and only dispense.
	//If slot is positive it will try to take item from that slot, but if it fails it will take from first available,
	//slot can be also negative, then it will immediately take from first available slot, if item is not found it will (TODO: fail or succeed?)
	public static boolean dispenseItem(org.bukkit.block.Block source, ItemStack drop, @Nullable ItemStack remove, int slot) {
		if (source.getType() == Material.DISPENSER) { return dispenseDispenser(source, drop, remove, slot); }
		if (source.getType() == Material.DROPPER) { return dispenseDropper(source, drop, remove, slot); }
		return false;
	}

	//TODO: Should we remove item, if result of dispense is modified item? (Currently: NO)
	private static boolean dispenseDispenser(org.bukkit.block.Block source, ItemStack drop, @Nullable ItemStack remove, int slot) {
		if (drop.getAmount() <= 0) { return true; }
		if ((source.getType() != Material.DISPENSER) && (source.getType() != Material.DROPPER)) { return false; }
		if (remove == null) { remove = new ItemStack(Material.AIR); }
		if (remove.getType() == Material.AIR) { slot = -1; }
		drop = drop.clone(); remove = remove.clone(); //Make sure drop and remove are not the same item

		BlockPos blockPos = new BlockPos(source.getX(), source.getY(), source.getZ());
		ServerLevel serverLevel = getWorld(source.getLocation().getWorld());
		BlockState blockState = serverLevel.getBlockState(blockPos);
		DispenserBlockEntity tileentitydispenser = serverLevel.getBlockEntity(blockPos, BlockEntityType.DISPENSER).orElse(null);
		BlockSource blockSource = new BlockSource(serverLevel, blockPos, blockState, tileentitydispenser);

		DispenseItemBehavior dispenseItemBehavior = (source.getType() == Material.DISPENSER) ? DispenserBlock.DISPENSER_REGISTRY.get(asNMSCopy(drop).getItem()) : new DefaultDispenseItemBehavior(true);
		net.minecraft.world.item.ItemStack result = dispenseItemBehavior.dispense(blockSource, asNMSCopy(drop));
		if (result.getCount() == drop.getAmount()) { return false; } //It failed to dispense or item was modified inside called event

		org.bukkit.block.Container container = (org.bukkit.block.Container) source.getState();
		remove.setAmount(1); //We always remove by 1 item
		drop.setAmount(result.getCount()); //Update drop amount after dispense

		//IDC, I will not check if item doesn't exist, I will just remove it
		if (slot < 0) {
			Utils.removeItem(container.getInventory(), remove); //Fuck you spigot, can't even make simple method to remove items
		} else {
			ItemStack itemStack = container.getInventory().getItem(slot);
			if (itemStack == null) { itemStack = new ItemStack(Material.AIR); }
			itemStack.setAmount(itemStack.getAmount()-1);
			if (itemStack.getAmount() <= 0) { itemStack = new ItemStack(Material.AIR); }
			container.getInventory().setItem(slot, itemStack);
		}

		//Sadly, but dispense only can dispense by 1 item
		return dispenseDispenser(source, drop, remove, slot);
	}

	private static boolean dispenseDropper(org.bukkit.block.Block source, ItemStack drop, ItemStack remove, int slot) {
		if (drop.getAmount() <= 0) { return true; }
		if (source.getType() != Material.DROPPER) { return false; }
		if (remove == null) { remove = new ItemStack(Material.AIR); }
		if (remove.getType() == Material.AIR) { slot = -1; }
		drop = drop.clone(); remove = remove.clone();

		BlockPos blockPos = new BlockPos(source.getX(), source.getY(), source.getZ());
		ServerLevel serverLevel = getWorld(source.getLocation().getWorld());
		DispenserBlockEntity tileentitydispenser = serverLevel.getBlockEntity(blockPos, BlockEntityType.DROPPER).orElse(null);
		Direction enumdirection = serverLevel.getBlockState(blockPos).getValue(DropperBlock.FACING);
		Container iinventory = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(enumdirection));
		BlockFace blockFace = ((Directional) source.getBlockData()).getFacing();
		org.bukkit.block.Block dblock = source.getRelative(blockFace);

		//If there is no container in front then just act like dispenser
		if (!(dblock.getState() instanceof org.bukkit.block.Container container)) {
			return dispenseDispenser(source, drop, remove, slot);
		}

		ItemStack event_item = Utils.cloneItem(drop, 1); //We always move by 1 item
		Inventory destinationInventory = (dblock.getState() instanceof DoubleChest) ? ((DoubleChest) dblock.getState()).getInventory() : container.getInventory();
		InventoryMoveItemEvent event = new InventoryMoveItemEvent(tileentitydispenser.getOwner().getInventory(), event_item, destinationInventory, true);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) { return false; }

		net.minecraft.world.item.ItemStack itemstack1 = HopperBlockEntity.addItem(tileentitydispenser, iinventory, asNMSCopy(event.getItem()), enumdirection.getOpposite());
		if (!(event.getItem().equals(event_item) && itemstack1.isEmpty())) { return false; } //If item was modified or was not moved successfully return

		org.bukkit.block.Container dropper = (org.bukkit.block.Container) source.getState();
		remove.setAmount(1); //We always remove by 1 item
		drop.setAmount(drop.getAmount()-1); //Update drop amount after moving

		//IDC, I will not check if item doesn't exist, I will just remove it
		if (slot < 0) {
			Utils.removeItem(dropper.getInventory(), remove); //Fuck you spigot, can't even make simple method to remove items
		} else {
			ItemStack itemStack = dropper.getInventory().getItem(slot);
			if (itemStack == null) { itemStack = new ItemStack(Material.AIR); }
			itemStack.setAmount(itemStack.getAmount()-1);
			if (itemStack.getAmount() <= 0) { itemStack = new ItemStack(Material.AIR); }
			dropper.getInventory().setItem(slot, itemStack);
		}

		return dispenseDropper(source, drop, remove, slot);
	}

	public static Packet<?> editSpawnPacket(Packet<?> packet, @Nullable Boolean isFlat, @Nullable World.Environment env) {
		if (packet instanceof ClientboundLoginPacket lPacket) {
			int playerId = lPacket.playerId();
			boolean hardcore = lPacket.hardcore();
			Set<ResourceKey<Level>> levels = lPacket.levels();
			int maxPlayers = lPacket.maxPlayers();
			int chunkRadius = lPacket.chunkRadius();
			int simulationDistance = lPacket.simulationDistance();
			boolean reducedDebugInfo = lPacket.reducedDebugInfo();
			boolean showDeathScreen = lPacket.showDeathScreen();
			boolean doLimitedCrafting = lPacket.doLimitedCrafting();

			CommonPlayerSpawnInfo commonPlayerSpawnInfo = editCommonPlayerSpawnInfo(lPacket.commonPlayerSpawnInfo(), isFlat, env);
			return new ClientboundLoginPacket(playerId, hardcore, levels, maxPlayers, chunkRadius, simulationDistance, reducedDebugInfo, showDeathScreen, doLimitedCrafting, commonPlayerSpawnInfo);
		}

		if (packet instanceof ClientboundRespawnPacket rPacket) {
			CommonPlayerSpawnInfo commonPlayerSpawnInfo = editCommonPlayerSpawnInfo(rPacket.commonPlayerSpawnInfo(), isFlat, env);
			return new ClientboundRespawnPacket(commonPlayerSpawnInfo, rPacket.dataToKeep());
		}

		return null;
	}

	public static CommonPlayerSpawnInfo editCommonPlayerSpawnInfo(CommonPlayerSpawnInfo commonPlayerSpawnInfo,  @Nullable Boolean isFlat, @Nullable World.Environment env) {
		ResourceKey<DimensionType> dimensionType = commonPlayerSpawnInfo.dimensionType();

		if (env == World.Environment.NORMAL) { dimensionType = BuiltinDimensionTypes.OVERWORLD; }
		if (env == World.Environment.NETHER) { dimensionType = BuiltinDimensionTypes.NETHER; }
		if (env == World.Environment.THE_END) { dimensionType = BuiltinDimensionTypes.END; }

		ResourceKey<Level> dimension = commonPlayerSpawnInfo.dimension();
		long seed = commonPlayerSpawnInfo.seed();
		GameType gameType = commonPlayerSpawnInfo.gameType();
		GameType previousGameType = commonPlayerSpawnInfo.previousGameType();
		boolean isDebug = commonPlayerSpawnInfo.isDebug();
		isFlat = (isFlat != null) ? isFlat : commonPlayerSpawnInfo.isFlat();
		Optional<GlobalPos> lastDeathLocation = commonPlayerSpawnInfo.lastDeathLocation();
		int portalCooldown = commonPlayerSpawnInfo.portalCooldown();
		return new CommonPlayerSpawnInfo(dimensionType, dimension, seed, gameType, previousGameType, isDebug, isFlat, lastDeathLocation, portalCooldown);
	}

	//This is very unstable and can produce server crash, use only in WorldInitEvent
	public static void setCustomDimension(World world, @Nullable DimensionType copy, @Nullable World.Environment env, @Nullable Long fixedTime, @Nullable Boolean hasSkyLight, @Nullable Boolean hasCeiling, @Nullable Boolean ultraWarm, @Nullable Boolean natural, @Nullable Double coordinateScale, @Nullable Boolean bedWorks, @Nullable Boolean respawnAnchorWorks, @Nullable Integer minY, @Nullable Integer height, @Nullable Integer logicalHeight, @Nullable TagKey<Block> infiniburn, @Nullable ResourceLocation effectsLocation, @Nullable Float ambientLight, @Nullable DimensionType.MonsterSettings monsterSettings) {
		ServerLevel level = getWorld(world);
		DimensionType original = level.dimensionType();

		Field CraftWorld_environment = getField(CraftWorld, World.Environment.class, null, true);
		if (env != null) { setValue(CraftWorld_environment, level.getWorld(), env); }

		//Get registry for default dimensions
		LayeredRegistryAccess<RegistryLayer> registries = (LayeredRegistryAccess<RegistryLayer>) Objects.requireNonNull(getValue(MinecraftServer_registries, MinecraftServer.getServer()));
		Registry<LevelStem> dimensions = registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);

		//Select dimension data if environment parameter is used and others are not
		boolean b1 = (fixedTime == null) && (hasSkyLight == null) && (hasCeiling == null) && (ultraWarm == null) && (natural == null) && (coordinateScale == null) && (bedWorks == null) && (respawnAnchorWorks == null) && (minY == null) && (height == null) && (logicalHeight == null) && (infiniburn == null) && (effectsLocation == null) && (ambientLight == null) && (monsterSettings == null);
		if (b1 && (env == World.Environment.NORMAL)) { copy = dimensions.get(LevelStem.OVERWORLD.location()).type().value(); }
		if (b1 && (env == World.Environment.NETHER)) { copy = dimensions.get(LevelStem.NETHER.location()).type().value(); }
		if (b1 && (env == World.Environment.THE_END)) { copy = dimensions.get(LevelStem.END.location()).type().value(); }

		//Replace dimension type if environment parameter is used
		if (env == World.Environment.NORMAL) { setValue(Level_dimensionTypeId, level, BuiltinDimensionTypes.OVERWORLD); }
		if (env == World.Environment.NETHER) { setValue(Level_dimensionTypeId, level, BuiltinDimensionTypes.NETHER); }
		if (env == World.Environment.THE_END) { setValue(Level_dimensionTypeId, level, BuiltinDimensionTypes.END); }

		//Create new dimension data from given parameters
		DimensionType type = new DimensionType(
				(fixedTime == null) ? ((copy == null) ? original.fixedTime() : copy.fixedTime()) : OptionalLong.of(fixedTime),
				(hasSkyLight == null) ? ((copy == null) ? original.hasSkyLight() : copy.hasSkyLight()) : hasSkyLight,
				(hasCeiling == null) ? ((copy == null) ? original.hasCeiling() : copy.hasCeiling()) : hasCeiling,
				(ultraWarm == null) ? ((copy == null) ? original.ultraWarm() : copy.ultraWarm()) : ultraWarm,
				(natural == null) ? ((copy == null) ? original.natural() : copy.natural()) : natural,
				(coordinateScale == null) ? ((copy == null) ? original.coordinateScale() : copy.coordinateScale()) : coordinateScale,
				(bedWorks == null) ? ((copy == null) ? original.bedWorks() : copy.bedWorks()) : bedWorks,
				(respawnAnchorWorks == null) ? ((copy == null) ? original.respawnAnchorWorks() : copy.respawnAnchorWorks()) : respawnAnchorWorks,
				(minY == null) ? ((copy == null) ? original.minY() : copy.minY()) : minY,
				(height == null) ? ((copy == null) ? original.height() : copy.height()) : height,
				(logicalHeight == null) ? ((copy == null) ? original.logicalHeight() : copy.logicalHeight()) : logicalHeight,
				(infiniburn == null) ? ((copy == null) ? original.infiniburn() : copy.infiniburn()) : infiniburn,
				(effectsLocation == null) ? ((copy == null) ? original.effectsLocation() : copy.effectsLocation()) : effectsLocation,
				(ambientLight == null) ? ((copy == null) ? original.ambientLight() : copy.ambientLight()) : ambientLight,
				(monsterSettings == null) ? ((copy == null) ? original.monsterSettings() : copy.monsterSettings()) : monsterSettings
		);

		//Create holder and replace dimension data inside level
		Holder<DimensionType> holder = (Holder<DimensionType>) getValue(Level_dimensionTypeRegistration, level); //Get original holder
		HolderOwner<DimensionType> owner = (HolderOwner<DimensionType>) getValue(Holder_owner, holder); //Get original holder owner
		Holder<DimensionType> newHolder = Holder.Reference.createIntrusive(owner, type); //Create new holder, so that we don't affect all worlds
		setValue(Level_dimensionTypeRegistration, level, newHolder); //Replace old holder with new one

		//Replace stupid paper EntityLookup system, because it initializes and uses old minY and height variables
		//FUCK YOU PAPER -> io.papermc.paper.chunk.system.entity.EntityLookup -> minSection = WorldUtil.getMinSection(world)
		if (PaperUtils.isPaper()) {
			Field ServerLevel_entityLookup = getField(ServerLevel.class, PaperUtils.EntityLookup, null, true);
			Field EntityLookup_worldCallback = getField(PaperUtils.EntityLookup, net.minecraft.world.level.entity.LevelCallback.class, null, true);
			net.minecraft.world.level.entity.LevelCallback<?> callback = (net.minecraft.world.level.entity.LevelCallback<?>) getValue(EntityLookup_worldCallback, level.getEntityLookup());
			Class<?>[] parameters = { ServerLevel.class, net.minecraft.world.level.entity.LevelCallback.class };
			Object entityLookup = newInstance(true, false, PaperUtils.EntityLookup, parameters, new Object[]{ level, callback });
			setValue(ServerLevel_entityLookup, level, entityLookup);

			//FUCK YOU PAPER, it took me like 5 days to find this, idk why I can modify final, but *this is fine*
			Field Level_minSection = Objects.requireNonNull(getField(Level.class, int.class, null, level, level.minSection, false));
			Field Level_maxSection = Objects.requireNonNull(getField(Level.class, int.class, null, level, level.maxSection, false));
			setValue(Level_minSection, level, (type.minY() >> 4));
			setValue(Level_maxSection, level, ((type.minY() + type.height() - 1) >> 4));
		}
	}
}
