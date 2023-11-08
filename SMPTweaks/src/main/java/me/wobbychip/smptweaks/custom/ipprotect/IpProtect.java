package me.wobbychip.smptweaks.custom.ipprotect;

import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class IpProtect extends CustomTweak {
	public static CustomTweak tweak;

	public IpProtect() {
		super(IpProtect.class, false, false);
		IpProtect.tweak = this;
		this.setCommand(new Commands(this, "ipprotect"));
		this.setConfigs(List.of("config.yml"));
		this.setReloadable(true);
		this.setDescription("Allow player to join only if ip address matches.");
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
