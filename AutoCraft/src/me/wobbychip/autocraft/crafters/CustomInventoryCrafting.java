package me.wobbychip.autocraft.crafters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.google.common.collect.Sets;

import me.wobbychip.autocraft.ReflectionUtils;
import me.wobbychip.autocraft.crafters.ContainerManager.SelfContainer;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.InventoryCraftResult;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;

/**
 * The important class, this is universal and what makes crafting tables public
 * 
 * @author BananaPuncher714
 */
public class CustomInventoryCrafting extends InventoryCrafting {
	Set<Container> containers = Sets.newHashSet();
	private List<ItemStack> items;
	private UUID id;
	private Location bloc;
	private ContainerManager manager;
	private CustomMinecart minecart;
	protected SelfContainer selfContainer;

	public CustomInventoryCrafting(Location workbenchLoc, ContainerManager manager, SelfContainer container, int i, int j) {
		super(container, i, j);
		id = UUID.randomUUID();
		bloc = workbenchLoc;
		selfContainer = container;
		this.manager = manager;
		minecart = new CustomMinecart(workbenchLoc, this);
		setDefaults();
	}

	private void setDefaults() {
		items = this.getContents();
	}

	@Override
	public void setItem(int index, ItemStack item) {
		// Instead of updating one container, update all the containers
		// That are looking at the table, basically the viewers
		
		items.set(index, item);
		for (Container container : containers) {
			container.a(this);
		}
	}

	@Override
	public ItemStack splitStack(int i, int j) {
		ItemStack itemstack = ContainerUtil.a(items, i, j);
		if (!itemstack.isEmpty()) {
			for (Container container : containers) {
				container.a(this);
			}
		}
		return itemstack;
	}

	// This is to fetch a nice list of Bukkit ItemStacks from the list of NMS ItemStacks
	public List<org.bukkit.inventory.ItemStack> getBukkitItems() {
		List<org.bukkit.inventory.ItemStack> bukkitItems = new ArrayList<org.bukkit.inventory.ItemStack>();
		for (ItemStack item : items) {
			bukkitItems.add(ReflectionUtils.asBukkitCopy(item));
		}
		return bukkitItems;
	}

	public org.bukkit.inventory.ItemStack getResult() {
		// Want to update the result without having to use a real player
		if (this.resultInventory instanceof InventoryCraftResult) {
			CustomContainerWorkbench container = new CustomContainerWorkbench(0, manager.mockPlayer.getBukkitEntity(), bloc, this, (InventoryCraftResult) resultInventory);
			container.a(this);
		}

		if (this.resultInventory != null) {
			return ReflectionUtils.asBukkitCopy(resultInventory.getItem(0));
		}
		return null;
	}

	public void craft() {
		for (int i = 0; i < getContents().size(); i++) {
			org.bukkit.inventory.ItemStack item = ReflectionUtils.asBukkitCopy(getContents().get(i));
			item.setAmount(item.getAmount()-1);
			setItem(i, ReflectionUtils.asNMSCopy(item));
		}
	}

	public boolean addItem(org.bukkit.inventory.ItemStack item) {
		for (int i = 0; i < getContents().size(); i++) {
			if (ReflectionUtils.asBukkitCopy(getContents().get(i)).getType() == Material.AIR) {
				setItem(i, ReflectionUtils.asNMSCopy(item));
				return true;
			}
		}
		return false;
	}

	protected void setItems(List<org.bukkit.inventory.ItemStack> items) {
		int index = 0;
		for (org.bukkit.inventory.ItemStack item : items) {
			this.items.set(index++, ReflectionUtils.asNMSCopy(item));
		}

		// Want to update the result without having to use a real player
		if (this.resultInventory instanceof InventoryCraftResult) {
			CustomContainerWorkbench container = new CustomContainerWorkbench(0, manager.mockPlayer.getBukkitEntity(), bloc, this, (InventoryCraftResult) resultInventory);
			container.a(this);
		}
	}
	
	// Add another viewer
	protected void addContainer(Container container) {
		containers.add(container);
	}
	
	// Remove a container that stopped viewing it
	protected void removeContainer(Container container) {
		containers.remove(container);
	}
	
	protected void setLocation(Location newLoc) {
		bloc = newLoc;
	}
	
	public UUID getUUID() {
		return id;
	}
	
	public Location getLocation() {
		return bloc;
	}

	public CustomMinecart getMinecart() {
		return minecart;
	}
	
	public CustomInventoryCrafting move(Location location) {
		if (manager.get(bloc) == this) {
			manager.benches.remove(bloc);
		}
		bloc = location;
		CustomInventoryCrafting whatsHere = manager.put(location, this);
		minecart.move(location);
		return whatsHere;
	}
	
	public void remove() {
		for (ItemStack item : items) {
			org.bukkit.inventory.ItemStack is = ReflectionUtils.asBukkitCopy(item);
			if (is.getType() != Material.AIR) {
				bloc.getWorld().dropItem(bloc.clone().add(.5, .9, .5), is);
			}
		}
		minecart.remove();
	}
	
	@Override
	public void update() {
		if (bloc.getBlock().getType() != Material.CRAFTING_TABLE) {
			remove();
			manager.benches.remove(bloc);
		} else {
			manager.put(bloc, this);
		}
	}
}

