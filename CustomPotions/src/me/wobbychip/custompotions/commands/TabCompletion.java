package me.wobbychip.custompotions.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TabCompletion implements TabCompleter {
	public static List<String> arguments = Arrays.asList("list", "info", "get");

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) { 
			return arguments;
		}

		if (args.length > 1) {
			switch (args[0].toLowerCase()) {
				case "info": return InfoCommand.onTabComplete(sender, command, alias, args);
				case "get": return GetCommand.onTabComplete(sender, command, alias, args);
			}
		}

		return null;
	}
}
