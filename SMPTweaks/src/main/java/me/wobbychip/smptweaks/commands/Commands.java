package me.wobbychip.smptweaks.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.Utils;

public class Commands implements CommandExecutor, TabCompleter {
	public static List<String> arguments = Arrays.asList("list", "info", "reload", "execute");
	public static String NO_PERMISSIONS = "&cYou do not have permissions!";
	public static String USAGE_MESSAGE = Main.MESSAGE_COLOR + "Usage /smptweaks [list | info | execute | reload]";
	public static String NO_TWEAK = Main.MESSAGE_COLOR + "Couldn\'t find the tweak!";

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "list": return ListCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "info": return InfoCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "execute": return ExecuteCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "reload": return ReloadCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		Utils.sendMessage(sender, USAGE_MESSAGE);
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) { return arguments; }

		if (args.length > 1) {
			switch (args[0].toLowerCase()) {
				case "info": return InfoCommand.onTabComplete(sender, command, alias, args);
				case "execute": return ExecuteCommand.onTabComplete(sender, command, alias, args);
				case "reload": return ReloadCommand.onTabComplete(sender, command, alias, args);
			}
		}

		return null;
	}
}
