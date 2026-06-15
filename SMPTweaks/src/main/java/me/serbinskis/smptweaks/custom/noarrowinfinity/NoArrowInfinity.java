package me.serbinskis.smptweaks.custom.noarrowinfinity;

import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import me.serbinskis.smptweaks.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class NoArrowInfinity extends CustomTweak {
	public final static String TAG_IS_CREATIVE_ONLY = "SMPTWEAKS_IS_CREATIVE_ONLY";
	public final static String TAG_IS_INSTABUILD = "SMPTWEAKS_IS_INSTABUILD";
	public static List<String> infinity = Arrays.asList("mendfinity", "infinity");
	public static boolean DEBUG = false;

	public NoArrowInfinity() {
		super(NoArrowInfinity.class, false, false);
		this.setGameRule("bow_infinity_arrows", true, false);
		this.setDescription("Allows players to use a bow with infinity without arrows.");
	}

	public void onEnable() {
		TaskUtils.scheduleSyncRepeatingTask(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) { checkPlayer(player); }
        }, 1L, 1L);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}

	// Instant build is not creative mode, and it gives different perks to player
	// Such as shooting with no arrows, infinite consumables and infinite durability
	// To prevent everything from above and get only shooting with no arrows
	// Give instant build only to client and not server
	@SuppressWarnings("removal")
	public void checkPlayer(Player player) {
		if (!this.getGameRuleBoolean(player.getWorld())) { return; }
		if (player.getGameMode() == GameMode.CREATIVE) { return; }
		if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) { return; }
		ItemStack mainahnd = player.getInventory().getItemInMainHand();
		ItemStack offhand = player.getInventory().getItemInOffHand();

		// Ignore creative players, they already have instant build
		// Prevent check if any inventory opened, except default players inventory
		// Can't check that because if no any other opened, then default will be always opened

		// Allow instant build only if infinity bow is in main hand or offhand
		// But if it is in offhand check if there is no any other bow or crossbow in mainhand
		// NOTE: serverSide: true -> Allows to draw arrow when looking at block, otherwise it requires looking in far distance
		// NOTE: cannot use true anymore, because that opens instabreak and dupe exploits

		if (isInfinityBow(mainahnd) || (isInfinityBow(offhand) && (mainahnd.getType() != Material.BOW) && (mainahnd.getType() != Material.CROSSBOW))) {
			setInstaBuildTag(player, !hasArrow(player));
			ReflectionUtils.setInstantBuild(player, !hasArrow(player), true, false);
			//if (DEBUG) { Utils.sendMessage(ReflectionUtils.getPlayerAbilities(player).instabuild); }
		} else {
			if (player.getItemInUse() != null) { return; } else { setInstaBuildTag(player, false); }
			ReflectionUtils.setInstantBuild(player, false, true, true);
			//if (DEBUG) { Utils.sendMessage(ReflectionUtils.getPlayerAbilities(player).instabuild); }
		}
	}

	public static void setInstaBuildTag(Player player, boolean instabuild) {
		if (instabuild) { PersistentUtils.setPersistentDataBoolean(player, TAG_IS_INSTABUILD, instabuild); }
		if (!instabuild) { PersistentUtils.removePersistentData(player, TAG_IS_INSTABUILD); }
	}

	public static boolean hasInstaBuildTag(Player player) {
		return PersistentUtils.hasPersistentDataBoolean(player, TAG_IS_INSTABUILD);
	}

	public static boolean isInfinityBow(ItemStack item) {
		if ((item == null) || (item.getType() != Material.BOW)) { return false; }
		return Utils.containsEnchantment(item, infinity);
	}

	public static boolean hasArrow(Player player) {
		if (isArrow(player.getInventory().getItemInOffHand())) { return true; }

		for (ItemStack item : player.getInventory().getStorageContents()) {
			if (isArrow(item)) { return true; }
		}

		return false;
	}

	public static boolean isArrow(ItemStack item) {
		return ((item != null) && ((item.getType() == Material.ARROW) || (item.getType() == Material.TIPPED_ARROW) || (item.getType() == Material.SPECTRAL_ARROW)));
	}
}
