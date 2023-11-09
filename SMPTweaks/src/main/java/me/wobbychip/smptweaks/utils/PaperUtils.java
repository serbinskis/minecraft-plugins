package me.wobbychip.smptweaks.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.entity.Villager;

public class PaperUtils {
	public static Class<?> Reputation;
	public static Class<?> ReputationType;
	public static Method v_getReputation = null;
	public static Method v_setReputation = null;
	public static Method r_getReputation = null;
	public static Method r_setReputation = null;
	public static boolean isPaper = isPaper();

	static {
		if (isPaper) {
			Reputation = ReflectionUtils.loadClass("com.destroystokyo.paper.entity.villager.Reputation", true);
			ReputationType = ReflectionUtils.loadClass("com.destroystokyo.paper.entity.villager.ReputationType", true);

			v_getReputation = ReflectionUtils.getMethod(Villager.class, "getReputation", UUID.class);
			v_setReputation = ReflectionUtils.getMethod(Villager.class, "setReputation", UUID.class, Reputation);

			r_getReputation = ReflectionUtils.getMethod(Reputation, "getReputation", ReputationType);
			r_setReputation = ReflectionUtils.getMethod(Reputation, "setReputation", ReputationType, int.class);
		}
	}

	public static boolean isPaper() {
		return (ReflectionUtils.loadClass("com.destroystokyo.paper.ParticleBuilder", false) != null);
	}

	public static int getReputation(Villager villager, UUID uuid, String type) {
		try {
			Object reputation = v_getReputation.invoke(villager, uuid);
			if (reputation == null) { return -1; }
			return (int) r_getReputation.invoke(reputation, getReputationType(type));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public static void setReputation(Villager villager, UUID uuid, String type, int amount) {
		try {
			Object reputation = Reputation.getDeclaredConstructor().newInstance();
			r_setReputation.invoke(reputation, getReputationType(type), amount);
			v_setReputation.invoke(villager, uuid, reputation);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			e.printStackTrace();
		}
	}

	public static Object getReputationType(String type) {
		for (Object object : ReputationType.getEnumConstants()) {
			if (object.toString().equalsIgnoreCase(type)) { return object; }
		}

		return null;
	}
}