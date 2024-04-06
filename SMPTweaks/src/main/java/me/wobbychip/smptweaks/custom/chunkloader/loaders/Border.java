package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class Border {
	private static final HashMap<Player, Map.Entry<Block, WorldBorder>> players = new HashMap<>();

	public static boolean containsPlayer(Player player) {
		return players.keySet().stream().anyMatch(e -> e.getUniqueId().equals(player.getUniqueId()));
	}

	public static void addPlayer(Player player, Block block) {
		if (containsPlayer(player)) { removePlayer(player); }

		WorldBorder border = Bukkit.getServer().createWorldBorder();
		int x = (int) ((block.getChunk().getX()*16+8) * block.getWorld().getCoordinateScale());
		int z = (int) ((block.getChunk().getZ()*16+8) * block.getWorld().getCoordinateScale());

		border.setCenter(new Location(block.getWorld(), x, block.getY(), z));
		border.setSize(ChunkLoader.viewDistance*16*2+16);
		border.setDamageAmount(0);
		border.setWarningDistance(-1);
		border.setWarningTime(-1);

		players.put(player, Map.entry(block, border));
		Utils.sendActionMessage(player, "Virtual border is now visible.");
		player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
	}

	public static void removePlayer(Player player) {
		players.keySet().removeIf(e -> e.getUniqueId().equals(player.getUniqueId()));
		player.setWorldBorder(player.getWorld().getWorldBorder());
		Utils.sendActionMessage(player, "Virtual border is now hidden.");
		player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
	}

	public static void togglePlayer(Player player, Block block) {
		if (containsPlayer(player)) {
			removePlayer(player);
		} else {
			addPlayer(player, block);
		}
	}

	public static void update() {
		List<Player> remove = new ArrayList<>();

		for (Map.Entry<Player, Map.Entry<Block, WorldBorder>> data : players.entrySet()) {
			if (!data.getKey().isOnline()) { remove.add(data.getKey()); continue; }
			if (!data.getValue().getKey().getWorld().equals(data.getKey().getWorld())) { remove.add(data.getKey()); continue; }

			data.getKey().setWorldBorder(data.getValue().getValue());
			Utils.sendActionMessage(data.getKey(), "Virtual border is now visible.");
		}

		remove.forEach(Border::removePlayer);
	}

	public static void remove(Block block) {
		players.forEach((key, value) -> {
            if (value.getKey().getLocation().equals(block.getLocation())) { removePlayer(key); }
        });
	}
}
