package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class FakePlayer {
	private static final HashMap<Location, Player> fakes = new HashMap<>();

	public static void setEnabled(boolean isEnabled, Block block) {
		Location location = block.getLocation().add(0.5, 0.5, 0.5);
		Player player = fakes.get(location);

		if (isEnabled) {
			if (player != null) { ReflectionUtils.removeFakePlayer(player); }
			player = ReflectionUtils.addFakePlayer(location, null, true, true, false);
			ReflectionUtils.setPlayerAdvancements(ChunkLoader.fakePlayer, player);
			fakes.put(location, player);
		} else {
			if (player != null) { ReflectionUtils.removeFakePlayer(player); }
			fakes.remove(location);
		}

		update();
	}

	public static void add(Block block) {
		setEnabled(true, block);
	}

	public static void remove(Block block) {
		setEnabled(false, block);
	}

	public static void update() {
		fakes.forEach((location, fplayer) -> {
			boolean isValid = fplayer.isValid();

			//Hide player for everyone, who is not OP
			for (Player player : fplayer.getWorld().getPlayers()) {
				if (isValid && !player.isOp()) { player.hidePlayer(Main.plugin, fplayer); }
			}

			//Put fake player back to his location
			if (isValid) { fplayer.teleport(location); }
			if (isValid) { fplayer.setCollidable(false); }
			if (isValid) { ReflectionUtils.updateFakePlayer(fplayer); }
		});
	}

	public static boolean isFakePlayer(UUID uuid) {
		return fakes.values().stream().anyMatch(e -> e.getUniqueId().equals(uuid));
	}
}
