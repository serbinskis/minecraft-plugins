package me.wobbychip.smptweaks.custom.customworld.commands;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.customworld.biomes.BiomeManager;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomBiome;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EffectsCommand {
	public static String USAGE_MESSAGE = "set effects <true|false>";

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length < 1) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }
		if (!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false")) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }

		CustomBiome cbiome = BiomeManager.getCustomBiome(((Player) sender).getWorld().getName());
		if (cbiome == null) { Utils.sendMessage(sender, Main.MESSAGE_COLOR + "First you must create custom biome."); return true; }
		cbiome = cbiome.setEffectsEnabled(Boolean.parseBoolean(args[0])).clone();

		BiomeManager.saveBiome(((Player) sender).getWorld(), cbiome);
		BiomeManager.registerBiomeAll(cbiome);
		Utils.sendMessage(sender, Main.MESSAGE_COLOR + "Set custom biome effects " + (cbiome.isEffectsEnabled() ? "enabled" : "disabled")+ ". (REQUIRES REJOIN).");
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		return (args.length == 1) ? List.of("true", "false") : null;
	}
}
