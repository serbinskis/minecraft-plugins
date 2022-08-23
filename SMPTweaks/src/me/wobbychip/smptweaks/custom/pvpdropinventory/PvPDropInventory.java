package me.wobbychip.smptweaks.custom.pvpdropinventory;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class PvPDropInventory extends CustomTweak {
	public static int timeout;
	public static boolean dropAllXp;
	public static boolean elytraAllowed;
	public static String actionBarMessage;
	public static PlayerTimer timer;
	public static Config config;
	public static Config playerConfig;

	public PvPDropInventory() {
		super(PvPDropInventory.class.getSimpleName(), false, false);
		this.setReloadable(true);
		this.setDescription("Drop inventory when a player dies in the middle of PvP. " +
							"Ment to be used with keep inventory enabled.");
	}

	public void onEnable() {
		loadConfig();
		PvPDropInventory.timer = new PlayerTimer(playerConfig, actionBarMessage);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onDisable() {
		PvPDropInventory.timer.Save(true);
	}

	public void onReload() {
		onDisable();
		loadConfig();
		PvPDropInventory.timer = new PlayerTimer(playerConfig, actionBarMessage);
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(PvPDropInventory.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		PvPDropInventory.config = new Config(configPath, "/tweaks/PvPDropInventory/config.yml");

		configPath = String.join("/", list.subList(0, list.size()-1)) + "/players.yml";
		PvPDropInventory.playerConfig = new Config(configPath, "/tweaks/PvPDropInventory/players.yml");

		PvPDropInventory.timeout = PvPDropInventory.config.getConfig().getInt("PvP_Timeout");
		PvPDropInventory.dropAllXp = PvPDropInventory.config.getConfig().getBoolean("PvP_DropAllXp");
		PvPDropInventory.elytraAllowed = PvPDropInventory.config.getConfig().getBoolean("PvP_ElytraAllowed");
		PvPDropInventory.actionBarMessage = PvPDropInventory.config.getConfig().getString("PvP_ActionBar");
	}
}
