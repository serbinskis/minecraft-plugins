package me.serbinskis.smptweaks.utils;

import me.serbinskis.smptweaks.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PersistentUtils {
	//Entity
	public static void removePersistentData(Entity entity, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		entity.getPersistentDataContainer().remove(namespacedKey);
	}

	public static boolean hasPersistentDataInteger(Entity entity, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return entity.getPersistentDataContainer().has(namespacedKey, PersistentDataType.INTEGER);
	}

	public static int getPersistentDataInteger(Entity entity, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return entity.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
	}

	public static void setPersistentDataInteger(Entity entity, String name, Integer value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		entity.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
	}

	public static boolean hasPersistentDataBoolean(Entity entity, String name) {
		return hasPersistentDataInteger(entity, name);
	}

	public static boolean getPersistentDataBoolean(Entity entity, String name) {
		return (getPersistentDataInteger(entity, name) > 0);
	}

	public static void setPersistentDataBoolean(Entity entity, String name, boolean value) {
		setPersistentDataInteger(entity, name, value ? 1 : 0);
	}

	public static boolean hasPersistentDataString(Entity entity, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return entity.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
	}

	public static String getPersistentDataString(Entity entity, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return entity.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
	}

	public static void setPersistentDataString(Entity entity, String name, String value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		entity.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
	}

	//Block - not all
	public static void removePersistentData(Block block, String name) {
		removePersistentData(block.getState(), name);
	}

	public static boolean hasPersistentDataInteger(Block block, String name) {
		return hasPersistentDataInteger(block.getState(), name);
	}

	public static int getPersistentDataInteger(Block block, String name) {
		return getPersistentDataInteger(block.getState(), name);
	}

	public static void setPersistentDataInteger(Block block, String name, Integer value) {
		setPersistentDataInteger(block.getState(), name, value);
	}

	public static boolean hasPersistentDataBoolean(Block block, String name) {
		return hasPersistentDataInteger(block, name);
	}

	public static boolean getPersistentDataBoolean(Block block, String name) {
		return (getPersistentDataInteger(block, name) > 0);
	}

	public static void setPersistentDataBoolean(Block block, String name, boolean value) {
		setPersistentDataInteger(block, name, value ? 1 : 0);
	}

	public static boolean hasPersistentDataString(Block block, String name) {
		return hasPersistentDataString(block.getState(), name);
	}

	public static String getPersistentDataString(Block block, String name) {
		return getPersistentDataString(block.getState(), name);
	}

	public static void setPersistentDataString(Block block, String name, String value) {
		setPersistentDataString(block.getState(), name, value);
	}

	public static boolean hasPersistentDataByteArray(Block block, String name) {
		return hasPersistentDataByteArray(block.getState(), name);
	}

	public static byte[] getPersistentDataByteArray(Block block, String name) {
		return getPersistentDataByteArray(block.getState(), name);
	}

	public static void setPersistentDataByteArray(Block block, String name, byte[] value) {
		setPersistentDataByteArray(block.getState(), name, value);
	}

	//BlockState - not all
	public static void removePersistentData(BlockState state, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		((TileState) state).getPersistentDataContainer().remove(namespacedKey);
		state.update();
	}

	public static boolean hasPersistentDataInteger(BlockState state, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return ((TileState) state).getPersistentDataContainer().has(namespacedKey, PersistentDataType.INTEGER);
	}

	public static int getPersistentDataInteger(BlockState state, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return ((TileState) state).getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
	}

	public static void setPersistentDataInteger(BlockState state, String name, Integer value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		((TileState) state).getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
		state.update();
	}

	public static boolean hasPersistentDataBoolean(BlockState state, String name) {
		return hasPersistentDataInteger(state, name);
	}

	public static boolean getPersistentDataBoolean(BlockState state, String name) {
		return (getPersistentDataInteger(state, name) > 0);
	}

	public static void setPersistentDataBoolean(BlockState state, String name, boolean value) {
		setPersistentDataInteger(state, name, value ? 1 : 0);
	}

	public static boolean hasPersistentDataString(BlockState state, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return ((TileState) state).getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
	}

	public static String getPersistentDataString(BlockState state, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return ((TileState) state).getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
	}

	public static void setPersistentDataString(BlockState state, String name, String value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		((TileState) state).getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
		state.update();
	}

	public static boolean hasPersistentDataByteArray(BlockState state, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return ((TileState) state).getPersistentDataContainer().has(namespacedKey, PersistentDataType.BYTE_ARRAY);
	}

	public static byte[] getPersistentDataByteArray(BlockState state, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return ((TileState) state).getPersistentDataContainer().get(namespacedKey, PersistentDataType.BYTE_ARRAY);
	}

	public static void setPersistentDataByteArray(BlockState state, String name, byte[] value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		((TileState) state).getPersistentDataContainer().set(namespacedKey, PersistentDataType.BYTE_ARRAY, value);
		state.update();
	}

	//ItemStack
	public static ItemStack removePersistentData(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().remove(namespacedKey);
		item.setItemMeta(itemMeta);
		return item;
	}

	public static boolean hasPersistentDataInteger(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.INTEGER);
	}

	public static int getPersistentDataInteger(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
	}

	public static ItemStack setPersistentDataInteger(ItemStack item, String name, Integer value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
		item.setItemMeta(itemMeta);
		return item;
	}

	public static boolean hasPersistentDataBoolean(ItemStack item, String name) {
		return hasPersistentDataInteger(item, name);
	}

	public static boolean getPersistentDataBoolean(ItemStack item, String name) {
		return (getPersistentDataInteger(item, name) > 0);
	}

	public static ItemStack setPersistentDataBoolean(ItemStack item, String name, boolean value) {
		return setPersistentDataInteger(item, name, value ? 1 : 0);
	}

	public static boolean hasPersistentDataString(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
	}

	public static String getPersistentDataString(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
	}

	public static ItemStack setPersistentDataString(ItemStack item, String name, String value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
		item.setItemMeta(itemMeta);
		return item;
	}

	//World
	public static void removePersistentData(World world, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		world.getPersistentDataContainer().remove(namespacedKey);
	}

	public static boolean hasPersistentDataInteger(World world, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return world.getPersistentDataContainer().has(namespacedKey, PersistentDataType.INTEGER);
	}

	public static int getPersistentDataInteger(World world, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return world.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
	}

	public static void setPersistentDataInteger(World world, String name, Integer value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		world.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
	}

	public static boolean hasPersistentDataBoolean(World world, String name) {
		return hasPersistentDataInteger(world, name);
	}

	public static boolean getPersistentDataBoolean(World world, String name) {
		return (getPersistentDataInteger(world, name) > 0);
	}

	public static void setPersistentDataBoolean(World world, String name, boolean value) {
		setPersistentDataInteger(world, name, value ? 1 : 0);
	}

	public static boolean hasPersistentDataString(World world, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return world.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
	}

	public static String getPersistentDataString(World world, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return world.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
	}

	public static void setPersistentDataString(World world, String name, String value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		world.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
	}
}
