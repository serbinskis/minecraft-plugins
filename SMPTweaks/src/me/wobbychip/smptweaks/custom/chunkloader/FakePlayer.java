package me.wobbychip.smptweaks.custom.chunkloader;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;

public class FakePlayer {
	public Location location;
	public Player player;
	public int task;

	public FakePlayer(Location location) {
		this.location = location;
		this.player = ReflectionUtils.addFakePlayer(location, true, false);

		this.task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				update();
			}
		}, 0L, 5L);
	}

	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			if (player != null) { ReflectionUtils.removeFakePlayer(player); }
			player = ReflectionUtils.addFakePlayer(location, true, false);
		} else {
			if (player != null) { ReflectionUtils.removeFakePlayer(player); }
			player = null;
		}
	}

	public void update() {
		if (player == null) { return; }

		if (!player.isValid()) {
			ReflectionUtils.removeFakePlayer(player);
			player = ReflectionUtils.addFakePlayer(location, true, false);
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.hidePlayer(Main.plugin, this.player);
		}

		player.teleport(location);
		player.setCollidable(false);
		ReflectionUtils.updateFakePlayer(player);
	}

	public void remove() {
		Bukkit.getServer().getScheduler().cancelTask(task);
		setEnabled(false);
	}
}
