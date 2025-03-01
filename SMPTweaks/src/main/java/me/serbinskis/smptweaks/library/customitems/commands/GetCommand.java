package me.serbinskis.smptweaks.library.customitems.commands;

import me.serbinskis.smptweaks.library.customitems.CustomItems;
import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GetCommand {
	public static String USAGE_MESSAGE = "get <item_name>";

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		boolean isCreative = ((sender instanceof Player) && (((Player) sender).getGameMode() == GameMode.CREATIVE));

		if (!(sender instanceof Player)) {
			Utils.sendMessage(sender, Commands.NO_CONSOLE);
			return true;
		}

		if (!Utils.hasPermissions(sender, "citems.get") && !isCreative) {
			Utils.sendMessage(sender, Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length < 1) {
			Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE);
			return true;
		}

		CustomItem customItem = CustomItems.getCustomItem(args[0].toLowerCase());

		if (customItem == null) {
			Utils.sendMessage(sender, Commands.NO_ITEM);
			return true;
		}

		ItemStack item = customItem.getItemStack(0);
		item = Utils.cloneItem(item, item.getMaxStackSize());
		((Player) sender).getInventory().addItem(item);
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		boolean isCreative = ((sender instanceof Player) && (((Player) sender).getGameMode() == GameMode.CREATIVE));
		if (!Utils.hasPermissions(sender, "citems.get") && !isCreative) { return null; }

		if (args.length == 2) {
			return CustomItems.REGISTRY_CUSTOM_ITEMS.keySet().stream().toList();
		}

		return null;
	}
}
