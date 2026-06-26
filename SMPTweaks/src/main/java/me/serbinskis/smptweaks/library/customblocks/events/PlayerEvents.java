package me.serbinskis.smptweaks.library.customblocks.events;

import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PlayerEvents implements Listener {
	public final CustomBlock customBlock;
	public final HashMap<String, CustomBlock> explodelist = new HashMap<>();

	public PlayerEvents(CustomBlock customBlock) { this.customBlock = customBlock; }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!EntityType.SULFUR_CUBE.equals(event.getRightClicked().getType())) { return; }
		ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getHand());
		if (!customBlock.isCustomBlock(itemStack)) { return; }
		if (!customBlock.isSulfurCubeInteractable()) { event.setCancelled(true); }
	}
}