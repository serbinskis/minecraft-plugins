package me.serbinskis.smptweaks.custom.entitylimit;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class EntityLimit extends CustomTweak {
	public static CustomTweak tweak;
	public static List<String> excludeReason = new ArrayList<>();
	public static String tooManyEntity = "";
	public static int maximumDistance = 0;
	public static int limit = 0;

	public EntityLimit() {
		super(EntityLimit.class, false, false);
		this.setDescription("Limits entity spawn for each entity type.");
		this.setGameRule("entity_limit", true, false);
		this.setConfigs(List.of("config.yml"));
		this.setReloadable(true);
		EntityLimit.tweak = this;
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		EntityLimit.excludeReason = this.getConfig(0).getConfig().getStringList("excludeReason");
		EntityLimit.tooManyEntity = this.getConfig(0).getConfig().getString("tooManyEntity");
		EntityLimit.maximumDistance = this.getConfig(0).getConfig().getInt("maximumDistance");
		EntityLimit.limit = this.getConfig(0).getConfig().getInt("limit");
	}
}
