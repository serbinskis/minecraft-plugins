package me.wobbychip.smptweaks.tweaks;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PaperUtils;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

			tweak.loadConfigs();
			tweak.onEnable();

			if (tweak.isEnabled()) { tweak.printEnabled(); } else { tweak.printDisabled(); }
			Entry<String, Object> gamerule = tweak.getGameRule();
			if (tweak.isEnabled() && (gamerule != null)) { Main.gameRules.addGameRule(gamerule.getKey(), gamerule.getValue(), tweak.isGameRuleGlobal()); }
		} else {
			tweak.printDisabled();
		}
	}

	public CustomTweak getTweak(String name, boolean commands) {
		for (Entry<String, CustomTweak> entry : tweaks.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}

		if (!commands) { return null; }

		for (Entry<String, CustomTweak> entry : tweaks.entrySet()) {
			if ((entry.getValue().getCommand() != null) && (entry.getValue().getCommand().getCommand().equalsIgnoreCase(name))) {
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
