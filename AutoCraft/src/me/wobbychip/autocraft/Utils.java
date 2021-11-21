package me.wobbychip.autocraft;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utils {
	@SuppressWarnings("deprecation")
	public static String iTs(int x) {
		return new Integer(x).toString();
	}

	@SuppressWarnings("deprecation")
	public static String bTs(boolean x) {
		return new Boolean(x).toString();
	}

	public static void DebugInfo(String message) {
		if (Main.plugin.getConfig().getBoolean("debug")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	public static void sendMessage(String arg0) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', arg0));
	}

	public static Material getWorkbenchMaterial() {
		Material material = Material.getMaterial("WORKBENCH");
		return material == null ? Material.getMaterial("CRAFTING_TABLE") : material;
	}

	public static Entity getEntityByUUID(UUID uuid, World world) {
		for (Entity entity : world.getEntities()) {
			if (entity.getUniqueId().equals(uuid)) {
				return entity;
			}
		}
		return null;
	}

	public static boolean canAdd(Inventory inv, ItemStack item) {
		if (inv.firstEmpty() != -1) { return true; }

		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack invItem = inv.getItem(i);
			if (invItem.isSimilar(item)) {
				if (invItem.getAmount()+item.getAmount() <= invItem.getMaxStackSize()) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean locationInChunk(Location loc, Chunk chunk) {
		return ((chunk.getWorld() == loc.getWorld()) && (chunk.getX() == (loc.getBlockX() >> 4)) && (chunk.getZ() == (loc.getBlockZ() >> 4)));
	}

	public static boolean isDummyItem(ItemStack item) {
		return ((item != null) && (item.getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE) && (item.getItemMeta() != null) && item.getItemMeta().isUnbreakable());
	}
}
