package me.serbinskis.smptweaks.custom.customworld.commands;

import me.serbinskis.smptweaks.custom.custompotions.commands.Commands;
import me.serbinskis.smptweaks.custom.customworld.CustomWorlds;
import me.serbinskis.smptweaks.custom.customworld.biomes.BiomeManager;
import me.serbinskis.smptweaks.custom.customworld.biomes.CustomBiome;
import me.serbinskis.smptweaks.custom.customworld.biomes.CustomWorld;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.Utils;
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
				"&9Effects: &f" + ((cbiome != null) ? (cbiome.isEffectsEnabled() ? "ENABLED" : "DISABLED") : "IGNORED") + "\n" +
				"&9SkyColor: &f" + ((cbiome != null) ? cbiome.getSkyColorHex() : "IGNORED") + "\n" +
				"&9FogColor: &f" + ((cbiome != null) ? cbiome.getFogColorHex() : "IGNORED") + "\n" +
				"&9FoliageColor: &f" + ((cbiome != null) ? cbiome.getFoliageColorHex() : "IGNORED") + "\n" +
				"&9GrassColor: &f" + ((cbiome != null) ? cbiome.getGrassColorHex() : "IGNORED") + "\n" +
				"&9WaterColor: &f" + ((cbiome != null) ? cbiome.getWaterColorHex() : "IGNORED") + "\n" +
				"&9WaterFogColor: &f" + ((cbiome != null) ? cbiome.getWaterFogColorHex() : "IGNORED") + "\n";

		Utils.sendMessage(sender, message);
		return true;
	}
}
