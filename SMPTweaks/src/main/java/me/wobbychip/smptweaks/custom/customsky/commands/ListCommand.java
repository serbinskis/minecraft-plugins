package me.wobbychip.smptweaks.custom.customsky.commands;

import me.wobbychip.smptweaks.custom.customsky.CustomSky;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

public class ListCommand {
	public static String NO_POTIONS = "There is no potions!";
	public static int MAX_TEXT_LENGTH = 1000;

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		Set<String> names = new HashSet<String>();
		int size = 0;

		for (String name : CustomSky.manager.getPotionSet()) {
			if (!CustomSky.manager.getCustomPotion(name).isEnabled()) { continue; }
			if (size+name.length() >= MAX_TEXT_LENGTH) { break; }
			size += name.length()+2;
			names.add(Utils.toTitleCase(name));
		}

		String message = "&a&lCustomPotions &8» &7" +
				((names.size() == 0) ? NO_POTIONS : String.join(", ", names));

		Utils.sendMessage(sender, message);
		return true;
	}
}
