package me.wobbychip.whitelist;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public Boolean whitelistEnabled = false;
	public List<String> PlayerList = new ArrayList<>();

	public String enableMessage = new String();
	public String permissionMessage = new String();
	public String reloadMessage = new String();
	public String usageMessage = new String();
	public String kickMessage = new String();

	public String saveMessage = new String();
	public String listMessage = new String();
	public String listEmpty = new String();

	public String onMessage = new String();
	public String alreadyOnMessage = new String();
	public String offMessage = new String();
	public String alreadyOffMessage = new String();
	public String incorrectArgument = new String();

	public String addedToWhitelist = new String();
	public String alreadyInWhitelist = new String();
	public String removedFromWhitelist = new String();
	public String notInWhitelist = new String();

	public void loadConfig() {
		whitelistEnabled = this.getConfig().getBoolean("Enabled");
		PlayerList = this.getConfig().getStringList("PlayerList");

		enableMessage = this.getConfig().getString("enableMessage");
		permissionMessage = this.getConfig().getString("permissionMessage");
		reloadMessage = this.getConfig().getString("reloadMessage");
		usageMessage = this.getConfig().getString("usageMessage");
		kickMessage = this.getConfig().getString("kickMessage");

		saveMessage = this.getConfig().getString("saveMessage");
		listMessage = this.getConfig().getString("listMessage");
		listEmpty = this.getConfig().getString("listEmpty");

		onMessage = this.getConfig().getString("onMessage");
		alreadyOnMessage = this.getConfig().getString("alreadyOnMessage");
		offMessage = this.getConfig().getString("offMessage");
		alreadyOffMessage = this.getConfig().getString("alreadyOffMessage");
		incorrectArgument = this.getConfig().getString("incorrectArgument");

		addedToWhitelist = this.getConfig().getString("addedToWhitelist");
		alreadyInWhitelist = this.getConfig().getString("alreadyInWhitelist");
		removedFromWhitelist = this.getConfig().getString("removedFromWhitelist");
		notInWhitelist = this.getConfig().getString("notInWhitelist");
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

	//Get player index from list
	public int GetPlayer(String PlayerName) {
		String playerName = new String(PlayerName.toLowerCase());

        for (int i = 0; i < PlayerList.size(); i++) {
            if (PlayerList.get(i).toLowerCase().equals(playerName)) {
            	return i;
            }
        }

		return -1;
	}

	//Whitelist commands list
	public void WhitelistCommands(CommandSender sender,  String[] args) {
		if (!CheckPermissions(sender, "wl.use")) { return; }

		if (args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMessage));
			return;
		}

		//Turn on whitelist
		if (args[0].equalsIgnoreCase("on")) {
			if (!CheckPermissions(sender, "wl.toggle")) { return; }

			if (whitelistEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOnMessage));
				return;
			}

			whitelistEnabled = true;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onMessage));
			return;
		}

		//Turn off whitelist
		if (args[0].equalsIgnoreCase("off")) {
			if (!CheckPermissions(sender, "wl.toggle")) { return; }

			if (!whitelistEnabled) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyOffMessage));
				return;
			}

			whitelistEnabled = false;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', offMessage));
			return;
		}

		//Reload Whitelist
		if (args[0].equalsIgnoreCase("reload")) {
			if (!CheckPermissions(sender, "wl.reload")) { return; }

			this.reloadConfig();
			loadConfig();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMessage));
			return;
		}

		//Save Whitelist
		if (args[0].equalsIgnoreCase("save")) {
			if (!CheckPermissions(sender, "wl.save")) { return; }

			this.getConfig().set("Enabled", whitelistEnabled);
			this.getConfig().set("PlayerList", PlayerList);
			this.saveConfig();

			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', saveMessage));
			return;
		}

		//Add player to whitelist
		if (args[0].equalsIgnoreCase("add")) {
			if (!CheckPermissions(sender, "wl.use")) { return; }

			if (args.length < 2) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', incorrectArgument));
				return;
			}

			if (GetPlayer(args[1]) > -1) {
				String replacedMessage = alreadyInWhitelist.replace("%player%", args[1]);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));
				return;
			}

			PlayerList.add(args[1]);
			String replacedMessage = addedToWhitelist.replace("%player%", args[1]);
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));
			return;
		}

		//Remove player from whitelist
		if (args[0].equalsIgnoreCase("remove")) {
			if (!CheckPermissions(sender, "wl.use")) { return; }

			if (args.length < 2) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', incorrectArgument));
				return;
			}

			if (GetPlayer(args[1]) == -1) {
				String replacedMessage = notInWhitelist.replace("%player%", args[1]);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));
				return;
			}

			PlayerList.remove(GetPlayer(args[1]));
			String replacedMessage = removedFromWhitelist.replace("%player%", args[1]);
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', replacedMessage));

			Player player = Bukkit.getServer().getPlayer(args[1]);
			if (whitelistEnabled && (player != null) && player.getName().equalsIgnoreCase(args[1])) {
	    		replacedMessage = kickMessage.replaceAll("%n", "\n");
				player.kickPlayer(ChatColor.translateAlternateColorCodes('&', replacedMessage));
			}

			return;
		}

		//Get player list
		if (args[0].equalsIgnoreCase("list")) {
			if (!CheckPermissions(sender, "wl.use")) { return; }

			if (PlayerList.isEmpty()) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', listEmpty));
				return;
			}

			String replacedMessage = listMessage.replace("%player_list%", String.join(", ", PlayerList));		
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
		this.getConfig().set("Enabled", whitelistEnabled);
		this.getConfig().set("PlayerList", PlayerList);
		this.saveConfig();
    }

	public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
		if (str.equalsIgnoreCase("wl")) {
			WhitelistCommands(sender, args);
			return true;
		}

		return false;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    	if (whitelistEnabled && (GetPlayer(event.getName()) == -1)) {
    		String replacedMessage = kickMessage.replaceAll("%n", "\n");
    		event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', replacedMessage));
        }
    }
}