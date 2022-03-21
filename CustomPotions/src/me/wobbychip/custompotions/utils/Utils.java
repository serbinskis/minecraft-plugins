package me.wobbychip.custompotions.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Utils {
	public static void sendMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static void sendActionMessage(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
	}

	public static boolean hasPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			return ((Player) sender).hasPermission(permission);
		}

		return true;
	}

	public static double randomRange(double min, double max) {
		return min + Math.random() * (max - min);
    }

	public static boolean isPotion(ItemStack itemStack) {
		return ((itemStack != null) && ((itemStack.getType() == Material.POTION) || (itemStack.getType() == Material.SPLASH_POTION) || (itemStack.getType() == Material.LINGERING_POTION)));
	}

	public static boolean isTippedArrow(ItemStack itemStack) {
		return ((itemStack != null) && ((itemStack.getType() == Material.TIPPED_ARROW)));
	}

	public static String getMaterialName(Material material) {
		return toTitleCase(material.name().replace("_", " "));
	}

	public static String toTitleCase(String s) {
		if (s == null || s.isEmpty()) { return ""; }
		if (s.length() == 1) { return s.toUpperCase(); }

		String[] parts = s.split(" ");
		StringBuilder sb = new StringBuilder(s.length());
        
		for (String part : parts) {
			if (part.length() > 1) {
				sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());                
			} else {
				sb.append(part.toUpperCase());
			}

			sb.append(" ");
		}

		return sb.toString().trim();
	}
}