package me.serbinskis.smptweaks.custom.custompotions.commands;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.custom.custompotions.CustomPotions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand {
	public static String NO_POTIONS = "There is no potions!";
	public static int MAX_TEXT_LENGTH = 1000;

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		List<String> names = new ArrayList<>();
		int size = 0;

		for (String name : CustomPotions.manager.getPotionSet()) {
			if (!CustomPotions.manager.getCustomPotion(name).isEnabled()) { continue; }
			if (size+name.length() >= MAX_TEXT_LENGTH) { break; }
			size += name.length()+2;
			names.add(Utils.toTitleCase(name.replaceAll("_", " ")));
		}

		String message = "&a&lCustomPotions &8» &7" +
				((names.isEmpty()) ? NO_POTIONS : String.join(", ", names));

		Utils.sendMessage(sender, message);
		return true;
	}
}
