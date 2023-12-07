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
import java.util.Random;

public class ColorCommand {
	public static String USAGE_MESSAGE = "set color <skyColor|-1> <fogColor|-1> <waterColor|-1> <waterFogColor|-1> <foliageColor|-1> <grassColor|-1>]";
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length < 1) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }
		CustomBiome biome = new CustomBiome(null, null, ((Player) sender).getWorld().getName());

		try { biome.setSkyColor(args[1]); } catch (Exception e) {};
		try { biome.setFogColor(args[2]); } catch (Exception e) {};
		try { biome.setWaterColor(args[3]); } catch (Exception e) {};
		try { biome.setWaterFogColor(args[4]); } catch (Exception e) {};
		try { biome.setFoliageColor(args[5]); } catch (Exception e) {};
		try { biome.setGrassColor(args[6]); } catch (Exception e) {};

		BiomeManager.saveBiome(((Player) sender).getWorld(), biome);
		BiomeManager.registerBiomeAll(biome);
		Utils.sendMessage(sender, Main.color + "Set custom world biome to " + biome.getName() + ". (REQUIRES REJOIN).");
		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		return List.of(CustomBiome.toHex(new Random().nextInt(0xFFFFFF + 1)));
	}
}
