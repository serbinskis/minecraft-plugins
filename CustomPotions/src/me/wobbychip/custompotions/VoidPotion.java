package me.wobbychip.custompotions;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.potions.CustomPotion;
import me.wobbychip.custompotions.potions.PotionManager;

public class VoidPotion extends CustomPotion {
	public VoidPotion() {
		super(PotionManager.getPotion(PotionType.AWKWARD, false, false), Material.NETHER_STAR, "void", Color.fromRGB(0, 0, 0));
		this.setDisplayName("§r§fPotion of Void");
		this.setLore(Arrays.asList("§9Destroys bedrock"));
		this.setTippedArrow(true, "§r§fArrow of Void");
	}

	@Override
	public void onPotionConsume(PlayerItemConsumeEvent event) {
		event.getPlayer().setHealth(0);
	}

	@Override
	public void onPotionSplash(PotionSplashEvent event) {
		Block block = event.getEntity().getLocation().getBlock();
		destroyBlock(block);
		destroyBlock(block.getRelative(BlockFace.UP));
		destroyBlock(block.getRelative(BlockFace.DOWN));
		destroyBlock(block.getRelative(BlockFace.EAST));
		destroyBlock(block.getRelative(BlockFace.WEST));
		destroyBlock(block.getRelative(BlockFace.NORTH));
		destroyBlock(block.getRelative(BlockFace.SOUTH));
	}

	@Override
	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
		Block block = event.getEntity().getLocation().getBlock();
		destroyBlock(block);
		destroyBlock(block.getRelative(BlockFace.UP));
		destroyBlock(block.getRelative(BlockFace.DOWN));
		destroyBlock(block.getRelative(BlockFace.EAST));
		destroyBlock(block.getRelative(BlockFace.WEST));
		destroyBlock(block.getRelative(BlockFace.NORTH));
		destroyBlock(block.getRelative(BlockFace.SOUTH));
		event.setCancelled(true);
	}

	@Override
	public void onProjectileHit(ProjectileHitEvent event) {
		if ((event.getHitBlock() != null) && destroyBlock(event.getHitBlock())) {
			event.getEntity().remove();
		}
	}

	public boolean destroyBlock(Block block) {
		if (block.getType() == Material.BEDROCK) {
			Location loc = block.getLocation().clone().add(.5, .5, .5);
			block.getWorld().playSound(loc, Sound.BLOCK_STONE_BREAK, 1.5f, 1.5f);
			block.getWorld().spawnParticle(Particle.BLOCK_DUST, loc, 50, block.getBlockData().clone());
			block.setType(Material.AIR);
			return true;
		}

		return false;
	}
}
