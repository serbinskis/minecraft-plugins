package me.wobbychip.recallpotion.events;

import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.wobbychip.recallpotion.Main;
import me.wobbychip.recallpotion.Utils;

public class PotionEvents implements Listener {
	@EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getItem().getType() != Material.POTION) { return; }
		if (!event.getItem().getItemMeta().getLocalizedName().equals(Main.potionItem.getItemMeta().getLocalizedName())) { return; }
		Utils.respawnPlayer(event.getPlayer());
    }

	@EventHandler(priority=EventPriority.MONITOR)
    public void onPotionSplash(PotionSplashEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getEntity().getItem().getItemMeta() == null) { return; }

		String name = event.getEntity().getItem().getItemMeta().getLocalizedName();
		if ((name == null) || !name.equals(Main.splashPotionItem.getItemMeta().getLocalizedName())) { return; }

		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { Utils.respawnPlayer((Player) livingEntity); }
		}
    }

	@EventHandler(priority=EventPriority.MONITOR)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getEntity().getItem().getItemMeta() == null) { return; }

		String name = event.getEntity().getItem().getItemMeta().getLocalizedName();
		if ((name == null) || !name.equals(Main.lingeringPotionItem.getItemMeta().getLocalizedName())) { return; }

		AreaEffectCloud effectCloud = event.getAreaEffectCloud();
		effectCloud.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 0, 0), false);
		effectCloud.setCustomName(Main.lingeringPotionItem.getItemMeta().getLocalizedName());
		effectCloud.setCustomNameVisible(false);
		effectCloud.setColor(((PotionMeta) Main.lingeringPotionItem.getItemMeta()).getColor());
    }

	@EventHandler(priority=EventPriority.MONITOR)
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		if (event.isCancelled()) { return; }
		String name = event.getEntity().getCustomName();
		if ((name == null) || !name.equals(Main.lingeringPotionItem.getItemMeta().getLocalizedName())) { return; }

		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { Utils.respawnPlayer((Player) livingEntity); }
		}
    }
}