package me.wobbychip.smptweaks.custom.shriekercansummon;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)  {
		if (event.getBlock().getType() == Material.SCULK_SHRIEKER) {
			PersistentUtils.setPersistentDataBoolean(event.getBlock(), ShriekerCanSummon.isPlayerPlaced, true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.getItem() == null) || (event.getItem().getType() != Material.SOUL_SAND)) { return; }
		if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && (event.getItem().getAmount() < 2)) { return; }
		if (event.getClickedBlock().getType() != Material.SCULK_SHRIEKER) { return; }

		Block block = event.getClickedBlock();
		if (canSummon(block)) { return; }
		
		if (!PersistentUtils.hasPersistentDataBoolean(block, ShriekerCanSummon.isPlayerPlaced)) {
			PersistentUtils.setPersistentDataBoolean(block, ShriekerCanSummon.isPlayerPlaced, true);
		}

		setCanSummon(block, true);
		event.setCancelled(true);

		World world = block.getLocation().getWorld();
		world.playSound(block.getLocation(), Sound.BLOCK_SOUL_SAND_PLACE, 1, 1);
		if (event.getHand() == EquipmentSlot.HAND) { event.getPlayer().swingMainHand(); }
		if (event.getHand() == EquipmentSlot.OFF_HAND) { event.getPlayer().swingOffHand(); }

		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.getItem().setAmount(event.getItem().getAmount()-2);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
		LivingEntity creature = event.getEntity();
		if ((event.getSpawnReason() != SpawnReason.DEFAULT) || (creature.getType() != EntityType.WARDEN)) { return; }
		Collection<Block> blocks = Utils.getNearestBlocks(creature.getLocation(), Material.SCULK_SHRIEKER, ShriekerCanSummon.WARDEN_SPAWN_DISATNCE);

		for (Block block : blocks) {
			if (PersistentUtils.hasPersistentDataBoolean(block, ShriekerCanSummon.isPlayerPlaced) && canSummon(block)) {
				setCanSummon(block, false);
				break;
			}
		}
	}

	public void setCanSummon(Block block, boolean canSummon) {
		SculkShrieker shrieker = (SculkShrieker) block.getBlockData();
		shrieker.setCanSummon(canSummon);
		block.setBlockData(shrieker);
	}

	public boolean canSummon(Block block) {
		SculkShrieker shrieker = (SculkShrieker) block.getBlockData();
		return shrieker.isCanSummon();
	}
}
