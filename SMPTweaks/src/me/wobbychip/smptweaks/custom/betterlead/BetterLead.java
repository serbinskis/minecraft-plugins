package me.wobbychip.smptweaks.custom.betterlead;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.Utils;
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;

public class BetterLead extends CustomTweak {
	public static List<Entry<Player, LivingEntity>> updateLeash = new ArrayList<>();
	public static String isUnbreakableLeash = "isUnbreakableLeash";
	public static double MAX_DISTANCE = 100;
	public static double tickCount = 0; //REMOVE THIS LATER

	public BetterLead() {
		super(BetterLead.class.getSimpleName(), false);
	}

	public void onEnable() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				for (Entry<Player, LivingEntity> entry : updateLeash) {
					broadcastLeash(entry.getKey(), entry.getValue());
					Utils.sendMessage(tickCount + " -> 3");
				}

				tickCount++;
				updateLeash.clear();
			}
		}, 1L, 1L);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
		ProtocolLibrary.getProtocolManager().addPacketListener(new ProtocolEvents(Main.plugin));
	}

	public static void setDeltaMovement(LivingEntity holder, LivingEntity target) {
		Location hLocation = holder.getLocation();
		Location tLocation = target.getLocation();

		double f = Utils.distance(hLocation, tLocation);
		double d0 = (hLocation.getX() - tLocation.getX()) / f;
		double d1 = (hLocation.getY() - tLocation.getY()) / f;
		double d2 = (hLocation.getZ() - tLocation.getZ()) / f;

		Vector vector = target.getVelocity().add(new Vector(Math.copySign(d0 * d0 * 0.4D, d0), Math.copySign(d1 * d1 * 0.4D, d1), Math.copySign(d2 * d2 * 0.4D, d2)));
		target.setVelocity(vector);
	}

	public static void broadcastLeash(LivingEntity holder, LivingEntity target) {
		for (Player player : holder.getWorld().getPlayers()) {
			ReflectionUtils.sendPacket(player, new PacketPlayOutAttachEntity(ReflectionUtils.getEntity(target), ReflectionUtils.getEntity(holder)));
		}
	}

	public static boolean isLeashable(LivingEntity entity) {
		if (entity instanceof Player) { return false; }
		if (entity instanceof EnderDragon) { return false; }
		if (entity instanceof Wither) { return false; }
		if (entity instanceof Bat) { return false; }
		return true;
	}
}
