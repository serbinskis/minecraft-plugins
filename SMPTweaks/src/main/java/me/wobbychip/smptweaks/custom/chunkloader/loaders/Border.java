package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Border {
	public int viewDistance;
	public Location center;
	WorldBorder border;
	List<Player> players = new ArrayList<>();

	public Border(int viewDistance, Location center) {
		this.viewDistance = viewDistance;
		this.center = center;

		border = Bukkit.getServer().createWorldBorder();
		int x = (int) ((center.getChunk().getX()*16+8) * center.getWorld().getCoordinateScale());
		int z = (int) ((center.getChunk().getZ()*16+8) * center.getWorld().getCoordinateScale());

		border.setCenter(new Location(center.getWorld(), x, center.getY(), z));
		border.setSize(viewDistance*16*2+16);
		border.setDamageAmount(0);
		border.setWarningDistance(-1);
		border.setWarningTime(-1);
	}

	public boolean containsPlayer(Player player) {
		return players.stream().anyMatch(e -> e.getUniqueId().equals(player.getUniqueId()));
	}

	public void addPlayer(Player player) {
		if (containsPlayer(player)) { return; }
		players.add(player);
		Utils.sendActionMessage(player, "Virtual border is now visible.");
		player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
	}

	public void removePlayer(Player player) {
		players.remove(player);
		if (!center.getWorld().equals(player.getWorld())) { return; }
		player.setWorldBorder(center.getWorld().getWorldBorder());
		Utils.sendActionMessage(player, "Virtual border is now hidden.");
		player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
	}

	public void togglePlayer(Player player) {
		if (containsPlayer(player)) {
			removePlayer(player);
		} else {
			addPlayer(player);
		}
	}

	public void update() {
		List<Player> remove = new ArrayList<>();

		for (Player player : players) {
			if (!player.isOnline()) { remove.add(player); continue; }
			if (!center.getWorld().equals(player.getWorld())) { remove.add(player); continue; }

			player.setWorldBorder(border);
			Utils.sendActionMessage(player, "Virtual border is now visible.");
		}

		remove.forEach(e -> removePlayer(e));
	}

	public void remove() {
		players.forEach(e -> removePlayer(e));
	}
}
