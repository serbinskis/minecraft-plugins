package me.wobbychip.custompotions.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.custompotions.Main;
import me.wobbychip.custompotions.utils.Utils;

public class ListCommand {
	public static String NO_POTIONS = "There is no potions!";
	public static int MAX_TEXT_LENGTH = 1000;

	public static boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		Set<String> potions = Main.manager.getPotionSet();
		Set<String> names = new HashSet<String>();
		int size = 0;

		for (String name : potions) {
			if (size+name.length() >= MAX_TEXT_LENGTH) { break; }
			size += name.length();
			names.add(Utils.toTitleCase(name));
		}

		String message = "&a&lCustomPotions &8» &7" +
				((names.size() == 0) ? NO_POTIONS : String.join(", ", names));

		Utils.sendMessage(sender, message);
		return true;
	}
}
