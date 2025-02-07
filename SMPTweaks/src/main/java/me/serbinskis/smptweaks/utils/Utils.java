package me.serbinskis.smptweaks.utils;

import me.serbinskis.smptweaks.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

public class Utils {
	public static double ITEM_HEIGHT = 0.25F;
	public static double ITEM_SPAWN_OFFSET = 0.25F;
	public static String delimiter = "#";

	//Send message to console
	public static void sendMessage(Object any) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', String.valueOf(any)));
	}

	//Send message to sender
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	//Send message to action bar
	public static void sendActionMessage(Player player, String message) {
		String text = ChatColor.translateAlternateColorCodes('&', message);
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
	}

	//Generate random range integer
	public static int randomRange(int min, int max) {
		return min + (int) (Math.random() * (max - min+1));
	}

	//Generate random range double
	public static double randomRange(double min, double max) {
		return min + Math.random() * (max - min);
	}

	//Return number after decimal, not precise tho
	public static double afterDecimal(double x) {
		return x - Math.floor(x);
	}

	//Calculate players current EXP amount
	public static int getPlayerExp(Player player) {
		int level = player.getLevel();
		int exp;

		if (level <= 16) {
			exp = (int) (Math.pow(level, 2) + 6*level);
		} else if (level <= 31) {
			exp = (int) (2.5*Math.pow(level, 2) - 40.5*level + 360.0);
		} else {
			exp = (int) (4.5*Math.pow(level, 2) - 162.5*level + 2220.0);
		}

		exp += Math.round(player.getExpToLevel() * player.getExp());
		return exp;
	}

	//Calculate experience reward on death
	public static int getExperienceReward(Player player, boolean dropAllXp) {
		if (player.getGameMode() != GameMode.SPECTATOR) {
			if (dropAllXp) {
				return getPlayerExp(player);
			} else {
				int i = player.getLevel() * 7;
				return Math.min(i, 100);
			}
		}

		return 0;
	}

	//Drop item from player position
	public static void dropItem(Player player, ItemStack item) {
		Location location = player.getLocation();
		location.setY(location.getY()+1.3);

		Vector vector = player.getLocation().getDirection();
		vector.multiply(0.32);

		Item itemDropped = player.getWorld().dropItem(location, item);
		itemDropped.setVelocity(vector);
		itemDropped.setPickupDelay(40);
	}

	public static void dropItems(Player player) {
		Arrays.stream(player.getInventory().getStorageContents()).filter(Objects::nonNull).forEach(itemStack -> {
			player.getWorld().dropItem(player.getLocation(), itemStack);
		});

		Arrays.stream(player.getEnderChest().getStorageContents()).filter(Objects::nonNull).forEach(itemStack -> {
			player.getWorld().dropItem(player.getLocation(), itemStack);
		});

		player.getInventory().clear();
		player.getEnderChest().clear();
	}

	//Check for permissions
	public static boolean hasPermissions(CommandSender sender, String permission) {
		if (sender instanceof Player) {
			return sender.hasPermission(permission);
		}

		return true;
	}

	//Get distance between two locations
	public static double distance(Location l1, Location l2) {
		double x = Math.pow(l1.getX()-l2.getX(), 2);
		double y = Math.pow(l1.getY()-l2.getY(), 2);
		double z = Math.pow(l1.getZ()-l2.getZ(), 2);
		return Math.sqrt(x+y+z);
	}

	//Check if itemstack is a potion
	public static boolean isPotion(ItemStack itemStack) {
		return ((itemStack != null) && ((itemStack.getType() == Material.POTION) || (itemStack.getType() == Material.SPLASH_POTION) || (itemStack.getType() == Material.LINGERING_POTION)));
	}

	//Check if itemstack is a tipped arrow
	public static boolean isTippedArrow(ItemStack itemStack) {
		return ((itemStack != null) && (itemStack.getType() == Material.TIPPED_ARROW));
	}

	//Get material name
	public static String getMaterialName(Material material) {
		return toTitleCase(material.name().replace("_", " "));
	}

	//Convert string to title case
	public static String toTitleCase(String s) {
		if (s == null || s.isEmpty()) { return ""; }
		if (s.length() == 1) { return s.toUpperCase(); }

		String[] parts = s.split(" ");
		StringBuilder sb = new StringBuilder(s.length());

		for (String part : parts) {
			if (part.length() > 1) {
				sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
			} else {
				sb.append(part.toUpperCase());
			}

			sb.append(" ");
		}

		return sb.toString().trim();
	}

	public static String reverseString(String s) {
		return new StringBuilder(s).reverse().toString();
	}

	public static String randomString(int count, boolean uppercase) {
		String s = RandomStringUtils.randomAlphanumeric(count);
		return uppercase ? s.toUpperCase() : s.toLowerCase();
	}

	//Get nearest player to entity
	public static Player getNearetPlayer(Location location) {
		Player best = null;
		double bestDistance = Double.MAX_VALUE;

		for (Player player : location.getWorld().getPlayers()) {
			double distance = location.distance(player.getLocation());

			if (distance < bestDistance) {
				best = player;
				bestDistance = distance;
			}
		}

		return best;
	}

	//Get nearest entities
	public static Collection<Entity> getNearbyEntities(Location location, @Nullable EntityType type, double distance, boolean maxHeight) {
		double height = maxHeight ? location.getWorld().getMaxHeight()*2 : distance;
		Collection<Entity> nearbyEntites = location.getWorld().getNearbyEntities(location, distance, height, distance);
		if (type != null) { nearbyEntites.removeIf(entity -> entity.getType() != type); }
		return nearbyEntites;
	}

	//Get nearest blocks
	public static List<Block> getNearbyBlocks(Location location, @Nullable Material type, double radius) {
		List<Block> nearbyBlock = new ArrayList<>();

		double pX = location.getX();
		double pY = location.getY();
		double pZ = location.getZ();

		for (double x = -radius; x <= radius; x++) {
			for (double y = -radius; y <= radius; y++) {
				for (double z = -radius; z <= radius; z++) {
					Block block = location.getWorld().getBlockAt((int) (pX+x), (int) (pY+y), (int) (pZ+z));
					if ((type == null) || (block.getType() == type)) { nearbyBlock.add(block); }
				}
			}
		}

		return nearbyBlock;
	}

	//Get attacker from entity
	public static Player getAttacker(Entity entity) {
		if (entity instanceof Player) { return ((Player) entity); }

		if ((entity instanceof Projectile)) {
			ProjectileSource attacker = ((Projectile) entity).getShooter();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof AreaEffectCloud)) {
			ProjectileSource attacker = ((AreaEffectCloud) entity).getSource();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof TNTPrimed)) {
			Entity attacker = ((TNTPrimed) entity).getSource();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		if ((entity instanceof Tameable)) {
			AnimalTamer attacker = ((Tameable) entity).getOwner();
			if ((attacker instanceof Player)) { return ((Player) attacker); }
		}

		return null;
	}

	public static void removeItem(Inventory inventory, ItemStack itemStack) {
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack item = inventory.getItem(i);
			if ((item == null) || !item.isSimilar(itemStack)) { continue; }
			item.setAmount(Math.max(item.getAmount()-itemStack.getAmount(), 0));
			if (item.getAmount() <= 0) { item = new ItemStack(Material.AIR); }
			inventory.setItem(i, item);
			return;
		}
	}

	public static ItemStack cloneItem(ItemStack itemStack, int amount) {
		ItemStack itemStack1 = itemStack.clone();
		itemStack1.setAmount(amount);
		return itemStack1;
	}

	//Set glow color entity
	public static void setGlowColor(Entity entity, ChatColor color) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team;

		if (!scoreboard.getTeams().stream().map(Team::getName).toList().contains(Main.PREFIX + color.name())) {
			team = scoreboard.registerNewTeam(Main.PREFIX + color.name());
		} else {
			team = scoreboard.getTeam(Main.PREFIX + color.name());
		}

		team.setColor(color);
		team.addEntry(entity.getUniqueId().toString());
		entity.setGlowing(!color.equals(ChatColor.RESET));
	}

	public static void fillChunk(Chunk chunk, Material material, boolean removeEntity) {
		if (removeEntity) {
			for (Entity entity : chunk.getEntities()) {
				if (entity.getType() != EntityType.PLAYER) {
					try { entity.remove(); } catch (Exception ignored) {}
				}
			}
		}

		int maxY = chunk.getWorld().getMaxHeight();
		int minY = chunk.getWorld().getMinHeight();
		BlockData blockData = material.createBlockData();

		for (int x = 0; x < 16; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = 0; z < 16 ; z++) {
					Block block = chunk.getBlock(x, y, z);
					if (block.getType() == material) { continue; }
					block.setBlockData(blockData, false);
				}
			}
		}
	}

	//Convert location to string
	public static String locationToString(Location location) {
		return location.getWorld().getName() + delimiter + location.getX() + delimiter + location.getY() + delimiter + location.getZ();
	}

	//Convert string to location
	public static Location stringToLocation(String location) {
		String[] splited = location.split(delimiter, 0);
		return new Location(Bukkit.getWorld(splited[0]), Double.parseDouble(splited[1]), Double.parseDouble(splited[2]), Double.parseDouble(splited[3]));
	}

	//Save resource to file
	public static File saveResource(String configPath, String savePath) {
		File file = new File(Main.plugin.getDataFolder() + savePath);

		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				InputStream inputStream = Main.plugin.getResource(configPath);
				Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	public static File saveResource(String savePath, byte[] data) {
		File file = new File(Main.plugin.getDataFolder() + savePath);

		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
				Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	public static void grantAdvancement(Player player, Advancement advancment) {
		AdvancementProgress progress = player.getAdvancementProgress(advancment);
		progress.getRemainingCriteria().forEach(progress::awardCriteria);
	}

	public static void revokeAdvancement(Player player, Advancement advancment) {
		AdvancementProgress progress = player.getAdvancementProgress(advancment);

		for (String criteria : progress.getAwardedCriteria()) {
			progress.revokeCriteria(criteria);
		}
	}

	public static boolean isLeashable(LivingEntity entity) {
		if (entity == null) { return false; }
		if (entity instanceof Player) { return false; }
		if (entity instanceof Shulker) { return false; }
		if (entity instanceof EnderDragon) { return false; }
		if (entity instanceof Wither) { return false; }
		if (entity instanceof Bat) { return false; }
        return !entity.isLeashed();
    }

	public static boolean isMovable(Entity entity) {
		if (!(entity instanceof Sittable)) { return true; }
		return !((Sittable) entity).isSitting();
	}

	public static boolean containsEnchantment(ItemStack item, List<String> enchantments) {
		for (Entry<Enchantment, Integer> entrySet : item.getEnchantments().entrySet()) {
			if (entrySet.getValue() > 0) {
				String[] splitted = entrySet.getKey().getKey().toString().split(":");
				String name = splitted[splitted.length-1].toLowerCase();
				if (enchantments.contains(name)) { return true; }
			}
		}

		return false;
	}

	public static BlockFace getClockWise(BlockFace facing) {
        return switch (facing) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            case EAST -> BlockFace.SOUTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + facing);
        };
	}

	public static BlockFace getCounterClockWise(BlockFace facing) {
        return switch (facing) {
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            case EAST -> BlockFace.NORTH;
            default -> throw new IllegalStateException("Unable to get CCW facing of " + facing);
        };
	}

	public static byte[] getFileHash(String fileUrl) {
		try {
			URL url = new URL(fileUrl);
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			int read;

			try (InputStream inputStream = url.openStream()) {
				byte[] buffer = new byte[8192];
				while ((read = inputStream.read(buffer)) > 0) { digest.update(buffer, 0, read); }
			}

			return digest.digest();
		} catch (IOException | NoSuchAlgorithmException e) {
			return new byte[0];
		}
	}

	public static byte[] getFileHash(byte[] fileData) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(fileData, 0, fileData.length);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			return new byte[0];
		}
	}

	public static float clamp(float value, float min, float max) {
		return value < min ? min : Math.min(value, max);
	}
}
