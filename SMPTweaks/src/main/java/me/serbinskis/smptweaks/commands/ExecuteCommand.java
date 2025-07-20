package me.serbinskis.smptweaks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.serbinskis.smptweaks.tweaks.TweakManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;

public class ExecuteCommand {
	public static String USAGE_MESSAGE = Main.MESSAGE_COLOR + "Usage /smptweaks execute <tweak_name>";
	public static String NO_COMMANDS = Main.MESSAGE_COLOR + "This tweak has no commands!";

	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}

		CustomTweak tweak = TweakManager.getTweak(args[0].toLowerCase(), true);

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

			for (CustomTweak tweak : TweakManager.getTweaks()) {
				if (tweak.getCommand() != null) { tweaks.add(tweak.getCommand().getCommand()); }
			}

			return tweaks;
		}

		if (args.length >= 3) {
			CustomTweak tweak = TweakManager.getTweak(args[1].toLowerCase(), true);
			if ((tweak != null) && (tweak.getCommand() != null)) {
				return tweak.getCommand().onTweakTabComplete(tweak, sender, command, alias, Arrays.copyOfRange(args, 2, args.length));
			}
		}

		return null;
	}
}
