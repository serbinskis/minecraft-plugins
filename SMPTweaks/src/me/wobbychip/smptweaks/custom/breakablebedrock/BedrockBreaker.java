package me.wobbychip.smptweaks.custom.breakablebedrock;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutWorldEvent;

public class BedrockBreaker {
	public static HashMap<UUID, BedrockBreaker> breakers = new HashMap<UUID, BedrockBreaker>();
	public static int MIX_ID = 1000000;
	public static float BREAK_AFTER = 1.0F;

	public Player player;
	public Block block;
	public long ticks = 0;
	public int timer = -1;
	public float progress = 0;

	public BedrockBreaker(Player player, Block block) {
		this.player = player;
		this.block = block;

		this.timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				update(true);
			}
		}, 1L, 1L);

		update(false); //Update visuals and do checks
	}

	public void remove() {
		if (block.getType() == Material.BEDROCK) { destroyBlockProgress(block, -1); }
		Bukkit.getServer().getScheduler().cancelTask(timer);
		breakers.remove(player.getUniqueId());
	}

	public void update(boolean bProgress) {
		ticks++;
		if (block.getType() != Material.BEDROCK) { remove(); return; }
		if (!player.isOnline()) { remove(); return; }
		if (BreakableBedrock.destroyTime < 0) { remove(); return; }
		incrementDestroyProgress(bProgress);
	}

	public void incrementDestroyProgress(boolean bProgress) {
		if (bProgress) { progress += BedrockBreaker.getDestroyProgress(player); }
		if (bProgress && BreakableBedrock.enableTimer && (ticks%5 == 0)) { sendProgressTime(); }
		int k = ((int) (progress * 10.0F));

		destroyBlockProgress(block, k);
		if (progress >= BREAK_AFTER) { destroyBlock(player, block); remove(); }
	}

	public static void addPlayer(Player player, Block block) {
		removePlayer(player);
		breakers.put(player.getUniqueId(), new BedrockBreaker(player, block));
	}

	public static void removePlayer(Player player) {
		if (!breakers.containsKey(player.getUniqueId())) { return; }
		breakers.remove(player.getUniqueId()).remove();
	}

	public static boolean shouldDrop(Player player) {
		return BreakableBedrock.shouldDrop && BedrockBreaker.hasCorrectToolForDrops(player);
	}

	public static boolean hasCorrectToolForDrops(Player player) {
		return BreakableBedrock.correctTools.contains(player.getInventory().getItemInMainHand().getType());
	}

	public static float getDestroyProgress(Player player) {
		float f = (float) BreakableBedrock.destroyTime;

		if (f == -1.0F) { return 0.0F; }
		float i = BedrockBreaker.hasCorrectToolForDrops(player) ? 30f : 100f;
		float k = ReflectionUtils.getPlayerDestroyTime(player, Material.STONE);
		//Any block that is affected by efficiency and is breakable with pickaxe will work

		return k / f / i;
	}

	public void sendProgressTime() {
		double pTick = BedrockBreaker.getDestroyProgress(player);
		double pLeft = BREAK_AFTER - progress;
		double pProgress = ((progress/BREAK_AFTER)*100);
		long msLeft = (long) (50*(pLeft/pTick));

		if (pLeft < 0) { msLeft = 0; }
		if (pLeft < 0) { pProgress = 100; }

		long millis = msLeft%1000;
		long seconds = (msLeft/1000)%60;
		long minutes = (msLeft/(1000*60))%60;
		long hours = (msLeft/(1000*60*60))%24;
		long days = (msLeft/(1000*60*60*24));

		String message = String.format("Time: %d:%02d:%02d:%02d.%03d | %.2f%%", days, hours, minutes, seconds, millis, pProgress);
		Utils.sendActionMessage(player, message);
	}

	//MIX_ID is required to prevent weird visual bug when client side and server side breaking are overlapping
	//Making two animations replace each other at the same time
	public static void destroyBlockProgress(Block block, int progress) {
		for (Player player : block.getWorld().getPlayers()) {
			double d0 = (double) block.getX() - player.getLocation().getX();
			double d1 = (double) block.getY() - player.getLocation().getY();
			double d2 = (double) block.getZ() - player.getLocation().getZ();

			if (d0 * d0 + d1 * d1 + d2 * d2 >= 1024.0D) { continue; }
			BlockPosition location = new BlockPosition(block.getX(), block.getY(), block.getZ());
			ReflectionUtils.sendPacket(player, new PacketPlayOutBlockBreakAnimation(player.getEntityId()+MIX_ID, location, progress));
		}
	}

	public static void destroyBlock(Player player, Block block) {
		destroyBlockProgress(block, -1);
		BlockPosition location = new BlockPosition(block.getX(), block.getY(), block.getZ());
		int id = ReflectionUtils.getBlockId(block.getType());
		ReflectionUtils.sendPacket(player, new PacketPlayOutWorldEvent(2001, location, id, false));
		player.breakBlock(block);
	}
}
