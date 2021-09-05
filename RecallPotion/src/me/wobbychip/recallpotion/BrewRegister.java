package me.wobbychip.recallpotion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.potion.PotionType;

import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class BrewRegister {
	public static Item findItem(Material material) {
		String materialKey = material.getKey().toString().replaceFirst("^[^:]*:", "");
		int itemCount = Items.class.getFields().length;

		for (int i = 0; i < itemCount; i++) {
			Item item = Item.getById(i);
			if (item.getName().replaceAll("^.*\\.(.*)$", "$1").equalsIgnoreCase(materialKey)) { return item; }
		}

		return null;
	}

	public static PotionRegistry findPotion(PotionType potion) {
		PotionRegistry rPotion = IRegistry.aa.get(MinecraftKey.a(potion.toString().toLowerCase()));

		if (rPotion.b("").equalsIgnoreCase("empty")) {
			return null;
		} else {
			return rPotion;
		}
	}

	public static boolean isRegisterBrewMethod(Method method) {
		if ((method == null) && (method.getParameterCount() != 3)) { return false; }

		Class<?>[] parameters = method.getParameterTypes();
		if (!parameters[0].getName().replaceAll("^.*\\.(.*)$", "$1").equalsIgnoreCase("PotionRegistry")) { return false; }
		if (!parameters[1].getName().replaceAll("^.*\\.(.*)$", "$1").equalsIgnoreCase("Item")) { return false; }
		if (!parameters[2].getName().replaceAll("^.*\\.(.*)$", "$1").equalsIgnoreCase("PotionRegistry")) { return false; }

		return true;
	}

	public static Method findRegisterBrewMethod() {
		for (Method method : PotionBrewer.class.getDeclaredMethods()) {
			if (isRegisterBrewMethod(method)) { return method; }
		}

		return null;
	}

	public static boolean registerBrewRecipe(PotionType base, Material ingredient, PotionType result ) {
		Item potionIngredient = findItem(ingredient);
		if (potionIngredient == null) { return false; }

		Method method = findRegisterBrewMethod();
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
