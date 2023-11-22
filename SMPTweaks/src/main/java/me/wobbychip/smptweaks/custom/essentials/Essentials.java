package me.wobbychip.smptweaks.custom.essentials;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Essentials extends CustomTweak {
	public static CustomTweak tweak;

	public Essentials() {
		super(Essentials.class, true, false);
		this.setGameRule("doCustomEssentials", true, false);
		this.setDescription("Adds custom tweaks to EssentialsX.");
		Essentials.tweak = this;
	}

	public void onEnable() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		this.setEnabled(plugin != null);
		if (this.isEnabled()) { Bukkit.getPluginManager().registerEvents(new Events(plugin), Main.plugin); }
	}
}
