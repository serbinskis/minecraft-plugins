package me.wobbychip.smptweaks.custom.customworld.biomes;

import org.bukkit.World;

public enum CustomWorld {
	NONE("none", false, false, World.Environment.CUSTOM),
	END("end", false, false, World.Environment.THE_END),
	NETHER("nether", false, false, World.Environment.NETHER),
	OVERWORLD("overworld", false, false, World.Environment.NORMAL),
	END_VOID("end_void", true, true, World.Environment.THE_END),
	NETHER_VOID("nether_void", true, true, World.Environment.NETHER),
	OVERWORLD_VOID("overworld_void", true, true, World.Environment.NORMAL);

	private String name;
	private boolean isVoid;
	private boolean isFlat;
	private World.Environment environment;

	CustomWorld(String name, boolean isVoid, boolean isFlat, World.Environment environment) {
		this.name = name;
		this.isVoid = isVoid;
		this.isFlat = isFlat;
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

	public static CustomWorld getCustomType(String string) {
		try {
			return CustomWorld.valueOf(string.toUpperCase());
		} catch (Exception e) { return null; }
	}
}
