package me.wobbychip.smptweaks.custom.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class Events implements Listener {
	private final com.earth2me.essentials.Essentials essentials;
	private final CustomTweak tweak;
	private final HashMap<UUID, Integer> reverser = new HashMap<>();
	private final HashMap<UUID, Boolean> collidables = new HashMap<>();

	public Events(Plugin essentials) {
		this.essentials = (Essentials) essentials;
		this.tweak = me.wobbychip.smptweaks.custom.essentials.Essentials.tweak;

		TaskUtils.scheduleSyncRepeatingTask(() -> {
			for (Player player : Bukkit.getOnlinePlayers()) { onPlayerTick(player); }
		}, 1L, 1L);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (!this.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (!event.getAction().isRightClick() || (event.getClickedBlock() == null)) { return; }
		if (event.getPlayer().isSneaking() && (event.getItem() != null) && (event.getItem().getType() != Material.AIR)) { return; }
		if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) { ReflectionUtils.unpackLoot(event.getClickedBlock()); return; }

		User user = essentials.getUser(event.getPlayer().getUniqueId());
		if ((user == null) || !user.isVanished()) { return; }
		if (!(event.getClickedBlock().getState() instanceof Container)) { return; }
		ReflectionUtils.unpackLoot(event.getClickedBlock()); //Generate loot, this function will check everything else

		Player player = event.getPlayer();
		GameMode gameMode = player.getGameMode();
		boolean isFlying = player.isFlying();
		boolean allowFlight = player.getAllowFlight();
		player.setGameMode(GameMode.SPECTATOR);
		if (!isFlying) { player.setFlying(false); }

		int task = TaskUtils.scheduleSyncDelayedTask(() -> {
			reverser.remove(player.getUniqueId());
			player.setGameMode(gameMode);
			player.setAllowFlight(allowFlight);
			player.setFlying(isFlying);
		}, 1L);

		reverser.put(player.getUniqueId(), task);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		if (!this.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		User user = essentials.getUser(event.getPlayer().getUniqueId());
		if ((user == null) || !user.isVanished()) { return; }

		if (!reverser.containsKey(user.getUUID())) { return; }
		TaskUtils.finishTask(reverser.get(user.getUUID()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player player)) { return; }
		if (!this.tweak.getGameRuleBoolean(player.getWorld())) { return; }
		User user = essentials.getUser(player.getUniqueId());
		if ((user == null) || !user.isVanished() || player.isSneaking()) { return; }
		event.setCancelled(true);
	}

	public void onPlayerTick(Player player) {
		User user = essentials.getUser(player.getUniqueId());
		if (user == null) { return; }
		boolean b1 = this.tweak.getGameRuleBoolean(player.getWorld());
		if (b1 && !player.isCollidable() && user.isVanished() && !player.isSneaking()) { collidables.put(player.getUniqueId(), true); }
		if (b1 && user.isVanished() && !player.isSneaking()) { player.setCollidable(false); return; }
		if (collidables.containsKey(player.getUniqueId())) { player.setCollidable(collidables.remove(player.getUniqueId())); }
	}
}
