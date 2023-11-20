package me.wobbychip.smptweaks.library.customessentials.events;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
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

public class PlayerEvents implements Listener {
    private final Essentials essentials;
    private final HashMap<UUID, Integer> reverser = new HashMap<>();
    private final HashMap<UUID, Boolean> collidables = new HashMap<>();

    public PlayerEvents(Plugin essentials) {
        this.essentials = (Essentials) essentials;

        TaskUtils.scheduleSyncRepeatingTask(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) { onPlayerTick(player); }
        }, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || (event.getClickedBlock() == null)) { return; }
        if (event.getPlayer().isSneaking() && (event.getItem() != null) && (event.getItem().getType() != Material.AIR)) { return; }
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) { return; }
        if (event.getPlayer().isSneaking()) { return; } //Allow open chest normally while sneaking, because in spectator you cannot generate loot inside chests

        User user = essentials.getUser(event.getPlayer().getUniqueId());
        if (!user.isVanished()) { return; }
        if (!(event.getClickedBlock().getState() instanceof Container container)) { return; }

        Player player = event.getPlayer();
        GameMode gameMode = player.getGameMode();
        boolean isFlying = player.isFlying();
        player.setGameMode(GameMode.SPECTATOR);
        if (!isFlying) { player.setFlying(false); }

        int task = TaskUtils.scheduleSyncDelayedTask(() -> {
            reverser.remove(player.getUniqueId());
            player.setGameMode(gameMode);
            player.setFlying(isFlying);
        }, 1L);

        reverser.put(player.getUniqueId(), task);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        User user = essentials.getUser(event.getPlayer().getUniqueId());
        if (!user.isVanished()) { return; }

        if (!reverser.containsKey(user.getUUID())) { return; }
        TaskUtils.finishSyncDelayedTask(reverser.get(user.getUUID()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) { return; }
        User user = essentials.getUser(player.getUniqueId());
        if (!user.isVanished() || (player.getGameMode() != GameMode.CREATIVE)) { return; }
        event.setCancelled(true);
    }

    public void onPlayerTick(Player player) {
        User user = essentials.getUser(player.getUniqueId());
        if (!player.isCollidable() && user.isVanished() && (player.getGameMode() == GameMode.CREATIVE)) { collidables.put(player.getUniqueId(), true); }
        if (user.isVanished() && (player.getGameMode() == GameMode.CREATIVE)) { player.setCollidable(false); return; }
        if (collidables.containsKey(player.getUniqueId())) { player.setCollidable(collidables.remove(player.getUniqueId())); }
    }
}
