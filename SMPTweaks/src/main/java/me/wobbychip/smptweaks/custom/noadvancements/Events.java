package me.wobbychip.smptweaks.custom.noadvancements;

import me.wobbychip.smptweaks.library.fakeplayer.FakePlayer;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketEvent;
import me.wobbychip.smptweaks.library.tinyprotocol.PacketType;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.*;
import java.util.stream.Stream;

public class Events implements Listener {
	public static List<String> EXCLUDE_ADVANCEMENT = List.of("recipes/decorations/crafting_table", "blazeandcave:technical/inventory_changed");
	public static HashMap<UUID, String> preventSoundPackets = new HashMap<>();
	public static HashMap<UUID, String> preventExperiencePackets = new HashMap<>();
	public static List<Integer> scheduled = new LinkedList<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		scheduled.removeIf(item -> { TaskUtils.finishTask(item); return true; });
		boolean isFakePlayer = FakePlayer.isFakePlayer(event.getPlayer());
		if (!isFakePlayer && NoAdvancements.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (EXCLUDE_ADVANCEMENT.stream().anyMatch(e -> event.getAdvancement().getKey().toString().contains(e))) { return; }

		Utils.revokeAdvancement(event.getPlayer(), event.getAdvancement());
		hijackAdvancement(event.getPlayer(), event.getAdvancement());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketEvent(PacketEvent event) {
		if ((event.getPacketType() == PacketType.SOUND) && (preventSoundPackets.remove(event.getPlayer().getUniqueId()) != null)) {
			Sound sound = ReflectionUtils.getBukkitSound(event.getPacket());
			if (sound.equals(Sound.ENTITY_PLAYER_LEVELUP)) { event.setCancelled(true); }
		}

		if ((event.getPacketType() == PacketType.SET_EXPERIENCE) && (preventExperiencePackets.remove(event.getPlayer().getUniqueId()) != null)) {
			event.setCancelled(true);
		}
	}

	private static void hijackAdvancement(Player player, Advancement advancement) {
		//Utils.sendMessage("hijackAdvancement ->");

		//Disable announce advancements to prevent chat messages
		Boolean gameRuleValue = player.getWorld().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
		player.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> player.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, gameRuleValue), 0L));

		//Replace loot resource keys with random gibberish
		ReflectionUtils.getAdvancementLoot(advancement).forEach(resourceLoot -> {
			//Utils.sendMessage("[" + advancement.getKey() + "] loot: " + List.of(Objects.requireNonNull(ReflectionUtils.getResourceLocation(resourceLoot))));
			String[] resourceLocation = ReflectionUtils.getResourceLocation(resourceLoot);
			ReflectionUtils.setResourceLocation(resourceLoot, Utils.randomString(9, false), Utils.randomString(9, false));
			scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> ReflectionUtils.setResourceLocation(resourceLoot, resourceLocation[0], resourceLocation[1]), 0L));
			//Utils.sendMessage("[" + advancement.getKey() + "] loot: " + List.of(Objects.requireNonNull(ReflectionUtils.getResourceLocation(resourceLoot))));
		});

		//Replace recipes resource keys with random gibberish
		ReflectionUtils.getAdvancementRecipes(advancement).forEach(resourceRecipes -> {
			//Utils.sendMessage("[" + advancement.getKey() + "] recipes: " + List.of(Objects.requireNonNull(ReflectionUtils.getResourceLocation(resourceRecipes))));
			String[] resourceLocation = ReflectionUtils.getResourceLocation(resourceRecipes);
			ReflectionUtils.setResourceLocation(resourceRecipes, Utils.randomString(9, false), Utils.randomString(9, false));
			scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> ReflectionUtils.setResourceLocation(resourceRecipes, resourceLocation[0], resourceLocation[1]), 0L));
			//Utils.sendMessage("[" + advancement.getKey() + "] recipes: " + List.of(Objects.requireNonNull(ReflectionUtils.getResourceLocation(resourceRecipes))));
		});

		//Replace advancement function to random gibberish, don't ask why I used stream :X
		Stream.ofNullable(ReflectionUtils.getAdvancementFunction(advancement)).forEach(function -> {
			//Utils.sendMessage("[" + advancement.getKey() + "] function: " + List.of(Objects.requireNonNull(ReflectionUtils.getAdvancementFunction(advancement))));
			ReflectionUtils.setAdvancementFunction(advancement, Utils.randomString(9, false), Utils.randomString(9, false));
			scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> ReflectionUtils.setAdvancementFunction(advancement, function[0], function[1]), 0L));
			//Utils.sendMessage("[" + advancement.getKey() + "] function: " + List.of(Objects.requireNonNull(ReflectionUtils.getAdvancementFunction(advancement))));
		});

		//Remove from player same amount of xp as advancement will give
		int experience = ReflectionUtils.getAdvancementExperience(advancement);
		if (experience <= 0) { return; }

		scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> preventExperiencePackets.remove(player.getUniqueId()), 0L));
		scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> preventSoundPackets.remove(player.getUniqueId()), 0L));
		scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> player.giveExp(-experience), 0L));
		preventExperiencePackets.putIfAbsent(player.getUniqueId(), "");
		preventSoundPackets.putIfAbsent(player.getUniqueId(), "");
	}
}
