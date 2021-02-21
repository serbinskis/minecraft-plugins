package me.wobbychip.perplayerkeepinventory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public Boolean pluginEnabled = false;

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

	//Check commands list
	public void CheckCommands(CommandSender sender,  String[] args) {
		//Check if args is empty
		if (args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMessage));
			return;
		}

		//Turn on PerPlayerKeepInventory
		if (args[0].equalsIgnoreCase("on")) {
			if (!CheckPermissions(sender, "perplayerkeepinventory.toggle")) { return; }

			if (pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOnMessage));
				return;
			}

			pluginEnabled = true;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onMessage));
			return;
		}

		//Turn off PerPlayerKeepInventory
		if (args[0].equalsIgnoreCase("off")) {
			if (!CheckPermissions(sender, "perplayerkeepinventory.toggle")) { return; }

			if (!pluginEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOffMessage));
				return;
			}

			pluginEnabled = false;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', offMessage));
			return;
		}

		//Reload PerPlayerKeepInventory
		if (args[0].equalsIgnoreCase("reload")) {
			if (!CheckPermissions(sender, "perplayerkeepinventory.reload")) {return; }

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
		if (str.equalsIgnoreCase("ppki")) {
			CheckCommands(sender, args);
			return true;
		}

		return false;
	}

	//When player dies
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if (!pluginEnabled) {
			return;
		}

		Player player = event.getEntity();

		if (player.hasPermission("perplayerkeepinventory.keepinvenotry")) {
			event.setKeepInventory(true);
			event.setKeepLevel(true);
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}
}