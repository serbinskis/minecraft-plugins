package me.wobbychip.chunkloader;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import me.wobbychip.chunkloader.events.BlockEvents;
import me.wobbychip.chunkloader.events.EntityEvents;
import me.wobbychip.chunkloader.events.InventoryEvents;
import me.wobbychip.chunkloader.events.WorldEvents;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static Config LoadersConfig;
	public static Config ChunksConfig;
	public static HashMap<String, ParticleManager> particleManagers = new HashMap<String, ParticleManager>();

	//Add config
	public void AddConfig() {
		Main.plugin.saveDefaultConfig();
		Main.LoadersConfig = new Config("chunkloaders.yml");
		Main.ChunksConfig = new Config("chunks.yml");
	}

	//Add crafting recipe
	public void AddCrafting() {
		NamespacedKey key = new NamespacedKey(Main.plugin, "chunk_loader");
		ShapedRecipe recipe = new ShapedRecipe(key, Utilities.ChunkLoaderItem());

		recipe.shape("SSS", "SNS", "SSS");
		recipe.setIngredient('S', Material.CHISELED_STONE_BRICKS);
		recipe.setIngredient('N', Material.NETHER_STAR);
		Bukkit.addRecipe(recipe);
	}

	@Override
	public void onEnable() {
		//Add plugin variable
		Main.plugin = this;

		//Add crafting
		AddCrafting();

		//Add config
		AddConfig();

		//Load worlds and chunks and check chunk loader existance
		Utilities.LoadWorlds();
		Utilities.CheckChunkLoaders();
		Utilities.LoadChunks();

		//Register events
		Bukkit.getPluginManager().registerEvents(new BlockEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new WorldEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new EntityEvents(), Main.plugin);
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("enableMessage")));
	}
}