package me.wobbychip.smptweaks.custom.entitylimit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.utils.Utils;

public class Commands {
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		//Return if no arguments
		if (args.length == 0) {
			Utils.sendMessage(sender, Utils.getString("usageMessage", EntityLimit.config));
			return true;
		}

		//Reload plugin
		if (args[0].equalsIgnoreCase("reload")) {
			if (!Utils.hasPermissions(sender, "entitylimit.reload")) {
				Utils.sendMessage(sender, Utils.getString("permissionMessage",  EntityLimit.config));
				return true;
			}

			EntityLimit.loadConfig();
			Utils.sendMessage(sender, Utils.getString("reloadMessage", EntityLimit.config));
			return true;
		}

		//Send usage command
		Utils.sendMessage(sender, Utils.getString("usageMessage", EntityLimit.config));
		return true;
	}
}
