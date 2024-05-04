package me.wobbychip.smptweaks.custom.custombreaking.breaking;

import me.wobbychip.smptweaks.custom.custombreaking.CustomBreaking;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class CustomBreaker {
	public static HashMap<UUID, CustomBreaker> breakers = new HashMap<>();
	public static HashMap<Material, CustomBlock> customs = new HashMap<>();
	public static int MIX_ID = 1724112;
	public static float BREAK_AFTER = 1f;
	public Player player;
	public Block block;
	public final CustomBlock customBreaking;
	public int timer;
	public long ticks = 0;
	public float progress = 0;

	public CustomBreaker(Player player, Block block, CustomBlock customBreaking) {
		this.player = player;
		this.block = block;
		this.customBreaking = customBreaking;
		this.timer = TaskUtils.scheduleSyncRepeatingTask(() -> update(true), 1L, 1L);
		update(false); //Update visuals and do checks
	}

	public void remove() {
		if (block.getType() == customBreaking.getType()) { ReflectionUtils.destroyBlockProgress(block, -1, MIX_ID); }
		TaskUtils.cancelTask(timer);
		breakers.remove(player.getUniqueId());
	}

	public void update(boolean bProgress) {
		ticks++;
		if (block.getType() != customBreaking.getType()) { remove(); return; }
		if (!player.isOnline()) { remove(); return; }
		incrementDestroyProgress(bProgress);
	}

	public void incrementDestroyProgress(boolean bProgress) {
		if (bProgress) { progress += getDestroyProgress(player); }
		if (bProgress && CustomBreaking.enableTimer && (ticks % 5 == 0)) { sendProgressTime(); }
		int k = ((int) (progress * 10f));

		ReflectionUtils.destroyBlockProgress(block, k, MIX_ID);
		if (progress >= BREAK_AFTER) { ReflectionUtils.destroyBlock(player, block); remove(); }
	}

	public float getDestroyProgress(Player player) {
		float f = customBreaking.getDestroyTime();

		if (f == -1f) { return 0f; }
		float i = customBreaking.hasCorrectToolForDrops(block, player) ? 30f : 100f;
		float k = ReflectionUtils.getPlayerDestroyTime(player, Material.STONE);
		//Any block that is affected by efficiency and is breakable with pickaxe will work

		return k / f / i;
	}

	public void sendProgressTime() {
		double pTick = getDestroyProgress(player);
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

	public static void addPlayer(Player player, Block block) {
		CustomBlock customBlock = getCustom(block);
		if ((customBlock == null) || (customBlock.getDestroyTime() < 0)) { return; }
		removePlayer(player);
		breakers.put(player.getUniqueId(), new CustomBreaker(player, block, customBlock));
	}

	public static void removePlayer(Player player) {
		CustomBreaker customBreaker = breakers.remove(player.getUniqueId());
		if (customBreaker != null) { customBreaker.remove(); }
	}

	public static void addCustom(CustomBlock customBreaking) {
		customs.put(customBreaking.getType(), customBreaking);
		customBreaking.isEnabled(); //Only used to create config settings
	}

	public static CustomBlock getCustom(Block block) {
		CustomBlock customBlock = customs.get(block.getType());
		if ((customBlock == null) || !customBlock.isCustomBlock(block)) { return null; }
		return customBlock;
	}
}
