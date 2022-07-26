package me.wobbychip.smptweaks.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.entity.Villager;

public class PaperUtils {
	public static Class<?> Reputation;
	public static Class<?> ReputationType;
	public static Method v_getRepution = null;
	public static Method v_setRepution = null;
	public static Method r_getRepution = null;
	public static Method r_setRepution = null;
	public static boolean isPaper = isPaper();

	static {
		if (isPaper) {
			Reputation = ReflectionUtils.loadClass("com.destroystokyo.paper.entity.villager.Reputation");
			ReputationType = ReflectionUtils.loadClass("com.destroystokyo.paper.entity.villager.ReputationType");

			v_getRepution = ReflectionUtils.getMethod(Villager.class, "getReputation", UUID.class);
			v_setRepution = ReflectionUtils.getMethod(Villager.class, "setReputation", UUID.class, Reputation);

			r_getRepution = ReflectionUtils.getMethod(Reputation, "getReputation", ReputationType);
			r_setRepution = ReflectionUtils.getMethod(Reputation, "setReputation", ReputationType, int.class);
		}
	}

	public static boolean isPaper() {
		try {
			Class.forName("com.destroystokyo.paper.ParticleBuilder");
			return true;
		} catch (ClassNotFoundException e) {}

		return false;
	}

	public static int getReputation(Villager villager, UUID uuid, String type) {
		try {
			Object reputation = v_getRepution.invoke(villager, uuid);
			if (reputation == null) { return -1; }
			return (int) r_getRepution.invoke(reputation, getReputationType(type));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public static void setReputation(Villager villager, UUID uuid, String type, int amount) {
		try {
			Object reputation = Reputation.getDeclaredConstructor().newInstance();
			r_setRepution.invoke(reputation, getReputationType(type), amount);
			v_setRepution.invoke(villager, uuid, reputation);
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
