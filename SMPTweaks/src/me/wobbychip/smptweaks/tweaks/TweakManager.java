package me.wobbychip.smptweaks.tweaks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.wobbychip.smptweaks.utils.PaperUtils;

public class TweakManager {
	protected Map<String, CustomTweak> tweaks = new HashMap<>();

	public void addTweak(CustomTweak tweak) {
		tweaks.put(tweak.getName(), tweak);

		if (tweak.isEnabled()) {
			if (PaperUtils.isPaper || !tweak.requiresPaper()) {
				tweak.onEnable();
				tweak.printEnabled();
			} else {
				tweak.setEnabled(false);
				tweak.printMessage("Requires PaperMC.", true);
			}
		} else {
			tweak.printDisabled();
		}
	}

	public String getTweaks() {
		Set<String> names = new HashSet<String>();

		for (CustomTweak tweak : tweaks.values()) {
			if (tweak.isEnabled()) { names.add(tweak.getName()); }
		}

		return String.join(", ", names);
	}

	public void disableAll() {
		for (CustomTweak tweak : tweaks.values()) {
			if (tweak.isEnabled()) { tweak.onDisable(); }
		}
	}

	public boolean sendCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		for (CustomTweak tweak : tweaks.values()) {
			if (tweak.isEnabled() && label.equalsIgnoreCase(tweak.getName())) {
				return tweak.onCommand(sender, command, label, args);
			}
		}

		return true;
	}
}
