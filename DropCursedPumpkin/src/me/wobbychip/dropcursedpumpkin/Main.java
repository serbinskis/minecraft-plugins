package me.wobbychip.dropcursedpumpkin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[DropCursedPumpkin] DropCursedPumpkin has loaded!"));
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!event.getKeepInventory()) { return; }

		ItemStack helmetSlot = event.getEntity().getInventory().getHelmet();
		if (helmetSlot == null || helmetSlot.getType() != Material.CARVED_PUMPKIN) { return; }
		if (!helmetSlot.hasItemMeta() || !helmetSlot.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) { return; }

		event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), helmetSlot);
		event.getEntity().getInventory().setHelmet(new ItemStack(Material.AIR));
	}
}