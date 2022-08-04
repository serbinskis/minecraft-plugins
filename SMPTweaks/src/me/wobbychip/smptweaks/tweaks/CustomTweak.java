package me.wobbychip.smptweaks.tweaks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.Utils;

public class CustomTweak {
	private String name;
	private boolean requiresPaper;
	private boolean enabled = true;

	public CustomTweak(String name, boolean requiresPaper) {
		this.requiresPaper = requiresPaper;
		this.name = name;

		if (!Main.plugin.getConfig().contains(name.toUpperCase())) {
			Main.plugin.getConfig().set(name.toUpperCase(), enabled);
			Main.plugin.saveConfig();
		} else {
			this.enabled = Main.plugin.getConfig().getBoolean(name.toUpperCase());
		}
	}

	public String getName() {
		return name;
	}

	public boolean requiresPaper() {
		return requiresPaper;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void printMessage(String message, boolean useName) {
		Utils.sendMessage(String.format("&9[%s] %s", (useName ? name : "SMPTweaks"), message));
	}

	public void printEnabled() {
		Utils.sendMessage(String.format("&9[SMPTweaks] %s has loaded.", this.getName()));
	}

	public void printDisabled() {
		Utils.sendMessage(String.format("&9[SMPTweaks] %s is set to disabled.", this.getName()));
	}

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) { return true; }
	public void onEnable() {}
	public void onDisable() {}
}
