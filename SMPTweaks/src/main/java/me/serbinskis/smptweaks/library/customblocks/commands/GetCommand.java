package me.serbinskis.smptweaks.library.customblocks.commands;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GetCommand {
	public static String USAGE_MESSAGE = "get <block_name>";

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		boolean isCreative = ((sender instanceof Player) && (((Player) sender).getGameMode() == GameMode.CREATIVE));

		if (!(sender instanceof Player)) {
			Utils.sendMessage(sender, Commands.NO_CONSOLE);
			return true;
		}

		if (!Utils.hasPermissions(sender, "cblocks.get") && !isCreative) {
			Utils.sendMessage(sender, Commands.NO_PERMISSIONS);
			return true;
		}

		if (args.length < 1) {
			Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE);
			return true;
		}

		CustomBlock customBlock = CustomBlocks.getCustomBlock(args[0].toLowerCase());

		if (customBlock == null) {
			Utils.sendMessage(sender, Commands.NO_BLOCK);
			return true;
		}

		ItemStack item = customBlock.getDropItem(false);
		item = Utils.cloneItem(item, item.getMaxStackSize());
		((Player) sender).getInventory().addItem(item);
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		boolean isCreative = ((sender instanceof Player) && (((Player) sender).getGameMode() == GameMode.CREATIVE));
		if (!Utils.hasPermissions(sender, "cblocks.get") && !isCreative) { return null; }

		if (args.length == 2) {
			return CustomBlocks.REGISTRY_CUSTOM_BLOCKS.keySet().stream().toList();
		}

		return null;
	}
}
