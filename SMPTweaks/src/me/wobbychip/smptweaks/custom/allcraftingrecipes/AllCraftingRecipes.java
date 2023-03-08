package me.wobbychip.smptweaks.custom.allcraftingrecipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class AllCraftingRecipes extends CustomTweak {
	public static List<NamespacedKey> recipeKeys = new ArrayList<>();

	public AllCraftingRecipes() {
		super(AllCraftingRecipes.class, false, false);
		this.setDescription("Give all crafting recipes when a player joins.");
	}

	public void onEnable() {
		Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {
			if (recipe instanceof ShapelessRecipe) {
				recipeKeys.add(((ShapelessRecipe) recipe).getKey());
			}

			if (recipe instanceof ShapedRecipe) {
				recipeKeys.add(((ShapedRecipe) recipe).getKey());
			}
		});
		
		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
