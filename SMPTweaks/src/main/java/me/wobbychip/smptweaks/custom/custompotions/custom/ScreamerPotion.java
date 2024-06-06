package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

public class ScreamerPotion extends CustomPotion {
	public ScreamerPotion() {
		super("amethyst", Material.WITHER_SKELETON_SKULL, "screamer", Color.fromRGB(0, 0, 0));
		this.setDisplayName("§r§fPotion of Screamer");
		this.setLore(List.of("§9Drop this on a friend"));
		this.setTippedArrow(true, "§r§fArrow of Screamer");
		this.setAllowVillagerTrades(true);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		screamerPlayer(player);

		if (event instanceof AreaEffectCloudApplyEvent areaEffectCloudApplyEvent) {
			areaEffectCloudApplyEvent.getEntity().setDuration(0);
		}

		return true;
	}

	public void screamerPlayer(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2*20, 1));

		TaskUtils.scheduleSyncDelayedTask(() -> {
			Vector view = getDirection(player.getLocation().getYaw(), 0).normalize().multiply(10);
			Location spawn = player.getLocation().clone().add(view.getX(), 0, view.getZ());
			spawn.setDirection(spawn.getDirection().multiply(-1));

			ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(spawn, EntityType.ARMOR_STAND);
			ReflectionUtils.setDisabledSlots(stand, 2039583);
			stand.setCanPickupItems(false);
			stand.setPersistent(false);
			stand.setGravity(false);
			stand.setInvulnerable(true);
			stand.setInvisible(true);
			stand.setCustomName(stand.getUniqueId().toString());
			stand.setCustomNameVisible(false);

			stand.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
			stand.getEquipment().setChestplate(makeColoredArmour(Material.LEATHER_CHESTPLATE, 0));
			stand.getEquipment().setLeggings(makeColoredArmour(Material.LEATHER_LEGGINGS, 0));
			stand.getEquipment().setBoots(makeColoredArmour(Material.LEATHER_BOOTS, 0));

			moveEntity(stand, player, 5);
			player.playSound(player, Sound.ENTITY_ENDERMAN_DEATH, 3, 1);
		}, 3L);
	}

	public Vector getDirection(float yaw, float pitch) {
		Vector vector = new Vector();
		vector.setY(-Math.sin(Math.toRadians(pitch)));

		double xz = Math.cos(Math.toRadians(pitch));
		vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
		vector.setZ(xz * Math.cos(Math.toRadians(yaw)));

		return vector;
	}

	public ItemStack makeColoredArmour(Material material, int color) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromBGR(color));
		item.setItemMeta(meta);
		return item;
	}

	public void moveEntity(Entity entity, Player player, int time) {
		int[] task = { 0, 0 };
		Location source = entity.getLocation();
		Location destination = player.getLocation().clone();
		destination.setPitch(0);

		task[0] = TaskUtils.scheduleSyncRepeatingTask(() -> {
			task[1]++;
			double x = (destination.getX() - source.getX())*((double) task[1]/time);
			double y = (destination.getY() - source.getY())*((double) task[1]/time);
			double z = (destination.getZ() - source.getZ())*((double) task[1]/time);
			entity.teleport(source.clone().add(x, y, z));
			player.teleport(destination);

			if ((task[1]/time) >= 1) { TaskUtils.cancelTask(task[0]); }
		}, 0L, 1L);

		TaskUtils.scheduleSyncDelayedTask(entity::remove, time+5L);
	}
}
