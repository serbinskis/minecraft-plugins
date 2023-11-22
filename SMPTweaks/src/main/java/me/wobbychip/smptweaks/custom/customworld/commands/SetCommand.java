package me.wobbychip.smptweaks.custom.customworld.commands;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.commands.Commands;
import me.wobbychip.smptweaks.custom.customworld.CustomWorld;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SetCommand {
	public static String USAGE_MESSAGE = "set <world_type>";
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!(sender instanceof Player player)) { Utils.sendMessage(sender, Commands.NO_CONSOLE); return true; }
		if (!Utils.hasPermissions(sender, "cworld.set")) { Utils.sendMessage(sender, Commands.NO_PERMISSIONS); return true; }
		if (args.length < 1) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }

		switch (args[0].toLowerCase()) {
			case "void" -> PersistentUtils.setPersistentDataString(player.getWorld(), CustomWorld.CUSTOM_WORLD_TAG, "void");
			case "none" -> PersistentUtils.removePersistentData(player.getWorld(), CustomWorld.CUSTOM_WORLD_TAG);
			default -> { Utils.sendMessage(sender, USAGE_MESSAGE); return true; }
		}

		Utils.sendMessage(sender, Main.color + "Set custom world to " + args[0].toLowerCase() + ". To see changes relogin.");
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (!Utils.hasPermissions(sender, "cworld.set")) { return null; }
		return Arrays.asList("void", "none");
	}
}
