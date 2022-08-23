package me.wobbychip.smptweaks.tweaks;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.Utils;

public class CustomTweak {
	private String name;
	private String description;
	private boolean requiresPaper;
	private boolean requiresProtocolLib;
	private boolean enabled = true;
	private boolean isReloadable = false;

	public CustomTweak(String name, boolean requiresPaper, boolean requiresProtocolLib) {
		this.requiresPaper = requiresPaper;
		this.requiresProtocolLib = requiresProtocolLib;
		this.name = name;
		this.description = "No description";

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setReloadable(boolean isReloadable) {
		this.isReloadable = isReloadable;
	}

	public boolean isReloadable() {
		return enabled && isReloadable;
	}

	public boolean requiresPaper() {
		return requiresPaper;
	}

	public boolean requiresProtocolLib() {
		return requiresProtocolLib;
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

	public void onEnable() {}
	public void onDisable() {}
	public void onReload() {}
}
