package me.wobbychip.custompotions.custom;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.wobbychip.custompotions.Main;
import me.wobbychip.custompotions.potions.CustomPotion;

public class ScreamerPotion extends CustomPotion {
	public ScreamerPotion() {
		super("amethyst", Material.DRAGON_HEAD, "screamer", Color.fromRGB(0, 0, 0));
		this.setDisplayName("§r§fPotion of Screamer");
		this.setLore(Arrays.asList("§9Drop this on a friend"));
		this.setTippedArrow(true, "§r§fArrow of Screamer");
	}

	public void onPotionConsume(PlayerItemConsumeEvent event) {
		screamerPlayer(event.getPlayer());
	}

	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { screamerPlayer((Player) livingEntity); }
		}
	}

	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
		for (LivingEntity livingEntity : event.getAffectedEntities()) {
			if (livingEntity instanceof Player) { screamerPlayer((Player) livingEntity); }
		}
	}

	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getHitEntity() instanceof Player) {
				screamerPlayer((Player) event.getHitEntity());
			}
		}
	}

	public void screamerPlayer(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2*20, 1));

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        public void run() {
	        	Vector view = getDirection(player.getLocation().getYaw(), 0).normalize().multiply(10);
	        	Location spawn = player.getLocation().clone().add(view.getX(), 0, view.getZ());
	        	spawn.setDirection(spawn.getDirection().multiply(-1));

	        	ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(spawn, EntityType.ARMOR_STAND);
	        	stand.setCanPickupItems(false);
	        	stand.setPersistent(false);
	        	stand.setGravity(false);
	        	stand.setInvulnerable(true);
	        	stand.setInvisible(true);
	        	stand.setCustomName(stand.getUniqueId().toString());
	        	stand.setCustomNameVisible(false);
 
	        	String command = "minecraft:data modify entity @e[type=minecraft:armor_stand,name=" + stand.getCustomName() + ",limit=1] DisabledSlots set value 2039583";
	        	Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);

	        	stand.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
	        	stand.getEquipment().setChestplate(makeColoredArmour(Material.LEATHER_CHESTPLATE, 0));
	        	stand.getEquipment().setLeggings(makeColoredArmour(Material.LEATHER_LEGGINGS, 0));
	        	stand.getEquipment().setBoots(makeColoredArmour(Material.LEATHER_BOOTS, 0));

	        	moveEntity(stand, player, 5);
	        	player.playSound(player, Sound.ENTITY_ENDERMAN_DEATH, 3, 1);
	        }
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
		int[] task = {0, 0};
		Location source = entity.getLocation();
		Location destination = player.getLocation().clone();
		destination.setPitch(0);

		task[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				task[1]++;
				double x = (destination.getX() - source.getX())*((double) task[1]/time);
				double y = (destination.getY() - source.getY())*((double) task[1]/time);
				double z = (destination.getZ() - source.getZ())*((double) task[1]/time);
				entity.teleport(source.clone().add(x, y, z));
				player.teleport(destination);

				if ((task[1]/time) >= 1) { Bukkit.getScheduler().cancelTask(task[0]); }
			}
		}, 0L, 1L);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				entity.remove();
			}
		}, time+5L);
	}
}
