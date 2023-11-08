package me.wobbychip.smptweaks.custom.funnymessages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class FunnyMessages extends CustomTweak {
	public static List<String> messages = new ArrayList<>();

	public FunnyMessages() {
		super(FunnyMessages.class, false, false);
		this.setConfigs(List.of("messages.txt"));
		this.setReloadable(true);
		this.setDescription("Sends funny message on player death.");
	}

	public void onEnable() {
		this.onReload();
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		messages.clear();

		for (String line : this.getConfig(0).getContent().split("\\r?\\n")) {
			if (line.contains("<player>")) { messages.add(line); }
		}
	}
}
