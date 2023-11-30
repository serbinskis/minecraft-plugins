package me.wobbychip.smptweaks.custom.pvpdropinventory;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import java.util.List;

public class PvPDropInventory extends CustomTweak {
	public static CustomTweak tweak;
	public static int timeout;
	public static boolean dropAllXp;
	public static boolean elytraAllowed;
	public static String elytraBarMessage;
	public static String actionBarMessage;
	public static PlayerTimer timer = null;

	public PvPDropInventory() {
		super(PvPDropInventory.class, false, false);
		this.setConfigs(List.of("config.yml", "players.yml"));
		this.setGameRule("doPvPDropInventory", false, false);
		this.setReloadable(true);
		this.setDescription("Drop inventory when a player dies in the middle of PvP. " +
							"Meant to be used with keep inventory enabled.");
		PvPDropInventory.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onDisable() {
		PvPDropInventory.timer.save(true);
	}

	public void onReload() {
		if (PvPDropInventory.timer != null) { this.onDisable(); }

		PvPDropInventory.timeout = this.getConfig(0).getConfig().getInt("PvP_Timeout");
		PvPDropInventory.dropAllXp = this.getConfig(0).getConfig().getBoolean("PvP_DropAllXp");
		PvPDropInventory.elytraAllowed = this.getConfig(0).getConfig().getBoolean("PvP_ElytraAllowed");
		PvPDropInventory.elytraBarMessage = this.getConfig(0).getConfig().getString("PvP_ElytraMessage");
		PvPDropInventory.actionBarMessage = this.getConfig(0).getConfig().getString("PvP_ActionBar");
		PvPDropInventory.timer = new PlayerTimer(this.getConfig(1), actionBarMessage);
	}
}
