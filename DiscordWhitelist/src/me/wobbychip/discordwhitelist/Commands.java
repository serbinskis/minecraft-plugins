package me.wobbychip.discordwhitelist;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
	//Turn on discord whitelist
	public void DiscordWhitelistOn(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "dwl.toggle")) { return; }

		if (Main.plugin.getConfig().getBoolean("Enabled")) {
			Utilities.SendMessage(sender, Utilities.getString("alreadyOnMessage"));
			return;
		}

		Main.plugin.getConfig().set("Enabled", true);
		Main.plugin.saveConfig();
		Utilities.SendMessage(sender, Utilities.getString("onMessage"));
	}

	//Turn on discord whitelist
	public void DiscordWhitelistOff(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "dwl.toggle")) { return; }

		if (!Main.plugin.getConfig().getBoolean("Enabled")) {
			Utilities.SendMessage(sender, Utilities.getString("alreadyOffMessage"));
			return;
		}

		Main.plugin.getConfig().set("Enabled", false);
		Utilities.SendMessage(sender, Utilities.getString("offMessage"));
	}

	//Reload discord whitelist
	public void ReloadDiscordWhitelist(CommandSender sender) {
		if (!Utilities.CheckPermissions(sender, "dwl.reload")) { return; }
		Main.plugin.reloadConfig();

		try {
			Utilities.EnableBot();
		} catch (LoginException | InterruptedException e) {
			Utilities.DebugInfo(Utilities.getString("loginException"));
			Bukkit.getPluginManager().disablePlugin(Main.plugin);
			return;
		}

		Utilities.SendMessage(sender, Utilities.getString("reloadMessage"));
	}

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    	//Check for permissions
    	if (!Utilities.CheckPermissions(sender, "dwl.use")) { return true; }

    	//Return if no arguments
		if (args.length == 0) {
			Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
			return true;
		}

		//Turn on discord whitelist
		if (args[0].equalsIgnoreCase("on")) {
			DiscordWhitelistOn(sender);
			return true;
		}

		//Turn off discord whitelist
		if (args[0].equalsIgnoreCase("off")) {
			DiscordWhitelistOff(sender);
			return true;
		}

		//Reload discord whitelist
		if (args[0].equalsIgnoreCase("reload")) {
			ReloadDiscordWhitelist(sender);
			return true;
		}

		Utilities.SendMessage(sender, Utilities.getString("usageMessage"));
        return true;
    }
}
