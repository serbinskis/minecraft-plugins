package me.wobbychip.smptweaks.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GameRules implements Listener {
	public HashMap<String, Object> rules = new HashMap<String, Object>();
	public List<String> globals = new ArrayList<>();
	public List<?> allowed = Arrays.asList(Boolean.class, Integer.class);
	private Plugin plugin;

	public GameRules(Plugin plugin) {
		this.plugin = plugin;
	}

	public GameRules register() {
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
		return this;
	}

	public boolean addGameRule(String name, Object value, boolean global) {
		if (!allowed.contains(value.getClass())) { return false; }
		rules.put(name, value);
		if (global) { globals.add(name); }
		return true;
	}

	public boolean removeGameRule(String name) {
		if (!globals.contains(name)) { globals.remove(name); }
		return (rules.remove(name) != null);
	}

	public boolean setGameRule(World world, String name, Object value) {
		if (!rules.containsKey(name)) { return false; }
		Object object = rules.get(name);
		if (!value.getClass().equals(object.getClass())) { return false; }
		if (globals.contains(name)) { world = Bukkit.getWorlds().get(0); }

		if (object.getClass().equals(Boolean.class)) {
			PersistentUtils.setPersistentDataBoolean(world, name, (boolean) value);
			return true;
		}

		if (object.getClass().equals(Integer.class)) {
			PersistentUtils.setPersistentDataInteger(world, name, (int) value);
			return true;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public <T> T getGameRule(World world, String name) {
		if (!rules.containsKey(name)) { return null; }
		Object object = rules.get(name);
		if (globals.contains(name)) { world = Bukkit.getWorlds().get(0); }

		if (object.getClass().equals(Boolean.class)) {
			if (!PersistentUtils.hasPersistentDataBoolean(world, name)) { return (T) object; }
			return (T) Boolean.valueOf(PersistentUtils.getPersistentDataBoolean(world, name));
		}

		if (object.getClass().equals(Integer.class)) {
			if (!PersistentUtils.hasPersistentDataInteger(world, name)) { return (T) object; }
			return (T) Integer.valueOf(PersistentUtils.getPersistentDataInteger(world, name));
		}

		return null;
	}

	public boolean sendGameRule(Player player, String name) {
		Object object = rules.get(name);

		if (object.getClass().equals(Boolean.class)) {
			boolean value = getGameRule(player.getWorld(), name);
			Utils.sendMessage(player, String.format("Gamerule %s is currently set to: %b", name, value));
			return true;
		}

		if (object.getClass().equals(Integer.class)) {
			int value = getGameRule(player.getWorld(), name);
			Utils.sendMessage(player, String.format("Gamerule %s is currently set to: %d", name, value));
			return true;
		}

		return false;
	}

	public boolean queryGameRule(Player player, String name, String value) {
		Object object = rules.get(name);

		if (object.getClass().equals(Boolean.class)) {
			if (!value.equals("true") && !value.equals("false")) { return false; }
			setGameRule(player.getWorld(), name, (boolean) Boolean.valueOf(value));
			Utils.sendMessage(player, String.format("Gamerule %s is now set to: %s", name, value));
			return true;
		}

		if (object.getClass().equals(Integer.class)) {
			if ((value.length()) > 9 || !value.matches("-?\\d+")) { return false; }
			setGameRule(player.getWorld(), name, (int) Integer.valueOf(value));
			Utils.sendMessage(player, String.format("Gamerule %s is now set to: %d", name, Integer.valueOf(value)));
			return true;
		}

		return false;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (!Utils.hasPermissions(event.getPlayer(), "minecraft.command.gamerule")) { return; }
		String[] args = event.getMessage().substring(1).trim().split("\\s+");

		if ((args.length < 2) || !args[0].equals("gamerule")) { return; }
		if (!rules.containsKey(args[1])) { return; }

		if (args.length == 2) {
			if (sendGameRule(event.getPlayer(), args[1])) { event.setCancelled(true); }
		}

		if (args.length == 3) {
			if (queryGameRule(event.getPlayer(), args[1], args[2])) { event.setCancelled(true); }
		}
	}
}
