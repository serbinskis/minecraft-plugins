package me.wobbychip.smptweaks.custom.noendportal;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPortalEvent(EntityPortalEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getEntity().getWorld())) { return; }
		if (event.getEntity().isOp()) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		if (event.getTo().getWorld().getEnvironment() != Environment.THE_END) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (event.getPlayer().isOp()) { return; }

		event.getPlayer().playSound(event.getPlayer().getLocation(), Main.DENY_SOUND_EFFECT, 1.0f, 1.0f);
		Utils.sendActionMessage(event.getPlayer(), "End portal is currently disabled.");
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if ((event.getClickedBlock() == null) || (event.getClickedBlock().getType() != Material.END_PORTAL_FRAME)) { return; }
		ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getHand());
		if (itemStack.getType() != Material.ENDER_EYE) { return; }
		if (NoEndPortal.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }

		event.getPlayer().playSound(event.getPlayer().getLocation(), Main.DENY_SOUND_EFFECT, 1.0f, 1.0f);
		Utils.sendActionMessage(event.getPlayer(), "End portal is currently disabled.");
		event.setCancelled(true);
	}
}
