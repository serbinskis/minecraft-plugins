package me.wobbychip.smptweaks.custom.expbottles;

import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!ExpBottles.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENCHANTING_TABLE) { return; }
		if (event.getItem() == null || event.getItem().getType() != Material.GLASS_BOTTLE) { return; }

		//Prevent player from using both hands at the same time
		ItemStack itemMainHand = event.getPlayer().getInventory().getItemInMainHand();
		if ((event.getHand() == EquipmentSlot.OFF_HAND) && (itemMainHand != null) && (itemMainHand.getType() == Material.GLASS_BOTTLE)) { return; }

		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		ItemMeta itemMeta = item.getItemMeta();

		//Part where duplication glitch fixed, weird method, but working.
		if ((itemMeta != null) && itemMeta.isUnbreakable() && (item.getAmount() <= 1)) { return; }

		if (player.getGameMode() != GameMode.CREATIVE) { 
			if (!player.isSneaking() || (Utils.getPlayerExp(player) < 11)) { return; }
			player.giveExp(Utils.randomRange(4, 11) * -1);

			//Needed to prevent duplication glitch
			if (item.getAmount() <= 1) {
				itemMeta.setUnbreakable(true);
				item.setItemMeta(itemMeta);
			} else {
				item.setAmount(item.getAmount()-1);
			}
		}

		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

		//Make this to avoid twice event glitch
		//Since replacing item in main hand will trigger event to run again
		//This also causes duplication glitch if clicking too fast, like with macro fast
		TaskUtils.scheduleSyncDelayedTask(() -> {
			if ((player.getGameMode() != GameMode.CREATIVE) && (itemMeta != null) && itemMeta.isUnbreakable()) { item.setAmount(0); }
			ItemStack expBootle = new ItemStack(Material.EXPERIENCE_BOTTLE);
			HashMap<Integer, ItemStack> items = player.getInventory().addItem(expBootle);
			if (!items.isEmpty()) { Utils.dropItem(player, expBootle); }
		}, 1L);

		event.setUseInteractedBlock(Result.DENY);
	}
}
