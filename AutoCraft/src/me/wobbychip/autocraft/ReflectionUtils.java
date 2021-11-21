package me.wobbychip.autocraft;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReflectionUtils {
	public static String version;
	public static Class<?> CraftPlayer;
	public static Class<?> CraftInventory;
	public static Class<?> CraftWorld;
	public static Class<?> CraftHumanEntity;
	public static Class<?> CraftItemStack;

	public static boolean loadClasses() {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		Utils.sendMessage(Main.versionMessage + version);

		CraftPlayer = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
		if (CraftPlayer == null) { return false; }

		CraftInventory = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftInventory");
		if (CraftInventory == null) { return false; }

		CraftWorld = loadClass("org.bukkit.craftbukkit." + version + ".CraftWorld");
		if (CraftWorld == null) { return false; }

		CraftHumanEntity = loadClass("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity");
		if (CraftHumanEntity == null) { return false; }

		CraftItemStack = loadClass("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
		if (CraftItemStack == null) { return false; }

		return true;
	}

	public static Class<?> loadClass(String arg0) {
    	try {
    		return Class.forName(arg0);
		} catch (ClassNotFoundException e) {
			Utils.sendMessage(Main.classErrorMessage + arg0);
        	Bukkit.getPluginManager().disablePlugin(Main.plugin);
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
}
