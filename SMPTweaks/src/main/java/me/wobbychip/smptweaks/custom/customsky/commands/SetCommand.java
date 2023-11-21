package me.wobbychip.smptweaks.custom.customsky.commands;

import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SetCommand {
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}
