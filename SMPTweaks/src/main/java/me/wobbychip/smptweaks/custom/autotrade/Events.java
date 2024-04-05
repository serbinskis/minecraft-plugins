package me.wobbychip.smptweaks.custom.autotrade;

import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;

public class Events implements Listener {
	//WHO TF IS CANCELLING MY EVENTS
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		boolean isFakePlayer = event.getPlayer().getUniqueId().equals(AutoTrade.fakePlayer.getUniqueId());
		if (isFakePlayer) { event.setCancelled(false); }

		if (event.isCancelled()) { return; }
		if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) { return; }

		InventoryHolder holder = event.getInventory().getHolder();
		if (!(holder instanceof BlockInventoryHolder)) { return; }

		Block block = ((BlockInventoryHolder) holder).getBlock();
		if (!(CustomBlocks.getCustomBlock(block) instanceof TraderBlock)) { return; }
		Villagers.releaseXp(block, event.getPlayer().getLocation());
	}
}