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
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.server.level.EntityPlayer;

public class Loader {
	public EntityPlayer player = null;
	public Location location;
	public Outline outline;
	public Border border;
	public boolean previous = false;

	public Loader(Block block) {
		location = block.getLocation();
		outline = new Outline(location);
		border = new Border(Bukkit.getServer().getViewDistance(), location);
		location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
		update(true);

		//Prevent some interaction with fake player
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
	        public void run() {
	        	if (player != null) { player.getBukkitEntity().teleport(location.clone().add(0.5, 0, 0.5)); }
	        	if (player != null) { player.getBukkitEntity().setCollidable(false); }
	        }
	    }, 5L, 5L);

		//Fix visual bug when adding nether star to powered block
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        public void run() {
	    		ItemFrame frame = getItemFrame();
	    		if (frame != null) { frame.setItem(frame.getItem()); }
	        }
	    }, 1L);
	}

	public Location getLocation() {
		return location;
	}

	public Outline getOutline() {
		return outline;
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

		if (disable) { return; }
		if (player != null) { ReflectionUtils.removeFakePlayer(player); }
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

		if (isPowered) {
			if (player == null) { player = ReflectionUtils.addFakePlayer(location.clone().add(0.5, 0, 0.5)); }
		} else {
			if (player != null) { ReflectionUtils.removeFakePlayer(player); }
			player = null;
		}

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
