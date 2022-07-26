package me.wobbychip.smptweaks.custom.custompotions.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.utils.Utils;

public class Commands implements CommandExecutor {
	public static String NO_PERMISSIONS = "&cYou do not have permissions!";
	public static String INCORRECT_ARGUMENT = "&cIncorrect Argument!";
	public static String USAGE_MESSAGE = "&9Usage /cpotions [list | info | get]";
	public static String NO_POTION = "&9Couldn\'t find the potion!";

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}
	
		switch (args[0].toLowerCase()) {
			case "list": return ListCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "info": return InfoCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "get": return GetCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		Utils.sendMessage(sender, USAGE_MESSAGE);
		return true;
	}
}
