package me.wobbychip.whitelist;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	//Turn on whitelist
	public void WhitelistOn(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "wl.toggle")) { return; }

		if (Main.plugin.getConfig().getBoolean("Enabled")) {
			Utilities.SendMessage(sender, Utilities.getString("alreadyOnMessage"));
			return;
		}

		Main.plugin.getConfig().set("Enabled", true);
		Main.plugin.saveConfig();
		Utilities.SendMessage(sender, Utilities.getString("onMessage"));
	}

	//Turn on whitelist
	public void WhitelistOff(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "wl.toggle")) { return; }

		if (!Main.plugin.getConfig().getBoolean("Enabled")) {
			Utilities.SendMessage(sender, Utilities.getString("alreadyOffMessage"));
			return;
		}

		Main.plugin.getConfig().set("Enabled", false);
		Utilities.SendMessage(sender, Utilities.getString("offMessage"));
	}

	//Reload whitelist
	public void ReloadWhitelist(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "wl.reload")) { return; }
		Main.plugin.reloadConfig();
		Main.PlayersConfig = new Config("players.yml");
		Utilities.SendMessage(sender, Utilities.getString("reloadMessage"));
	}

	//Add player to whitelist
	public void AddWhitelist(CommandSender sender, String[] args) {
		if (!Utilities.CheckPermissions(sender, "wl.modify")) { return; }

		//Check if player argument is empty
		if (args.length < 2) {
			Utilities.SendMessage(sender, Utilities.getString("incorrectArgument"));
			return;
		}

		//Check if player is already whitelisted
		if (Utilities.PlayerWhitelisted(args[1])) {
			String replacedMessage = Utilities.getString("alreadyInWhitelist").replace("%player%", args[1]);
			Utilities.SendMessage(sender, replacedMessage);
			return;
		}

		//Add player
		List<String> players = Main.PlayersConfig.getConfig().getStringList("players");
		players.add(args[1]);
		Main.PlayersConfig.getConfig().set("players", players);
		Main.PlayersConfig.Save();

		//Send notify message
		String replacedMessage = Utilities.getString("addedToWhitelist").replace("%player%", args[1]);
		Utilities.SendMessage(sender, replacedMessage);
	}

	//Remove player from whitelist
	public void RemoveWhitelist(CommandSender sender, String[] args) {
		if (!Utilities.CheckPermissions(sender, "wl.modify")) { return; }

		//Check if player argument is empty
		if (args.length < 2) {
			Utilities.SendMessage(sender, Utilities.getString("incorrectArgument"));
			return;
		}

		//Check if player is not whitelisted
		if (!Utilities.PlayerWhitelisted(args[1])) {
			String replacedMessage = Utilities.getString("notInWhitelist").replace("%player%", args[1]);
			Utilities.SendMessage(sender, replacedMessage);
			return;
		}

		//Remove player
		List<String> players = Main.PlayersConfig.getConfig().getStringList("players");
		players.remove(args[1]);
		Main.PlayersConfig.getConfig().set("players", players);
		Main.PlayersConfig.Save();

		//Send notify message
		String replacedMessage = Utilities.getString("removedFromWhitelist").replace("%player%", args[1]);
		Utilities.SendMessage(sender, replacedMessage);

		//Kick player if player was online
		Player player = Bukkit.getServer().getPlayer(args[1]);

		if (Main.plugin.getConfig().getBoolean("Enabled") && (player != null) && player.getName().equalsIgnoreCase(args[1])) {
    		replacedMessage = Utilities.getString("kickMessage").replaceAll("%n", "\n");
			player.kickPlayer(ChatColor.translateAlternateColorCodes('&', replacedMessage));
		}
	}

	//Get player list
	public void ListWhitelist(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "wl.use")) { return; }

		List<String> players = Main.PlayersConfig.getConfig().getStringList("players");

		if (players.isEmpty()) {
			Utilities.SendMessage(sender, Utilities.getString("listEmpty"));
			return;
		}

		String replacedMessage = Utilities.getString("listMessage").replace("%player_list%", String.join(", ", players));
		Utilities.SendMessage(sender, replacedMessage);
	}

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    	//Check for permissions
    	if (!Utilities.CheckPermissions(sender, "wl.use")) { return true; }

    	//Return if no arguments
		if (args.length == 0) {
			Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
			return true;
		}

		//Turn on whitelist
		if (args[0].equalsIgnoreCase("on")) {
			WhitelistOn(sender);
			return true;
		}

		//Turn off whitelist
		if (args[0].equalsIgnoreCase("off")) {
			WhitelistOff(sender);
			return true;
		}

		//Reload whitelist
		if (args[0].equalsIgnoreCase("reload")) {
			ReloadWhitelist(sender);
			return true;
		}

		//Add player to whitelist
		if (args[0].equalsIgnoreCase("add")) {
			AddWhitelist(sender, args);
			return true;
		}

		//Remove player from whitelist
		if (args[0].equalsIgnoreCase("remove")) {
			RemoveWhitelist(sender, args);
			return true;
		}

		//Get player list
		if (args[0].equalsIgnoreCase("list")) {
			ListWhitelist(sender);
			return true;
		}

		Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
        return true;
    }
}
