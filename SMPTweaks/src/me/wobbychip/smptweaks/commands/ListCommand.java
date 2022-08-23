package me.wobbychip.smptweaks.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class ListCommand {
	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Set<String> names = new HashSet<String>();

		for (CustomTweak tweak : Main.manager.getTweaks()) {
			names.add("&7" + tweak.getName() + " (" + (tweak.isEnabled() ? "&a+&7" : "&c+&7") + ")");
		}

		Utils.sendMessage(sender, "&a&lSMPTweaks &8» " + String.join(", ", names));
		return true;
	}
}
