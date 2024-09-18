package me.serbinskis.smptweaks.custom.customworld.commands;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.customworld.CustomWorlds;
import me.serbinskis.smptweaks.custom.customworld.biomes.CustomWorld;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class TypeCommand {
	public static String USAGE_MESSAGE = "set type <world_type>";

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length < 1) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }
		CustomWorld type = CustomWorld.getCustomType(args[0]);

		if (type == null) {
			Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE);
		} else if (type == CustomWorld.NONE) {
			PersistentUtils.removePersistentData(((Player) sender).getWorld(), CustomWorlds.TAG_CUSTOM_WORLD);
		} else {
			PersistentUtils.setPersistentDataString(((Player) sender).getWorld(), CustomWorlds.TAG_CUSTOM_WORLD, args[0].toLowerCase());
		}

		Utils.sendMessage(sender, Main.MESSAGE_COLOR + "Set custom world type to " + args[0].toLowerCase() + ". (REQUIRES RESTART).");
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		return (args.length == 1) ? Arrays.stream(CustomWorld.values()).map(e -> e.toString().toLowerCase()).toList() : null;
	}
}
