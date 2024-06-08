package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

public class RecallPotion extends CustomPotion {
	public RecallPotion() {
		super("amethyst", Material.CHORUS_FRUIT, "recall", Color.fromRGB(23, 193, 224));
		this.setDisplayName("§r§fPotion of Recalling");
		this.setLore(List.of("§9Teleports to Spawnpoint"));
		this.setTippedArrow(true, "§r§fArrow of Recalling");
		this.setAllowVillagerTrades(true);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		respawnPlayer(player);
		return true;
	}

	public void respawnPlayer(Player player) {
		Location location = player.getBedSpawnLocation();

		if (location == null) {
			World world = ReflectionUtils.getRespawnWorld(player);
			location = world.getSpawnLocation().clone().add(.5, 0, .5);
			while ((location.getY() >= world.getMinHeight()) && (location.getBlock().getType() == Material.AIR)) { location.setY(location.getY()-1); }
			while ((location.getY() < world.getMaxHeight()) && (location.getBlock().getType() != Material.AIR)) { location.setY(location.getY()+1); }
		}

		location.setDirection(player.getLocation().getDirection());
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(location);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
	}
}
