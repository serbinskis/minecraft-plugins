package me.serbinskis.smptweaks.custom.pvpdropinventory;

import me.serbinskis.smptweaks.Config;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.*;
import java.util.Map.Entry;

public class PlayerTimer {
	protected Config config;
	protected Map<UUID, Integer> timers = new HashMap<>();
	protected List<UUID> tried = new ArrayList<>();
	protected int taskId;
	protected String actionBarMessage;

	public PlayerTimer(Config config, String actionBarMessage) {
		this.config = config;
		this.actionBarMessage = actionBarMessage;

		for (String key : this.config.getConfig().getKeys(false)) {
			int seconds = config.getConfig().getInt(key);
			timers.put(UUID.fromString(key), seconds);
		}

		taskId = TaskUtils.scheduleSyncRepeatingTask(this::checkPlayers, 1L, 20L);
	}

	public void save(boolean bStop) {
		if (bStop) { TaskUtils.cancelTask(taskId); }

		for (String key : this.config.getConfig().getKeys(false)) {
			this.config.getConfig().set(key, null);
		}

		for (UUID key : timers.keySet()) {
			int seconds = timers.get(key);
			this.config.getConfig().set(key.toString(), seconds);
		}

		this.config.save();
	}

	public void addPlayer(Player player, int seconds) {
		timers.put(player.getUniqueId(), seconds);
	}

	public void addPlayers(Player player1, Player player2) {
		if ((player1 == null) || (player2 == null)) { return; }
		if (player1.getUniqueId().equals(player2.getUniqueId())) { return; }

		PvPDropInventory.timer.addPlayer(player1, PvPDropInventory.timeout);
		Utils.sendActionMessage(player1, PvPDropInventory.timer.actionBarMessage);
		if (!PvPDropInventory.elytraAllowed) { player1.setGliding(false); }

		PvPDropInventory.timer.addPlayer(player2, PvPDropInventory.timeout);
		Utils.sendActionMessage(player2, PvPDropInventory.timer.actionBarMessage);
		if (!PvPDropInventory.elytraAllowed) { player2.setGliding(false); }
	}

	public void addTried(Player player) {
		tried.remove(player.getUniqueId());
		tried.add(player.getUniqueId());
	}

	public void removePlayer(Player player) {
		timers.remove(player.getUniqueId());
	}

	public boolean isPlayer(Player player) {
		return timers.containsKey(player.getUniqueId());
	}

	public void checkPlayers() {
		Iterator<Entry<UUID, Integer>> iterator = timers.entrySet().iterator();

		while (iterator.hasNext()) {
			UUID uuid = iterator.next().getKey();
			Player player = Bukkit.getPlayer(uuid);
			if ((player == null) || !player.isOnline()) { continue; }
			if ((!PvPDropInventory.tweak.getGameRuleBoolean(player.getWorld()))) { iterator.remove(); continue; }
			
			int seconds = timers.get(uuid)-1;
			EntityDamageEvent event = new EntityDamageEvent(player, DamageCause.VOID, DamageSource.builder(DamageType.GENERIC).build(), 0);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) { seconds += 1; }

			if (seconds <= 0) { iterator.remove(); continue; }
			timers.put(uuid, seconds);

			if (!tried.contains(uuid)) {
				Utils.sendActionMessage(player, actionBarMessage);
			} else {
				tried.remove(uuid);
			}
		}
	}
}
