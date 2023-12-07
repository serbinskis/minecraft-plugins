package me.wobbychip.smptweaks.custom.customworld.commands;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.customworld.CustomWorlds;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomWorld;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class TypeCommand {
	public static String USAGE_MESSAGE = "set type <world_type>";
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length < 1) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }
		CustomWorld type = CustomWorld.getCustomType(args[1]);

		if (type == null) {
			Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE);
		} else if (type == CustomWorld.NONE) {
			PersistentUtils.removePersistentData(((Player) sender).getWorld(), CustomWorlds.TAG_CUSTOM_WORLD);
		} else {
			PersistentUtils.setPersistentDataString(((Player) sender).getWorld(), CustomWorlds.TAG_CUSTOM_WORLD, args[1].toLowerCase());
		}

		Utils.sendMessage(sender, Main.color + "Set custom world type to " + args[1].toLowerCase() + ". (REQUIRES RESTART).");
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		Utils.sendMessage(args.length);
		return Arrays.stream(CustomWorld.values()).map(e -> e.toString().toLowerCase()).toList();
	}
}
