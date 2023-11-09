package me.wobbychip.smptweaks.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import org.bukkit.util.Vector;

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
	public static Method EntityVillager_startTrading_Or_updateSpecialPrices;
	public static Method IRegistry_keySet;
	public static Method Potions_register;
	public static Method RegistryMaterials_getHolder;
	public static Method PotionBrewer_register;

	static {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		CraftServer = loadClass("org.bukkit.craftbukkit." + version + ".CraftServer", true);
		CraftEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftEntity", true);
		CraftHumanEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity", true);
		CraftPlayer = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer", true);
		CraftVillager = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftVillager", true);
		CraftInventory = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftInventory", true);
		CraftItemStack = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack", true);
		CraftWorld = loadClass("org.bukkit.craftbukkit." + version + ".CraftWorld", true);
		CraftMagicNumbers = loadClass("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers", true);

		//Fuck it I am not interested in updating nms every time
		//So I will just search fields and methods by their types and arguments

		DATA_LIVING_ENTITY_FLAGS = (EntityDataAccessor<Byte>) getValue(getField(LivingEntity.class, EntityDataAccessor.class, Byte.class, true), null);
		setRegistryMap(POTION, new HashMap<>());

		Entity_bukkitEntity = getField(net.minecraft.world.entity.Entity.class, CraftEntity, null, true);
		EntityPlayer_playerConnection = getField(ServerPlayer.class, ServerGamePacketListenerImpl.class, null, true);
		EntityPlayer_chatVisibility = getField(ServerPlayer.class, ChatVisiblity.class, null, true);
		RegistryMaterials_frozen = getField(MappedRegistry.class, boolean.class, null, true);
		RegistryMaterials_nextId = getField(MappedRegistry.class, int.class, null, true);

		MinecraftServer_levels = getField(MinecraftServer.class, Map.class, null, true);
		CustomFunctionData_ticking = getField(ServerFunctionManager.class, List.class, null, true);
		CustomFunctionData_postReload = getField(ServerFunctionManager.class, boolean.class, null, true);

		EntityVillager_startTrading_Or_updateSpecialPrices = findMethod(false, Modifier.PRIVATE, Villager.class, Void.TYPE, null, net.minecraft.world.entity.player.Player.class);
		IRegistry_keySet = findMethod(true, null, Registry.class, Set.class, ResourceLocation.class);
		Potions_register = findMethod(false, Modifier.PRIVATE, Potions.class, Potion.class, null, String.class, Potion.class);
		RegistryMaterials_getHolder = findMethod(true, null, Registry.class, Optional.class, null, int.class);
		PotionBrewer_register = findMethod(false, null, PotionBrewing.class, Void.TYPE, null, Potion.class, Item.class, Potion.class);
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
				if (!((Class<?>) argTypes[0]).equals(gType)) { continue; }
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

	public static Field getField(Class<?> clazz, Class<?> fType, Class<?> gType, boolean bDeclared) {
		for (Field field : bDeclared ? clazz.getDeclaredFields() : clazz.getFields()) {
			if (!field.getType().equals(fType)) { continue; }

			if (gType != null) {
				Type genericType = field.getGenericType();
				if (!(genericType instanceof ParameterizedType)) { continue; }
				Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
				if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
				if (!((Class<?>) argTypes[0]).equals(gType)) { continue; }
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

	public static Object newInstance(boolean verbose, boolean bDeclared, Class<?> clazz, Class<?>[] parameters, Object[] args) {
		try {
			Constructor<?> constructor = bDeclared ? clazz.getDeclaredConstructor(parameters) : clazz.getConstructor(parameters);
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
			net.minecraft.world.level.Level world = getWorld(location.getWorld());
			BlockPos position = new BlockPos((int) location.getX(), (int) location.getY(), (int) location.getZ());
			Block.popResource(world, position, asNMSCopy(item));
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

	@SuppressWarnings("deprecation")
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
			ReflectionUtils.sendPacket(player, new ClientboundBlockDestructionPacket(player.getEntityId()+mix_id, location, progress));
		}
	}

	public static void destroyBlock(Player player, org.bukkit.block.Block block) {
		destroyBlockProgress(block, -1, 0);
		BlockPos location = new BlockPos(block.getX(), block.getY(), block.getZ());
		int id = ReflectionUtils.getBlockId(block.getType());
		ReflectionUtils.sendPacket(player, new ClientboundLevelEventPacket(2001, location, id, false));
		player.breakBlock(block);
	}

	public static void dropBlockItem(org.bukkit.block.Block block, Player player, ItemStack itemStack) {
		double x = block.getLocation().getX() + 0.5;
		double y = block.getLocation().getY() + 0.5;
		double z = block.getLocation().getZ() + 0.5;

		double f = (Utils.ITEM_HEIGHT / 2.0F);
		double d0 = x + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET);
		double d1 = y + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET) - f;
		double d2 = z + Utils.randomRange(-Utils.ITEM_SPAWN_OFFSET, Utils.ITEM_SPAWN_OFFSET);

		ItemEntity entityItem = new ItemEntity(ReflectionUtils.getWorld(block.getWorld()), d0, d1, d2, ReflectionUtils.asNMSCopy(itemStack));
		ReflectionUtils.getBukkitEntity(entityItem).setVelocity(new Vector(Math.random()*0.2F-0.1F, 0.2F, Math.random()*0.2F-0.1F));
		ArrayList<org.bukkit.entity.Item> items = new ArrayList<>(Arrays.asList((org.bukkit.entity.Item) ReflectionUtils.getBukkitEntity(entityItem)));

		BlockDropItemEvent dropEvent = new BlockDropItemEvent(block, block.getState(), player, items);
		Bukkit.getServer().getPluginManager().callEvent(dropEvent);
		if (dropEvent.isCancelled()) { return; }

		for (org.bukkit.entity.Item drop : dropEvent.getItems()) {
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
}
