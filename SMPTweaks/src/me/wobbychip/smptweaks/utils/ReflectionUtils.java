package me.wobbychip.smptweaks.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistryBlocks;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.PotionUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReflectionUtils {
	public static DataWatcherObject<Byte> DATA_LIVING_ENTITY_FLAGS;
	public static int LIVING_ENTITY_FLAG_IS_USING = 1;

	public static IRegistry<MobEffectList> MOB_EFFECT;
	public static RegistryBlocks<PotionRegistry> POTION;

	public static String version;
	public static Class CraftPlayer;
	public static Class CraftInventory;
	public static Class CraftWorld;
	public static Class CraftHumanEntity;
	public static Class CraftEntity;
	public static Class CraftItemStack;
	public static Class CraftMagicNumbers;

	public static Field EntityPlayer_invulnerableTicks = null;
	public static Field EntityPlayer_playerConnection;
	public static Field EntityHuman_playerAbilities;
	public static Field RegistryMaterials_frozen;
	public static Field MinecraftServer_playerList;
	public static Field PlayerList_players;
	public static Field WorldServer_players;
	public static Method PotionBrewer_register;
	public static Method PotionUtil_getPotion;
	public static Method PlayerConnection_sendPacket;
	public static Method Entity_getEntityData;
	public static Method EntityData_get;
	public static Method Item_get;
	public static Method Item_releaseUsing;
	public static Method WorldServer_addPlayer;
	public static Method WorldServer_removePlayer;
	public static Method WorldServer_getChunkProviderServer;
	public static Method ChunkProviderServer_move;

	static {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		CraftPlayer = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
		CraftInventory = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftInventory");
		CraftWorld = loadClass("org.bukkit.craftbukkit." + version + ".CraftWorld");
		CraftHumanEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity");
		CraftEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");
		CraftItemStack = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
		CraftMagicNumbers = loadClass("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");

		MOB_EFFECT = (IRegistry<MobEffectList>) getValue(getParameterizedField(IRegistry.class, IRegistry.class, MobEffectList.class), null);
		POTION = (RegistryBlocks<PotionRegistry>) getValue(getParameterizedField(IRegistry.class, RegistryBlocks.class, PotionRegistry.class), null);
		DATA_LIVING_ENTITY_FLAGS = (DataWatcherObject<Byte>) getValue(getParameterizedField(EntityLiving.class, DataWatcherObject.class, Byte.class), null);

		//Fuck it I am not interested in updating stuff every time
		//So I will just search fields and methods by their types and arguments

		EntityPlayer_playerConnection = getField(EntityPlayer.class, PlayerConnection.class);
		EntityHuman_playerAbilities = getField(EntityHuman.class, PlayerAbilities.class);
		RegistryMaterials_frozen = getField(RegistryMaterials.class, boolean.class);
		MinecraftServer_playerList = getField(MinecraftServer.class, PlayerList.class);
		PlayerList_players = getParameterizedField(PlayerList.class, List.class, EntityPlayer.class);
		WorldServer_players = getParameterizedField(WorldServer.class, List.class, EntityPlayer.class);

		PotionBrewer_register = findMethod(true, PotionBrewer.class, Void.TYPE, PotionRegistry.class, Item.class, PotionRegistry.class);
		PotionUtil_getPotion = findMethod(false, PotionUtil.class, PotionRegistry.class, net.minecraft.world.item.ItemStack.class);
		PlayerConnection_sendPacket = findMethod(false, PlayerConnection.class, null, Packet.class);
		Entity_getEntityData = findMethod(false, net.minecraft.world.entity.Entity.class, DataWatcher.class);
		EntityData_get = findMethod(false, DataWatcher.class, Object.class, DataWatcherObject.class);
		Item_get = findMethod(false, Item.class, Item.class, int.class);
		Item_releaseUsing = findMethod(false, Item.class, Void.TYPE, net.minecraft.world.item.ItemStack.class, net.minecraft.world.level.World.class, net.minecraft.world.entity.EntityLiving.class, int.class);
		WorldServer_addPlayer = findMethod(false, WorldServer.class, Void.TYPE, EntityPlayer.class);
		WorldServer_removePlayer = findMethod(false, WorldServer.class, Void.TYPE, EntityPlayer.class, RemovalReason.class);
		WorldServer_getChunkProviderServer = findMethod(false, WorldServer.class, ChunkProviderServer.class);
		ChunkProviderServer_move = findMethod(false, ChunkProviderServer.class, Void.TYPE, EntityPlayer.class);
	}

	public static Class loadClass(String arg0) {
		try {
			return Class.forName(arg0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Method getMethod(Class clazz, String name, Class... parameters) {
		try {
			Method method = clazz.getDeclaredMethod(name, parameters);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Method findMethod(boolean isPrivate, Class clazz, Class rType, Class... parameters) {
		for (Method method : isPrivate ? clazz.getDeclaredMethods() : clazz.getMethods()) {
			if (method.getParameterCount() != parameters.length) { continue; }
			if ((rType != null) && !method.getReturnType().equals(rType)) { continue; }

			boolean match = true;
			for (int i = 0; i < parameters.length; i++) {
				match = method.getParameterTypes()[i].equals(parameters[i]);
				if (!match) { break; }
			}

			if (!match) { continue; }
			if (isPrivate) { method.setAccessible(true); }
			return method;
		}

		return null;
	}

	public static Field getField(Class clazz, Class fType) {
		for (Field field : clazz.getDeclaredFields()) {
			if (!field.getType().equals(fType)) { continue; }
			field.setAccessible(true);
			return field;
		}

		return null;
	}

	public static Object getValue(Field field, Object obj) {
		try {
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Field getParameterizedField(Class clazz, Class fType, Class gType) {
		for (Field field : clazz.getDeclaredFields()) {
			if (!field.getType().equals(fType)) { continue; }
			Type genericType = field.getGenericType();
			if (!(genericType instanceof ParameterizedType)) { continue; }
			Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
			if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
			if (!((Class) argTypes[0]).equals(gType)) { continue; }

			field.setAccessible(true);
			return field;
		}

		return null;
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

	public static net.minecraft.world.entity.Entity getEntity(Entity entity) {
		try {
			Object craftEntity = CraftEntity.cast(entity);
			return (net.minecraft.world.entity.Entity) entity.getClass().getDeclaredMethod("getHandle").invoke(craftEntity);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
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

	public static void setInvulnerableTicks(Player player, int ticks) {
		EntityPlayer entityPlayer = getEntityPlayer(player);

		if (EntityPlayer_invulnerableTicks == null) {
			for (Field field : EntityPlayer.class.getDeclaredFields()) {
				try {
					if (!field.getType().equals(int.class)) { continue; }
					if (((int) field.get(entityPlayer)) != 60) { continue; }
					EntityPlayer_invulnerableTicks = field;
					break;
				} catch (IllegalArgumentException | IllegalAccessException e) {}
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
			PlayerConnection_sendPacket.invoke(connection, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
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
		//    boolean instabuild -> d

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

	public static boolean isUsingItem(Player player) {
		try {
			DataWatcher entityData = (DataWatcher) Entity_getEntityData.invoke(getEntityPlayer(player));
			Byte entityFlags = (Byte) EntityData_get.invoke(entityData, DATA_LIVING_ENTITY_FLAGS);
			return (entityFlags & LIVING_ENTITY_FLAG_IS_USING) > 0;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}

		//Yes solution bellow is much easier, but I don't want to update it every time
		//So I will better search for everything by types and use reflection

		//net.minecraft.world.entity.LivingEntity ->
		//    boolean isUsingItem() -> eU
		//return getEntityPlayer(player).eU();
	}

	public static void shootBow(Player player, ItemStack bow, int ticks) {
		try {
			net.minecraft.world.item.ItemStack item = ReflectionUtils.asNMSCopy(bow);
			net.minecraft.world.level.World world = ReflectionUtils.getWorld(player.getWorld());
			EntityPlayer entityPlayer = ReflectionUtils.getEntityPlayer(player);
			Item_releaseUsing.invoke(Item_get.invoke(null, 718), item, world, entityPlayer, (72000 - ticks));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static Player addFakePlayer(Location location, boolean hideOnline, boolean hideWorld) {
		MinecraftServer server = MinecraftServer.getServer();
		WorldServer world = (WorldServer) getWorld(location.getWorld());
		EntityPlayer entityPlayer = new EntityPlayer(server, world, new GameProfile(UUID.randomUUID(), ""), null);
		PlayerConnection connection = new PlayerConnection(server, new NetworkManager(EnumProtocolDirection.b), entityPlayer) {};
		Player player = entityPlayer.getBukkitEntity();

		try {
			EntityPlayer_playerConnection.set(entityPlayer, connection);
			WorldServer_addPlayer.invoke(world, entityPlayer);

			//Will hide from Bukkit.getOnlinePlayers()
			if (hideOnline) {
				Object playerList = MinecraftServer_playerList.get(server);
				List<EntityPlayer> players = (List<EntityPlayer>) PlayerList_players.get(playerList);
				players.removeIf(e -> e.getBukkitEntity().getUniqueId().equals(player.getUniqueId()));
			}

			//Will hide from World.getPlayers(), but also will prevent mob spawning
			if (hideWorld) {
				List<EntityPlayer> players = (List<EntityPlayer>) WorldServer_players.get(world);
				players.removeIf(e -> e.getBukkitEntity().getUniqueId().equals(player.getUniqueId()));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			return null;
		}

		return player;
	}

	public static void removeFakePlayer(Player player) {
		try {
			EntityPlayer entityPlayer = getEntityPlayer(player);
			WorldServer world = (WorldServer) getWorld(player.getWorld());
			WorldServer_removePlayer.invoke(world, entityPlayer, RemovalReason.b);
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

	public static void setRegistryFrozen(Object registry, boolean frozen) {
        try {
        	RegistryMaterials_frozen.set(registry, frozen);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
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
}
