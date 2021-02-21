package me.wobbychip.gmusicjukeboxcrafting;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		if (getServer().getPluginManager().getPlugin("GMusic") == null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[GMusicJukeBoxCrafting] GMusic is missing!"));
			return;
		}

		ItemStack item = new ItemStack(Material.JUKEBOX);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName("§cJukeBox");
		meta.setLocalizedName("GMusic_JB");
		meta.setLore(Arrays.asList("§aJukeBox, which allows", "§aplaying special Music!"));
		item.setItemMeta(meta);

		NamespacedKey key = new NamespacedKey(this, "gmusic_jukebox");
		ShapedRecipe recipe = new ShapedRecipe(key, item);

		List<Material> planks = Arrays.asList(Material.OAK_PLANKS, Material.ACACIA_PLANKS, Material.BIRCH_PLANKS,
											  Material.CRIMSON_PLANKS, Material.DARK_OAK_PLANKS, Material.JUNGLE_PLANKS,
											  Material.SPRUCE_PLANKS, Material.WARPED_PLANKS);

		recipe.shape("PPP", "PNP", "PPP");
		recipe.setIngredient('P', new RecipeChoice.MaterialChoice(planks));
		recipe.setIngredient('N', Material.NETHER_STAR);

		Bukkit.addRecipe(recipe);
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&9[GMusicJukeBoxCrafting] GMusicJukeBoxCrafting has loaded!"));
	}
}