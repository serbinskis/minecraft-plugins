package me.wobbychip.smptweaks.custom.autotrade;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class AutoTrade extends CustomTweak {
	public static CustomTweak tweak;
	public static int task = -1;
	public static String isAutoTrade = "isAutoTrade";
	public static int tradeCooldown = 20;
	public static int tradeDistance = 2;
	public static String redstoneMode = "indirect";
	public static boolean allowBlockRecipeModification = true;
	public static Traders traders;
	public static Player fakePlayer;

	public AutoTrade() {
		super(AutoTrade.class, false, false);
		AutoTrade.tweak = this;
		this.setConfigs(List.of("config.yml"));
		this.setGameRule("doAutoTrade", true, false);
		this.setReloadable(true);
		this.setDescription("Put on a dispenser an item frame with a nether star. " +
							"Put items from trade in the dispenser. " +
							"Move villager to the dispenser in 2 block radius. " +
							"Input any container behind the dispenser, output in front. " +
							"Experience from trades is also saved and given upon opening dispenser.");
	}

	public void onEnable() {
		this.onReload();
		Location location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		AutoTrade.fakePlayer = ReflectionUtils.addFakePlayer(location, new UUID(0, 0), false, false, false);
		AutoTrade.traders = new Traders();

		Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
			Utils.grantAdvancemnt(fakePlayer, advancement);
		});

		AutoTrade.task = TaskUtils.scheduleSyncRepeatingTask(new Runnable() {
			public void run() {
				if (!ServerUtils.isPaused()) { traders.handleTraders(); }
			}
		}, 1L, AutoTrade.tradeCooldown);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public void onReload() {
		AutoTrade.tradeCooldown = this.getConfig(0).getConfig().getInt("tradeCooldown");
		AutoTrade.tradeDistance = this.getConfig(0).getConfig().getInt("tradeDistance");
		AutoTrade.redstoneMode = this.getConfig(0).getConfig().getString("redstoneMode");
		AutoTrade.allowBlockRecipeModification = this.getConfig(0).getConfig().getBoolean("allowBlockRecipeModification");
		AutoTrade.task = TaskUtils.rescheduleSyncRepeatingTask(AutoTrade.task, 0L, AutoTrade.tradeCooldown);
	}
}
