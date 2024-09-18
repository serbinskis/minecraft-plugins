package me.serbinskis.smptweaks.custom.dropcursedpumpkin;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		if (!event.getKeepInventory()) { return; }

		ItemStack helmetSlot = event.getEntity().getInventory().getHelmet();
		if (helmetSlot == null || helmetSlot.getType() != Material.CARVED_PUMPKIN) { return; }
		if (!helmetSlot.hasItemMeta() || !helmetSlot.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) { return; }

		event.getDrops().remove(helmetSlot);
		event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), helmetSlot);
		event.getEntity().getInventory().setHelmet(new ItemStack(Material.AIR));
	}
}
