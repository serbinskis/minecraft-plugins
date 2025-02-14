package me.serbinskis.smptweaks.tweaks;

import me.serbinskis.smptweaks.Config;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomTweak {
	private final Class<?> clazz;
	private final String name;
	private String description;
	private String gamerule = null;
	private Object gameruleValue = null;
	private TweakCommands command;
	private final List<String> configNames = new ArrayList<>();
	private final List<Config> configs = new ArrayList<>();
	private boolean gameruleGlobal = false;
	private final boolean requiresPaper;
	private final boolean requiresProtocolLib;
	private boolean startup = false;
	private boolean enabled = true;
	private boolean loaded = false;
	private boolean isReloadable = false;

	public CustomTweak(Class<?> clazz, boolean requiresPaper) {
		this(clazz, requiresPaper, false, false);
	}

	public CustomTweak(Class<?> clazz, boolean requiresPaper, boolean requiresProtocolLib) {
		this(clazz, requiresPaper, requiresProtocolLib, false);
	}

	public CustomTweak(Class<?> clazz, boolean requiresPaper, boolean requiresProtocolLib, boolean permanent) {
		this.clazz = clazz;
		this.requiresPaper = requiresPaper;
		this.requiresProtocolLib = requiresProtocolLib;
		this.name = clazz.getSimpleName();
		this.description = "No description";
		if (permanent) { return; }

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

	public void setCommand(@Nonnull TweakCommands command) {
		this.command = command;
	}

	@Nullable
	public TweakCommands getCommand() {
		if (!enabled) { return null; }
		return this.command;
	}

	public void setConfigs(@Nonnull List<String> configs) {
		this.configNames.clear();
		this.configNames.addAll(configs);
	}

	@Nullable
	public Config getConfig(int index) {
		if (index >= this.configs.size()) { return null; }
		return this.configs.get(index);
	}

	public void loadConfigs() {
		this.configs.clear();

		for (String configName : this.configNames) {
			List<String> list = Arrays.asList(this.clazz.getCanonicalName().split("\\."));
			String configPath = String.join("/", list.subList(0, list.size()-1)) + "/" + configName;
			this.configs.add(new Config(configPath, "/tweaks/" + this.name + "/" + configName));
		}
	}

	public void setStartup(boolean startup) {
		this.startup = startup;
	}

	public boolean isStartup() {
		return startup;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setGameRule(String gamerule, Object value, boolean global) {
		this.gamerule = gamerule;
		this.gameruleValue = value;
		this.gameruleGlobal = global;
	}

	public boolean isGameRuleGlobal() {
		return this.gameruleGlobal;
	}

	@Nullable
	public Map.Entry<String, Object> getGameRule() {
		boolean arg0 = ((gamerule != null) && (gameruleValue != null));
		return arg0 ? Map.entry(gamerule, gameruleValue) : null;
	}

	public <T> T getGameRule(World world) {
		return (gamerule != null) ? Main.gameRules.getGameRule(world, gamerule) : null;
	}

	public boolean getGameRuleBoolean(World world) {
		return this.getGameRule(world);
	}

	public int getGameRuleInteger(World world) {
		return this.getGameRule(world);
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
		Utils.sendMessage(String.format("[%s] %s", (useName ? name : "SMPTweaks"), message));
	}

	public void printEnabled() {
		Utils.sendMessage(String.format("[SMPTweaks] %s has loaded.", this.getName()));
	}

	public void printDisabled() {
		Utils.sendMessage(String.format("[SMPTweaks] %s is set to disabled.", this.getName()));
	}

	public void onEnable() {}
	public void onDisable() {}
	public void onReload() {}
}
