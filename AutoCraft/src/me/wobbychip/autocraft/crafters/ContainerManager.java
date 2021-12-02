package me.wobbychip.autocraft.crafters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.ReflectionUtils;
import me.wobbychip.autocraft.Utils;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.TileInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.InventoryCraftResult;

public class ContainerManager {
	protected Map<Location, CustomInventoryCrafting> benches = new HashMap<Location, CustomInventoryCrafting>(); 
	protected final EntityPlayer mockPlayer;

	@SuppressWarnings("deprecation")
	public ContainerManager() {
		MinecraftServer server = MinecraftServer.getServer();
		WorldServer world = server.getWorlds().iterator().next();
		mockPlayer = new EntityPlayer(server, world, new GameProfile(UUID.randomUUID(), ""));
		
		mockPlayer.b = new PlayerConnection(server, new NetworkManager(EnumProtocolDirection.b), mockPlayer) {
			@Override
			public void sendPacket(Packet<?> packet) {}
		};
	}

	protected CustomInventoryCrafting put(Location loc, CustomInventoryCrafting cont) {
		CustomInventoryCrafting crafting = benches.get(loc);
		if (cont == null) {
			benches.remove(loc);
			return crafting;
		}
		benches.put(loc, cont);
		return crafting;
	}

	public Location getLocation(Inventory inventory) {
		if (inventory == null) {
			return null;
		}
		if (!(ReflectionUtils.CraftInventory.isInstance(inventory))) {
			return null;
		}
		try {
			Field ic = ReflectionUtils.CraftInventory.getDeclaredField("inventory");
			ic.setAccessible(true);

			Object crafting = ic.get(inventory);
			if (crafting instanceof CustomInventoryCrafting) {
				CustomInventoryCrafting table = (CustomInventoryCrafting) crafting;
				return table.getLocation();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

	public CustomInventoryCrafting get(Location loc) {
		return benches.containsKey(loc) ? benches.get(loc) : null;
	}

	public void remove(Location location) {
		if (benches.containsKey(location)) {
			CustomInventoryCrafting bench = benches.get(location);
			bench.remove();
			benches.remove(location);
		}
	}

	public void stopAll() {
		for (CustomInventoryCrafting inventory : benches.values()) {
			CraftInventoryLoader.save(Main.plugin.getSaveFolder(), inventory);
		}
	}

	public void load(Location location, List<ItemStack> items) {
		// Is 0 really ok as an id?!?
		CustomInventoryCrafting crafting = new CustomInventoryCrafting(location, this, new SelfContainer(0), 3, 3);
		InventoryCraftResult result = new InventoryCraftResult();

		crafting.resultInventory = result;
		crafting.setItems(items);
		benches.put(location, crafting);
	}
	
	public boolean unload(Chunk chunk) {
		Set<Location> locations = new HashSet<Location>();
		boolean found = false;
		for (Location location : benches.keySet()) {
			//Utils.sendMessage(Utils.iTs(location.getChunk().getX()) + " = " + Utils.iTs(chunk.getX()) + "; " + Utils.iTs(location.getChunk().getZ()) + " = " + Utils.iTs(chunk.getZ()) + "; " + location.getWorld().getName() + " = " + chunk.getWorld().getName());
			//Utils.sendMessage(Utils.bTs(location.getChunk().getX() == chunk.getX()) + " " + Utils.bTs(location.getChunk().getZ() == chunk.getZ()) + " " + Utils.bTs(location.getWorld() == chunk.getWorld()));
 			//Utils.sendMessage("in chunk -> " + Utils.bTs(Utils.locationInChunk(location, chunk)));
			if (Utils.locationInChunk(location, chunk)) {
 				//Utils.sendMessage(location.toString());
 				CustomInventoryCrafting crafting = benches.get(location);
				locations.add(location);
				crafting.getMinecart().remove();
				CraftInventoryLoader.save(Main.plugin.getSaveFolder(), crafting);
			}
		}
		found = !locations.isEmpty();
		for (Location location : locations) {
			benches.remove(location);
		}
		return found;
	}

	public void createWorkbench(Location loc) {
		if (benches.containsKey(loc)) {
			return;
		}

		TileInventory tileEntity = new TileInventory(new CustomTileEntityContainerWorkbench(this, loc), new ChatMessage(Main.plugin.getConfig().getString("title"), new Object[0]));
		mockPlayer.openContainer(tileEntity);
		mockPlayer.closeInventory();
	}

	public void openWorkbench(Player player, Location loc, InventoryType type) {
		TileInventory tileEntity = new TileInventory(new CustomTileEntityContainerWorkbench(this, loc), new ChatMessage(Main.plugin.getConfig().getString("title"), new Object[0]));
		EntityPlayer entityPlayer = ReflectionUtils.getEntityPlayer(player);

		//How the fuck do I check for regions
		entityPlayer.openContainer(tileEntity);
	}

	protected static class SelfContainer extends Container {
		private Container container;

		protected SelfContainer(int id) {
			super(null, id);
		}

		protected void setContainer(Container container) {
			this.container = container;
		}

		@Override
		public boolean canUse(EntityHuman entity) {
			return container == null ? false : container.canUse(entity);
		}

		@Override
		public InventoryView getBukkitView() {
			return container == null ? null : container.getBukkitView();
		}
	}
}
