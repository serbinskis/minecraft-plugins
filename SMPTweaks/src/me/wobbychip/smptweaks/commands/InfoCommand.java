package me.wobbychip.smptweaks.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class InfoCommand {
	public static String USAGE_MESSAGE = Main.color + "Usage /smptweaks info <tweak_name>";

	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}

		CustomTweak tweak = Main.manager.getTweak(args[0].toLowerCase());

		if (tweak == null) {
			Utils.sendMessage(sender, Commands.NO_TWEAK);
			return true;
		}

		String gamerule_message = "";
		String enabled = tweak.isEnabled() ? "Yes" : "No";
		String requires = tweak.requiresPaper() ? "PaperMC" : "Nothing";
		requires = tweak.requiresProtocolLib() ? "ProtocolLib" : requires;
		Entry<String, Object> gamerule = tweak.getGameRule();

		if (gamerule != null) {
			String gamerule_current = "";
			if (sender instanceof Player) { gamerule_current = String.format(", current: %s", (Object) tweak.getGameRule(((Player) sender).getWorld())); }
			gamerule_message = String.format("&9Gamerule: &f%s, default: %s%s\n", gamerule.getKey(), gamerule.getValue(), gamerule_current);
		}

		String message = "&a&lSMPTweaks &8» &7" + tweak.getName() + "\n" +
						 "&9Enabled: &f" + enabled + "\n" +
						 "&9Requires: &f" + requires + "\n" +
						 gamerule_message +
						 "&9Description: &f" + tweak.getDescription();

		Utils.sendMessage(sender, message);
		return true;
	}

	public static List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 2) {
			return new ArrayList<>(Main.manager.keySet());
		}

		return null;
	}
}
