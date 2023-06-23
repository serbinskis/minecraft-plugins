package me.wobbychip.smptweaks.custom.noadvancements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Events implements Listener {
	public static List<UUID> chats = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		if (NoAdvancements.tweak.getGameRuleBoolean(event.getPlayer().getWorld()))  { return; }
		Utils.revokeAdvancemnt(event.getPlayer(), event.getAdvancement());

		//Prevent BlazeandCave's Advancements messages in the chat and experience
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (chats.contains(player.getUniqueId())) { continue; }
			chats.add(player.getUniqueId());

			UUID uuid = player.getUniqueId();
			String visibility = ReflectionUtils.getChatVisibility(player);
			ReflectionUtils.setChatVisibility(player, "options.chat.visibility.hidden");
			int totalExperience = player.getTotalExperience();
			float exp = player.getExp();
			int level = player.getLevel();

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				public void run() {
					chats.remove(uuid);
					Player player = Bukkit.getPlayer(uuid);
					if (player == null) { return; }
					ReflectionUtils.setChatVisibility(player, visibility);
					if (totalExperience == player.getTotalExperience()) { return; }
					player.setTotalExperience(totalExperience);
					player.setExp(exp);
					player.setLevel(level);
				}
			}, 0L);
		}
	}
}
