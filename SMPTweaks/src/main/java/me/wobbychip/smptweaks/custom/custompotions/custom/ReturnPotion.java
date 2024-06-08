package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

public class ReturnPotion extends CustomPotion {
	public ReturnPotion() {
		super("recall", Material.ENDER_EYE, "return", Color.fromRGB(129, 111, 179));
		this.setDisplayName("§r§fPotion of Return");
		this.setLore(List.of("§9Teleports to Deathpoint"));
		this.setTippedArrow(true, "§r§fArrow of Return");
		this.setAllowVillagerTrades(true);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		returnPlayer(player);
		return true;
	}

	public void returnPlayer(Player player) {
		Location location = player.getLastDeathLocation();
		if (location == null) { return; }

		location.setDirection(player.getLocation().getDirection());
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
	}
}
