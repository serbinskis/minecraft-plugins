package me.wobbychip.smptweaks.custom.noadvancements;

import me.wobbychip.smptweaks.library.tinyprotocol.PacketEvent;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketType;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.advancements.DisplayInfo;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class Events implements Listener {
	public static String EXCLUDE_ADVANCEMENT = "recipes/decorations/crafting_table";
	public HashMap<UUID, String> preventSound = new HashMap<>();
	public HashMap<UUID, Object[]> preventExperience = new HashMap<>();
	public HashMap<UUID, Object> preventChat = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		if (NoAdvancements.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (event.getAdvancement().getKey().toString().contains(EXCLUDE_ADVANCEMENT)) { return; }
		Utils.revokeAdvancemnt(event.getPlayer(), event.getAdvancement());
		Utils.sendMessage("PlayerAdvancementDoneEvent: " + event.getAdvancement().getKey());

		//Prevent player experience
		int totalExperience = event.getPlayer().getTotalExperience();
		float exp = event.getPlayer().getExp();
		int level = event.getPlayer().getLevel();
		preventExperience.putIfAbsent(event.getPlayer().getUniqueId(), new Object[] { totalExperience, exp, level });
		preventSound.putIfAbsent(event.getPlayer().getUniqueId(), "");

		//Remove display info from optional, this will prevent messages in chat
		if (preventChat.containsKey(event.getPlayer().getUniqueId())) { return; }
		Optional<DisplayInfo> display = ((CraftAdvancement) event.getAdvancement()).getHandle().value().display();
		Field field = ReflectionUtils.getField(Optional.class, Object.class, null, display, display.orElse(null), true, null, null);
		preventChat.putIfAbsent(event.getPlayer().getUniqueId(), ReflectionUtils.getValue(field, display));

		//Put it back after event
		TaskUtils.scheduleSyncDelayedTask(() -> {
			preventChat.computeIfPresent(event.getPlayer().getUniqueId(), (key, value) -> {
				ReflectionUtils.setValue(field, display, value);
                return null;
            });
		}, 0L);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketEvent(PacketEvent event) {
		if ((event.getPacketType() == PacketType.SET_EXPERIENCE) && preventExperience.containsKey(event.getPlayer().getUniqueId())) {
			Utils.sendMessage("PacketType.SET_EXPERIENCE");
			Object[] remove = preventExperience.remove(event.getPlayer().getUniqueId());
			event.getPlayer().setTotalExperience((Integer) remove[0]);
			event.getPlayer().setExp((Float) remove[1]);
			event.getPlayer().setLevel((Integer) remove[2]);
			event.setCancelled(true);
		}

		if ((event.getPacketType() == PacketType.SOUND) && (preventSound.remove(event.getPlayer().getUniqueId()) != null)) {
			Sound sound = ReflectionUtils.getBukkitSound(event.getPacket());
			if (sound.equals(Sound.ENTITY_PLAYER_LEVELUP)) { event.setCancelled(true); }
		}
	}
}
