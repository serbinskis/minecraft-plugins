package me.wobbychip.recallpotion.potions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import me.wobbychip.recallpotion.utils.ReflectionUtil;
import me.wobbychip.recallpotion.utils.Utils;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.effect.InstantMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInfo;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.Potions;

public class PotionManager {
	protected Map<String, CustomPotion> potions = new HashMap<String, CustomPotion>();
	protected Map<String, PotionRegistry> registry = new HashMap<String, PotionRegistry>();

	public PotionManager() {}

	public boolean registerPotion(CustomPotion potion) {
		potions.put(potion.getName(), potion);
		PotionRegistry result = registerInstantPotion(potion.getName(), potion.getIntColor());
		registry.put(potion.getName(), result);
		return registerBrewRecipe(potion.getBase(), potion.getMaterial(), result);
	}

	public PotionRegistry getPotionRegistry(CustomPotion potion) {
		return registry.containsKey(potion.getName()) ? registry.get(potion.getName()) : null;
	}

	public CustomPotion getCustomPotion(String name) {
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(Entity entity) {
		if (potions.containsKey(entity.getCustomName())) { return potions.get(entity.getCustomName()); }
		NBTTagCompound tag = new NBTTagCompound();
		ReflectionUtil.getEntity(entity).e(tag);
		String name = tag.hasKey("Potion") ? tag.get("Potion").asString().replace("minecraft:", "") : "";
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		NBTTagCompound tag = ReflectionUtil.asNMSCopy(item).getOrCreateTag();
		String name = tag.hasKey("Potion") ? tag.get("Potion").asString().replace("minecraft:", "") : "";

		if (potions.containsKey(name)) {
			return potions.get(name);
		}

		if (item.getItemMeta() == null) { return null; }
		name = item.getItemMeta().getLocalizedName();
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public static PotionRegistry getPotion(PotionType potionType, boolean extended, boolean upgraded) {
		ItemStack item = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
		item.setItemMeta(potionMeta);

		net.minecraft.world.item.ItemStack nmsItem = ReflectionUtil.asNMSCopy(item);
		if (nmsItem == null) { return null; }

		//Potions.a -> Potions.EMPTY
		PotionRegistry rPotion = net.minecraft.world.item.alchemy.PotionUtil.d(nmsItem);
		return (rPotion != Potions.a) ? rPotion : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PotionRegistry registerInstantPotion(String name, int color) {
		//IRegistry.V -> MobEffectList
		//MobEffectInfo.c -> MobEffectInfo.NEUTRAL
		//IRegistry.aa -> PotionRegistry
		
		int id = IRegistry.V.keySet().size()+1;
		InstantMobEffect instantMobEffect = new InstantMobEffect(MobEffectInfo.c, color);
		MobEffectList mobEffectList = (MobEffectList) IRegistry.a(IRegistry.V, id, name, instantMobEffect);
		PotionRegistry potionRegistry = new PotionRegistry(new MobEffect[]{new MobEffect(mobEffectList, 1)});
		return (PotionRegistry) IRegistry.a((IRegistry) IRegistry.aa, name, (Object) potionRegistry);
	}

	public static boolean registerBrewRecipe(PotionRegistry base, Material ingredient, PotionRegistry result) {
		Method method = ReflectionUtil.getRegisterBrewMethod();
		if (method == null) { return false; }

		Item potionIngredient = ReflectionUtil.asNMSCopy(new ItemStack(ingredient)).getItem();
		if (potionIngredient == null) { return false; }

		try {
			method.invoke(method, base, potionIngredient, result);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}
}
