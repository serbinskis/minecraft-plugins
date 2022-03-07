package me.wobbychip.smptweaks.tweaks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TweakManager {
	protected Map<String, CustomTweak> tweaks = new HashMap<String, CustomTweak>();

	public void addTweak(CustomTweak tweak) {
		tweaks.put(tweak.getName(), tweak);
	}

	public void disableAll() {
		Iterator<Entry<String, CustomTweak>> iterator = tweaks.entrySet().iterator();

		while (iterator.hasNext()) {
			CustomTweak tweak = iterator.next().getValue();
			if (tweak.isEnabled()) { tweak.onDisable(); }
		}
	}

	public boolean sendCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		Iterator<Entry<String, CustomTweak>> iterator = tweaks.entrySet().iterator();

		while (iterator.hasNext()) {
			CustomTweak tweak = iterator.next().getValue();
			if (tweak.isEnabled() && label.equalsIgnoreCase(tweak.getName())) {
				return tweak.onCommand(sender, command, label, args);
			}
		}

		return true;
	}
}
