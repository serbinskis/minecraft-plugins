package me.wobbychip.smptweaks.custom.noendportal;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPortalEvent(EntityPortalEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if ((event.getClickedBlock() == null) || (event.getClickedBlock().getType() != Material.END_PORTAL_FRAME)) { return; }
		ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getHand());
		if ((itemStack == null) || (itemStack.getType() != Material.ENDER_EYE)) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
		Utils.sendActionMessage(event.getPlayer(), "End portal is currently disabled.");
		event.setCancelled(true);
	}
}
