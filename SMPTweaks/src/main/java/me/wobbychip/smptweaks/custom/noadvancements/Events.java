package me.wobbychip.smptweaks.custom.noadvancements;

import me.wobbychip.smptweaks.library.tinyprotocol.PacketEvent;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketType;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.HashMap;
import java.util.UUID;

public class Events implements Listener {
	public static String EXCLUDE_ADVANCEMENT = "recipes/decorations/crafting_table";
	public HashMap<UUID, Object[]> preventExperience = new HashMap<>();
	public HashMap<UUID, String> preventChat = new HashMap<>();
	public HashMap<UUID, String> preventSound = new HashMap<>();
	public boolean preventConsole = false;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		if (NoAdvancements.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (event.getAdvancement().getKey().toString().contains(EXCLUDE_ADVANCEMENT)) { return; }
		Utils.revokeAdvancemnt(event.getPlayer(), event.getAdvancement());

		//Prevent player experience and sound
		UUID uniqueId = event.getPlayer().getUniqueId();
		int totalExperience = event.getPlayer().getTotalExperience();
		float exp = event.getPlayer().getExp();
		int level = event.getPlayer().getLevel();
		preventExperience.putIfAbsent(uniqueId, new Object[] { totalExperience, exp, level });
		preventSound.putIfAbsent(uniqueId, "");
		TaskUtils.scheduleSyncDelayedTask(() -> preventSound.remove(uniqueId), 0L);

		//Prevent chat messages in console
		Boolean gameRuleValue = event.getPlayer().getWorld().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
		if (!preventConsole) { event.getPlayer().getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false); }
		if (!preventConsole) { TaskUtils.scheduleSyncDelayedTask(() -> event.getPlayer().getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, gameRuleValue), 0L); }
		if (!preventConsole) { TaskUtils.scheduleSyncDelayedTask(() -> preventConsole = false, 0L); }
		preventConsole = true;

		//Prevent chat messages from advancements, including from custom data packs
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (preventChat.containsKey(player.getUniqueId())) { continue; }
			UUID playerId = player.getUniqueId();
			preventChat.put(playerId, ReflectionUtils.getChatVisibility(player));
			ReflectionUtils.setChatVisibility(player, "options.chat.visibility.hidden");
			TaskUtils.scheduleSyncDelayedTask(() -> ReflectionUtils.setChatVisibility(Bukkit.getPlayer(playerId), preventChat.remove(playerId)), 0L);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketEvent(PacketEvent event) {
		if ((event.getPacketType() == PacketType.SET_EXPERIENCE) && preventExperience.containsKey(event.getPlayer().getUniqueId())) {
			Object[] remove = preventExperience.remove(event.getPlayer().getUniqueId());
			event.getPlayer().setTotalExperience((Integer) remove[0]);
			event.getPlayer().setExp((Float) remove[1]);
			event.getPlayer().setLevel((Integer) remove[2]);
			event.setCancelled(true);
		}

		if ((event.getPacketType() == PacketType.SOUND) && (preventSound.containsKey(event.getPlayer().getUniqueId()))) {
			Sound sound = ReflectionUtils.getBukkitSound(event.getPacket());
			if (sound.equals(Sound.ENTITY_PLAYER_LEVELUP)) { event.setCancelled(true); }
		}
	}
}
