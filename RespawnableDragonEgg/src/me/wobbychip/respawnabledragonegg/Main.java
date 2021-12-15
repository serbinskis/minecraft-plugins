package me.wobbychip.respawnabledragonegg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[RespawnableDragonEgg] RespawnableDragonEgg has loaded!"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!event.getEntityType().equals(EntityType.ENDER_DRAGON)) { return; }
		if (!event.getEntity().getWorld().getEnvironment().equals(Environment.THE_END)) { return; }
		if (!event.getEntity().getWorld().getEnderDragonBattle().hasBeenPreviouslyKilled()) { return; }

		World world = event.getEntity().getWorld();
		Location location = new Location(world, 0, 0, 0);
		location.setY(location.getWorld().getHighestBlockYAt(0, 0) + 1);
		Block block = world.getBlockAt(location);
		block.setType(Material.DRAGON_EGG);
	}
}