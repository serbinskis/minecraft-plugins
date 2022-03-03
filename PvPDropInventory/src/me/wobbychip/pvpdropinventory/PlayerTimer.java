package me.wobbychip.pvpdropinventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerTimer {
	protected Config config;
	protected Map<UUID, Integer> timers = new HashMap<UUID, Integer>();
	protected int taskId;
	protected String message;

	public PlayerTimer(String configName) {
		this.config = new Config(configName);
		this.message = Main.plugin.getConfig().getString("PvP_ActionBar");

		for (String key : this.config.getConfig().getKeys(false)) {
			int seconds = config.getConfig().getInt(key);
			timers.put(UUID.fromString(key), seconds);
		}

		taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
            public void run() {
            	checkPlayers();
            }
        }, 0L, 20L);
	}

	public void Save(boolean bStop) {
		if (bStop) {
			Bukkit.getServer().getScheduler().cancelTask(taskId);
		}

		for (String key : this.config.getConfig().getKeys(false)) {
			this.config.getConfig().set(key, null);
		}

		for (UUID key : timers.keySet()) {
			int seconds = timers.get(key);
			this.config.getConfig().set(key.toString(), seconds);
		}

		this.config.Save();
	}

	public void addPlayer(Player player, int seconds) {
		timers.put(player.getUniqueId(), seconds);
	}

	public void removePlayer(Player player) {
		timers.remove(player.getUniqueId());
	}

	public boolean isPlayer(Player player) {
		return timers.containsKey(player.getUniqueId());
	}

	public void sendActionMessage(Player player) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
	}

	private void checkPlayers() {
		Iterator<Entry<UUID, Integer>> iterator = timers.entrySet().iterator();

		while (iterator.hasNext()) {
			UUID uuid = iterator.next().getKey();
			Player player = Bukkit.getPlayer(uuid);
			if ((player != null) && player.isOnline() && !player.isInvulnerable()) {
				int seconds = timers.get(uuid)-1;
				if (seconds > 0) {
					timers.put(uuid, seconds);
					sendActionMessage(player);
				} else {
					iterator.remove();
				}
			}
		}
	}
}
