package me.wobbychip.entitylimit;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Utilities {
	//Get string from config
	static String getString(String arg0) {
		return Main.plugin.getConfig().getString(arg0);
	}

	//Send message to sender
	static void SendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	//Check if player has permissions
	static boolean CheckPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission(permission)) {
				SendMessage(sender, getString("permissionMessage"));
				return false;
			}
		}

		return true;
	}

	//Find nearest player to entity
	static Player NearetPlayer(Location location) {
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

	//Find nearest entities count to entity
	static int GetNearestEntitiesCount(Location location, EntityType type, int Distance) {
    	int Height = location.getWorld().getMaxHeight()*2;
    	Collection<Entity> nearbyEntites = location.getWorld().getNearbyEntities(location, Distance, Height, Distance);
    	int count = 0;

        for (Entity entity : nearbyEntites) {
        	if (entity.getType() == type) {
            	count += 1;
            }
        }

        return count;
	}

    //Check entity 
    @SuppressWarnings("deprecation")
	static boolean checkEntityLimit(EntityType type, Location location) {
    	//Get nearest player to entity
    	Player player = NearetPlayer(location);

    	//Check if player has bypass permissions
    	if ((player != null) && player.hasPermission("entitylimit.bypass")) {
    		return false;
    	}

    	//Get entity count
	    int NearbyEntities = GetNearestEntitiesCount(location, type, Main.maximumDistance);

	    //Cancel entity if count is over limit
	    if (NearbyEntities > Main.Limit) {
	    	if (player != null) {
				String replacedMessage = Utilities.getString("tooManyEntity").replace("%value%", new Integer(Main.Limit).toString());
				Utilities.SendMessage(player, replacedMessage);
	    	}

	    	return true;
	    }

	    return false;
    }
}
