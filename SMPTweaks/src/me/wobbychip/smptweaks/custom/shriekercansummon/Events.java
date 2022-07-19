package me.wobbychip.smptweaks.custom.shriekercansummon;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
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
import org.bukkit.persistence.PersistentDataType;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.Utils;

public class Events implements Listener {
	public NamespacedKey namespacedKey = new NamespacedKey(Main.plugin, "isPlayerPlaced");
	public int WARDEN_SPAWN_DISATNCE = 10;

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)  {
		if (event.getBlock().getType() == Material.SCULK_SHRIEKER) {
			setIsPlayerPlaced(event.getBlock());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.getItem() == null) || (event.getItem().getType() != Material.SOUL_SAND)) { return; }
		if ((event.getPlayer().getGameMode() != GameMode.CREATIVE) && (event.getItem().getAmount() < 2)) { return; }
		if (event.getClickedBlock().getType() != Material.SCULK_SHRIEKER) { return; }

		if (canSummon(event.getClickedBlock())) { return; }
		if (!isPlayerPlaced(event.getClickedBlock())) { setIsPlayerPlaced(event.getClickedBlock()); }
		setCanSummon(event.getClickedBlock(), true);
		event.setCancelled(true);

		World world = event.getClickedBlock().getLocation().getWorld();
		world.playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_SOUL_SAND_PLACE, 1, 1);
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
		Collection<Block> blocks = Utils.getNearestBlocks(creature.getLocation(), Material.SCULK_SHRIEKER, WARDEN_SPAWN_DISATNCE);

		for (Block block : blocks) {
			if (isPlayerPlaced(block) && canSummon(block)) {
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

	public boolean isPlayerPlaced(Block block) {
		TileState tileState = (TileState) block.getState();
		return tileState.getPersistentDataContainer().has(namespacedKey, PersistentDataType.INTEGER);
	}

	public void setIsPlayerPlaced(Block block) {
		TileState tileState = (TileState) block.getState();
		tileState.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, 1);
		tileState.update();
	}
}
