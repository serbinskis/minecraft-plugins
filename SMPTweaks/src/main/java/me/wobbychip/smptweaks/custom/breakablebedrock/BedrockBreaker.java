package me.wobbychip.smptweaks.custom.breakablebedrock;

import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

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
		this.timer = TaskUtils.scheduleSyncRepeatingTask(() -> update(true), 1L, 1L);

		update(false); //Update visuals and do checks
	}

	public void remove() {
		if (block.getType() == Material.BEDROCK) { ReflectionUtils.destroyBlockProgress(block, -1, MIX_ID); }
		TaskUtils.cancelTask(timer);
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

		ReflectionUtils.destroyBlockProgress(block, k, MIX_ID);
		if (progress >= BREAK_AFTER) { ReflectionUtils.destroyBlock(player, block); remove(); }
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
}
