package me.wobbychip.smptweaks.tweaks;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PaperUtils;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.Map.Entry;

public class TweakManager {
	protected Map<String, CustomTweak> tweaks = new HashMap<>();
	public boolean isProtocolLib = (Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null);

	public void loadTweaks(boolean startup) {
		List<CustomTweak> values = tweaks.values().stream().sorted(Comparator.comparing(e -> e.getClass().getSimpleName())).toList();

		for (CustomTweak tweak : values) {
			if (startup && tweak.isStartup()) { loadTweak(tweak); }
			if (!startup && !tweak.isStartup()) { loadTweak(tweak); }
		}
	}

	public void addTweak(CustomTweak tweak) {
		tweaks.put(tweak.getName(), tweak);
	}

	private void loadTweak(CustomTweak tweak) {
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
			if (gamerule != null) { Main.gameRules.addGameRule(gamerule.getKey(), gamerule.getValue(), tweak.isGameRuleGlobal()); }

			tweak.loadConfigs();
			tweak.onEnable();

			if (tweak.isEnabled()) { tweak.printEnabled(); } else { tweak.printDisabled(); }
			if (!tweak.isEnabled() && (gamerule != null)) { Main.gameRules.removeGameRule(gamerule.getKey()); }
			if (tweak.isEnabled()) { tweak.setLoaded(true); }
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
			if (tweak.isEnabled() && tweak.isLoaded()) { tweak.onDisable(); }
		}
	}
}
