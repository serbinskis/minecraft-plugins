package me.serbinskis.smptweaks.custom.custompotions.custom;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OverworldRecallPotion extends CustomPotion {
	public ArrayList<UUID> TELEPORT_ALLOW = new ArrayList<>();

	public OverworldRecallPotion() {
		super("nether_recall", Material.GRASS_BLOCK, "overworld_recall", Color.fromRGB(91, 135, 49));
		this.setDisplayName("§r§fPotion of Overworld Recall");
		this.setLore(List.of("§9Teleports to The Overworld"));
		this.setTippedArrow(true, "§r§fArrow of End Overworld");
		this.setAllowVillagerTrades(false);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		TELEPORT_ALLOW.add(player.getUniqueId());
		ReflectionUtils.changeDimension(player, World.Environment.NORMAL);
		TELEPORT_ALLOW.remove(player.getUniqueId());
		return true;
	}

	//Disable portal creation for OP players
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPortalEvent(PlayerPortalEvent event) {
		if (!TELEPORT_ALLOW.contains(event.getPlayer().getUniqueId()) || !event.getPlayer().hasPermission("smptweaks.potions.ignore.portal")) { return; }
		event.getPlayer().teleport(event.getTo());
		event.setCancelled(true);
	}
}
