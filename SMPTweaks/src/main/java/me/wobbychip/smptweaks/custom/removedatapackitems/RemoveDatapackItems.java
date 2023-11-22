package me.wobbychip.smptweaks.custom.removedatapackitems;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.UUID;

public class RemoveDatapackItems extends CustomTweak {
	public static CustomTweak tweak;
	public static Player fakePlayer;
	public static boolean incendium = false;
	public static boolean stellarity = false;

	public RemoveDatapackItems() {
		super(RemoveDatapackItems.class, true, false);
		this.setGameRule("doRemoveDatapackItems", true, false);
		this.setDescription("Removes or normalizes custom items from datapacks: Incendium, Stellarity.");
		RemoveDatapackItems.tweak = this;
	}

	public void onEnable() {
		incendium = Bukkit.getDatapackManager().getPacks().stream().anyMatch(e -> e.getName().toLowerCase().contains("incendium"));
		stellarity = Bukkit.getDatapackManager().getPacks().stream().anyMatch(e -> e.getName().toLowerCase().contains("stellarity"));

		this.setEnabled(incendium || stellarity);
		if (!this.isEnabled()) { return; }

		fakePlayer = ReflectionUtils.addFakePlayer(new Location(Bukkit.getWorlds().get(0), 0, 0, 0), new UUID(0, 0), false, true, true);
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}

	public static ItemStack normalizeDatapackItem(ItemStack itemStack) {
		ItemMeta oldMeta = itemStack.getItemMeta();
		ItemMeta newMeta = new ItemStack(itemStack.getType()).getItemMeta();
		oldMeta.getEnchants().forEach((k, v) -> newMeta.addEnchant(k, v, true));

		if (itemStack.getType() == Material.SHIELD) {
			BlockStateMeta newBlockMeta = (BlockStateMeta) newMeta;
			Banner oldBanner = (Banner) ((BlockStateMeta) oldMeta).getBlockState();
			Banner newBanner = (Banner) newBlockMeta.getBlockState();

			newBanner.setBaseColor(oldBanner.getBaseColor());
			newBanner.setPatterns(oldBanner.getPatterns());
			newBanner.update();
			newBlockMeta.setBlockState(newBanner);
			newBlockMeta.setDisplayName(oldMeta.getDisplayName());
		}

		if (Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS).contains(itemStack.getType())) {
			LeatherArmorMeta oldLeatherMeta = (LeatherArmorMeta) oldMeta;
			LeatherArmorMeta newLeatherMeta = (LeatherArmorMeta) newMeta;
			newLeatherMeta.setColor(oldLeatherMeta.getColor());
		}

		itemStack.setItemMeta(newMeta);
		return (itemStack.getType() != Material.PLAYER_HEAD) ? itemStack : new ItemStack(Material.AIR);
	}
}
