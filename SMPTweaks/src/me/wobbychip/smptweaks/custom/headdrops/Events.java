package me.wobbychip.smptweaks.custom.headdrops;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Events implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity().getKiller();
		if ((player == null) || player.getUniqueId().equals(player.getUniqueId())) { return; }

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(event.getEntity());
        skullMeta.setOwnerProfile(event.getEntity().getPlayerProfile());
        playerHead.setItemMeta(skullMeta);

		event.getDrops().add(playerHead);
	}
}
