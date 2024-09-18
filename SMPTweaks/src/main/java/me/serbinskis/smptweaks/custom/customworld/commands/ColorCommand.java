package me.serbinskis.smptweaks.custom.customworld.commands;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.custom.customworld.biomes.BiomeManager;
import me.serbinskis.smptweaks.custom.customworld.biomes.CustomBiome;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class ColorCommand {
	public static String USAGE_MESSAGE = "set color <skyColor|-1> <fogColor|-1> <foliageColor|-1> <grassColor|-1> <waterColor|-1> <waterFogColor|-1>]";

	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length < 1) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }

		CustomBiome cbiome = BiomeManager.getCustomBiome(((Player) sender).getWorld().getName());
		if (cbiome == null) { cbiome = new CustomBiome(null, null, ((Player) sender).getWorld().getName()); }

		try { if (!args[0].isEmpty()) { cbiome.setSkyColor(args[0]); } } catch (Exception e) {};
		try { if (!args[1].isEmpty()) { cbiome.setFogColor(args[1]); } } catch (Exception e) {};
		try { if (!args[2].isEmpty()) { cbiome.setFoliageColor(args[2]); } } catch (Exception e) {};
		try { if (!args[3].isEmpty()) { cbiome.setGrassColor(args[3]); } } catch (Exception e) {};
		try { if (!args[4].isEmpty()) { cbiome.setWaterColor(args[4]); } } catch (Exception e) {};
		try { if (!args[5].isEmpty()) { cbiome.setWaterFogColor(args[5]); } } catch (Exception e) {};

		BiomeManager.saveBiome(((Player) sender).getWorld(), cbiome);
		BiomeManager.registerBiomeAll(cbiome.clone());
		Utils.sendMessage(sender, Main.MESSAGE_COLOR + "Set custom world biome to " + cbiome.getName() + ". (REQUIRES REJOIN).");
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		CustomBiome cbiome = BiomeManager.getCustomBiome(((Player) sender).getWorld().getName());

		if (cbiome != null) {
			switch (args.length) {
				case 1 -> { return List.of((cbiome.getSkyColor() >= 0) ? cbiome.getSkyColorHex() : "-1"); }
				case 2 -> { return List.of((cbiome.getFogColor() >= 0) ? cbiome.getFogColorHex() : "-1"); }
				case 3 -> { return List.of((cbiome.getFoliageColor() >= 0) ? cbiome.getFoliageColorHex() : "-1"); }
				case 4 -> { return List.of((cbiome.getGrassColor() >= 0) ? cbiome.getGrassColorHex() : "-1"); }
				case 5 -> { return List.of((cbiome.getWaterColor() >= 0) ? cbiome.getWaterColorHex() : "-1"); }
				case 6 -> { return List.of((cbiome.getWaterFogColor() >= 0) ? cbiome.getWaterFogColorHex() : "-1"); }
			}
		}

		return ((args.length >= 1) && (args.length <= 6)) ? List.of(CustomBiome.toHex(new Random().nextInt(0xFFFFFF + 1))) : null;
	}
}
