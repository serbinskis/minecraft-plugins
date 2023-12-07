package me.wobbychip.smptweaks.utils;

import org.bukkit.entity.Villager;

import java.util.UUID;

public class PaperUtils {
	public static Class<?> EntityLookup;
	public static boolean isPaper = isPaper();

	static {
		if (isPaper) {
			EntityLookup = io.papermc.paper.chunk.system.entity.EntityLookup.class;
		}
	}

	public static boolean isPaper() {
		try {
			return (com.destroystokyo.paper.ParticleBuilder.class != null);
		} catch (Exception e) { return false; }
	}

	public static int getReputation(Villager villager, UUID uuid, String type) {
		com.destroystokyo.paper.entity.villager.Reputation reputation = villager.getReputation(uuid);
		return (reputation != null) ? reputation.getReputation(com.destroystokyo.paper.entity.villager.ReputationType.valueOf(type)) : -1;
	}

	public static void setReputation(Villager villager, UUID uuid, String type, int amount) {
		com.destroystokyo.paper.entity.villager.Reputation reputation = new com.destroystokyo.paper.entity.villager.Reputation();
		reputation.setReputation(com.destroystokyo.paper.entity.villager.ReputationType.valueOf(type), amount);
		villager.setReputation(uuid, reputation);
	}
}