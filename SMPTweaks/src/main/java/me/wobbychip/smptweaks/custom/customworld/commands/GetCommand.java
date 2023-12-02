package me.wobbychip.smptweaks.custom.customworld.commands;

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

public class GetCommand {
	public static boolean onTweakCommand(CustomTweak tweak, final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!(sender instanceof Player player)) { Utils.sendMessage(sender, Commands.NO_CONSOLE); return true; }
		if (!Utils.hasPermissions(sender, "cworld.get")) { Utils.sendMessage(sender, Commands.NO_PERMISSIONS); return true; }
		CustomWorld type = CustomWorld.getCustomType(PersistentUtils.getPersistentDataString(player.getWorld(), CustomWorlds.TAG_CUSTOM_WORLD));
		CustomBiome cbiome = BiomeManager.getCustomBiome(player.getWorld().getName());

		String message = "&a&lCustomWorld &8» &7" + player.getWorld().getName() + "\n" +
				"&9Type: &f" + ((type != null) ? type.toString().toLowerCase(): "NONE") + "\n" +
				"&9SkyColor: &f" + ((cbiome != null) ? cbiome.getSkyColorHex() : "NONE") + "\n" +
				"&9FogColor: &f" + ((cbiome != null) ? cbiome.getFogColorHex() : "NONE") + "\n" +
				"&9FoliageColor: &f" + ((cbiome != null) ? cbiome.getFoliageColorHex() : "NONE") + "\n" +
				"&9GrassColor: &f" + ((cbiome != null) ? cbiome.getGrassColorHex() : "NONE") + "\n" +
				"&9WaterColor: &f" + ((cbiome != null) ? cbiome.getWaterColorHex() : "NONE") + "\n" +
				"&9WaterFogColor: &f" + ((cbiome != null) ? cbiome.getWaterFogColorHex() : "NONE") + "\n";

		Utils.sendMessage(sender, message);
		return true;
	}
}
