package me.wobbychip.recallpotion.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.recallpotion.Main;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class ReflectionUtil {
	public static String version;
	public static Class<?> CraftPlayer;
	public static Class<?> CraftInventory;
	public static Class<?> CraftWorld;
	public static Class<?> CraftHumanEntity;
	public static Class<?> CraftEntity;
	public static Class<?> CraftItemStack;

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

	public static Method getRegisterBrewMethod() {
		try {
			Method method = PotionBrewer.class.getDeclaredMethod("a", PotionRegistry.class, Item.class, PotionRegistry.class);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
