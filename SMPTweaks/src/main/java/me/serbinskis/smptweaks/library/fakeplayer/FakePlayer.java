package me.serbinskis.smptweaks.library.fakeplayer;

import me.serbinskis.smptweaks.library.fakeplayer.events.PlayerEvents;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class FakePlayer {
    private static final String TAG_FAKE_PLAYER_LOCATION = "TAG_FAKE_PLAYER_LOCATION";
    private static final String TAG_FAKE_PLAYER_ADD_PLAYER = "TAG_FAKE_PLAYER_ADD_PLAYER";
    private static final String TAG_FAKE_PLAYER_HIDE_ONLINE = "TAG_FAKE_PLAYER_HIDE_ONLINE";
    private static final String TAG_FAKE_PLAYER_HIDE_WORLD = "TAG_FAKE_PLAYER_HIDE_WORLD";
    private static final String TAG_FAKE_PLAYER_ADD_ADVANCEMENTS = "TAG_FAKE_PLAYER_ADD_ADVANCEMENTS";
    private static final String TAG_FAKE_PLAYER_UPDATE_CHUNKS = "TAG_FAKE_PLAYER_UPDATE_CHUNKS";
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

        PersistentUtils.setPersistentDataString(fakePlayer, TAG_FAKE_PLAYER_LOCATION, Utils.locationToString(location));
        PersistentUtils.setPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_ADD_PLAYER, addPlayer);
        PersistentUtils.setPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_HIDE_ONLINE, hideOnline);
        PersistentUtils.setPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_HIDE_WORLD, hideWorld);
        PersistentUtils.setPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_ADD_ADVANCEMENTS, addAdvancements);
        PersistentUtils.setPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_UPDATE_CHUNKS, updateChunks);

        fakes.put(fakePlayer.getUniqueId(), fakePlayer);
        return fakePlayer;
    }

    public static Player recreateFakePlayer(Player player) {
        return recreateFakePlayer(player.getUniqueId());
    }

    public static Player recreateFakePlayer(UUID uuid) {
        Player fakePlayer = removeFakePlayer(uuid);
        if (fakePlayer == null) { return null; }

        Location location = Utils.stringToLocation(PersistentUtils.getPersistentDataString(fakePlayer, TAG_FAKE_PLAYER_LOCATION));
        boolean addPlayer = PersistentUtils.getPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_ADD_PLAYER);
        boolean hideOnline = PersistentUtils.getPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_HIDE_ONLINE);
        boolean hideWorld = PersistentUtils.getPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_HIDE_WORLD);
        boolean addAdvancements = PersistentUtils.getPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_ADD_ADVANCEMENTS);
        boolean updateChunks = PersistentUtils.getPersistentDataBoolean(fakePlayer, TAG_FAKE_PLAYER_UPDATE_CHUNKS);
        return addFakePlayer(location, fakePlayer.getUniqueId(), addPlayer, hideOnline, hideWorld, addAdvancements, updateChunks);
    }

    public static Player removeFakePlayer(Player player) {
        if (!isFakePlayer(player)) { return null; }
        ReflectionUtils.removeFakePlayer(player);
        return fakes.remove(player.getUniqueId());
    }

    public static Player removeFakePlayer(UUID uuid) {
        if (!isFakePlayer(uuid) && !fakes.containsKey(uuid)) { return null; }
        return ReflectionUtils.removeFakePlayer(fakes.remove(uuid));
    }

    public static boolean isFakePlayer(HumanEntity player) {
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
