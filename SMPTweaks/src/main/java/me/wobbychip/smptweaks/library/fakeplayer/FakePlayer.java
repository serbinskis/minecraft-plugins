package me.wobbychip.smptweaks.library.fakeplayer;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.library.fakeplayer.events.PlayerEvents;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class FakePlayer {
    private static Object advancements;
    private static final HashMap<UUID, Player> fakes = new HashMap<>();
    private static final HashMap<UUID, ArmorStand> stands = new HashMap<>();

    public static void start() {
        Bukkit.getPluginManager().registerEvents(new PlayerEvents(), Main.plugin);
        if (Main.DEBUG_MODE) { TaskUtils.scheduleSyncRepeatingTask(FakePlayer::debugUpdate, 1L, 1L); }
    }

    public static Player addFakePlayer(Location location, boolean addPlayer, boolean hideOnline, boolean hideWorld, boolean addAdvancements, boolean updateChunks) {
        return addFakePlayer(location, UUID.randomUUID(), addPlayer, hideOnline, hideWorld, addAdvancements, updateChunks);
    }

    public static Player addFakePlayer(Location location, UUID uuid, boolean addPlayer, boolean hideOnline, boolean hideWorld, boolean addAdvancements, boolean updateChunks) {
        Player fakePlayer = ReflectionUtils.addFakePlayer(location, uuid, addPlayer, hideOnline, hideWorld);
        if (addAdvancements && (advancements != null)) { ReflectionUtils.setPlayerAdvancements(fakePlayer, advancements); }
        if (updateChunks) { ReflectionUtils.updateFakePlayerChunks(fakePlayer); }
        fakes.put(fakePlayer.getUniqueId(), fakePlayer);
        return fakePlayer;
    }

    public static void removeFakePlayer(Player player) {
        if (!isFakePlayer(player)) { return; }
        ReflectionUtils.removeFakePlayer(player);
        fakes.remove(player.getUniqueId());
    }

    public static void removeFakePlayer(UUID uuid) {
        if (!isFakePlayer(uuid) && !fakes.containsKey(uuid)) { return; }
        ReflectionUtils.removeFakePlayer(fakes.remove(uuid));
    }

    public static boolean isFakePlayer(Player player) {
        return isFakePlayer(player.getUniqueId());
    }

    public static boolean isFakePlayer(UUID uuid) {
        return fakes.containsKey(uuid);
    }

    public static void updateFakePlayerChunks(Player player) {
        if (!isFakePlayer(player)) { return; }
        ReflectionUtils.updateFakePlayerChunks(player);
    }

    public static void teleport(Player player, Location location) {
        if (!isFakePlayer(player)) { return; }
        PlayerEvents.allowTeleport = true;
        player.teleport(location);
        PlayerEvents.allowTeleport = false;
    }

    public static Collection<Player> getFakes() {
        return fakes.values();
    }

    private static void debugUpdate() {
        stands.keySet().removeIf(uuid -> {
            if (fakes.containsKey(uuid)) { return !stands.get(uuid).isValid(); }
            stands.get(uuid).remove();
            return true;
        });

        fakes.forEach(((uuid, player) -> {
            if (stands.containsKey(uuid)) { stands.get(uuid).teleport(player.getLocation()); return; }
            if (!player.isValid()) { return; }

            stands.put(uuid, player.getWorld().spawn(player.getLocation(), ArmorStand.class, armorStand -> {
                armorStand.setGravity(false);
                armorStand.setSilent(false);
                armorStand.setInvulnerable(true);
                armorStand.setGliding(true);
                armorStand.setPersistent(false);
                armorStand.setHelmet(new ItemStack(Material.PLAYER_HEAD));
                Arrays.asList(EquipmentSlot.values()).forEach(e -> armorStand.addEquipmentLock(e, ArmorStand.LockType.REMOVING_OR_CHANGING));
            }));
        }));
    }
}
