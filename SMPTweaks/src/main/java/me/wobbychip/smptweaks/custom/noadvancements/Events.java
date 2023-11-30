package me.wobbychip.smptweaks.custom.noadvancements;

import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

			TaskUtils.scheduleSyncDelayedTask(() -> {
				chats.remove(uuid);
				Player player1 = Bukkit.getPlayer(uuid);
				if (player1 == null) { return; }
				ReflectionUtils.setChatVisibility(player1, visibility);
				if (totalExperience == player1.getTotalExperience()) { return; }
				player1.setTotalExperience(totalExperience);
				player1.setExp(exp);
				player1.setLevel(level);
			}, 0L);
		}
	}
}
