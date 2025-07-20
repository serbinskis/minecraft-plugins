package me.serbinskis.smptweaks.tweaks;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.utils.GameRules;
import me.serbinskis.smptweaks.utils.PaperUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.Map.Entry;

public class TweakManager {
	private static final String TWEAKS_PACKAGE = Main.class.getPackageName();
	private static final Map<String, CustomTweak> tweaks = new HashMap<>();
	private static final boolean isProtocolLib = (Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null);

	public static void loadTweaks(boolean startup) {
		List<CustomTweak> instances = ReflectionUtils.getInstances(Main.getPluginClassLoader(), TWEAKS_PACKAGE, CustomTweak.class, true, true, true);
		List<CustomTweak> values = instances.stream().sorted(Comparator.comparing(e -> e.getClass().getSimpleName())).toList();

		for (CustomTweak tweak : values) {
			if (startup && tweak.isStartup()) { loadTweak(tweak); }
			if (!startup && !tweak.isStartup()) { loadTweak(tweak); }
		}
	}

	private static void loadTweak(CustomTweak tweak) {
		tweaks.put(tweak.getName(), tweak);

		if (tweak.isEnabled()) {
			if (tweak.requiresPaper() && !PaperUtils.isPaper()) {
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
			if (gamerule != null) { GameRules.addGameRule(gamerule.getKey(), gamerule.getValue(), tweak.isGameRuleGlobal()); }

			tweak.loadConfigs();
			tweak.onEnable();

			if (tweak.isEnabled()) { tweak.printEnabled(); } else { tweak.printDisabled(); }
			if (!tweak.isEnabled() && (gamerule != null)) { GameRules.removeGameRule(gamerule.getKey()); }
			if (tweak.isEnabled()) { tweak.setLoaded(true); }
		} else {
			tweak.printDisabled();
		}
	}

	public static CustomTweak getTweak(String name, boolean commands) {
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

	public static Collection<CustomTweak> getTweaks() {
		return tweaks.values();
	}

	public static Set<String> keySet() {
		return tweaks.keySet();
	}

	public static void disableAll() {
		for (CustomTweak tweak : tweaks.values()) {
			if (tweak.isEnabled() && tweak.isLoaded()) { tweak.onDisable(); }
		}
	}
}
