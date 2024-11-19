package me.serbinskis.smptweaks.library.resourcepack.commands;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.tweaks.TweakCommands;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class Commands extends TweakCommands {
	public static String NO_RESOURCE_PACK = Main.MESSAGE_COLOR + "Couldn't find the resource pack!";

	public Commands(CustomTweak tweak, String command) {
		super(tweak, command, Arrays.asList("add", "remove"));
	}

	public boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, this.getUsage() + " [" + String.join(" | ", this.getArguments()) + "]");
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "add": return AddCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "remove": return RemoveCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		Utils.sendMessage(sender, this.getUsage());
		return true;
	}

	public List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) { return this.getArguments(); }

		if (args.length > 1) {
			switch (args[0].toLowerCase()) {
				case "add": return AddCommand.onTweakTabComplete(tweak, sender, command, alias, args);
				case "remove": return RemoveCommand.onTweakTabComplete(tweak, sender, command, alias, args);
			}
		}

		return null;
	}
}
