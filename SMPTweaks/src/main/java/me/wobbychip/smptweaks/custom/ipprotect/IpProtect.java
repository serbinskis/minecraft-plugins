package me.wobbychip.smptweaks.custom.ipprotect;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

import java.util.List;

public class IpProtect extends CustomTweak {
	public static CustomTweak tweak;

	public IpProtect() {
		super(IpProtect.class, false, false);
		this.setCommand(new Commands(this, "ipprotect"));
		this.setConfigs(List.of("config.yml"));
		this.setReloadable(true);
		this.setDescription("Allow player to join only if ip address matches.");
		IpProtect.tweak = this;
	}

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
