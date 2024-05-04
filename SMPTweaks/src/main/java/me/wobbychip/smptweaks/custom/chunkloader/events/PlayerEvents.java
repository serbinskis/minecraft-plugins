package me.wobbychip.smptweaks.custom.chunkloader.events;

import me.wobbychip.smptweaks.custom.chunkloader.loaders.Border;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.LoaderBlock;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerEvents implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) { return; }

		ItemStack mainhand = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);
		if (mainhand.getType() != Material.AIR) { return; }

		ItemStack offhand = event.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND);
		if (offhand.getType() != Material.AIR) { return; }

		if (!LoaderBlock.LOADER_BLOCK.isCustomBlock(event.getClickedBlock())) { return; }

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Border.togglePlayer(event.getPlayer(), event.getClickedBlock());
		}
	}
}