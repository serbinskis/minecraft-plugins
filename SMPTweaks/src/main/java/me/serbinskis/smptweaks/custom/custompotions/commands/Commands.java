package me.serbinskis.smptweaks.custom.custompotions.commands;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.tweaks.TweakCommands;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class Commands extends TweakCommands {
	public static String NO_POTION = Main.MESSAGE_COLOR + "Couldn't find the potion!";

	public Commands(CustomTweak tweak, String command) {
		super(tweak, command, Arrays.asList("list", "info", "get"));
	}

	public boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, this.getUsage() + " [" + String.join(" | ", this.getArguments()) + "]");
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "list": return ListCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "info": return InfoCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "get": return GetCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		Utils.sendMessage(sender, this.getUsage());
		return true;
	}

	public List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) { return this.getArguments(); }

		if (args.length > 1) {
			switch (args[0].toLowerCase()) {
				case "info": return InfoCommand.onTweakTabComplete(tweak, sender, command, alias, args);
				case "get": return GetCommand.onTweakTabComplete(tweak, sender, command, alias, args);
			}
		}

		return null;
	}
}
