package me.serbinskis.smptweaks.custom.autotrade;

import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
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
		if (FakePlayer.isFakePlayer(event.getPlayer().getUniqueId())) { event.setCancelled(false); }
		if (event.isCancelled() || (event.getPlayer().getGameMode() == GameMode.SPECTATOR)) { return; }

		InventoryHolder holder = event.getInventory().getHolder();
		if (!(holder instanceof BlockInventoryHolder)) { return; }

		Block block = ((BlockInventoryHolder) holder).getBlock();
		if (!(CustomBlocks.getCustomBlock(block) instanceof TraderBlock)) { return; }
		Villagers.releaseXp(block, event.getPlayer().getLocation());
	}
}