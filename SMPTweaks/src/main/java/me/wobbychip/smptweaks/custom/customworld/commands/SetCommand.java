package me.wobbychip.smptweaks.custom.customworld.commands;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.custompotions.commands.Commands;
import me.wobbychip.smptweaks.custom.customworld.CustomWorlds;
import me.wobbychip.smptweaks.custom.customworld.biomes.BiomeManager;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomBiome;
import me.wobbychip.smptweaks.custom.customworld.biomes.CustomWorld;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SetCommand {
	public static String USAGE_MESSAGE = "set [type <world_type> | color <skyColor|-1> <fogColor|-1> <waterColor|-1> <waterFogColor|-1> <foliageColor|-1> <grassColor|-1>]";
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!(sender instanceof Player player)) { Utils.sendMessage(sender, Commands.NO_CONSOLE); return true; }
		if (!Utils.hasPermissions(sender, "cworld.set")) { Utils.sendMessage(sender, Commands.NO_PERMISSIONS); return true; }
		if (args.length < 2) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }

		if (args[0].equalsIgnoreCase("type")) {
			CustomWorld type = CustomWorld.getCustomType(args[1]);

			if (type == null) {
				Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE);
			} else if (type == CustomWorld.NONE) {
				PersistentUtils.removePersistentData(player.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD);
			} else {
				PersistentUtils.setPersistentDataString(player.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD, args[1].toLowerCase());
			}

			Utils.sendMessage(sender, Main.color + "Set custom world type to " + args[1].toLowerCase() + ". (REQUIRES RESTART).");
		}

		if (args[0].equalsIgnoreCase("color")) {
			if (args.length < 3) { Utils.sendMessage(sender, tweak.getCommand().getUsage() + " " + USAGE_MESSAGE); return true; }
			CustomBiome biome = new CustomBiome(null, null, player.getWorld().getName());

			try { biome.setSkyColor(args[1]); } catch (Exception e) {};
			try { biome.setFogColor(args[2]); } catch (Exception e) {};
			try { biome.setWaterColor(args[3]); } catch (Exception e) {};
			try { biome.setWaterFogColor(args[4]); } catch (Exception e) {};
			try { biome.setFoliageColor(args[5]); } catch (Exception e) {};
			try { biome.setGrassColor(args[6]); } catch (Exception e) {};

			BiomeManager.saveBiome(player.getWorld(), biome);
			BiomeManager.registerBiomeAll(biome);
			Utils.sendMessage(sender, Main.color + "Set custom world biome to " + biome.getName() + ". (REQUIRES REJOIN).");
		}

		return true;
	}

	public static List<String> onTweakTabComplete(CustomTweak tweak, CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 2) { return List.of("type", "color"); }

		if ((args.length == 3) && args[1].equalsIgnoreCase("type")) {
			if (!Utils.hasPermissions(sender, "cworld.set")) { return null; }
			return Arrays.stream(CustomWorld.values()).map(e -> e.toString().toLowerCase()).toList();
		}

		if ((args.length >= 3) && args[1].equalsIgnoreCase("color")) {
			if (!Utils.hasPermissions(sender, "cworld.set")) { return null; }
			return List.of(CustomBiome.toHex(new Random().nextInt(0xFFFFFF + 1)));
		}

		return null;
	}
}
