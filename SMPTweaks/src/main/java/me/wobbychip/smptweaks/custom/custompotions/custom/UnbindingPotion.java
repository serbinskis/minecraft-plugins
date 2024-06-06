package me.wobbychip.smptweaks.custom.custompotions.custom;

import me.wobbychip.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.List;

public class UnbindingPotion extends CustomPotion {
	public UnbindingPotion() {
		super(PotionType.AWKWARD, Material.HONEY_BLOCK, "unbinding", Color.fromRGB(255, 128, 0));
		this.setDisplayName("§r§fPotion of Unbinding");
		this.setLore(List.of("§9Drops armour with curse of binding"));
		this.setTippedArrow(true, "§r§fArrow of Unbinding");
		this.setAllowVillagerTrades(true);
	}

	@Override
	public boolean onAffectPlayer(Player player, Event event) {
		removeBindedArmour(player);
		return true;
	}

	public void removeBindedArmour(Player player) {
		ItemStack helmetSlot = player.getInventory().getHelmet();
		if ((helmetSlot != null) && helmetSlot.hasItemMeta() && helmetSlot.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) {
			player.getWorld().dropItemNaturally(player.getLocation(), helmetSlot);
			player.getInventory().setHelmet(new ItemStack(Material.AIR));
			return;
		}

		ItemStack chestplateSlot = player.getInventory().getChestplate();
		if ((chestplateSlot != null) && chestplateSlot.hasItemMeta() && chestplateSlot.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) {
			player.getWorld().dropItemNaturally(player.getLocation(), chestplateSlot);
			player.getInventory().setChestplate(new ItemStack(Material.AIR));
			return;
		}

		ItemStack leggingsSlot = player.getInventory().getLeggings();
		if ((leggingsSlot != null) && leggingsSlot.hasItemMeta() && leggingsSlot.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) {
			player.getWorld().dropItemNaturally(player.getLocation(), leggingsSlot);
			player.getInventory().setLeggings(new ItemStack(Material.AIR));
			return;
		}

		ItemStack bootsSlot = player.getInventory().getBoots();
		if ((bootsSlot != null) && bootsSlot.hasItemMeta() && bootsSlot.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) {
			player.getWorld().dropItemNaturally(player.getLocation(), bootsSlot);
			player.getInventory().setBoots(new ItemStack(Material.AIR));
			return;
		}
	}
}
