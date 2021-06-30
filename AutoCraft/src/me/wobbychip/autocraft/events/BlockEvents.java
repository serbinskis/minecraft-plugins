package me.wobbychip.autocraft.events;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import com.mojang.authlib.GameProfile;

import me.wobbychip.autocraft.InventoryManager;
import me.wobbychip.autocraft.Utilities;
import net.minecraft.server.v1_16_R3.BlockWorkbench;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.ContainerAccess;
import net.minecraft.server.v1_16_R3.ContainerWorkbench;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.InventoryCrafting;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.PlayerInventory;
import net.minecraft.server.v1_16_R3.WorldServer;

import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;

public class BlockEvents implements Listener {
	@EventHandler(ignoreCancelled = true, priority=EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getBlock().getType() != Material.CRAFTING_TABLE) { return; }
		
		//InventoryManager inventoryManager = Utilities.getInventoryManager(event.getBlock().getLocation());
		//inventoryManager.destroyInventory();
	}

    @EventHandler(priority=EventPriority.MONITOR)
	public void onBlockExplode(EntityExplodeEvent event) {
    	for (Block block : event.blockList()) {
    		if (block.getType() == Material.CRAFTING_TABLE) {
    			InventoryManager inventoryManager = Utilities.getInventoryManager(block.getLocation());
    			inventoryManager.destroyInventory();
    		}
        }
    }

	@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }

		if (event.getClickedBlock().getType() == Material.STONE) {
			Utilities.DebugInfo("onPlayerInteract");

			MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
			WorldServer world = ((CraftWorld) event.getClickedBlock().getWorld()).getHandle();
			GameProfile profile = new GameProfile(UUID.randomUUID(), "AutoCraft");
			EntityPlayer entityPlayer = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));

			//entityPlayer.getBukkitEntity().toString();
			//ContainerWorkbench workbench = new ContainerWorkbench(-1, new PlayerInventory(entityPlayer), ContainerAccess.a);
		    //InventoryCrafting crafting = new InventoryCrafting(workbench, 3, 3, new CraftPlayer(world, entityPlayer));

		    //ContainerWorkbench containerWorkbench = new ContainerWorkbench(-1, new PlayerInventory(null));
		    //InventoryCrafting invCrafting = new InventoryCrafting(containerWorkbench, 3, 3, null);
		    //invCrafting.container.setTitle(new ChatComponentText("AutoCraft"));
		    //invCrafting.startOpen(((EntityHuman) ((CraftPlayer) event.getPlayer()).getHandle()));

		    
		    //invCrafting.container.getBukkitView()
		    //event.getPlayer().openInventory(invCrafting.container.getBukkitView());
		    
		    //InventoryView invView = invCrafting.container.getBukkitView();
		    //Utilities.DebugInfo(invView.toString());
		    //event.getPlayer().openInventory(invView);

		    return;
		}

		if (event.getClickedBlock().getType() != Material.CRAFTING_TABLE) { return; }

		event.setCancelled(true);
		if (event.getHand() != EquipmentSlot.HAND) { return; }

		InventoryManager inventoryManager = Utilities.getInventoryManager(event.getClickedBlock().getLocation());
		inventoryManager.openInventory(event.getPlayer());
	}
}
