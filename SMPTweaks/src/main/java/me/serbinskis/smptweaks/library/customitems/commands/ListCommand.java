package me.serbinskis.smptweaks.library.customitems.commands;

import me.serbinskis.smptweaks.library.customitems.CustomItems;
import me.serbinskis.smptweaks.library.customitems.items.CustomItem;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand {
	public static String NO_ITEMS = "There is no items!";
	public static int MAX_TEXT_LENGTH = 1000;

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		List<String> names = new ArrayList<>();
		int size = 0;

		for (CustomItem customItem : CustomItems.REGISTRY_CUSTOM_ITEMS.values()) {
			String name = ChatColor.stripColor(customItem.getId());
			if (size+name.length() >= MAX_TEXT_LENGTH) { break; }
			size += name.length()+2;
			names.add(Utils.toTitleCase(name.replace("_", " ")));
		}

		String message = "&a&lCustomItems &8» &7" +
				((names.isEmpty()) ? NO_ITEMS : String.join(", ", names));

		Utils.sendMessage(sender, message);
		return true;
	}
}
