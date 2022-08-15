package me.wobbychip.smptweaks.custom.autotrade;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.Config;
import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class AutoTrade extends CustomTweak {
	public static String isAutoTrade = "isAutoTrade";
	public static int tradeCooldown = 20;
	public static int tradeDistance = 2;
	public static String redstoneMode = "indirect";
	public static boolean allowBlockRecipeModification = true;
	public static Traders traders;
	public static Config config;
	public static Player fakePlayer;

	public AutoTrade() {
		super(AutoTrade.class.getSimpleName(), false, false);
	}

	public void onEnable() {
		loadConfig();
		Location location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		fakePlayer = ReflectionUtils.addFakePlayer(location, new UUID(0, 0), false, false, false);
		traders = new Traders();

		Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
			Utils.grantAdvancemnt(fakePlayer, advancement);
		});

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				traders.handleTraders();
			}
		}, 1L, tradeCooldown);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public static void loadConfig() {
		List<String> list = Arrays.asList(AutoTrade.class.getCanonicalName().split("\\."));
		String configPath = String.join("/", list.subList(0, list.size()-1)) + "/config.yml";
		AutoTrade.config = new Config(configPath, "/tweaks/AutoTrade/config.yml");

		AutoTrade.tradeCooldown = AutoTrade.config.getConfig().getInt("tradeCooldown");
		AutoTrade.tradeDistance = AutoTrade.config.getConfig().getInt("tradeDistance");
		AutoTrade.redstoneMode = AutoTrade.config.getConfig().getString("redstoneMode");
		AutoTrade.allowBlockRecipeModification = AutoTrade.config.getConfig().getBoolean("allowBlockRecipeModification");
	}
}
