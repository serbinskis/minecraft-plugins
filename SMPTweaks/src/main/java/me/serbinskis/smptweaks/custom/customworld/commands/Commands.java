package me.serbinskis.smptweaks.custom.customworld.commands;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.tweaks.TweakCommands;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class Commands extends TweakCommands {
	public Commands(CustomTweak tweak, String command) {
		super(tweak, command, Arrays.asList("set", "get"));
	}

	public boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, this.getUsage() + " [" + String.join(" | ", this.getArguments()) + "]");
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "set": return SetCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "get": return GetCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		Utils.sendMessage(sender, this.getUsage());
		return true;
	}

	public List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) { return this.getArguments(); }

		if (args.length > 1) {
			switch (args[0].toLowerCase()) {
				case "set": return SetCommand.onTweakTabComplete(tweak, sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
			}
		}

		return null;
	}
}
