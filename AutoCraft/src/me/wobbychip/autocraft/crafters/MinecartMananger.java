package me.wobbychip.autocraft.crafters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MinecartMananger {
	protected Map<UUID, CustomMinecart> minecarts = new HashMap<UUID, CustomMinecart>(); 

	public MinecartMananger() {}

	public void put(UUID uuid, CustomMinecart minecart) {
		if (minecarts.containsValue(minecart)) {
			remove(uuid);
		}
		minecarts.put(uuid, minecart);
	}

	public CustomMinecart get(UUID uuid) {
		return minecarts.containsKey(uuid) ? minecarts.get(uuid) : null;
	}

	public void remove(UUID uuid) {
		if (minecarts.containsKey(uuid)) {
			minecarts.remove(uuid);
		}
	}
}
