package me.serbinskis.smptweaks.custom.noadvancements;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.List;

public class Events implements Listener {
	public static List<String> EXCLUDE_ADVANCEMENT = List.of("recipes/decorations/crafting_table", "blazeandcave:technical/inventory_changed", "dungeons_arise:wda_root");

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerAdvancementCriterionGrantEvent(PlayerAdvancementCriterionGrantEvent event) {
		boolean isFakePlayer = FakePlayer.isFakePlayer(event.getPlayer());
		if (!isFakePlayer && NoAdvancements.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (EXCLUDE_ADVANCEMENT.stream().anyMatch(e -> event.getAdvancement().getKey().toString().contains(e))) { return; }
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		boolean isFakePlayer = FakePlayer.isFakePlayer(event.getPlayer());
		if (!isFakePlayer && NoAdvancements.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (EXCLUDE_ADVANCEMENT.stream().anyMatch(e -> event.getAdvancement().getKey().toString().contains(e))) { return; }
		Utils.revokeAdvancement(event.getPlayer(), event.getAdvancement());
	}

	/*@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		scheduled.removeIf(item -> { TaskUtils.finishTask(item); return true; });
		boolean isFakePlayer = FakePlayer.isFakePlayer(event.getPlayer());
		if (!isFakePlayer && NoAdvancements.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
		if (EXCLUDE_ADVANCEMENT.stream().anyMatch(e -> event.getAdvancement().getKey().toString().contains(e))) { return; }

		Utils.revokeAdvancement(event.getPlayer(), event.getAdvancement());
		hijackAdvancement(event.getPlayer(), event.getAdvancement(), event);
	}*/

	/*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPacketEvent(PacketEvent event) {
		if ((event.getPacketType() == PacketType.SOUND) && (preventSoundPackets.remove(event.getPlayer().getUniqueId()) != null)) {
			Sound sound = ReflectionUtils.getBukkitSoundInfo(event.getPacket()).getKey();
			if (sound.equals(Sound.ENTITY_PLAYER_LEVELUP)) { event.setCancelled(true); }
		}

		if ((event.getPacketType() == PacketType.SET_EXPERIENCE) && (preventExperiencePackets.remove(event.getPlayer().getUniqueId()) != null)) {
			event.setCancelled(true);
		}
	}*/

	/*private static void hijackAdvancement(Player player, Advancement advancement, PlayerAdvancementDoneEvent event) {
		//Utils.sendMessage("hijackAdvancement ->");

		//Disable announce advancements to prevent chat messages
		boolean announceChat = ReflectionUtils.getAnnounceChat(advancement);
		ReflectionUtils.setAnnounceChat(advancement, event, false);
		scheduled.add(TaskUtils.scheduleSyncDelayedTask(() -> ReflectionUtils.setAnnounceChat(advancement, null, announceChat), 0L));

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
	}*/
}
