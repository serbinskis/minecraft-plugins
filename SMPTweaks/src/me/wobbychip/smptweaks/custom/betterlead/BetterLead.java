package me.wobbychip.smptweaks.custom.betterlead;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.Utils;

public class BetterLead extends CustomTweak {
	public static CustomTweak tweak;
	public static String isUnbreakableLeash = "isUnbreakableLeash";
	public static int maxDistance = 100;
	public static List<String> custom = new ArrayList<>();;
	public static List<UUID> preventPacket = new ArrayList<>();

	public BetterLead() {
		super(BetterLead.class, false, true);
		BetterLead.tweak = this;
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doBetterLead", true, false);
		this.setReloadable(true);
		this.setDescription("Make lead much longer and allow lead almost any mob.");
	}

	public void onEnable() {
		this.onReload();
		new ProtocolEvents(Main.plugin);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		BetterLead.maxDistance = this.getConfig(0).getConfig().getInt("maxDistance");
		BetterLead.custom = this.getConfig(0).getConfig().getStringList("custom");
	}

	public static void setDeltaMovement(Entity holder, Entity target) {
		Location hLocation = holder.getLocation();
		Location tLocation = target.getLocation();

		double f = Utils.distance(hLocation, tLocation);
		double d0 = (hLocation.getX() - tLocation.getX()) / f;
		double d1 = (hLocation.getY() - tLocation.getY()) / f;
		double d2 = (hLocation.getZ() - tLocation.getZ()) / f;

		Vector vector = target.getVelocity().add(new Vector(Math.copySign(d0 * d0 * 0.4D, d0), Math.copySign(d1 * d1 * 0.4D, d1), Math.copySign(d2 * d2 * 0.4D, d2)));
		target.setVelocity(vector);
	}
}
