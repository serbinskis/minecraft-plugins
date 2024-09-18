package me.serbinskis.smptweaks.library.fakeplayer.events;

import me.serbinskis.smptweaks.custom.noadvancements.Events;
import me.serbinskis.smptweaks.custom.noadvancements.NoAdvancements;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerEvents implements Listener {
	public static boolean allowTeleport = false;

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		ReflectionUtils.getAffectedEntities(event).keySet().removeIf(e -> FakePlayer.isFakePlayer(e.getUniqueId()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		ReflectionUtils.getAffectedEntities(event).removeIf(e -> FakePlayer.isFakePlayer(e.getUniqueId()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		if (!FakePlayer.isFakePlayer(event.getPlayer())) { return; }
		if (!allowTeleport) { event.setTo(event.getFrom()); }
		event.setCancelled(allowTeleport); //For some reason this doesn't work
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
		if (!FakePlayer.isFakePlayer(event.getPlayer()) || NoAdvancements.tweak.isEnabled()) { return; }
		new Events().onPlayerAdvancementDoneEvent(event);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		if (FakePlayer.isFakePlayer(event.getEntity())) { FakePlayer.recreateFakePlayer(event.getEntity()); }
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		if (event.getPlayer().isOp()) { return; }
		FakePlayer.getFakes().forEach(e -> event.getPlayer().hidePlayer(Main.plugin, e));
	}
}