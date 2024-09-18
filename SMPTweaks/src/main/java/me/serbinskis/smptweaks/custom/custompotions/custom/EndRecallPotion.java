package me.serbinskis.smptweaks.custom.custompotions.custom;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class EndRecallPotion extends CustomPotion {
	public EndRecallPotion() {
		super("nether_recall", Material.DRAGON_HEAD, "end_recall", Color.fromRGB(60, 0, 100));
		this.setDisplayName("§r§fPotion of End Recall");
		this.setLore(List.of("§9Teleports to The End"));
		this.setTippedArrow(true, "§r§fArrow of End Recall");
		this.setAllowVillagerTrades(false);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		if (player.getWorld().getEnvironment() == World.Environment.NORMAL) { ReflectionUtils.changeDimension(player, World.Environment.THE_END); }
		if (player.getWorld().getEnvironment() != World.Environment.NORMAL) { Utils.sendActionMessage(player, "Potion can only be used in the overworld."); }
		return true;
	}
}
