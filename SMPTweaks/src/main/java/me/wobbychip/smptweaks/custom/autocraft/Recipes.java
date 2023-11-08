package me.wobbychip.smptweaks.custom.autocraft;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.FireworkMeta;

public class Recipes {
	public static ItemStack getCraftResult(List<ItemStack> items) {
		if (items.size() != 9) { return null; }
		ItemStack[] craftingItems = new ItemStack[9];

		for (int i = 0; i < items.size(); i++) {
			craftingItems[i] = (items.get(i) == null) ? new ItemStack(Material.AIR) : items.get(i);
		}

		Recipe recipe = Bukkit.getCraftingRecipe(craftingItems, Bukkit.getWorlds().get(0));
		if ((recipe == null) || recipe.getResult().getType().isAir()) { return null; }

		ItemStack result = recipe.getResult();
		if (result.getType() == Material.FIREWORK_ROCKET) { getFireworkResult(Arrays.asList(craftingItems), result); }
		return result;
	}

	public static void getFireworkResult(List<ItemStack> items, ItemStack result) {
		ItemStack gunpowder = new ItemStack(Material.GUNPOWDER);
		int count = (int) items.stream().filter(i -> i.isSimilar(gunpowder)).count();

		FireworkMeta fireworkMeta = (FireworkMeta) result.getItemMeta();
		fireworkMeta.setPower((count <= 3 && count >= 1) ? count : 1);
		if ((count <= 3 && count >= 1)) { result.setAmount(3); }
		result.setItemMeta(fireworkMeta);
	}
}
