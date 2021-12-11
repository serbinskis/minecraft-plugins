package me.wobbychip.recallpotion.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Utils {
	public static void sendMessage(String arg0) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', arg0));
	}

	public static void respawnPlayer(Player player) {
		Location location = player.getBedSpawnLocation();

		if (location == null) {
			World world = Bukkit.getServer().getWorlds().get(0);
			location = world.getSpawnLocation().clone().add(.5, 0, .5);
			while ((location.getY() >= world.getMinHeight()) && (location.getBlock().getType() == Material.AIR)) { location.setY(location.getY()-1); }
			while ((location.getY() < world.getMaxHeight()) && (location.getBlock().getType() != Material.AIR)) { location.setY(location.getY()+1); }
		}

		location.setDirection(player.getLocation().getDirection());
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
	}

	public static boolean isPotion(ItemStack itemStack) {
		return ((itemStack != null) && ((itemStack.getType() == Material.POTION) || (itemStack.getType() == Material.SPLASH_POTION) || (itemStack.getType() == Material.LINGERING_POTION)));
	}

	public static boolean isTippedArrow(ItemStack itemStack) {
		return ((itemStack != null) && ((itemStack.getType() == Material.TIPPED_ARROW)));
	}
}
