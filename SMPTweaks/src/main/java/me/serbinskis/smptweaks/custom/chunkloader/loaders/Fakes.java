package me.serbinskis.smptweaks.custom.chunkloader.loaders;

import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Fakes {
	private static final HashMap<Location, Player> fakes = new HashMap<>();

	public static void setEnabled(boolean isEnabled, Block block) {
		Location location = block.getLocation().add(0.5, 0.5, 0.5);
		Player player = fakes.get(location);

		if (isEnabled) {
			if (player != null) { FakePlayer.removeFakePlayer(player); }
			player = FakePlayer.addFakePlayer(location, true, true, false, true, true);
			fakes.put(location, player);
		} else {
			if (player != null) { FakePlayer.removeFakePlayer(player); }
			fakes.remove(location);
		}
	}
}
