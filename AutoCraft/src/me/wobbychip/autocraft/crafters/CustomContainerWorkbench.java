package me.wobbychip.autocraft.crafters;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;

import me.wobbychip.autocraft.Utils;
import me.wobbychip.autocraft.Main;
import me.wobbychip.autocraft.ReflectionUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerWorkbench;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.inventory.InventoryCraftResult;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SlotResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.level.World;

/**
 * @author BananaPuncher714
 */
public class CustomContainerWorkbench extends ContainerWorkbench {
	protected InventoryCraftResult resultInventory;
	protected CustomInventoryCrafting craftInventory;
	protected ContainerAccess containerAccess;
	protected World world;
	protected HumanEntity viewer;
	private List<Slot> theseSlots;
	
	@SuppressWarnings("unchecked")
	public CustomContainerWorkbench(int id, HumanEntity player, Location blockLocation, CustomInventoryCrafting crafting, InventoryCraftResult result) {
		super(id, ReflectionUtils.getEntityHuman(player).getInventory());

		try {
			Field slots = this.getClass().getField("i");
			theseSlots = (List<Slot>) slots.get(this);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		theseSlots.clear();
		
		try {
			Field slots = Container.class.getField("k");
			((NonNullList<ItemStack>) slots.get(this)).clear();
			slots = Container.class.getField("n");
			((NonNullList<ItemStack>) slots.get(this)).clear();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	
		containerAccess = ContainerAccess.a;
		viewer = player;
		resultInventory = result;
		craftInventory = crafting;
	
		world = ReflectionUtils.getWorld(blockLocation.getWorld());

		a(new SlotResult(ReflectionUtils.getEntityHuman(player), craftInventory, resultInventory, 0, 124, 35));
		for (int i = 0; i <3; i++) {
			for (int j = 0; j <3; j++) {
				a(new Slot(craftInventory, j + i * 3, 30 + j * 18, 17 + i * 18));
			}
		}
		for (int i = 0; i <3; i++) {
			for (int j = 0; j <9; j++) {
				a(new Slot(ReflectionUtils.getEntityHuman(player).getInventory(), j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for (int i = 0; i <9; i++) {
			a(new Slot(ReflectionUtils.getEntityHuman(player).getInventory(), i, 8 + i * 18, 142));
		}
		a(craftInventory);
	}

	public void setInventoryCrafting(CustomInventoryCrafting crafting) {
		craftInventory = crafting;
	}

	@Override
	public boolean a(ItemStack itemstack, Slot slot) {
		return isNotResultSlot(slot);
	}

	@Override
	public void a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
		craftInventory.selfContainer.setContainer(this);
		super.a(i, j, inventoryclicktype, entityhuman);
	}

	@Override
	public void a(IInventory iinventory) {
		// Crafting
		a(this, world, ReflectionUtils.getEntityHuman(viewer), craftInventory, resultInventory);
		//Utils.sendMessage(ReflectionUtils.asBukkitCopy(resultInventory.getItem(0)).getType().toString());
		//Try adding here result
	}

	public boolean isNotResultSlot(Slot slot) {
		return slot.c != resultInventory;
	}

	/**
	 * This might be for when the inventory closes?
	 */
	@Override
	public void b(EntityHuman entity) {
		craftInventory.selfContainer.setContainer(this);
		super.b(entity);
		// Make sure the craft inventory stops watching this container
		craftInventory.removeContainer(this);

		if (!world.y && Main.dropItem) {
			a(entity, craftInventory);
			a(craftInventory);
			craftInventory.update();
		}
	}
	
	@Override
	public boolean canUse(EntityHuman entity) {
		if (!checkReachable) {
			return true;
		}
		// Should make more efficient
		return craftInventory.getLocation().getBlock().getType() == Utils.getWorkbenchMaterial();
	}

	protected HumanEntity getViewer() {
		return viewer;
	}

	@Override
	public void a(AutoRecipeStackManager manager) {
		craftInventory.a(manager);
	}

	@Override
	public boolean a(IRecipe<? super InventoryCrafting> irecipe) {
		return irecipe.a(craftInventory, ReflectionUtils.getEntityHuman(viewer).t);
	}

	@Override
	public void l() {
		resultInventory.clear();
		craftInventory.clear();
	}
	
	// getResultSlotIndex
	@Override
	public int m() {
		return 0;
	}

	// getGridWidth
	@Override
	public int n() {
		return craftInventory.g();
	}

	// getGridHeight
	@Override
	public int o() {
		return craftInventory.f();
	}
}
