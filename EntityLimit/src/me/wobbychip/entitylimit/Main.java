package me.wobbychip.entitylimit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public List<String> ExcludeReason = new ArrayList<>();
	public Boolean pluginEnabled = false;
	public int MaximumDistance = 0;
	public int Limit = 0;

	public String enableMessage = new String();
	public String permissionMessage = new String();
	public String incorrectArgument = new String();
	public String reloadMessage = new String();
	public String usageMessage = new String();
	
	public String onMessage = new String();
	public String alreadyOnMessage = new String();
	public String offMessage = new String();
	public String alreadyOffMessage = new String();

	public String limitMessage = new String();
	public String distanceMessage = new String();
	public String tooManyEntity = new String();
	
	public void loadConfig() {
		ExcludeReason = this.getConfig().getStringList("ExcludeReason");
		pluginEnabled = this.getConfig().getBoolean("Enabled");
		MaximumDistance = this.getConfig().getInt("MaximumDistance");
		Limit = this.getConfig().getInt("Limit");

		enableMessage = this.getConfig().getString("enableMessage");
		permissionMessage = this.getConfig().getString("permissionMessage");
		incorrectArgument = this.getConfig().getString("incorrectArgument");
		reloadMessage = this.getConfig().getString("reloadMessage");
		usageMessage = this.getConfig().getString("usageMessage");

		onMessage = this.getConfig().getString("onMessage");
		alreadyOnMessage = this.getConfig().getString("alreadyOnMessage");
		offMessage = this.getConfig().getString("offMessage");
		alreadyOffMessage = this.getConfig().getString("alreadyOffMessage");

		limitMessage = this.getConfig().getString("limitMessage");
		distanceMessage = this.getConfig().getString("distanceMessage");
		tooManyEntity = this.getConfig().getString("tooManyEntity");
	}

//==============================================================================================================================================
//==============================================================================================================================================
//==============================================================================================================================================
	
	//Check if player has permissions
	public boolean CheckPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission(permission)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', permissionMessage));
				return false;
			}
		}

		return true;
	}

	//Find nearest player to entity
	private Player NearetPlayer(Location location) {
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
	private int GetNearestEntitiesCount(Location location, EntityType type, int Distance) {
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
	public boolean CheckEntityLimit(EntityType type, Location location) {
    	//Get nearest player to entity
    	Player player = NearetPlayer(location);

    	//Check if player has bypass permissions
    	if ((player != null) && player.hasPermission("entitylimit.bypass")) {
    		return false;
    	}

    	//Get entity count
	    int NearbyEntities = GetNearestEntitiesCount(location, type, MaximumDistance);

	    //Cancel entity if count is over limit
	    if (NearbyEntities > Limit) {
	    	if (player != null) {
				String replacedMessage = tooManyEntity.replace("%value%", new Integer(Limit).toString());
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));
	    	}

	    	return true;
	    }

	    return false;
    }

	//check commands list
	@SuppressWarnings("deprecation")
	public void CheckCommands(CommandSender sender,  String[] args) {
		//Check if args is empty
		if (args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMessage));
			return;
		}

		//Turn on EntityLimit
		if (args[0].equalsIgnoreCase("on")) {
			if (!CheckPermissions(sender, "entitylimit.toggle")) { return; }

			if (pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOnMessage));
				return;
			}

			pluginEnabled = true;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onMessage));
			return;
		}

		//Turn off EntityLimit
		if (args[0].equalsIgnoreCase("off")) {
			if (!CheckPermissions(sender, "entitylimit.toggle")) { return; }

			if (!pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOffMessage));
				return;
			}

			pluginEnabled = false;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', offMessage));
			return;
		}

		//Reload EntityLimit
		if (args[0].equalsIgnoreCase("reload")) {
			if (!CheckPermissions(sender, "entitylimit.reload")) {return; }

			this.reloadConfig();
			loadConfig();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMessage));
			return;
		}

		//Set entity limit
		if (args[0].equalsIgnoreCase("limit")) {
			if (!CheckPermissions(sender, "entitylimit.limit")) {return; }

			if ((args.length < 2) || !args[1].matches("-?\\d+") || (args[1].length() > 4)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', incorrectArgument));
				return;
			}

			Limit = Integer.parseInt(args[1]);
			if (Limit < 0) { Limit = 0; }

			String replacedMessage = limitMessage.replace("%value%", new Integer(Limit).toString());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));
			return;
		}

		//Set maximum distance
		if (args[0].equalsIgnoreCase("distance")) {
			if (!CheckPermissions(sender, "entitylimit.distance")) {return; }

			if ((args.length < 2) || !args[1].matches("-?\\d+") || (args[1].length() > 4)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', incorrectArgument));
				return;
			}

			MaximumDistance = Integer.parseInt(args[1]);
			if (MaximumDistance < 0) { MaximumDistance = 0; }

			String replacedMessage = distanceMessage.replace("%value%", new Integer(MaximumDistance).toString());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));
			return;
		}

		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMessage));
		return;
	}

//==============================================================================================================================================
//==============================================================================================================================================
//==============================================================================================================================================

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
		loadConfig();
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', enableMessage));
	}

	@Override
    public void onDisable() {
		this.getConfig().set("Enabled", pluginEnabled);
		this.getConfig().set("Limit", Limit);
		this.getConfig().set("MaximumDistance", MaximumDistance);
		this.saveConfig();
    }

	//When command executed
	public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
		if (str.equalsIgnoreCase("entitylimit")) {
			CheckCommands(sender, args);
			return true;
		}

		return false;
	}

	//Creature spawn event
	@EventHandler(priority=EventPriority.HIGH)
	public void onCreatureSpawnn(CreatureSpawnEvent event) {
		//Return if disabled or spawn reason is excluded
		if (!pluginEnabled || ExcludeReason.contains(event.getSpawnReason().toString())) {
			return;
		}

		//Check for entity limit
		if (CheckEntityLimit(event.getEntity().getType(), event.getEntity().getLocation())) {
			event.setCancelled(true);
		}
	}
}