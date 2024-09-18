package me.serbinskis.smptweaks.custom.betterlead;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BetterLead extends CustomTweak {
	public static CustomTweak tweak;
	public static String TAG_IS_UNBREAKABLE_LEASH = "isUnbreakableLeash";
	public static int maxDistance = 100;
	public static List<String> custom = new ArrayList<>();

	public BetterLead() {
		super(BetterLead.class, false, false);
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doBetterLead", true, false);
		this.setReloadable(true);
		this.setDescription("Make lead much longer and allow lead other pre-configured mobs.");
		BetterLead.tweak = this;
	}

	public void onEnable() {
		this.onReload();
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
