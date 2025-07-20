package me.serbinskis.smptweaks.utils;

import io.papermc.paper.entity.Leashable;
import org.bukkit.entity.*;

public class PaperUtils {
	private static final boolean isPaper = ReflectionUtils.loadClass("com.destroystokyo.paper.ParticleBuilder", false) != null;

	public static boolean isPaper() {
		return isPaper;
	}

	public static boolean isLeashable(Entity entity) {
		if (!(entity instanceof Leashable leashable)) { return false; }
		if (entity instanceof Shulker) { return false; }
		if (entity instanceof EnderDragon) { return false; }
		if (entity instanceof Wither) { return false; }
		return !leashable.isLeashed();
	}
}