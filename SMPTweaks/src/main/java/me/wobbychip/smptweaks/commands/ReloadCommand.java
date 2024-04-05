package me.wobbychip.smptweaks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class ReloadCommand {
	public static String USAGE_MESSAGE = Main.MESSAGE_COLOR + "Usage /smptweaks reload <tweak_name>";
	public static String NOT_RELOADABLE = Main.MESSAGE_COLOR + "This tweak is not reloadable!";
	public static String RELOAD_MESSAGE = Main.MESSAGE_COLOR + "Reloaded %s.";

	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!Utils.hasPermissions(sender, "smptweaks.reload")) {
			Utils.sendMessage(sender, Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length == 0) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}

		CustomTweak tweak = Main.manager.getTweak(args[0].toLowerCase(), false);

		if (tweak == null) {
			Utils.sendMessage(sender, Commands.NO_TWEAK);
			return true;
		}

		if (!tweak.isReloadable()) {
			Utils.sendMessage(sender, NOT_RELOADABLE);
			return true;
		}

		tweak.loadConfigs();
		tweak.onReload();
		Utils.sendMessage(sender, String.format(RELOAD_MESSAGE, tweak.getName()));
		return true;
	}

	public static List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if ((args.length == 2) && Utils.hasPermissions(sender, "smptweaks.reload")) {
			ArrayList<String> tweaks = new ArrayList<String>();
			
			for (CustomTweak tweak : Main.manager.getTweaks()) {
				if (tweak.isReloadable()) { tweaks.add(tweak.getName()); }
			}

			return tweaks;
		}

		return null;
	}
}
