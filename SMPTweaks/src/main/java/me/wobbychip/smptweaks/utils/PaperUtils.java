package me.wobbychip.smptweaks.utils;

public class PaperUtils {
	public static Class<?> EntityLookup;
	public static boolean isPaper = isPaper();

	static {
		if (isPaper) {
			EntityLookup = io.papermc.paper.chunk.system.entity.EntityLookup.class;
		}
	}

	public static boolean isPaper() {
		return (ReflectionUtils.loadClass("com.destroystokyo.paper.ParticleBuilder", false) != null);
	}
}