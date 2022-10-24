package me.wobbychip.smptweaks.custom.breakablebedrock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutWorldEvent;

public class BedrockBreaker {
	public static HashMap<Material, Float> cache = new HashMap<Material, Float>();
	public static HashMap<UUID, BedrockBreaker> breakers = new HashMap<UUID, BedrockBreaker>();
	public List<Material> correctTools = Arrays.asList(Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE);
	public Player player;
	public Block block;
	public PotionEffect effect;
	public int timer = -1;
	public float progress = 0;

	public BedrockBreaker(Player player, Block block) {
		this.player = player;
		this.block = block;

		//addSlowDigging();
		destroyBlockProgress(block, 0);

		timer = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				update();
			}
		}, 1L, 1L);
	}

	public static void addPlayer(Player player, Block block) {
		removePlayer(player);
		breakers.put(player.getUniqueId(), new BedrockBreaker(player, block));
	}

	public static void removePlayer(Player player) {
		if (!breakers.containsKey(player.getUniqueId())) { return; }
		breakers.remove(player.getUniqueId()).remove();
	}

	public void remove() {
		if (block.getType() == Material.BEDROCK) { destroyBlockProgress(block, -1); }
		Bukkit.getServer().getScheduler().cancelTask(timer);
		breakers.remove(player.getUniqueId());
		removeSlowDigging();
	}

	public void update() {
		if (block.getType() != Material.BEDROCK) { remove(); return; }
		if (!player.isOnline()) { remove(); return; }
		incrementDestroyProgress();
	}

	public void addSlowDigging() {
		if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
			effect = player.getPotionEffect(PotionEffectType.SLOW_DIGGING);
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}

		boolean ambient = (effect == null) ? false : effect.isAmbient();
		boolean particles = (effect == null) ? false : effect.hasParticles();
		boolean icon = (effect == null) ? false : effect.hasIcon();
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, -1, ambient, particles, icon));
	}

	public void removeSlowDigging() {
		player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		if (effect == null) { return; }

		//+ calculate time passed for returned effect

		int duration = effect.getDuration();
		int amplifier = effect.getAmplifier();
		boolean ambient = effect.isAmbient();
		boolean particles = effect.hasParticles();
		boolean icon = effect.hasIcon();
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, amplifier, ambient, particles, icon));
	}

	public void incrementDestroyProgress() {
		progress += getDestroyProgress(player, block, correctTools, effect);
		int k = (int) (progress * 10.0F);

		destroyBlockProgress(block, k);
		if (progress >= 0.7F) { destroyBlock(player, block); remove(); }
	}

	public static boolean hasCorrectToolForDrops(Player player, List<Material> correctTools) {
		return correctTools.contains(player.getInventory().getItemInMainHand().getType());
	}

	//k *= 1.0F + (float) (MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2F;
	public static float getDestroyProgress(Player player, Block block, List<Material> correctTools, PotionEffect effect) {
		float f = getDestroySpeed(block); //Replace later with BreakableBedrock.destroyTime

		if (f == -1.0F) { return 0.0F; }
		int i = hasCorrectToolForDrops(player, correctTools) ? 30 : 100;
		float k = ReflectionUtils.getPlayerDestroyTime(player, block.getType());

		k = 0.2f; //Remove this later
		return k / f / (float) i;
	}

	public static void destroyBlockProgress(Block block, int progress) {
		//BreakableBedrock.preventPacket = false;

		for (Player player : block.getWorld().getPlayers()) {
			double d0 = (double) block.getX() - player.getLocation().getX();
			double d1 = (double) block.getY() - player.getLocation().getY();
			double d2 = (double) block.getZ() - player.getLocation().getZ();

			if (d0 * d0 + d1 * d1 + d2 * d2 >= 1024.0D) { continue; }
			BlockPosition location = new BlockPosition(block.getX(), block.getY(), block.getZ());
			ReflectionUtils.sendPacket(player, new PacketPlayOutBlockBreakAnimation(player.getEntityId(), location, progress));
		}

		//BreakableBedrock.preventPacket = true;
	}

	public static void destroyBlock(Player player, Block block) {
		destroyBlockProgress(block, -1);
		BlockPosition location = new BlockPosition(block.getX(), block.getY(), block.getZ());
		int id = ReflectionUtils.getBlockId(block.getType());
		ReflectionUtils.sendPacket(player, new PacketPlayOutWorldEvent(2001, location, id, false));
		player.breakBlock(block);
	}

	public static float getDestroySpeed(Block block) {
		if (cache.containsKey(block.getType())) { return cache.get(block.getType()); }
		float destroyTime = ReflectionUtils.getBlockDestroyTime(block.getType());
		cache.put(block.getType(), destroyTime);
		return destroyTime;
	}
}
