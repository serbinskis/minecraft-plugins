package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ServerUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class Loader {
	public Location location;
	public Outline outline;
	public Border border;
	public FakePlayer fakePlayer;
	public Aggravator aggravator;
	public boolean previous = false;
	public int task;
	public int updater;

	public Loader(Block block) {
		this.location = block.getLocation();
		this.outline = new Outline(location);
		this.border = new Border(ChunkLoader.viewDistance, location);
		this.fakePlayer = new FakePlayer(location.clone().add(0.5, 0, 0.5));
		this.aggravator = new Aggravator(block, fakePlayer);
		this.update(true);

		this.updater = TaskUtils.scheduleSyncRepeatingTask(() -> update(false), 1L, 1L);

		this.task = TaskUtils.scheduleSyncRepeatingTask(() -> {
			if (ServerUtils.isPaused()) { return; }
			border.update();
			fakePlayer.update();
			aggravator.update();
		}, 1L, 5L);

		location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);

		//Fix visual bug when adding nether star to powered block
		TaskUtils.scheduleSyncDelayedTask(() -> {
			ItemFrame frame = getItemFrame(location);
			if (frame != null) { frame.setItem(frame.getItem()); }
		}, 1L);

		//Since aggravator setting is stored inside an entity and these are not loaded instantly
		//Wait for them to load and then update chunk loader
		//previous -> isPowered

		if (block.getChunk().isEntitiesLoaded() || !previous) { return; }
		int[] task = { 0 };

		task[0] = TaskUtils.scheduleSyncRepeatingTask(() -> {
			if (!block.getChunk().isEntitiesLoaded()) { return; }
			TaskUtils.cancelSyncRepeatingTask(task[0]);
			update(true);
		}, 1L, 5L);
	}

	public Player getFakePlayer() {
		return fakePlayer.getPlayer();
	}

	public Aggravator getAggravator() {
		return aggravator;
	}

	public Location getLocation() {
		return location;
	}

	public Border getBorder() {
		return border;
	}

	public boolean isLoader() {
		if (location.getBlock().getType() != Material.LODESTONE) { return false; }
		ItemFrame frame = getItemFrame(location);
		if ((frame == null) || (frame.getItem().getType() != Material.NETHER_STAR)) { return false; }
		return true;
	}

	public void update(boolean force) {
		int x = location.getBlockX() >> 4;
		int z = location.getBlockZ() >> 4;
		if (!force && !location.getWorld().isChunkLoaded(x, z)) { return; }

		if ((!ChunkLoader.tweak.getGameRuleBoolean(location.getWorld()))) {
			ChunkLoader.manager.removeLoader(location.getBlock(), false);
			return;
		}

		Block block = location.getBlock();
		boolean isPowered = (block.isBlockIndirectlyPowered() || block.isBlockPowered());
		if ((previous == isPowered) && !force) { return; } else { previous = isPowered; }
		if (!force) { location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1); }
		fakePlayer.setEnabled(isPowered);
		Chunks.markChunks(location, ChunkLoader.viewDistance, isPowered);

		boolean isAggravator = aggravator.isEnabled() && ChunkLoader.enableAggravator;
		TaskUtils.scheduleSyncDelayedTask(() -> outline.setColor(isPowered ? (isAggravator ? ChatColor.GOLD : ChatColor.GREEN) : ChatColor.RED), 2L);
	}

	public void remove(boolean disable, boolean dropFrame) {
		outline.removeShulker();
		border.remove();
		if (!disable) { aggravator.remove(); }
		fakePlayer.remove();

		TaskUtils.cancelSyncRepeatingTask(task);
		TaskUtils.cancelSyncRepeatingTask(updater);

		if (disable) { return; }
		location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
		if (!dropFrame) { return; }

		TaskUtils.scheduleSyncDelayedTask(() -> {
			ItemFrame frame = getItemFrame(location);
			if ((frame == null) || (location.getBlock().getType() != Material.AIR)) { return; }
			location.getWorld().dropItemNaturally(frame.getLocation(), frame.getItem());
			location.getWorld().dropItemNaturally(frame.getLocation(), new ItemStack((frame instanceof GlowItemFrame) ? Material.GLOW_ITEM_FRAME : Material.ITEM_FRAME));
			frame.remove();
  		}, 1L);
	}

	public static ItemFrame getItemFrame(Location location) {
		for (Entity entity : location.getWorld().getNearbyEntities(location.clone().add(0.5, 1.5, 0.5), 0.5, 0.5, 0.5)) {
			if ((entity instanceof ItemFrame) && (((ItemFrame) entity).getAttachedFace() == BlockFace.DOWN)) { return (ItemFrame) entity; }
		}

		return null;
	}

	static class Outline {
		public Location location;

		public Outline(Location location) {
			this.location = location;
		}

		public Entity getShulker() {
			removeShulker();
			Shulker shulker = (Shulker) location.getWorld().spawnEntity(location, EntityType.SHULKER);
			PersistentUtils.setPersistentDataBoolean(shulker, ChunkLoader.isChunkLoader, true);

			shulker.setInvisible(true);
			shulker.setSilent(true);
			shulker.setInvulnerable(true);
			shulker.setAI(false);
			return shulker;
		}

		public void removeShulker() {
			for (Entity entity : location.getWorld().getNearbyEntities(location.clone().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)) {
				if (PersistentUtils.hasPersistentDataBoolean(entity, ChunkLoader.isChunkLoader)) {
					entity.remove();
				}
			}
		}

		public void setColor(ChatColor color) {
			Utils.setGlowColor(getShulker(), ChunkLoader.highlighting ? color : ChatColor.RESET);
		}
	}
}
