package me.wobbychip.smptweaks.custom.custompotions.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.Utils;

public class InfoCommand {
	public static String USAGE_MESSAGE = "&9Usage /cpotions info <potion_name>";

	public static boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}

		CustomPotion potion = CustomPotions.manager.getCustomPotion(args[0].toLowerCase());

		if ((potion == null) || !potion.isEnabled()) {
			Utils.sendMessage(sender, Commands.NO_POTION);
			return true;
		}

		String name = Utils.toTitleCase(potion.getName());
		String displayName = ChatColor.stripColor(potion.getDisplayName());
		String description = ChatColor.stripColor(potion.getLore().get(0));
		String ingredient = (potion.getMaterial() != null) ? Utils.getMaterialName(potion.getMaterial()) : "Unknown";
		String basePotion = Utils.toTitleCase(potion.getBaseName());
		String allowArrow = potion.getAllowTippedArrow() ? "Yes" : "No";
		String allowTrade = potion.getAllowVillagerTrades() ? "Yes" : "No";

		String message = "&a&lCustomPotions &8» &7" + name + "\n" +
				"&9Display Name: &f" + displayName + "\n" +
				"&9Description: &f" + description + "\n" +
				"&9Ingredient: &f" + ingredient + "\n" +
				"&9Base Potion: &f" + basePotion + "\n" +
				"&9Can villagers trade: &f" + allowTrade + "\n" +
				"&9Can craft arrows: &f" + allowArrow;

		Utils.sendMessage(sender, message);
		return true;
	}

	public static List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 2) {
			ArrayList<String> potions = new ArrayList<String>();

			for (String name : CustomPotions.manager.getPotionSet()) {
				if (!CustomPotions.manager.getCustomPotion(name).isEnabled()) { continue; }
				potions.add(name);
			}

			return potions;
		}

		return null;
	}
}
