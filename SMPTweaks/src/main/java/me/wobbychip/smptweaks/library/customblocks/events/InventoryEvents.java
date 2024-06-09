package me.wobbychip.smptweaks.library.customblocks.events;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.library.customblocks.blocks.CustomBlock;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.inventory.ItemStack;

public record InventoryEvents(CustomBlock customBlock) implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
		World world = event.getInventory().getLocation().getWorld();
		ItemStack result = event.getInventory().getResult();

		if (!customBlock.isCustomBlock(result)) { return; }
		result = customBlock.prepareCraft(event, world, result);

		if (result == null) {
			Player player = (Player) event.getViewers().getFirst();
			player.playSound(player.getLocation(), Main.DENY_SOUND_EFFECT, 1.0f, 1.0f);
			Utils.sendActionMessage(player, "This recipe is disabled.");
			result = new ItemStack(Material.AIR);
		}

		event.getInventory().setResult(result);
	}

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRecipeBookClickEvent(PlayerRecipeBookClickEvent event) {
		Player player = event.getPlayer();
		World world = player.getLocation().getWorld();
		ItemStack result = event.getRecipe().getResult();

		if (!customBlock.isCustomBlock(result)) { return; }
		result = customBlock.prepareCraft(null, world, result);

		if (result == null) {
			player.playSound(player.getLocation(), Main.DENY_SOUND_EFFECT, 1.0f, 1.0f);
			Utils.sendActionMessage(player, "This recipe is disabled.");
			event.setRecipe(CustomBlocks.EMPTY_RECIPE);
		}
	}
}