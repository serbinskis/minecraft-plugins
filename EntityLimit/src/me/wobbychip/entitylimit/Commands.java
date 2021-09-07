package me.wobbychip.entitylimit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
	//Disable plugin
	public void EnablePlugin(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "entitylimit.toggle")) { return; }

		if (Main.plugin.getConfig().getBoolean("Enabled")) {
			Utilities.SendMessage(sender, Utilities.getString("alreadyOnMessage"));
			return;
		}

		Main.pluginEnabled = true;
		Main.plugin.getConfig().set("Enabled", Main.pluginEnabled);
		Main.plugin.saveConfig();
		Utilities.SendMessage(sender, Utilities.getString("onMessage"));
	}

	//Enable plugin
	public void DisablePlugin(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "entitylimit.toggle")) { return; }

		if (!Main.plugin.getConfig().getBoolean("Enabled")) {
			Utilities.SendMessage(sender, Utilities.getString("alreadyOffMessage"));
			return;
		}

		Main.pluginEnabled = false;
		Main.plugin.getConfig().set("Enabled", Main.pluginEnabled);
		Main.plugin.saveConfig();
		Utilities.SendMessage(sender, Utilities.getString("offMessage"));
	}

	//Reload plugin
	public void ReloadPlugin(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "entitylimit.reload")) { return; }
		Main.plugin.reloadConfig();
		Main.loadConfig();
		Utilities.SendMessage(sender, Utilities.getString("reloadMessage"));
	}

	//Set limit
	public void SetLimit(CommandSender sender, String[] args) {
		if (!Utilities.CheckPermissions(sender, "entitylimit.limit")) { return; }

		//Check if argument is integer
		if ((args.length < 2) || !args[1].matches("-?\\d+") || (args[1].length() > 4)) {
			Utilities.SendMessage(sender, Utilities.getString("incorrectArgument"));
			return;
		}

		Main.Limit = Integer.parseInt(args[1]);
		if (Main.Limit < 0) { Main.Limit = 0; }

		Main.plugin.getConfig().set("Limit", Main.Limit);
		Main.plugin.saveConfig();

		String replacedMessage = Utilities.getString("limitMessage").replace("%value%", String.valueOf(Main.Limit));
		Utilities.SendMessage(sender, replacedMessage);
	}

	//Set limit
	public void SetDistance(CommandSender sender, String[] args) {
		if (!Utilities.CheckPermissions(sender, "entitylimit.distance")) { return; }

		//Check if argument is integer
		if ((args.length < 2) || !args[1].matches("-?\\d+") || (args[1].length() > 4)) {
			Utilities.SendMessage(sender, Utilities.getString("incorrectArgument"));
			return;
		}

		Main.maximumDistance = Integer.parseInt(args[1]);
		if (Main.maximumDistance < 0) { Main.maximumDistance = 0; }

		Main.plugin.getConfig().set("maximumDistance", Main.maximumDistance);
		Main.plugin.saveConfig();

		String replacedMessage = Utilities.getString("distanceMessage").replace("%value%", String.valueOf(Main.maximumDistance));
		Utilities.SendMessage(sender, replacedMessage);
	}

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    	//Return if no arguments
		if (args.length == 0) {
			Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
			return true;
		}

		//Enable plugin
		if (args[0].equalsIgnoreCase("on")) {
			EnablePlugin(sender);
			return true;
		}

		//Disable plugin
		if (args[0].equalsIgnoreCase("off")) {
			DisablePlugin(sender);
			return true;
		}

		//Reload plugin
		if (args[0].equalsIgnoreCase("reload")) {
			ReloadPlugin(sender);
			return true;
		}

		//Set limit
		if (args[0].equalsIgnoreCase("limit")) {
			SetLimit(sender, args);
			return true;
		}

		//Set distance
		if (args[0].equalsIgnoreCase("distance")) {
			SetDistance(sender, args);
			return true;
		}

		Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
        return true;
    }
}
