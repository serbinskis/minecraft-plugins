package me.wobbychip.smptweaks.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

import me.wobbychip.smptweaks.Main;

public class PersistentUtils {
	//Entity
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
		return (getPersistentDataInteger(entity, name) > 0) ? true : false;
	}

	public static void setPersistentDataBoolean(Entity entity, String name, boolean value) {
		setPersistentDataInteger(entity, name, value ? 1 : 0);
	}

	//Block - not all
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
		return (getPersistentDataInteger(block, name) > 0) ? true : false;
	}

	public static void setPersistentDataBoolean(Block block, String name, boolean value) {
		setPersistentDataInteger(block, name, value ? 1 : 0);
	}
}
