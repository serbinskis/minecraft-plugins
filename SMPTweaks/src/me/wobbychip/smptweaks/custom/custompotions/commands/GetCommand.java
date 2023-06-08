package me.wobbychip.smptweaks.custom.custompotions.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.Utils;

public class GetCommand {
	public static List<String> arguments = Arrays.asList("potion", "splash", "lingering", "arrow");
	public static String USAGE_MESSAGE = Main.color + "Usage /cpotions get <potion_name> [potion | splash | lingering | arrow]";
	public static String NO_CONSOLE = Main.color + "This command can only be executed by a player!";
	public static String ARROW_DISABLED = Main.color + "Arrows for this potion is disabled!";

	public static boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		boolean isCreative = ((sender instanceof Player) && (((Player) sender).getGameMode() == GameMode.CREATIVE));

		if (!(sender instanceof Player)) {
			Utils.sendMessage(sender, NO_CONSOLE);
			return true;
		}

		if (!Utils.hasPermissions(sender, "cpotions.get") && !isCreative) {
			Utils.sendMessage(sender, Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length < 2) {
			Utils.sendMessage(sender, USAGE_MESSAGE);
			return true;
		}

		ItemStack item = new ItemStack(Material.AIR);
		CustomPotion potion = CustomPotions.manager.getCustomPotion(args[0].toLowerCase());

		if ((potion == null) || !potion.isEnabled()) {
			Utils.sendMessage(sender, Commands.NO_POTION);
			return true;
		}

		switch (args[1].toLowerCase()) {
			case "potion":
				item.setType(Material.POTION);
				item = potion.setProperties(item);
				break;
			case "splash":
				item.setType(Material.SPLASH_POTION);
				item = potion.setProperties(item);
				break;
			case "lingering":
				item.setType(Material.LINGERING_POTION);
				item = potion.setProperties(item);
				break;
			case "arrow":
				item = potion.getTippedArrow(false, 64);
				if (item.getType() == Material.AIR) { Utils.sendMessage(sender, ARROW_DISABLED); }
				break;
			default:
				Utils.sendMessage(sender, USAGE_MESSAGE);
				return true;
		}

		((Player) sender).getInventory().addItem(item);
		return true;
	}

	public static List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		boolean isCreative = ((sender instanceof Player) && (((Player) sender).getGameMode() == GameMode.CREATIVE));

		if (!Utils.hasPermissions(sender, "cpotions.get") && !isCreative) {
			return null;
		}

		if (args.length == 2) {
			ArrayList<String> potions = new ArrayList<String>();

			for (String name : CustomPotions.manager.getPotionSet()) {
				if (!CustomPotions.manager.getCustomPotion(name).isEnabled()) { continue; }
				potions.add(name);
			}

			return potions;
		}

		if (args.length == 3) {
			return arguments;
		}

		return null;
	}
}
