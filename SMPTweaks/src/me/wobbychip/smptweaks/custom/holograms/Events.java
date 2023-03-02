package me.wobbychip.smptweaks.custom.holograms;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	HashMap<String, ArmorStand> holograms = new HashMap<String, ArmorStand>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (!PersistentUtils.hasPersistentDataBoolean(event.getRightClicked(), Holograms.isHologram)) { return; }
		ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
		if (item.getType() != Material.NAME_TAG) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
		if (event.getRightClicked().getType() != EntityType.ARMOR_STAND) { return; }
		if (event.getHand() != EquipmentSlot.HAND) { return; }

		boolean arg0 = PersistentUtils.hasPersistentDataBoolean(event.getRightClicked(), Holograms.isHologram);
		boolean arg1 = Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.edit");
		if (arg0 && !arg1) { event.setCancelled(true); }

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WRITABLE_BOOK) {
			if (arg0 || !Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.create")) { return; }
			PersistentUtils.setPersistentDataBoolean(event.getRightClicked(), Holograms.isHologram, true);

			ArmorStand armorStand = (ArmorStand) event.getRightClicked();
			armorStand.setInvisible(true);
			armorStand.setInvulnerable(true);
			armorStand.setCustomName("§bHologram - right click with empty hand");
			armorStand.setCustomNameVisible(true);
			armorStand.setGravity(false);
			armorStand.setAI(false);
			return;
		}

		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
			if (!arg0 || !arg1) { return; }
			ArmorStand armorStand = (ArmorStand) event.getRightClicked();
			holograms.put(armorStand.getUniqueId().toString(), armorStand);

			ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
			BookMeta bookMeta = (BookMeta) book.getItemMeta();
			bookMeta.setDisplayName("§bHologram");
			bookMeta.setTitle("Hologram");
			bookMeta.setAuthor(armorStand.getUniqueId().toString());
			bookMeta.setPages(armorStand.getCustomName());
			book.setItemMeta(bookMeta);

			event.getPlayer().getInventory().setItemInMainHand(book);
			return;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerEditBookEvent(PlayerEditBookEvent event) {
		if (!Utils.hasPermissions(event.getPlayer(), "smptweaks.holograms.edit")) { return; }
		if (!holograms.containsKey(event.getPreviousBookMeta().getAuthor())) { return; }

		BookMeta bookMeta = event.getNewBookMeta();
		ArmorStand armorStand = holograms.remove(event.getPreviousBookMeta().getAuthor());
		armorStand.setCustomName((bookMeta.getPageCount() > 0) ? bookMeta.getPages().get(0) : "");
		armorStand.setCustomNameVisible((bookMeta.getPageCount() > 0));
		event.setCancelled(true);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				event.getPlayer().getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
			}
		}, 0L);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) { return; }
		if (!PersistentUtils.hasPersistentDataBoolean(event.getEntity(), Holograms.isHologram)) { return; } else { event.setCancelled(true); }
		if (((Player) event.getDamager()).getInventory().getItemInMainHand().getType() != Material.WRITABLE_BOOK) { return; }
		if (!Utils.hasPermissions(event.getDamager(), "smptweaks.holograms.destroy")) { return; }
		event.getEntity().remove();
	}
}
