package me.serbinskis.smptweaks.custom.customworld.biomes;

import org.bukkit.World;

public enum CustomWorld {
	NONE("none", false, false, 0, World.Environment.CUSTOM),
	OVERWORLD("overworld", false, false, -64, World.Environment.NORMAL),
	NETHER("nether", false, false, 0, World.Environment.NETHER),
	END("end", false, false, 0, World.Environment.THE_END),
	OVERWORLD_VOID("overworld_void", true, true, -64, World.Environment.NORMAL),
	NETHER_VOID("nether_void", true, true, 0, World.Environment.NETHER),
	END_VOID("end_void", true, true, 0, World.Environment.THE_END);

	private final String name;
	private final boolean isVoid;
	private final boolean isFlat;
	private final int minY;
	private final World.Environment environment;

	CustomWorld(String name, boolean isVoid, boolean isFlat, int minY, World.Environment environment) {
		this.name = name;
		this.isVoid = isVoid;
		this.isFlat = isFlat;
		this.minY = minY;
		this.environment = environment;
	}

	public boolean isVoid() {
		return isVoid;
	}

	public boolean isFlat() {
		return isFlat;
	}

	public World.Environment getEnvironment() {
		return environment;
	}

	public int getMinY() {
		return minY;
	}

	public static CustomWorld getCustomType(String string) {
		try {
			return CustomWorld.valueOf(string.toUpperCase());
		} catch (Exception e) { return null; }
	}
}
