package me.wobbychip.smptweaks.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Utils {
	public static String delimiter = "#";

	//Send message to console
	public static void sendMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	//Send message to sender
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	//Send message to action bar
	public static void sendActionMessage(net.md_5.bungee.api.ChatColor color, Player player, String message) {
		TextComponent text = new TextComponent(message);
		if (color != null) { text.setColor(color); }
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
	}

	//Get string from config
	public static String getString(String arg0, Config config) {
		return config.getConfig().getString(arg0);
	}

	//Generate random range integer
	public static int randomRange(int min, int max) {
		return min + (int) (Math.random() * (max - min+1));
	}

	//Generate random range double
	public static double randomRange(double min, double max) {
		return min + Math.random() * (max - min);
	}

	//Return number after decimal, not precise tho
	public static double afterDecimal(double x) {
		return x - Math.floor(x);
	}

	//Calculate total experience up to a level
	public static int getExpAtLevel(int level) {
		if (level <= 16) {
			return (int) (Math.pow(level,2) + 6*level);
		} else if (level <= 31) {
			return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
		} else {
			return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
		}
	}

	//Calculate players current EXP amount
	public static int getPlayerExp(Player player) {
		int level = player.getLevel();
		int exp = getExpAtLevel(level);
		exp += Math.round(player.getExpToLevel() * player.getExp());
		return exp;
	}

	//Calculate experience reward on death
	public static int getExperienceReward(Player player, boolean dropAllXp) {
		if (player.getGameMode() != GameMode.SPECTATOR) {
			if (dropAllXp) {
				return getPlayerExp(player);
			} else {
				int i = player.getLevel() * 7;
				return i > 100 ? 100 : i;
			}
		}

		return 0;
	}

	//Drop item from player position
	public static void dropItem(Player player, ItemStack item) {
		Location location = player.getLocation();
		location.setY(location.getY()+1.3);

		Vector vector = player.getLocation().getDirection();
		vector.multiply(0.32);

		Item itemDropped = player.getWorld().dropItem(location, item);
		itemDropped.setVelocity(vector);
		itemDropped.setPickupDelay(40);
	}

	//Check for permissions
	public static boolean hasPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			return ((Player) sender).hasPermission(permission);
		}

		return true;
	}

	//Get distance between two locations
	public static double distance(Location l1, Location l2) {
		double x = Math.pow(l1.getX()-l2.getX(), 2);
		double y = Math.pow(l1.getY()-l2.getY(), 2);
		double z = Math.pow(l1.getZ()-l2.getZ(), 2);
		return Math.sqrt(x+y+z);
	}

	//Check if itemstack is a potion
	public static boolean isPotion(ItemStack itemStack) {
		return ((itemStack != null) && ((itemStack.getType() == Material.POTION) || (itemStack.getType() == Material.SPLASH_POTION) || (itemStack.getType() == Material.LINGERING_POTION)));
	}

	//Check if itemstack is a tipped arrow
	public static boolean isTippedArrow(ItemStack itemStack) {
		return ((itemStack != null) && (itemStack.getType() == Material.TIPPED_ARROW));
	}

	//Get material name
	public static String getMaterialName(Material material) {
		return toTitleCase(material.name().replace("_", " "));
	}

	//Convert string to title case
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

	//Get nearest player to entity
	public static Player getNearetPlayer(Location location) {
		Player best = null;
		double bestDistance = Double.MAX_VALUE;

		for (Player player : location.getWorld().getPlayers()) {
			double distance = location.distance(player.getLocation());

			if (distance < bestDistance) {
				best = player;
				bestDistance = distance;
			}
		}

		return best;
	}

	//Get nearest entities
	public static Collection<Entity> getNearestEntities(Location location, EntityType type, double distance, boolean maxHeight) {
		double height = maxHeight ? location.getWorld().getMaxHeight()*2 : distance;
		Collection<Entity> nearbyEntites = location.getWorld().getNearbyEntities(location, distance, height, distance);
		Iterator<Entity> iterator = nearbyEntites.iterator();

		while (iterator.hasNext()) {
			if (iterator.next().getType() != type) {
				iterator.remove();
			}
		}

		return nearbyEntites;
	}

	//Get nearest blocks
	public static List<Block> getNearestBlocks(Location location, Material type, double radius) {
		List<Block> nearbyBlock = new ArrayList<>();

		double pX = location.getX();
		double pY = location.getY();
		double pZ = location.getZ();

		for (double x = -radius; x <= radius; x++) {
			for (double y = -radius; y <= radius; y++) {
				for (double z = -radius; z <= radius; z++) {
					Block block = location.getWorld().getBlockAt((int) (pX+x), (int) (pY+y), (int) (pZ+z));
					if (block.getType() == type) { nearbyBlock.add(block); }
				}
			}
		}

		return nearbyBlock;
	}

	//Get attacker from entity
	public static Player getAttacker(Entity entity) {
		if (entity instanceof Player) { return ((Player) entity); }

		if ((entity instanceof Projectile)) {
			ProjectileSource attacker = ((Projectile) entity).getShooter();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof AreaEffectCloud)) {
			ProjectileSource attacker = ((AreaEffectCloud) entity).getSource();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof TNTPrimed)) {
			Entity attacker = ((TNTPrimed) entity).getSource();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof Tameable)) {
			AnimalTamer attacker = ((Tameable) entity).getOwner();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		return null;
	}

	//Set glow color entity
	public static void setGlowColor(Entity entity, ChatColor color) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team;

		if (!scoreboard.getTeams().stream().map(Team::getName).toList().contains(Main.prefix + color.name())) {
			team = scoreboard.registerNewTeam(Main.prefix + color.name());
		} else {
			team = scoreboard.getTeam(Main.prefix + color.name());
		}

		team.setColor(color);
		team.addEntry(entity.getUniqueId().toString());
		entity.setGlowing(!color.equals(ChatColor.RESET));
	}

	//Convert location to string
	public static String locationToString(Location location) {
		return location.getWorld().getName() + delimiter + String.valueOf(location.getX()) + delimiter + String.valueOf(location.getY()) + delimiter +  String.valueOf(location.getZ());
	}

	//String to location
	public static Location stringToLocation(String location) {
		String[] splited = location.split(delimiter, 0);
		return new Location(Bukkit.getWorld(splited[0]), Double.parseDouble(splited[1]), Double.parseDouble(splited[2]), Double.parseDouble(splited[3]));
	}

	//Save resource to file
	public static File saveResource(String configPath, String savePath) {
		File file = new File(Main.plugin.getDataFolder() + savePath);

		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				InputStream inputStream = Main.plugin.getResource(configPath);
				Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}
}