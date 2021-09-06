package me.wobbychip.recallpotion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.PotionUtil;

public class BrewRegister {
	public static MinecraftKey minecraftKey(Material material) {
		return new MinecraftKey(material.getKey().getNamespace(), material.getKey().getKey());
	}

	public static net.minecraft.world.item.ItemStack asNMSCopy(ItemStack itemStack) {
    	try {
    		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    		Class<?> CraftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
    		Method method = CraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
    		return (net.minecraft.world.item.ItemStack) method.invoke(method, itemStack);
    	} catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
        	e.printStackTrace();
        	return null;
		}
	}

	//This function is ignoring legacy items since their anyways are deprecated
	public static Item findItem(Material material) {
		Item[] result = {null};

        //IRegistry.ITEM
        IRegistry.Z.getOptional(minecraftKey(material)).ifPresent((item) -> {
        	result[0] = item;
        });

		return result[0];
	}

	public static PotionRegistry findPotion(PotionType potionType) {
		ItemStack potionItemStack = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) potionItemStack.getItemMeta();
		potionMeta.setBasePotionData(new PotionData(potionType));
		potionItemStack.setItemMeta(potionMeta);

		net.minecraft.world.item.ItemStack item = asNMSCopy(potionItemStack);
		if (item == null) { return null; }

		return PotionUtil.d(item);
	}

	public static Method getRegisterBrewMethod() {
		try {
			Method method = PotionBrewer.class.getDeclaredMethod("a", PotionRegistry.class, Item.class, PotionRegistry.class);
			return method;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean registerBrewRecipe(PotionType base, Material ingredient, PotionType result) {
		Item potionIngredient = findItem(ingredient);
		if (potionIngredient == null) { return false; }

		Method method = getRegisterBrewMethod();
		if (method == null) { return false; }

		PotionRegistry potionBase = findPotion(base);
		if (potionBase == null) { return false; }

		PotionRegistry potionResult = findPotion(result);
		if (potionResult == null) { return false; }

		try {
			method.setAccessible(true);
			method.invoke(method, potionBase, potionIngredient, potionResult);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
