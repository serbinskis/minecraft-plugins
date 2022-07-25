package me.wobbychip.smptweaks.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;

public class ReflectionUtils {
	private static DataWatcherObject<Byte> DATA_LIVING_ENTITY_FLAGS = null;
	public static int LIVING_ENTITY_FLAG_IS_USING = 1;

	public static String version;
	public static Class<?> CraftPlayer;
	public static Class<?> CraftInventory;
	public static Class<?> CraftWorld;
	public static Class<?> CraftHumanEntity;
	public static Class<?> CraftEntity;
	public static Class<?> CraftItemStack;

	private static Field playerConnection = null;
	private static Field playerAbilities = null;
	private static Method sendPacket = null;
	private static Method getEntityData = null;
	private static Method entityDataGet = null;

	static {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		CraftPlayer = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
		CraftInventory = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftInventory");
		CraftWorld = loadClass("org.bukkit.craftbukkit." + version + ".CraftWorld");
		CraftHumanEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity");
		CraftEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");
		CraftItemStack = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
	}

	public static Class<?> loadClass(String arg0) {
    	try {
    		return Class.forName(arg0);
		} catch (ClassNotFoundException e) {
        	e.printStackTrace();
        	return null;
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
    	try {
			return clazz.getDeclaredMethod(name, parameters);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
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

	public static PlayerAbilities getPlayerAbilities(Player player) {
		if (playerAbilities == null) {
			for (Field field : EntityHuman.class.getDeclaredFields()) {
				if (field.getType().equals(PlayerAbilities.class)) {
					playerAbilities = field;
					playerAbilities.setAccessible(true);
					break;
				}
			}
		}

		try {
			return (PlayerAbilities) playerAbilities.get(getEntityPlayer(player));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object getPlayerConnection(net.minecraft.server.level.EntityPlayer player) {
		if (playerConnection == null) {
			for (Field field : EntityPlayer.class.getDeclaredFields()) {
				if (field.getType().equals(PlayerConnection.class)) {
					playerConnection = field;
					break;
				}
			}
		}

		try {
			return playerConnection.get(player);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void sendPacket(Player player, Packet<?> packet) {
		if (sendPacket == null) {
			for (Method method : PlayerConnection.class.getMethods()) {
				if ((method.getParameterCount() == 1) && method.getParameterTypes()[0].equals(Packet.class)) {
					sendPacket = method;
					break;
				}
			}
		}

		try {
			Object connection = getPlayerConnection(getEntityPlayer(player));
			sendPacket.invoke(connection, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	//Doesn't work in creative, it sets actual item
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean isUsingItem(Player player) {
		if (getEntityData == null) {
			for (Method method : net.minecraft.world.entity.Entity.class.getMethods()) {
				if ((method.getParameterCount() == 0) && method.getReturnType().equals(DataWatcher.class)) {
					getEntityData = method;
					break;
				}
			}
		}

		if (entityDataGet == null) {
			for (Method method : DataWatcher.class.getMethods()) {
				if ((method.getParameterCount() == 1) && method.getParameterTypes()[0].equals(DataWatcherObject.class) && !method.getReturnType().equals(Void.class)) {
					entityDataGet = method;
					break;
				}
			}
		}

		if (DATA_LIVING_ENTITY_FLAGS == null) {
			for (Field field : EntityLiving.class.getDeclaredFields()) {
				if (!field.getType().equals(DataWatcherObject.class)) { continue; }
				Type genericType = field.getGenericType();
				if (!(genericType instanceof ParameterizedType)) { continue; }
				Type[] argTypes = ((ParameterizedType) genericType).getActualTypeArguments();
				if ((argTypes.length == 0) || !(argTypes[0] instanceof Class)) { continue; }
				if (!((Class) argTypes[0]).equals(Byte.class)) { continue; }

				try {
					field.setAccessible(true);
					DATA_LIVING_ENTITY_FLAGS = (DataWatcherObject<Byte>) field.get(null);
					break;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			DataWatcher entityData = (DataWatcher) getEntityData.invoke(getEntityPlayer(player));
			Byte entityFalgs = (Byte) entityDataGet.invoke(entityData, DATA_LIVING_ENTITY_FLAGS);
			return (entityFalgs & LIVING_ENTITY_FLAG_IS_USING) > 0;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}

		//Yes bellow one solution is much easier, but I don't want to update it every time
		//So I will better search for everything by types and use reflection

		//net.minecraft.world.entity.LivingEntity ->
		//    boolean isUsingItem() -> eU
		//return getEntityPlayer(player).eU();*/
	}
}
