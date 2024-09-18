package me.serbinskis.smptweaks.custom.autotrade;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AutoTrade extends CustomTweak {
	public static String TAG_AUTO_TRADE_XP = "isAutoTrade";
	public static int TRADE_DISTANCE = 2;
	public static CustomTweak tweak;
	public static Player fakePlayer;

	public AutoTrade() {
		super(AutoTrade.class, false, false);
		this.setGameRule("doAutoTrade", true, false);
		this.setDescription("Put on a dispenser an item frame with a nether star. " +
							"Put items from trade in the dispenser. " +
							"Move villager to the dispenser in 2 block radius. " +
							"Input any container behind the dispenser, output in front. " +
							"Experience from trades is also saved and given upon opening dispenser.");
		AutoTrade.tweak = this;
	}

	public void onEnable() {
		Location location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		AutoTrade.fakePlayer = FakePlayer.addFakePlayer(location, new UUID(0, 0), false, false, false, true, false);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
		CustomBlocks.registerBlock(new TraderBlock());
	}
}
