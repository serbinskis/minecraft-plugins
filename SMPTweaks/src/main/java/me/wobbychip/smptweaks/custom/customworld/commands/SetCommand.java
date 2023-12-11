package me.wobbychip.smptweaks.custom.customworld.commands;

import me.wobbychip.smptweaks.custom.custompotions.commands.Commands;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SetCommand {
	public static String USAGE_MESSAGE = "set [type|color|effects]";
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!(sender instanceof Player player)) { Utils.sendMessage(sender, Commands.NO_CONSOLE); return true; }
		if (!Utils.hasPermissions(sender, "cworld.set")) { Utils.sendMessage(sender, Commands.NO_PERMISSIONS); return true; }
		if (args.length < 1) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }

		switch (args[0].toLowerCase()) {
			case "type": return TypeCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "color": return ColorCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
			case "effects": return EffectsCommand.onTweakCommand(tweak, sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
		}

		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (!Utils.hasPermissions(sender, "cworld.set")) { return null; }
		if (args.length == 1) { return List.of("type", "color", "effects"); }

		switch (args[0].toLowerCase()) {
			case "type": return TypeCommand.onTweakTabComplete(tweak, sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
			case "color": return ColorCommand.onTweakTabComplete(tweak, sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
			case "effects": return EffectsCommand.onTweakTabComplete(tweak, sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
		}

		return null;
	}
}
