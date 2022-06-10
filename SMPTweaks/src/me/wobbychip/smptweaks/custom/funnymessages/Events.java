package me.wobbychip.smptweaks.custom.funnymessages;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import me.wobbychip.smptweaks.Utils;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (FunnyMessages.messages.size() == 0) { return; }
		int index = Utils.randomRange(0, FunnyMessages.messages.size()-1);
		String message = FunnyMessages.messages.get(index).replace("<player>", event.getEntity().getName());
		Utils.sendMessage(event.getDeathMessage());
		event.setDeathMessage(message);
	}
}
