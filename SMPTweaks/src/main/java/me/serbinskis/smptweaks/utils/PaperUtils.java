package me.serbinskis.smptweaks.utils;

public class PaperUtils {
	public static Class<?> EntityLookup;
	public static Class<?> ServerEntityLookup;
	public static boolean isPaper = isPaper();

	static {
		if (isPaper) {
			EntityLookup = ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup.class;
			ServerEntityLookup = ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup.class;
		}
	}

	public static boolean isPaper() {
		return (ReflectionUtils.loadClass("com.destroystokyo.paper.ParticleBuilder", false) != null);
	}
}