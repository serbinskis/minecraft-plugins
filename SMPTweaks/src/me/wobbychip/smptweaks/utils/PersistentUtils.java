package me.wobbychip.smptweaks.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.wobbychip.smptweaks.Main;

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
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		TileState tileState = (TileState) block.getState();
		tileState.getPersistentDataContainer().remove(namespacedKey);
		tileState.update();
	}

	public static boolean hasPersistentDataInteger(Block block, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		TileState tileState = (TileState) block.getState();
		return tileState.getPersistentDataContainer().has(namespacedKey, PersistentDataType.INTEGER);
	}

	public static int getPersistentDataInteger(Block block, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		TileState tileState = (TileState) block.getState();
		return tileState.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
	}

	public static void setPersistentDataInteger(Block block, String name, Integer value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		TileState tileState = (TileState) block.getState();
		tileState.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
		tileState.update();
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
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		TileState tileState = (TileState) block.getState();
		return tileState.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
	}

	public static String getPersistentDataString(Block block, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		TileState tileState = (TileState) block.getState();
		return tileState.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
	}

	public static void setPersistentDataString(Block block, String name, String value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		TileState tileState = (TileState) block.getState();
		tileState.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
		tileState.update();
	}

	//ItemStack
	public static void removePersistentData(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().remove(namespacedKey);
		item.setItemMeta(itemMeta);
	}

	public static boolean hasPersistentDataInteger(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.INTEGER);
	}

	public static int getPersistentDataInteger(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
	}

	public static void setPersistentDataInteger(ItemStack item, String name, Integer value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
		item.setItemMeta(itemMeta);
	}

	public static boolean hasPersistentDataBoolean(ItemStack item, String name) {
		return hasPersistentDataInteger(item, name);
	}

	public static boolean getPersistentDataBoolean(ItemStack item, String name) {
		return (getPersistentDataInteger(item, name) > 0);
	}

	public static void setPersistentDataBoolean(ItemStack item, String name, boolean value) {
		setPersistentDataInteger(item, name, value ? 1 : 0);
	}

	public static boolean hasPersistentDataString(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
	}

	public static String getPersistentDataString(ItemStack item, String name) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		return item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
	}

	public static void setPersistentDataString(ItemStack item, String name, String value) {
		NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, name);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
		item.setItemMeta(itemMeta);
	}
}
