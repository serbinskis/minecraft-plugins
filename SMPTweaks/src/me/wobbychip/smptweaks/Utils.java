package me.wobbychip.smptweaks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Utils {
	//Send message to console
	public static void sendMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	//Send message to sender
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
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

	//Check if player has permissions
	public static boolean hasPermissions(CommandSender sender, String permission, Config config) {
		if (sender instanceof Player) {
			if (!((Player) sender).hasPermission(permission)) {
				sendMessage(sender, getString("permissionMessage", config));
				return false;
			}
		}

		return true;
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
			if (iterator.next().getType() == type) {
				iterator.remove();
			}
		}

        return nearbyEntites;
	}

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
