package me.serbinskis.smptweaks.custom.autotrade.events;

import me.serbinskis.smptweaks.library.fakeplayer.FakePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryEvents implements Listener {
    //WHO TF IS CANCELLING MY EVENTS
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (FakePlayer.isFakePlayer(event.getPlayer())) { event.setCancelled(false); }
    }
}
