package me.wobbychip.smptweaks.custom.removedatapackitems;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;

public class RemoveDatapackItems extends CustomTweak {
	public static CustomTweak tweak;
	public static boolean incendium = false;
	public static boolean stellarity = false;

	public RemoveDatapackItems() {
		super(RemoveDatapackItems.class, true, false);
		RemoveDatapackItems.tweak = this;
		this.setGameRule("doRemoveDatapackItems", true, false);
		this.setDescription("Removes or normalizes custom items from datapacks: Incendium, Stellarity.");
	}

	public void onEnable() {
		incendium = Bukkit.getDatapackManager().getPacks().stream().filter(e -> e.getName().toLowerCase().contains("incendium")).count() > 0;
		stellarity = Bukkit.getDatapackManager().getPacks().stream().filter(e -> e.getName().toLowerCase().contains("stellarity")).count() > 0;

		this.setEnabled(incendium || stellarity);
		if (!this.isEnabled()) { return; }

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
		TaskUtils.scheduleSyncRepeatingTask(() -> normalizeDatapackItems(), 20L, 20L);
	}

	public static void normalizeDatapackItems() {
		for (World world : Bukkit.getWorlds()) {
			if (!RemoveDatapackItems.tweak.getGameRuleBoolean(world)) { continue; }

			for (Entity entity : world.getEntities()) {
				if (entity.getType() != EntityType.DROPPED_ITEM) { continue; }
				if (incendium) { Incendium.processItemEntity((Item) entity); }
			}
		}
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
		return itemStack;
	}
}
