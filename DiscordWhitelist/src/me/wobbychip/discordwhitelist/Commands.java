package me.wobbychip.discordwhitelist;

import javax.security.auth.login.LoginException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
	//Turn on discord whitelist
	public void DiscordWhitelistOn(CommandSender sender) {
		if (!Utils.checkPermissions(sender, "dwl.toggle")) { return; }

		if (Main.plugin.getConfig().getBoolean("Enabled")) {
			Utils.sendMessage(sender, Utils.getString("alreadyOnMessage"));
			return;
		}

		Main.plugin.getConfig().set("Enabled", true);
		Main.plugin.saveConfig();
		Utils.sendMessage(sender, Utils.getString("onMessage"));
	}

	//Turn on discord whitelist
	public void DiscordWhitelistOff(CommandSender sender) {
		if (!Utils.checkPermissions(sender, "dwl.toggle")) { return; }

		if (!Main.plugin.getConfig().getBoolean("Enabled")) {
			Utils.sendMessage(sender, Utils.getString("alreadyOffMessage"));
			return;
		}

		Main.plugin.getConfig().set("Enabled", false);
		Utils.sendMessage(sender, Utils.getString("offMessage"));
	}

	//Reload discord whitelist
	public void ReloadDiscordWhitelist(CommandSender sender) {
		if (!Utils.checkPermissions(sender, "dwl.reload")) { return; }
		Main.plugin.reloadConfig();

		if (Main.jda != null) {
			Main.jda.shutdown();
		}

		try {
			if (!Main.enableBot()) {
				Utils.sendMessage(sender, Utils.getString("guildException"));
				return;
			}
		} catch (LoginException | InterruptedException e) {
			Utils.sendMessage(Utils.getString("loginException"));
			Utils.sendMessage(sender, Utils.getString("loginException"));
			return;
		}

		Utils.sendMessage(sender, Utils.getString("reloadMessage"));
	}

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    	//Check for permissions
    	if (!Utils.checkPermissions(sender, "dwl.use")) { return true; }

    	//Return if no arguments
		if (args.length == 0) {
			Utils.sendMessage(sender, Utils.getString("usageMessage"));
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

		Utils.sendMessage(sender, Utils.getString("usageMessage"));
        return true;
    }
}
