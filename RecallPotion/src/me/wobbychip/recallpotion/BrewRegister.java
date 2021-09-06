package me.wobbychip.recallpotion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.bukkit.Material;
import org.bukkit.potion.PotionType;

import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.alchemy.PotionRegistry;

public class BrewRegister {
	public static MinecraftKey minecraftKey(Material material) {
		return new MinecraftKey(material.getKey().getNamespace(), material.getKey().getKey());
	}

	public static MinecraftKey minecraftKey(PotionType potionType) {
		return MinecraftKey.a(potionType.toString().toLowerCase());
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

	//This is not working always since potion names doesn't match
	public static PotionRegistry findPotion(PotionType potionType) {
		PotionRegistry[] result = {null};

        //IRegistry.POTION
        IRegistry.aa.getOptional(minecraftKey(potionType)).ifPresent((potion) -> {
        	result[0] = potion;
        });

		return result[0];
	}

	public static boolean isRegisterBrewMethod(Method method) {
		if ((method == null) || (method.getParameterCount() != 3)) { return false; }

		Type[] parameters = method.getParameterTypes();
		if (!parameters[0].equals(PotionRegistry.class)) { return false; }
		if (!parameters[1].equals(Item.class)) { return false; }
		if (!parameters[2].equals(PotionRegistry.class)) { return false; }

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
