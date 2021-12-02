package me.wobbychip.autocraft.crafters;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.ReflectionUtils;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;

public class CustomMinecart {
	private Location loc;
	private CustomInventoryCrafting crafting;
	private UUID uuid;
	private Entity minecart;
	private int TaskID;

	public CustomMinecart(Location location, CustomInventoryCrafting crafting) {
		loc = location.clone().add(.5, 0, .5);
		this.crafting = crafting;

		minecart = loc.getWorld().spawnEntity(this.loc, EntityType.MINECART_CHEST);
		uuid = minecart.getUniqueId();
		Main.mmanager.put(uuid, this);
		minecart.setMetadata("AutoCrafter", new FixedMetadataValue(Main.plugin, 0));
		minecart.setPersistent(false);
		minecart.setPortalCooldown(Integer.MIN_VALUE);
		minecart.setInvulnerable(true);
		minecart.setGravity(false);
		minecart.setSilent(true);
		if (!Main.plugin.getConfig().getBoolean("debug")) { hideEntity(); }

		//Put dummy item wich gonna be used to trigger item move event
		StorageMinecart storageMinecart = ((StorageMinecart) minecart);
		ItemStack dummyItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
		ItemMeta meta = dummyItem.getItemMeta();
		meta.setUnbreakable(true);
		dummyItem.setItemMeta(meta);
		storageMinecart.getInventory().setItem(26, dummyItem);

		//Every tick hide minecart on client side if player is atleast 128 block near minecart
		if (!Main.plugin.getConfig().getBoolean("debug")) {
			TaskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable(){
	            public void run() {
	            	hideEntity();
	            }
	        }, 0L, 1L);
		}
	}

	public CustomInventoryCrafting getCrafting() {
		return crafting;
	}

	public Entity getEntity() {
		return minecart;
	}

	public void move(Location location) {
		loc = location.clone().add(.5, 0, .5);
		minecart.teleport(loc);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			public void run() {
				minecart.teleport(loc);
			}
		}, 2L);
	}

	public void clearStorage() {
		StorageMinecart storageMinecart = ((StorageMinecart) minecart);
		storageMinecart.getInventory().clear();
	}

	public void remove() {
		clearStorage();
		Bukkit.getScheduler().cancelTask(TaskID);
		Main.mmanager.remove(uuid);
		minecart.remove();
	}

	public void hideEntity() {
        for (Player player : loc.getWorld().getPlayers()) {
        	double distance = loc.distance(player.getLocation());

            if (distance < 128) {
        		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(minecart.getEntityId());
        		ReflectionUtils.getEntityPlayer(player).b.sendPacket(packet);
            }
        }
	}
}
