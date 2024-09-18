package me.serbinskis.smptweaks.custom.holograms;

import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.UUID;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (event.getHand() != EquipmentSlot.HAND) { return; }
		if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.BOOK) { return; }
		if (!event.getPlayer().isSneaking()) { return; }
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.create")) { return; }

		Hologram hologram = Hologram.create(event.getClickedBlock().getLocation(), "§bHologram - right click with an empty hand");
		hologram.updateRotation(event.getPlayer());
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
		if (event.getRightClicked().getType() != EntityType.INTERACTION) { return; }
		if (event.getHand() != EquipmentSlot.HAND) { return; }
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.edit")) { return; }

		Hologram hologram = Hologram.get((Interaction) event.getRightClicked());
		if (hologram == null) { return; }

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
			event.getPlayer().getInventory().setItemInMainHand(hologram.getBook());
		}

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.STICK) {
			hologram.setSeeThrough(!hologram.getSeeThrough());
		}

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
			hologram.setAlignment(hologram.getAlignment()+1);
		}

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WRITABLE_BOOK) {
			ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
			BookMeta bookMeta = (BookMeta) item.getItemMeta();
			hologram.setText((bookMeta.getPageCount() > 0) ? bookMeta.getPages().get(0) : "");
		}

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS) {
			hologram.updateRotation(45);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerEditBookEvent(PlayerEditBookEvent event) {
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.edit")) { return; }
		UUID uuid;

		try {
			ItemStack item = event.getPlayer().getInventory().getItem(event.getSlot());
			uuid = UUID.fromString(PersistentUtils.getPersistentDataString(item, Hologram.TAG_DISPLAY_UUID));
		} catch (Exception e) { return; }

		Hologram hologram = Hologram.get(uuid);
		if (hologram == null) { return; }

		BookMeta bookMeta = event.getNewBookMeta();
		hologram.setText((bookMeta.getPageCount() > 0) ? bookMeta.getPages().get(0) : "");
		bookMeta.setAuthor(event.getPreviousBookMeta().getAuthor());
		event.setNewBookMeta(bookMeta);
		event.setSigning(false);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) { return; }
		if (event.getEntity().getType() != EntityType.INTERACTION) { return; }
		if (!Utils.hasPermissions(event.getDamager(), "smptweaks.holograms.destroy")) { return; }

		Hologram hologram = Hologram.get((Interaction) event.getEntity());
		if (hologram == null) { return; }

		if (((Player) event.getDamager()).getInventory().getItemInMainHand().getType() == Material.BOOK) {
			hologram.remove();
		}

		if (((Player) event.getDamager()).getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
			hologram.setAlignment(0);
		}

		if (((Player) event.getDamager()).getInventory().getItemInMainHand().getType() == Material.STICK) {
			hologram.setBillboard((hologram.getBillboard() == Billboard.VERTICAL) ? Billboard.FIXED : Billboard.VERTICAL);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.WRITABLE_BOOK) { return; }
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.edit")) { return; }

		ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
		BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
		UUID uuid;

		try {
			ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
			uuid = UUID.fromString(PersistentUtils.getPersistentDataString(item, Hologram.TAG_DISPLAY_UUID));
		} catch (Exception e) { return; }

		Hologram hologram = Hologram.get(uuid);
		if (hologram == null) { return; }

		hologram.teleport(event.getBlock().getLocation());
		hologram.updateRotation(event.getPlayer());
		bookMeta.setDisplayName(hologram.getBook().getItemMeta().getDisplayName());
		itemStack.setItemMeta(bookMeta);
		event.setCancelled(true);
	}
}
