package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;

public class FakePlayer {
	public Location location;
	public Player player;

	public FakePlayer(Location location) {
		this.location = location;
	}

	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			if (player != null) { ReflectionUtils.removeFakePlayer(player); }
			player = ReflectionUtils.addFakePlayer(location, null, true, true, false);
		} else {
			if (player != null) { ReflectionUtils.removeFakePlayer(player); }
			player = null;
		}
	}

	public Player getPlayer() {
		return player;
	}

	public void update() {
		if (player == null) { return; }
		boolean isValid = player.isValid();

		//Hide player for everyone, who is not OP
		for (Player player : player.getWorld().getPlayers()) {
			if (isValid && !player.isOp()) { player.hidePlayer(Main.plugin, this.player); }
		}

		//Put fake player back to his location
		if (isValid) { player.teleport(location); }
		if (isValid) { player.setCollidable(false); }
		if (isValid) { ReflectionUtils.updateFakePlayer(player); }
	}

	public void remove() {
		setEnabled(false);
	}
}
