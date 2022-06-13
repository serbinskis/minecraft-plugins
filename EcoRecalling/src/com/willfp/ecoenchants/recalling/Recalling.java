package com.willfp.ecoenchants.recalling;

import com.willfp.ecoenchants.enchantments.itemtypes.Spell;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Recalling extends Spell {
    public Recalling() {
        super("recalling");
    }

	@Override
	public boolean onUse(@NotNull Player player, int level, @NotNull PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return false;
        }

		respawnPlayer(player);
		return true;
	}

	public void respawnPlayer(Player player) {
		Location location = player.getBedSpawnLocation();

		if (location == null) {
			World world = Bukkit.getServer().getWorlds().get(0);
			location = world.getSpawnLocation().clone().add(.5, 0, .5);
			while ((location.getY() >= world.getMinHeight()) && (location.getBlock().getType() == Material.AIR)) { location.setY(location.getY()-1); }
			while ((location.getY() < world.getMaxHeight()) && (location.getBlock().getType() != Material.AIR)) { location.setY(location.getY()+1); }
		}

		location.setDirection(player.getLocation().getDirection());
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(location);
	}
}
