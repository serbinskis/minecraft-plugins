package me.wobbychip.smptweaks.custom.custompotions.events;

import me.wobbychip.smptweaks.custom.custompotions.CustomPotions;
import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class ProjectileEvents implements Listener {
	HashMap<Location, ItemStack> fuckBukkit = new HashMap<>();
	//Since bukkit again is shit and not including ItemStack in ProjectileLaunchEvent I have to do a workaround

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if (!Utils.isTippedArrow(event.getItem()) || (event.getBlock().getType() != Material.DISPENSER)) { return; }
		CustomPotion customPotion = CustomPotions.manager.getCustomPotion(event.getItem());
		if (customPotion != null) { fuckBukkit.put(event.getBlock().getLocation(), event.getItem()); }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof Arrow) {
			//Just really fuck you bukkit, even entity.getFacing() not working correctly
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
				CustomPotion customPotion = CustomPotions.manager.getCustomPotion(item);
				if (customPotion != null) { PersistentUtils.setPersistentDataString(event.getEntity(), CustomPotions.TAG_CUSTOM_POTION, customPotion.getName()); }
				if (event.isCancelled() || !customPotion.isEnabled()) { return; }
				customPotion.onProjectileLaunch(event);
			}
		}

		if (event.getEntity() instanceof ThrownPotion potion) {
			//Some plugins don't work with custom potion tag, e.g. WorldGuard
			//Since potion name is also saved in LocName, we can set potion tag back to empty
			//Technically these tags are only needed in brewing stand

			if ((potion.getItem() == null) || (potion.getItem().getType() != Material.SPLASH_POTION)) { return; }
			CustomPotion customPotion = CustomPotions.manager.getCustomPotion(potion.getItem());
			if (customPotion == null) { return; }

			potion.setItem(ReflectionUtils.setPotionTag(potion.getItem(), "minecraft:empty"));
			customPotion.onProjectileLaunch(event);
		}
	}
}
