package me.wobbychip.smptweaks.custom.chunkloader.events;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Aggravator;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Border;
import me.wobbychip.smptweaks.custom.chunkloader.loaders.Loader;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerEvents implements Listener {
	public List<Player> chats = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
		if (!ChunkLoader.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (!(event.getRightClicked() instanceof ItemFrame frame)) { return; }
		if (frame.getAttachedFace() != BlockFace.DOWN) { return; }

		TaskUtils.scheduleSyncDelayedTask(() -> {
			if (frame.getItem().getType() != Material.NETHER_STAR) { return; }
			Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
			ChunkLoader.manager.addLoader(block, true);
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) { return; }

		ItemStack mainhand = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);
		if ((mainhand == null) || (mainhand.getType() != Material.AIR)) { return; }

		ItemStack offhand = event.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND);
		if ((offhand == null) || (offhand.getType() != Material.AIR)) { return; }

		Loader loader = ChunkLoader.manager.getLoader(event.getClickedBlock());
		if (loader == null) { return; }

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Border border = ChunkLoader.manager.getBorder(event.getPlayer());
			if ((border != null) && !border.equals(loader.getBorder())) { border.removePlayer(event.getPlayer()); }
			loader.getBorder().togglePlayer(event.getPlayer());
		}

		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (!ChunkLoader.enableAggravator) { return; }
			if (event.getPlayer().getGameMode() == GameMode.CREATIVE) { return; }
			Aggravator aggravator = loader.getAggravator();
			aggravator.setEnabled(!aggravator.isEnabled(), event.getPlayer());
			loader.update(true);
		}
	}

	//Prevent BlazeandCave's Advancements messages for fake player
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		if (!ChunkLoader.manager.isFakePlayer(event.getPlayer().getUniqueId())) { return; }

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