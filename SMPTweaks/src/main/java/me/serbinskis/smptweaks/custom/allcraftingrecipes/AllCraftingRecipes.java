package me.serbinskis.smptweaks.custom.allcraftingrecipes;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class AllCraftingRecipes extends CustomTweak {
	public static CustomTweak tweak;
	public static List<NamespacedKey> recipeKeys = new ArrayList<>();

	public AllCraftingRecipes() {
		super(AllCraftingRecipes.class, false, false);
		this.setDescription("Give all crafting recipes when player joins.");
		this.setGameRule("doAllCraftingRecipes", true, false);
		AllCraftingRecipes.tweak = this;
	}

	public void onEnable() {
		TaskUtils.scheduleSyncDelayedTask(() -> {
			Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
			Stream<Recipe> recipeStream = Stream.generate(() -> null).takeWhile(e -> recipeIterator.hasNext()).map(n -> recipeIterator.next());
			Stream<CraftingRecipe> craftingRecipeStream = recipeStream.filter(CraftingRecipe.class::isInstance).map(CraftingRecipe.class::cast);
			craftingRecipeStream.filter(e -> !e.getResult().getType().equals(Material.STRUCTURE_VOID)).forEach(e -> recipeKeys.add(e.getKey()));
		}, 0);

		//Filter STRUCTURE_VOID, it is support for Stellarity datapack, since they use special recipes for custom events
		Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
	}
}
