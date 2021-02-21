package me.wobbychip.nogravityarmourstand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public Boolean gravity = false;
	public Boolean pluginEnabled = false;
	public int MaximumDistance = 0;

	public String enableMessage = new String();
	public String permissionMessage = new String();
	public String incorrectArgument = new String();
	public String reloadMessage = new String();
	public String usageMessage = new String();
	
	public String onMessage = new String();
	public String alreadyOnMessage = new String();
	public String offMessage = new String();
	public String alreadyOffMessage = new String();
	
	public void loadConfig() {
		pluginEnabled = this.getConfig().getBoolean("Enabled");
		MaximumDistance = this.getConfig().getInt("MaximumDistance");

		enableMessage = this.getConfig().getString("enableMessage");
		permissionMessage = this.getConfig().getString("permissionMessage");
		incorrectArgument = this.getConfig().getString("incorrectArgument");
		reloadMessage = this.getConfig().getString("reloadMessage");
		usageMessage = this.getConfig().getString("usageMessage");

		onMessage = this.getConfig().getString("onMessage");
		alreadyOnMessage = this.getConfig().getString("alreadyOnMessage");
		offMessage = this.getConfig().getString("offMessage");
		alreadyOffMessage = this.getConfig().getString("alreadyOffMessage");
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
    private Player NearetPlayer(Location location, int maximumDistance) {
        Player best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Player player : location.getWorld().getPlayers()) {
        	double distance = location.distance(player.getLocation());

            if ((distance < bestDistance) && (distance < maximumDistance)) {
                best = player;
                bestDistance = distance;
            }
        }

        return best;
    }

	//NoGravityArmourStand commands list
	public void NoGravityArmourStandCommands(CommandSender sender,  String[] args) {
		//Check if args is empty
		if (args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMessage));
			return;
		}

		//Turn on NoGravityArmourStand
		if (args[0].equalsIgnoreCase("on")) {
			if (!CheckPermissions(sender, "nogravityarmourstand.toggle")) { return; }

			if (pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOnMessage));
				return;
			}

			pluginEnabled = true;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onMessage));
			return;
		}

		//Turn off NoGravityArmourStand
		if (args[0].equalsIgnoreCase("off")) {
			if (!CheckPermissions(sender, "nogravityarmourstand.toggle")) { return; }

			if (!pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOffMessage));
				return;
			}

			pluginEnabled = false;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', offMessage));
			return;
		}

		//Reload NoGravityArmourStand
		if (args[0].equalsIgnoreCase("reload")) {
			if (!CheckPermissions(sender, "nogravityarmourstand.reload")) {return; }

			this.reloadConfig();
			loadConfig();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMessage));
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
		this.saveConfig();
    }

	//When command executed
	public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
		if (str.equalsIgnoreCase("nogravityarmourstand")) {
			NoGravityArmourStandCommands(sender, args);
			return true;
		}

		return false;
	}

	//Entity spawn event
	@EventHandler(priority=EventPriority.HIGH)
	public void onEntitySpawn(CreatureSpawnEvent event) {
	    Entity entity = event.getEntity();
	    if ((entity instanceof ArmorStand) && pluginEnabled) {
	    	//Get nearest player to entity
	    	Player player = NearetPlayer(event.getLocation(), MaximumDistance);

	    	//If player not found just return
	    	if (player == null) {
	    		entity.setGravity(false);
	    		return;
	    	}

	    	//Check if player has bypass permissions
	    	if (player.hasPermission("nogravityarmourstand.bypass")) {
	    		gravity = true;
	    	} else {
	    		gravity = false;
	    	}
	    	
	    	//Give time for armour stand to rotate to player position
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	            public void run() {
	            	entity.setGravity(gravity);
	            }
	        }, 5L);
	    }
	}
}