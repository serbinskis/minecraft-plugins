package me.serbinskis.smptweaks.utils;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.serbinskis.smptweaks.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import javax.annotation.Nullable;
import java.util.*;

public class GameRules implements Listener {
	private static final HashMap<String, Object> rules = new HashMap<>();
	private static final List<String> globals = new ArrayList<>();
	private static final List<?> allowed = Arrays.asList(Boolean.class, Integer.class);
	private static boolean registered = false;

	public static boolean addGameRule(String name, Object value, boolean global) {
		if (!registered) { Bukkit.getPluginManager().registerEvents(new GameRules(), Main.getPlugin()); }
		registered = true;

		if (!allowed.contains(value.getClass())) { return false; }
		rules.put(name, value);
		if (global) { globals.add(name); }
		return true;
	}

	public static boolean removeGameRule(String name) {
		if (!globals.contains(name)) { globals.remove(name); }
		return (rules.remove(name) != null);
	}

	public static boolean setGameRule(World world, String name, Object value) {
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
	public static <T> T getGameRule(@Nullable World world, String name) {
		if (!rules.containsKey(name)) { return null; }
		Object object = rules.get(name);
		if (globals.contains(name)) { world = Bukkit.getWorlds().getFirst(); }
		if (world == null) { throw new RuntimeException("Global gamerule not found!"); }

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

	public static boolean sendGameRule(CommandSender sender, String name) {
		Object object = rules.get(name);

		if (object.getClass().equals(Boolean.class)) {
			boolean value = getGameRule(resolveWorld(sender), name);
			Utils.sendMessage(sender, String.format("Gamerule %s is currently set to: %b", name, value));
			return true;
		}

		if (object.getClass().equals(Integer.class)) {
			int value = getGameRule(resolveWorld(sender), name);
			Utils.sendMessage(sender, String.format("Gamerule %s is currently set to: %d", name, value));
			return true;
		}

		return false;
	}

	public static boolean queryGameRule(CommandSender sender, String name, String value) {
		Object object = rules.get(name);

		if (object.getClass().equals(Boolean.class)) {
			if (!value.equals("true") && !value.equals("false")) { return false; }
			setGameRule(resolveWorld(sender), name, Boolean.valueOf(value));
			Utils.sendMessage(sender, String.format("Gamerule %s is now set to: %s", name, value));
			return true;
		}

		if (object.getClass().equals(Integer.class)) {
			if ((value.length()) > 9 || !value.matches("-?\\d+")) { return false; }
			setGameRule(resolveWorld(sender), name, Integer.valueOf(value));
			Utils.sendMessage(sender, String.format("Gamerule %s is now set to: %d", name, Integer.valueOf(value)));
			return true;
		}

		return false;
	}

	public static World resolveWorld(CommandSender sender) {
		if (sender instanceof Entity entity) { return entity.getWorld(); }
		if (sender instanceof BlockCommandSender cb) { return cb.getBlock().getWorld(); }
		return Bukkit.getWorlds().getFirst();
	}

	public static void registerGruleCommand() {
		Main.getPlugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
			final Commands commands = event.registrar();
			var gruleRoot = Commands.literal("grule").requires(source -> source.getSender().hasPermission("minecraft.command.gamerule"));

			// Argument 1: Rule Name (suggests dynamically from map)
			gruleRoot.then(Commands.argument("gamerule", StringArgumentType.word())
				.suggests((ctx, builder) -> {
					String remaining = builder.getRemaining().toLowerCase();
					rules.keySet().stream().filter(rule -> rule.toLowerCase().startsWith(remaining)).forEach(builder::suggest);
					return builder.buildFuture();
				})
				// Runs when typing: /grule <rule>
				.executes(ctx -> {
					String ruleName = StringArgumentType.getString(ctx, "gamerule");
					CommandSender sender = ctx.getSource().getSender();
					if (!rules.containsKey(ruleName)) { sender.sendMessage("§cUnknown gamerule: " + ruleName); return 0; }
					GameRules.sendGameRule(sender, ruleName);
					return 1;
				})

			// Argument 2: Value (optional)
			.then(Commands.argument("value", StringArgumentType.word())
				.suggests((ctx, builder) -> {
					String ruleName = StringArgumentType.getString(ctx, "gamerule");
					Object val = rules.get(ruleName);
					if (val instanceof Boolean) { builder.suggest("true").suggest("false"); }
					return builder.buildFuture();
				})
				// Runs when typing: /grule <rule> <value>
				.executes(ctx -> {
					String ruleName = StringArgumentType.getString(ctx, "gamerule");
					String valueStr = StringArgumentType.getString(ctx, "value");
					CommandSender sender = ctx.getSource().getSender();
					if (!rules.containsKey(ruleName)) { sender.sendMessage("§cUnknown gamerule: " + ruleName); return 0; }

					Object defaultValue = rules.get(ruleName);
					World world = GameRules.resolveWorld(sender);
					var result = GameRules.queryGameRule(sender, ruleName, valueStr);
					if (!result) { sender.sendMessage("§cIncorrect value!"); return 0; }
					return 1;
				})
			));

			commands.register(gruleRoot.build(), "Manage custom gamerules");
		});
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
