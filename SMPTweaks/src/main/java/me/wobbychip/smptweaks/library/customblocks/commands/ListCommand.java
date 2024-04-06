package me.wobbychip.smptweaks.library.customblocks.commands;

import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand {
	public static String NO_BLOCKS = "There is no blocks!";
	public static int MAX_TEXT_LENGTH = 1000;

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		List<String> names = new ArrayList<>();
		int size = 0;

		for (CustomBlock customBlock : CustomBlocks.REGISTRY_CUSTOM_BLOCKS.values()) {
			String name = ChatColor.stripColor(customBlock.getCustomName());
			if (size+name.length() >= MAX_TEXT_LENGTH) { break; }
			size += name.length()+2;
			names.add(Utils.toTitleCase(name));
		}

		String message = "&a&lCustomBlocks &8» &7" +
				((names.isEmpty()) ? NO_BLOCKS : String.join(", ", names));

		Utils.sendMessage(sender, message);
		return true;
	}
}
