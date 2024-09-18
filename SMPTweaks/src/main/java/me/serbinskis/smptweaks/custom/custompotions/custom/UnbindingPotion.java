package me.serbinskis.smptweaks.custom.custompotions.custom;

import me.serbinskis.smptweaks.custom.custompotions.potions.CustomPotion;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Objects;

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
		removeBoundArmour(player);
		return true;
	}

	public void removeBoundArmour(Player player) {
		List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).forEach(equipmentSlot -> {
			ItemStack itemStack = Objects.requireNonNullElseGet(player.getInventory().getItem(equipmentSlot), () -> new ItemStack(Material.AIR));
			if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasEnchant(Enchantment.BINDING_CURSE)) { return; }
			player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
			player.getInventory().setItem(equipmentSlot, new ItemStack(Material.AIR));
		});
	}
}
