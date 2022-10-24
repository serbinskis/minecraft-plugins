package me.wobbychip.smptweaks.custom.breakablebedrock;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.world.entity.item.EntityItem;

public class Events implements Listener {
	public double ITEM_HEIGHT = 0.25F;
	public double ITEM_SPAWN_OFFSET = 0.25F;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamageEvent(BlockDamageEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) { return; }
		if (event.getBlock().getType() != Material.BEDROCK) { return; }
		if (BreakableBedrock.destroyTime < 0) { return; }
		BedrockBreaker.addPlayer(event.getPlayer(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamageAbortEvent(BlockDamageAbortEvent event) {
		if (event.getBlock().getType() != Material.BEDROCK) { return; }
		BedrockBreaker.removePlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.BEDROCK) { return; }
		if (!event.isDropItems() || !BedrockBreaker.shouldDrop(event.getPlayer())) { return; }

		net.minecraft.world.item.ItemStack itemStack = ReflectionUtils.asNMSCopy(new ItemStack(Material.BEDROCK));
		net.minecraft.world.level.World world = ReflectionUtils.getWorld(event.getBlock().getWorld());

		double x = event.getBlock().getLocation().getX() + 0.5;
		double y = event.getBlock().getLocation().getY() + 0.5;
		double z = event.getBlock().getLocation().getZ() + 0.5;

		double f = (ITEM_HEIGHT / 2.0F);
		double d0 = x + Utils.randomRange(-ITEM_SPAWN_OFFSET, ITEM_SPAWN_OFFSET);
		double d1 = y + Utils.randomRange(-ITEM_SPAWN_OFFSET, ITEM_SPAWN_OFFSET) - f;
		double d2 = z + Utils.randomRange(-ITEM_SPAWN_OFFSET, ITEM_SPAWN_OFFSET);

		EntityItem entityItem = new EntityItem(world, x, y, z, itemStack, d0, d1, d2);
		ArrayList<Item> items = new ArrayList<>(Arrays.asList((Item) entityItem.getBukkitEntity()));

		BlockDropItemEvent dropEvent = new BlockDropItemEvent(event.getBlock(), event.getBlock().getState(), event.getPlayer(), items);
		Bukkit.getServer().getPluginManager().callEvent(dropEvent);
		if (dropEvent.isCancelled()) { return; }

		for (Item drop : dropEvent.getItems()) {
			event.getBlock().getWorld().spawn(drop.getLocation(), Item.class, (item) -> {
				item.setItemStack(drop.getItemStack());
				item.setVelocity(drop.getVelocity());
			});
		}
	}
}
