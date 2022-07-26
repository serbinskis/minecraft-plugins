package me.wobbychip.smptweaks;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.utils.Utils;

public class Commands implements CommandExecutor {
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length > 0) {
			return Main.manager.sendCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
		} else {
			Utils.sendMessage(sender, "&a&lSMPTweaks &8» &7" + Main.manager.getTweaks());
		}

		return true;
	}
}
