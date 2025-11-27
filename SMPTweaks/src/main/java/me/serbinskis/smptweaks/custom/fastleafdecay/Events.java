package me.serbinskis.smptweaks.custom.fastleafdecay;

import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.GameRule;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
		if (!Tag.LEAVES.isTagged(event.getBlock().getType())) { return; }
		if (!event.getBlock().getBlockData().isRandomlyTicked()) { return; }
		Integer randomTickSpeed = event.getBlock().getWorld().getGameRuleValue(GameRule.RANDOM_TICK_SPEED);
		if (randomTickSpeed == null || randomTickSpeed == 0) { return; }

		TaskUtils.scheduleSyncDelayedTask(() -> {
			Block block = event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation());
			if (Tag.LEAVES.isTagged(block.getType())) { block.randomTick(); }
		}, 1 + (int)(Math.random() * 100));
	}
}
