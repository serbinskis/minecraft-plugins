package me.serbinskis.smptweaks.custom.betterlead;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;

public class BetterLead extends CustomTweak {
	public static int LEASH_MAX_DISTANCE = getLeashMaxDistance();
	public static CustomTweak tweak;

	public BetterLead() {
		super(BetterLead.class, true, false);
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doBetterLead", true, false);
		this.setDescription("Make lead much longer and allow lead other mobs.");
		BetterLead.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public static int getLeashMaxDistance() {
		YamlConfiguration spigotConfig = Bukkit.spigot().getSpigotConfig();
		ConfigurationSection section = spigotConfig.getConfigurationSection("world-settings.default.entity-tracking-range");
		return (section == null) ? 48 : Math.max(section.getInt("animals", 48), 96);
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
