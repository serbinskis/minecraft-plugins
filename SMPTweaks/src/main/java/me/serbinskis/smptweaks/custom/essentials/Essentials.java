package me.serbinskis.smptweaks.custom.essentials;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Essentials extends CustomTweak {
	public static CustomTweak tweak;

	public Essentials() {
		super(Essentials.class, false, false);
		this.setGameRule("custom_essentials", true, false);
		this.setDescription("Adds custom tweaks to EssentialsX Vanish.");
		Essentials.tweak = this;
	}

	public void onEnable() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		this.setEnabled(plugin != null);
		if (this.isEnabled()) { Bukkit.getPluginManager().registerEvents(new Events(plugin), Main.plugin); }
	}
}
