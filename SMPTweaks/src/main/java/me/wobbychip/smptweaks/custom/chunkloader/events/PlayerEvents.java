package me.wobbychip.smptweaks.custom.chunkloader.events;

import me.wobbychip.smptweaks.custom.chunkloader.loaders.Border;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.FakePlayer;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.LoaderBlock;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerEvents implements Listener {
	public List<Player> chats = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) { return; }

		ItemStack mainhand = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);
		if ((mainhand == null) || (mainhand.getType() != Material.AIR)) { return; }

		ItemStack offhand = event.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND);
		if ((offhand == null) || (offhand.getType() != Material.AIR)) { return; }

		if (!LoaderBlock.LOADER_BLOCK.isCustomBlock(event.getClickedBlock())) { return; }

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Border.togglePlayer(event.getPlayer(), event.getClickedBlock());
		}
	}

	//Prevent BlazeandCave's Advancements messages for fake player
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		if (!FakePlayer.isFakePlayer(event.getPlayer().getUniqueId())) { return; }

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (chats.contains(player)) { continue; }
			chats.add(player);

			String visibility = ReflectionUtils.getChatVisibility(player);
			ReflectionUtils.setChatVisibility(player, "options.chat.visibility.hidden");

			TaskUtils.scheduleSyncDelayedTask(() -> {
				chats.remove(player);
				ReflectionUtils.setChatVisibility(player, visibility);
			}, 1L);
		}
	}
}