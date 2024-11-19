package me.serbinskis.smptweaks.library.resourcepack.commands;

import me.serbinskis.smptweaks.library.resourcepack.ResourcePacks;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AddCommand {
	public static String USAGE_MESSAGE = "add <url>";

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!Utils.hasPermissions(sender, "rpacks.modify")) {
			Utils.sendMessage(sender, Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length < 1) {
			Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE);
			return true;
		}

		if (Utils.getFileHash(args[0]).length == 0) {
			Utils.sendMessage(sender, Commands.NO_RESOURCE_PACK);
			return true;
		}

		ResourcePacks.addResourcePack(args[0]);
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (!Utils.hasPermissions(sender, "rpacks.modify")) { return null; }
		if (args.length == 2) { return ResourcePacks.resourcePacks.keySet().stream().toList(); }
		return null;
	}
}
