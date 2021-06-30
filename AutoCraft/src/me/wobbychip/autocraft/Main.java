package me.wobbychip.autocraft;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.authlib.GameProfile;

import me.wobbychip.autocraft.events.BlockEvents;
import me.wobbychip.autocraft.events.InventoryEvents;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	public static HashMap<String, InventoryManager> inventoryManagers = new HashMap<String, InventoryManager>();
	public static final String Delimiter = "#";

	@Override
	public void onEnable() {
		//Add plugin variable
		Main.plugin = this;
		Main.plugin.saveDefaultConfig();

		//MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		//WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		//EntityPlayer entityPlayer = new EntityPlayer(server, world, new GameProfile(UUID.randomUUID(), "AutoCraft"), new PlayerInteractManager(world));

		//Utilities.DebugInfo("=======================================");
		//Utilities.DebugInfo(entityPlayer.getBukkitEntity().toString());
		//Utilities.DebugInfo("=======================================");

		//Location location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		//entityPlayer.getBukkitEntity().openWorkbench(location, true);

		//Register events
		Bukkit.getPluginManager().registerEvents(new BlockEvents(), Main.plugin);
		Bukkit.getPluginManager().registerEvents(new InventoryEvents(), Main.plugin);
		Utilities.DebugInfo(this.getConfig().getString("enableMessage"));
	}
}