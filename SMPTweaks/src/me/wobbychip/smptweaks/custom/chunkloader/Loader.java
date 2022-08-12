package me.wobbychip.smptweaks.custom.chunkloader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Loader {
	public FakePlayer fakePlayer;
	public Location location;
	public Outline outline;
	public Border border;
	public boolean previous = false;

	public Loader(Block block) {
		this.location = block.getLocation();
		this.outline = new Outline(location);
		this.border = new Border(ChunkLoader.viewDistance, location);
		this.location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
		this.fakePlayer = new FakePlayer(location.clone().add(0.5, 0, 0.5));
		this.update(true);

		//Fix visual bug when adding nether star to powered block
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				ItemFrame frame = getItemFrame();
				if (frame != null) { frame.setItem(frame.getItem()); }
			}
		}, 1L);
	}

	public Player getFakePlayer() {
		return fakePlayer.getPlayer();
	}

	public Location getLocation() {
		return location;
	}

	public Border getBorder() {
		return border;
	}

	public boolean isLoader() {
		if (location.getBlock().getType() != Material.LODESTONE) { return false; }
		ItemFrame frame = getItemFrame();
		if ((frame == null) || (frame.getItem().getType() != Material.NETHER_STAR)) { return false; }
		return true;
	}

	public void remove(boolean disable) {
		outline.removeShulker();
		border.remove();
		fakePlayer.remove();

		if (disable) { return; }
		location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				ItemFrame frame = getItemFrame();
				if ((frame != null) && (location.getBlock().getType() == Material.AIR)) {
					location.getWorld().dropItemNaturally(frame.getLocation(), frame.getItem());
					location.getWorld().dropItemNaturally(frame.getLocation(), new ItemStack((frame instanceof GlowItemFrame) ? Material.GLOW_ITEM_FRAME : Material.ITEM_FRAME));
					frame.remove();
				}
			}
		}, 1L);
	}

	public void update(boolean force) {
		Block block = location.getBlock();
		boolean isPowered = (block.isBlockIndirectlyPowered() || block.isBlockPowered());
		if ((previous == isPowered) && !force) { return; } else { previous = isPowered; }
		if (!force) { location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1); }
		fakePlayer.setEnabled(isPowered);
		Chunks.markChunks(location, ChunkLoader.viewDistance, isPowered);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				outline.setColor(isPowered ? ChatColor.GREEN : ChatColor.RED);
			}
		}, 1L);
	}

	public ItemFrame getItemFrame() {
		for (Entity entity : location.getWorld().getNearbyEntities(location.clone().add(0.5, 1.5, 0.5), 0.5, 0.5, 0.5)) {
			if ((entity instanceof ItemFrame) && (((ItemFrame) entity).getAttachedFace() == BlockFace.DOWN)) { return (ItemFrame) entity; }
		}

		return null;
	}

	class Outline {
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
			Utils.setGlowColor(getShulker(), color);
		}
	}
}
