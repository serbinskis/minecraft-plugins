package me.wobbychip.smptweaks.tweaks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PaperUtils;

public class TweakManager {
	protected Map<String, CustomTweak> tweaks = new HashMap<>();
	public boolean isProtocolLib = (Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null);

	public void addTweak(CustomTweak tweak) {
		tweaks.put(tweak.getName(), tweak);

		if (tweak.isEnabled()) {
			if (tweak.requiresPaper() && !PaperUtils.isPaper) {
				tweak.setEnabled(false);
				tweak.printMessage("Requires PaperMC.", true);
				return;
			}

			if (tweak.requiresProtocolLib() && !isProtocolLib) {
				tweak.setEnabled(false);
				tweak.printMessage("Requires ProtocolLib.", true);
				return;
			}

			Entry<String, Object> gamerule = tweak.getGameRule();
			if (gamerule != null) { Main.gameRules.addGameRule(gamerule.getKey(), gamerule.getValue()); }

			tweak.onEnable();
			tweak.printEnabled();
		} else {
			tweak.printDisabled();
		}
	}

	public CustomTweak getTweak(String name) {
		for (Entry<String, CustomTweak> entry : tweaks.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}

		return null;
	}

	public Collection<CustomTweak> getTweaks() {
		return tweaks.values();
	}

	public Set<String> keySet() {
		return tweaks.keySet();
	}

	public void disableAll() {
		for (CustomTweak tweak : tweaks.values()) {
			if (tweak.isEnabled()) { tweak.onDisable(); }
		}
	}
}
