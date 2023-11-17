package me.wobbychip.smptweaks.library.customessentials;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomMarker;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class CustomEssentials {
	public static void start() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		if (plugin == null) { return; }
		Bukkit.getPluginManager().registerEvents(new me.wobbychip.smptweaks.library.customessentials.events.PlayerEvents(plugin), Main.plugin);
	}
}
