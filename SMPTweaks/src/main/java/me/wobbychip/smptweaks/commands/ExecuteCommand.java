package me.wobbychip.smptweaks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class ExecuteCommand {
	public static String USAGE_MESSAGE = Main.color + "Usage /smptweaks execute <tweak_name>";
	public static String NO_COMMANDS = Main.color + "This tweak has no commands!";

	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}

		CustomTweak tweak = Main.manager.getTweak(args[0].toLowerCase(), true);

		if (tweak == null) {
			Utils.sendMessage(sender, Commands.NO_TWEAK);
			return true;
		}

		if (tweak.getCommand() == null) {
			Utils.sendMessage(sender, NO_COMMANDS);
			return true;
		}

		return tweak.getCommand().onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	public static List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 2) {
			ArrayList<String> tweaks = new ArrayList<String>();

			for (CustomTweak tweak : Main.manager.getTweaks()) {
				if (tweak.getCommand() != null) { tweaks.add(tweak.getCommand().getCommand()); }
			}

			return tweaks;
		}

		if (args.length >= 3) {
			CustomTweak tweak = Main.manager.getTweak(args[1].toLowerCase(), true);
			if ((tweak != null) && (tweak.getCommand() != null)) {
				return tweak.getCommand().onTweakTabComplete(tweak, sender, command, alias, Arrays.copyOfRange(args, 2, args.length));
			}
		}

		return null;
	}
}
