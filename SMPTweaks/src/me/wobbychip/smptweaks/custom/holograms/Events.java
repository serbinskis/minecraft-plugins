package me.wobbychip.smptweaks.custom.holograms;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (event.getHand() != EquipmentSlot.HAND) { return; }
		if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.BOOK) { return; }
		if (!event.getPlayer().isSneaking()) { return; }
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.create")) { return; }

		Hologram.create(event.getClickedBlock().getLocation(), "§bHologram - right click with an empty hand");
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
		if (event.getRightClicked().getType() != EntityType.INTERACTION) { return; }
		if (event.getHand() != EquipmentSlot.HAND) { return; }
		if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) { return; }
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.edit")) { return; }

		Hologram hologram = Hologram.get((Interaction) event.getRightClicked());
		if (hologram == null) { return; }

		ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		String location = Utils.locationToString(hologram.getLocation());
		bookMeta.setDisplayName("§bHologram (" + location + ")");
		bookMeta.setAuthor(hologram.getInteraction().getUniqueId().toString());
		bookMeta.setPages(hologram.getText());
		book.setItemMeta(bookMeta);

		event.getPlayer().getInventory().setItemInMainHand(book);
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerEditBookEvent(PlayerEditBookEvent event) {
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.edit")) { return; }
		UUID uuid;

		try {
			uuid = UUID.fromString(event.getPreviousBookMeta().getAuthor());
		} catch (Exception e) { return; }

		Hologram hologram = Hologram.get((Interaction) Utils.getEntityByUniqueId(uuid));
		if (hologram == null) { return; }

		BookMeta bookMeta = event.getNewBookMeta();
		hologram.setText((bookMeta.getPageCount() > 0) ? bookMeta.getPages().get(0) : "");
		bookMeta.setAuthor(hologram.getInteraction().getUniqueId().toString());
		event.setNewBookMeta(bookMeta);
		event.setSigning(false);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) { return; }
		if (event.getEntity().getType() != EntityType.INTERACTION) { return; }
		if (((Player) event.getDamager()).getInventory().getItemInMainHand().getType() != Material.BOOK) { return; }
		if (!Utils.hasPermissions(event.getDamager(), "smptweaks.holograms.destroy")) { return; }

		Hologram hologram = Hologram.get((Interaction) event.getEntity());
		if (hologram != null) { hologram.remove(); }
	}
}
