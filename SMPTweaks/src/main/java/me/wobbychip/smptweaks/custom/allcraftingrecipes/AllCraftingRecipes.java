package me.wobbychip.smptweaks.custom.allcraftingrecipes;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import me.wobbychip.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class AllCraftingRecipes extends CustomTweak {
	public static List<NamespacedKey> recipeKeys = new ArrayList<>();

	public AllCraftingRecipes() {
		super(AllCraftingRecipes.class, false, false);
		this.setDescription("Give all crafting recipes when player joins.");
	}

	public void onEnable() {
		TaskUtils.scheduleSyncDelayedTask(() -> {
			Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
			Stream<Recipe> recipeStream = Stream.generate(() -> null).takeWhile(e -> recipeIterator.hasNext()).map(n -> recipeIterator.next());
			recipeStream.filter(CraftingRecipe.class::isInstance).map(CraftingRecipe.class::cast).forEach(e -> recipeKeys.add(e.getKey()));
		}, 0);

		Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
	}
}
