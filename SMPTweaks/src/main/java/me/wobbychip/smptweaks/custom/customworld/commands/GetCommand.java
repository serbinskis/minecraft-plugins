package me.wobbychip.smptweaks.custom.customworld.commands;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.commands.Commands;
import me.wobbychip.smptweaks.custom.customworld.CustomWorld;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetCommand {
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!(sender instanceof Player player)) { Utils.sendMessage(sender, Commands.NO_CONSOLE); return true; }
		if (!Utils.hasPermissions(sender, "cworld.get")) { Utils.sendMessage(sender, Commands.NO_PERMISSIONS); return true; }
		CustomWorld.Type type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(player.getWorld(), CustomWorld.TAG_CUSTOM_WORLD));

		Utils.sendMessage(sender, Main.color + "Custom world type is " + ((type == null) ? "none" : type.toString().toLowerCase()) + ".");
		return true;
	}
}
