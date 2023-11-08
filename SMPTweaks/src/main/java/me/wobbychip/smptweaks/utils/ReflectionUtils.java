package me.wobbychip.smptweaks.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistryBlocks;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConnection;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.gossip.Reputation;
import net.minecraft.world.entity.ai.gossip.ReputationType;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.world.entity.player.PlayerAbilities;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.PotionUtil;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

@SuppressWarnings("unchecked")
public class ReflectionUtils {
	public static DataWatcherObject<Byte> DATA_LIVING_ENTITY_FLAGS;
	public static int LIVING_ENTITY_FLAG_IS_USING = 1;
	public static RegistryBlocks<PotionRegistry> POTION;

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
	public static Field EntityHuman_playerAbilities;
	public static Field EntityHuman_container;
	public static Field EntityPlayer_invulnerableTicks;
	public static Field EntityPlayer_playerConnection;
	public static Field EntityPlayer_chatVisibility;
	public static Field EntityPlayer_respawnDimension;
	public static Field EntityVillager_Reputation;
	public static Field MinecraftServer_playerList;
	public static Field MinecraftServer_levels;
	public static Field MinecraftServer_functionManager;
	public static Field ServerConnection_connections;
	public static Field NetworkManager_channel;
	public static Field CustomFunctionData_ticking;
	public static Field CustomFunctionData_postReload;
	public static Field PlayerList_players;
	public static Field RegistryMaterials_frozen;
	public static Field RegistryMaterials_nextId;
	public static Field WorldServer_players;
	public static Field ItemStack_tag;
	public static Field ItemBlock_block;
	public static Field Block_defaultBlockState;
	public static Field BlockBase_properties;
	public static Field BlockBase_drops;
	public static Field BlockBase_BlockData_destroySpeed;

	public static Method Container_quickMoveStack;
	public static Method ChunkProviderServer_move;
	public static Method EntityData_get;
	public static Method Entity_getEntityData;
	public static Method EntityVillager_startTrading_Or_updateSpecialPrices;
	public static Method EntityHuman_getDestroySpeed;
	public static Method EnumChatVisibility_getKey;
	public static Method IRegistry_keySet;
	public static Method Potions_register;
	public static Method RegistryMaterials_getHolder;
	public static Method Item_get;
	public static Method Item_releaseUsing;
	public static Method Block_getId;
	public static Method Block_popResource;
	public static Method BlockBase_getLootTable;
	public static Method BlockBase_Info_strength;
	public static Method ServerCommonPacketListenerImpl_send;
	public static Method PotionBrewer_register;
	public static Method PotionRegistry_getName;
	public static Method PotionUtil_getPotion;
	public static Method WorldServer_addPlayer;
	public static Method WorldServer_getChunkProviderServer;
	public static Method WorldServer_removePlayer;
	public static Method MinecraftServer_getLevel;
	public static Method MinecraftServer_getConnection;
	public static Method NBTTagCompound_putString;
	public static Method NBTTagCompound_getString;
	public static Method Reputation_getReputation;
	public static Method CommonListenerCookie_createInitial;
	public static Method ClientInformation_createDefault;

	
	public static EnumProtocolDirection PROTOCL_SERVERBOUND = EnumProtocolDirection.a; //net.minecraft.network.protocol.PacketFlow SERVERBOUND ->
	public static EnumProtocolDirection PROTOCL_CLIENTBOUND = EnumProtocolDirection.b; //net.minecraft.network.protocol.PacketFlow CLIENTBOUND ->
	public static EnumProtocol PROTOCL_PLAY = EnumProtocol.b; //net.minecraft.network.ConnectionProtocol PLAY ->
	public static RemovalReason REASON_DISCARDED = RemovalReason.b; //net.minecraft.world.entity.Entity$RemovalReason DISCARDED ->

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

		DATA_LIVING_ENTITY_FLAGS = (DataWatcherObject<Byte>) getValue(Objects.requireNonNull(getField(EntityLiving.class, DataWatcherObject.class, Byte.class, true)), null);
		POTION = (RegistryBlocks<PotionRegistry>) getValue(Objects.requireNonNull(getField(BuiltInRegistries.class, RegistryBlocks.class, PotionRegistry.class, true)), null);
		setRegistryMap(POTION, new HashMap<>());

		Entity_bukkitEntity = getField(net.minecraft.world.entity.Entity.class, CraftEntity, null, true);
		EntityHuman_playerAbilities = getField(EntityHuman.class, PlayerAbilities.class, null, true);
		EntityHuman_container = getField(EntityHuman.class, Container.class, null, true);
		EntityPlayer_playerConnection = getField(EntityPlayer.class, PlayerConnection.class, null, true);
		EntityPlayer_chatVisibility = getField(EntityPlayer.class, EnumChatVisibility.class, null, true);
		EntityPlayer_respawnDimension = getField(EntityPlayer.class, ResourceKey.class, net.minecraft.world.level.World.class, true);
		EntityVillager_Reputation = getField(EntityVillager.class, Reputation.class, null, true);
		MinecraftServer_playerList = getField(MinecraftServer.class, PlayerList.class, null, true);
		MinecraftServer_levels = getField(MinecraftServer.class, Map.class, null, true);
		MinecraftServer_functionManager = getField(MinecraftServer.class, CustomFunctionData.class, null, true);
		ServerConnection_connections = getField(ServerConnection.class, List.class, NetworkManager.class, true);
		NetworkManager_channel = getField(NetworkManager.class, Channel.class, null, true);
		CustomFunctionData_ticking = getField(CustomFunctionData.class, List.class, null, true);
		CustomFunctionData_postReload = getField(CustomFunctionData.class, boolean.class, null, true);
		PlayerList_players = getField(PlayerList.class, List.class, EntityPlayer.class, true);
		RegistryMaterials_frozen = getField(RegistryMaterials.class, boolean.class, null, true);
		RegistryMaterials_nextId = getField(RegistryMaterials.class, int.class, null, true);
		WorldServer_players = getField(WorldServer.class, List.class, EntityPlayer.class, true);
		ItemStack_tag = getField(net.minecraft.world.item.ItemStack.class, NBTTagCompound.class, null, true);
		ItemBlock_block = getField(ItemBlock.class, Block.class, null, true);
		Block_defaultBlockState = getField(Block.class, IBlockData.class, null, true);
		BlockBase_properties = getField(BlockBase.class, BlockBase.Info.class, null, true);
		BlockBase_drops = getField(BlockBase.class, MinecraftKey.class, null, true);
		BlockBase_BlockData_destroySpeed = getField(net.minecraft.world.level.block.state.BlockBase.BlockData.class, float.class, null, false);

		Container_quickMoveStack = findMethod(false, null, Container.class, net.minecraft.world.item.ItemStack.class, null, EntityHuman.class, int.class);
		ChunkProviderServer_move = findMethod(true, null, ChunkProviderServer.class, Void.TYPE, null, EntityPlayer.class);
		EntityData_get = findMethod(true, null, DataWatcher.class, Object.class, null, DataWatcherObject.class);
		Entity_getEntityData = findMethod(true, null, net.minecraft.world.entity.Entity.class, DataWatcher.class, null);
		EntityVillager_startTrading_Or_updateSpecialPrices = findMethod(false, Modifier.PRIVATE, EntityVillager.class, Void.TYPE, null, EntityHuman.class);
		EntityHuman_getDestroySpeed = findMethod(true, null, EntityHuman.class, float.class, null, IBlockData.class);
		EnumChatVisibility_getKey = findMethod(true, null, EnumChatVisibility.class, String.class, null);
		IRegistry_keySet = findMethod(true, null, IRegistry.class, Set.class, MinecraftKey.class);
		Potions_register = findMethod(false, Modifier.PRIVATE, Potions.class, PotionRegistry.class, null, String.class, PotionRegistry.class);
		RegistryMaterials_getHolder = findMethod(true, null, RegistryMaterials.class, Optional.class, null, int.class);
		Item_get = findMethod(true, null, Item.class, Item.class, null, int.class);
		Item_releaseUsing = findMethod(true, null, Item.class, Void.TYPE, null, net.minecraft.world.item.ItemStack.class, net.minecraft.world.level.World.class, net.minecraft.world.entity.EntityLiving.class, int.class);
		Block_getId = findMethod(true, null, Block.class, int.class, null, IBlockData.class);
		Block_popResource = findMethod(true, null, Block.class, Void.TYPE, null, net.minecraft.world.level.World.class, BlockPosition.class, net.minecraft.world.item.ItemStack.class);
		BlockBase_getLootTable = findMethod(true, null, BlockBase.class, MinecraftKey.class, null);
		BlockBase_Info_strength = findMethod(true, null, BlockBase.Info.class, BlockBase.Info.class, null, float.class, float.class);
		ServerCommonPacketListenerImpl_send = findMethod(true, null, ServerCommonPacketListenerImpl.class, null, null, Packet.class);
		PotionBrewer_register = findMethod(false, null, PotionBrewer.class, Void.TYPE, null, PotionRegistry.class, Item.class, PotionRegistry.class);
		PotionRegistry_getName = findMethod(true, null, PotionRegistry.class, String.class, null, String.class);
		PotionUtil_getPotion = findMethod(true, null, PotionUtil.class, PotionRegistry.class, null, net.minecraft.world.item.ItemStack.class);
		WorldServer_addPlayer = findMethod(true, null, WorldServer.class, Void.TYPE, null, EntityPlayer.class);
		WorldServer_getChunkProviderServer = findMethod(true, null, WorldServer.class, ChunkProviderServer.class, null);
		WorldServer_removePlayer = findMethod(true, null, WorldServer.class, Void.TYPE, null, EntityPlayer.class, RemovalReason.class);
		MinecraftServer_getLevel = findMethod(true, null, MinecraftServer.class, WorldServer.class, null, ResourceKey.class);
		MinecraftServer_getConnection = findMethod(true, null, MinecraftServer.class, ServerConnection.class, null);
		NBTTagCompound_putString = findMethod(true, null, NBTTagCompound.class, Void.TYPE, null, String.class, String.class);
		NBTTagCompound_getString = findMethod(true, null, NBTTagCompound.class, String.class, null, String.class);
		Reputation_getReputation = findMethod(true, null, Reputation.class, int.class, null, UUID.class, Predicate.class);
		CommonListenerCookie_createInitial = findMethod(true, Modifier.STATIC, CommonListenerCookie.class, CommonListenerCookie.class, null, GameProfile.class);
		ClientInformation_createDefault = findMethod(true, Modifier.STATIC, ClientInformation.class, ClientInformation.class, null);
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

	public static net.minecraft.server.level.EntityPlayer getEntityPlayer(Player player) {
		try {
			Object craftPlayer = CraftPlayer.cast(player);
			return (net.minecraft.server.level.EntityPlayer) player.getClass().getDeclaredMethod("getHandle").invoke(craftPlayer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.level.World getWorld(World world) {
		try {
			Object craftWorld = CraftWorld.cast(world);
			return (net.minecraft.world.level.World) world.getClass().getDeclaredMethod("getHandle").invoke(craftWorld);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.player.EntityHuman getEntityHuman(HumanEntity humanEntity) {
		try {
			Object craftHumanEntity = CraftHumanEntity.cast(humanEntity);
			return (net.minecraft.world.entity.player.EntityHuman) humanEntity.getClass().getDeclaredMethod("getHandle").invoke(craftHumanEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static net.minecraft.world.entity.npc.EntityVillager getEntityVillager(Villager villager) {
		try {
			Object craftEntity = CraftVillager.cast(villager);
			return (net.minecraft.world.entity.npc.EntityVillager) villager.getClass().getDeclaredMethod("getHandle").invoke(craftEntity);
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
		try {
			if (!block.isBlock()) { return null; }
			return (Block) getValue(ItemBlock_block, getItem(new ItemStack(block)));
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getBlockId(Material block) {
		try {
			IBlockData blockData = (IBlockData) Block_defaultBlockState.get(getBlock(block));
			return (int) Block_getId.invoke(Block_getId, blockData);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static World getRespawnWorld(Player player) {
		try {
			MinecraftServer server = getServer();
			Object respawnDimension = EntityPlayer_respawnDimension.get(getEntityPlayer(player));
			WorldServer worldServer = (WorldServer) MinecraftServer_getLevel.invoke(server, respawnDimension);
			return (World) worldServer.getWorld();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return Bukkit.getWorlds().get(0);
		}
	}

	//Get block destroy time per tick which is based on player
	public static float getPlayerDestroyTime(Player player, Material block) {
		try {
			IBlockData blockData = (IBlockData) Block_defaultBlockState.get(getBlock(block));
			net.minecraft.world.entity.player.EntityHuman entityHuman = getEntityHuman(player);
			return (float) EntityHuman_getDestroySpeed.invoke(entityHuman, blockData);
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			e.printStackTrace();
			return 0;
		}
	}

	//Get raw block destroy time which is not based on player
	public static float getBlockDestroyTime(Material block) {
		try {
			IBlockData blockData = (IBlockData) Block_defaultBlockState.get(getBlock(block));
			return (float) BlockBase_BlockData_destroySpeed.get(blockData);
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			e.printStackTrace();
			return -1;
		}
	}

	//Sets what the block drops, for some reason does not work with
	//block that by default do not drop anything, like, bedrock
	public static void setBlockDrop(Material block, Material drop) {
		try {
			BlockBase baseBlock = (BlockBase) getBlock(block);
			BlockBase baseDrop = (BlockBase) getBlock(drop);

			Object original = BlockBase_drops.get(baseDrop);
			BlockBase_drops.set(baseDrop, null);
			Object updated = BlockBase_getLootTable.invoke(baseDrop);
			BlockBase_drops.set(baseDrop, original);

			BlockBase_drops.set(baseBlock, updated);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void popResource(Location location, ItemStack item) {
		try {
			net.minecraft.world.level.World world = getWorld(location.getWorld());
			BlockPosition position = new BlockPosition((int) location.getX(), (int) location.getY(), (int) location.getZ());
			Block_popResource.invoke(Block_popResource, world, position, asNMSCopy(item));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void setInvulnerableTicks(Player player, int ticks) {
		Object entityPlayer = (Object) getEntityPlayer(player);

		if (EntityPlayer_invulnerableTicks == null) {
            for (Field field : entityPlayer.getClass().getDeclaredFields()) {
				try {
					if (!field.getType().equals(int.class)) { continue; }
					if (((int) field.get(entityPlayer)) != 60) { continue; }
					EntityPlayer_invulnerableTicks = field;
					break;
				} catch (IllegalArgumentException | IllegalAccessException ignored) {}
			}
		}

		try {
			EntityPlayer_invulnerableTicks.set(entityPlayer, ticks);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static PlayerAbilities getPlayerAbilities(Player player) {
		try {
			return (PlayerAbilities) EntityHuman_playerAbilities.get(getEntityPlayer(player));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void sendPacket(Player player, Packet<?> packet) {
		try {
			Object connection = EntityPlayer_playerConnection.get(getEntityPlayer(player));
			ServerCommonPacketListenerImpl_send.invoke(connection, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
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

	public static HashMap<NetworkManager, Channel> getConnections() {
		try {
			HashMap<NetworkManager, Channel> connections = new HashMap<>();
			ServerConnection serverConnection = (ServerConnection) MinecraftServer_getConnection.invoke(getServer());
			List<NetworkManager> managers = (List<NetworkManager>) ServerConnection_connections.get(serverConnection);

			for (NetworkManager manager : managers) {
				connections.put(manager, (Channel) NetworkManager_channel.get(manager));
			}

			return connections;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
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

		sendPacket(player, new PacketPlayOutSetSlot(0, 0, slot, asNMSCopy(item)));
	}

	public static void setInstantBuild(Player player, boolean instantbuild, boolean clientSide, boolean serverSide) {
		//net.minecraft.world.entity.player.Abilities ->
		//	boolean instabuild -> d

		PlayerAbilities abilities = getPlayerAbilities(player);

		if (clientSide) {
			boolean temp = abilities.d;
			abilities.d = instantbuild;
			sendPacket(player, new PacketPlayOutAbilities(abilities));
			abilities.d = temp;
		}

		if (serverSide) {
			abilities.d = instantbuild;
		}
	}

	public static void setChatVisibility(Player player, String visibility) {
		try {
			for (EnumChatVisibility chat : EnumChatVisibility.values()) {
				String key = (String) EnumChatVisibility_getKey.invoke(chat);
				if (!key.equalsIgnoreCase(visibility)) { continue; }

				EntityPlayer entityPlayer = getEntityPlayer(player);
				EntityPlayer_chatVisibility.set(entityPlayer, chat);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static String getChatVisibility(Player player) {
		try {
			EntityPlayer entityPlayer = getEntityPlayer(player);
			EnumChatVisibility chat = (EnumChatVisibility) EntityPlayer_chatVisibility.get(entityPlayer);
			return (String) EnumChatVisibility_getKey.invoke(chat);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	//The most useless shit I ever made
	public static boolean isUsingItem(Player player) {
		try {
			DataWatcher entityData = (DataWatcher) Entity_getEntityData.invoke(getEntityPlayer(player));
			Byte entityFlags = (Byte) EntityData_get.invoke(entityData, DATA_LIVING_ENTITY_FLAGS);
			return (entityFlags & LIVING_ENTITY_FLAG_IS_USING) > 0;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return (player.getItemInUse() != null);
		}
	}

	public static void shootBow(Player player, ItemStack bow, int ticks) {
		try {
			net.minecraft.world.item.ItemStack item = asNMSCopy(bow);
			net.minecraft.world.level.World world = getWorld(player.getWorld());
			EntityPlayer entityPlayer = getEntityPlayer(player);
			Item_releaseUsing.invoke(Item_get.invoke(null, 718), item, world, entityPlayer, (72000 - ticks));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static Player addFakePlayer(Location location, UUID uuid, boolean addPlayer, boolean hideOnline, boolean hideWorld) {		
		//io.netty.util.AttributeKey ATTRIBUTE_SERVERBOUND_PROTOCOL -> b
		if (uuid == null) { uuid = UUID.randomUUID(); }

		try {
			MinecraftServer server = getServer();
			WorldServer world = (WorldServer) getWorld(location.getWorld());
			ClientInformation clientInformation = (ClientInformation) ClientInformation_createDefault.invoke(null);
			EntityPlayer entityPlayer = new EntityPlayer(server, world, new GameProfile(uuid, " ".repeat(5)), clientInformation);

			NetworkManager networkManager = new NetworkManager(PROTOCL_SERVERBOUND);
			EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ChannelHandler[] { networkManager });
			embeddedChannel.attr(NetworkManager.e).set(PROTOCL_PLAY.b(PROTOCL_SERVERBOUND));

			CommonListenerCookie commonListenerCookie = (CommonListenerCookie) CommonListenerCookie_createInitial.invoke(null, new GameProfile(uuid, " ".repeat(5)));
			PlayerConnection connection = new PlayerConnection(server, networkManager, entityPlayer, commonListenerCookie) {};
			Player player = (Player) getBukkitEntity(entityPlayer);

			EntityPlayer_playerConnection.set(entityPlayer, connection);
			if (addPlayer) { WorldServer_addPlayer.invoke(world, entityPlayer); }

			//Will hide from Bukkit.getOnlinePlayers()
			if (addPlayer && hideOnline) {
				Object playerList = MinecraftServer_playerList.get(server);
				List<EntityPlayer> players = (List<EntityPlayer>) PlayerList_players.get(playerList);
				players.removeIf(e -> ((Player) getBukkitEntity(e)).getUniqueId().equals(player.getUniqueId()));
			}

			//Will hide from World.getPlayers(), but also will prevent mob spawning
			if (addPlayer && hideWorld) {
				List<EntityPlayer> players = (List<EntityPlayer>) WorldServer_players.get(world);
				players.removeIf(e -> ((Player) getBukkitEntity(e)).getUniqueId().equals(player.getUniqueId()));
			}

			player.setSleepingIgnored(true);
			return player;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void removeFakePlayer(Player player) {
		try {
			EntityPlayer entityPlayer = getEntityPlayer(player);
			WorldServer world = (WorldServer) getWorld(player.getWorld());
			WorldServer_removePlayer.invoke(world, entityPlayer, REASON_DISCARDED);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
	}

	//This needed to update player chunks and make them tickable
	public static void updateFakePlayer(Player player) {
		try {
			WorldServer world = (WorldServer) getWorld(player.getWorld());
			ChunkProviderServer provider = (ChunkProviderServer) WorldServer_getChunkProviderServer.invoke(world);
			ChunkProviderServer_move.invoke(provider, getEntityPlayer(player));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	//Open trading screen of a villager
	public static void startTrading(Player player, Villager villager) {
		try {
			EntityVillager entityVillager = getEntityVillager(villager);
			EntityPlayer entityPlayer = getEntityPlayer(player);
			EntityVillager_startTrading_Or_updateSpecialPrices.invoke(entityVillager, entityPlayer);
			player.openMerchant(villager, false);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
	}

	//Simulate shift+click on specific slot on opened inventory
	public static void quickMoveStack(Player player, int slot) {
		try {
			EntityPlayer entityPlayer = getEntityPlayer(player);
			Container container = (Container) EntityHuman_container.get(entityPlayer);
			Container_quickMoveStack.invoke(container, entityPlayer, slot);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	//Gets player's reputation from villager (all types)
	public static int getPlayerReputation(Villager villager, Player player) {
		try {
			EntityVillager entityVillager = getEntityVillager(villager);
			Reputation reputation = (Reputation) EntityVillager_Reputation.get(entityVillager);
			Predicate<ReputationType> predicate = (reputationtype) -> { return true; };
			return (int) Reputation_getReputation.invoke(reputation, player.getUniqueId(), predicate);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static boolean registerBrewRecipe(PotionRegistry base, Material ingredient, PotionRegistry result) {
		try {
			Item potionIngredient = getItem(new ItemStack(ingredient));
			PotionBrewer_register.invoke(PotionBrewer_register, base, potionIngredient, result);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		if (potionType == PotionType.UNCRAFTABLE) { return null; }

		ItemStack item = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
		item.setItemMeta(potionMeta);

		net.minecraft.world.item.ItemStack nmsItem = asNMSCopy(item);
		if (nmsItem == null) { return null; }

		try {
			return (PotionRegistry) PotionUtil_getPotion.invoke(PotionUtil_getPotion, nmsItem);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getPotionRegistryName(PotionRegistry potion) {
		try {
			return (String) PotionRegistry_getName.invoke(potion, "");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getPotionTag(ItemStack item) {
		try {
			net.minecraft.world.item.ItemStack nmsItem = asNMSCopy(item);
			NBTTagCompound tag = (NBTTagCompound) ItemStack_tag.get(nmsItem);
			if (tag == null) { return ""; }
			String potion = (String) NBTTagCompound_getString.invoke(tag, "Potion");
			return potion.replace("minecraft:", "");
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static ItemStack setPotionTag(ItemStack item, String name) {
		try {
			net.minecraft.world.item.ItemStack nmsItem = asNMSCopy(item);
			NBTTagCompound tag = (NBTTagCompound) ItemStack_tag.get(nmsItem);
			if (tag == null) { tag = new NBTTagCompound(); }
			NBTTagCompound_putString.invoke(tag, "Potion", name);
			ItemStack_tag.set(nmsItem, tag);
			return asBukkitMirror(nmsItem);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void setRegistryFrozen(Object registry, boolean frozen) {
		try {
			RegistryMaterials_frozen.set(registry, frozen);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void setRegistryMap(Object registry, Object hashMap) {
		Field[] fields = RegistryMaterials.class.getDeclaredFields();

		for (Field field : fields) {
			if (!field.getType().equals(Map.class)) { continue; }
			if (getValue(field, POTION) != null) { continue; }
			setValue(field, POTION, hashMap); break;
		}
	}

	public static int getRegistrySize(IRegistry<?> registry) {
		try {
			return ((Set<MinecraftKey>) IRegistry_keySet.invoke(registry)).size();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static PotionRegistry registerInstantPotion(String name) {
		try {
			setRegistryFrozen(POTION, false);
			PotionRegistry potionRegistry = new PotionRegistry(new MobEffect[0]);
			potionRegistry = (PotionRegistry) Potions_register.invoke(Potions_register, name, potionRegistry);
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
			Holder.c<?> holder_c = (Holder.c<?>) holder.get();

			for (Field field : Holder.c.class.getDeclaredFields()) {
				field.setAccessible(true);
				if (field.get(holder_c) == null) { field.set(holder_c, value); }
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
