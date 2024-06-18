package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
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

public class NetherRecallPotion extends CustomPotion {
	public ArrayList<UUID> TELEPORT_ALLOW = new ArrayList<>();

	public NetherRecallPotion() {
		super("recall", Material.CRYING_OBSIDIAN, "nether_recall", Color.fromRGB(174, 55, 255));
		this.setDisplayName("§r§fPotion of Nether Recall");
		this.setLore(List.of("§9Teleports to Nether"));
		this.setTippedArrow(true, "§r§fArrow of Nether Recall");
		this.setAllowVillagerTrades(false);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		TELEPORT_ALLOW.add(player.getUniqueId());
		ReflectionUtils.changeDimension(player, World.Environment.NETHER);
		TELEPORT_ALLOW.remove(player.getUniqueId());
		return true;
	}

	//Disable portal creation for OP players
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPortalEvent(PlayerPortalEvent event) {
		if (!TELEPORT_ALLOW.contains(event.getPlayer().getUniqueId()) || !event.getPlayer().isOp()) { return; }
		event.getPlayer().teleport(event.getTo());
		event.setCancelled(true);
	}
}
