package me.wobbychip.mendfinity;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static NamespacedKey namespacedKey;

	@Override
	public void onEnable() {
		Main.plugin = this;

		ItemStack mendingBow = Utilities.createBow(Enchantment.MENDING);
		ItemStack infinityBow = Utilities.createBow(Enchantment.ARROW_INFINITE);

		Main.namespacedKey = new NamespacedKey(Main.plugin, "mendfinity");
		ShapedRecipe recipe = new ShapedRecipe(Main.namespacedKey, Utilities.mendifnityBow());

		List<ItemStack> firstBow = Arrays.asList(mendingBow, infinityBow);
		List<ItemStack> secondBow = Arrays.asList(infinityBow, mendingBow);

		recipe.shape("EEE", "FNS", "EEE");
		recipe.setIngredient('E', Material.EXPERIENCE_BOTTLE);
		recipe.setIngredient('N', Material.NETHER_STAR);
		recipe.setIngredient('F', new RecipeChoice.ExactChoice(firstBow));
		recipe.setIngredient('S', new RecipeChoice.ExactChoice(secondBow));

		Bukkit.addRecipe(recipe);
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), Main.plugin);
		Utilities.DebugInfo("&9[Mendfinity] Mendfinity has loaded!");
	}
}