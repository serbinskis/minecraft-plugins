package me.wobbychip.recallpotion.events;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.wobbychip.recallpotion.Main;
import me.wobbychip.recallpotion.potions.CustomPotion;
import me.wobbychip.recallpotion.utils.Utils;

public class DispenserEvents implements Listener {
	HashMap<Location, ItemStack> fuckBukkit = new HashMap<Location, ItemStack>();
	//Since bukkit again is shit and not including ItemStack in ProjectileLaunchEvent I have to do workaround

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if (!Utils.isTippedArrow(event.getItem()) || (event.getBlock().getType() != Material.DISPENSER)) { return; }
		CustomPotion customPotion = Main.manager.getCustomPotion(event.getItem());
		if (customPotion != null) { fuckBukkit.put(event.getBlock().getLocation(), event.getItem()); }
    }

	@EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Arrow)) { return; }

		//Just really fuck bukkit, even entity.getFacing() not working correctly
		Block block = event.getEntity().getLocation().getBlock();
		Vector vector = event.getEntity().getVelocity();
		double x = Math.abs(vector.getX());
		double y = Math.abs(vector.getY());
		double z = Math.abs(vector.getZ());

		if (x >= y && x >= z) {
			block = block.getRelative((int) -Math.round(vector.getX()), 0, 0);
		} else if (y >= x && y >= z) {
			block = block.getRelative(0, (int) -Math.round(vector.getY()), 0);
		} else {
			block = block.getRelative(0, 0, (int) -Math.round(vector.getZ()));
		}

		if (fuckBukkit.containsKey(block.getLocation())) {
			ItemStack item = fuckBukkit.remove(block.getLocation());
			CustomPotion customPotion = Main.manager.getCustomPotion(item);
			if (customPotion != null) { event.getEntity().setCustomName(customPotion.getName()); }
			if (event.isCancelled()) { return; }
			customPotion.onProjectileLaunch(event);
		}
    }
}
