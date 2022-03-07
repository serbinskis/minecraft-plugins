package me.wobbychip.custompotions.potions;

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

import me.wobbychip.custompotions.utils.ReflectionUtil;
import me.wobbychip.custompotions.utils.Utils;
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

	public String getPotions() {
		return String.join(", ", this.potions.keySet());
	}

	public boolean registerPotion(CustomPotion potion) {
		potions.put(potion.getName(), potion);
		PotionRegistry result = registerInstantPotion(potion.getName(), potion.getIntColor());
		registry.put(potion.getName(), result);

		//If potion disabled register it, but don't add brew recipe
		if (!potion.isEnabled()) { return false; }
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
		String name = tag.e("Potion") ? tag.c("Potion").e_().replace("minecraft:", "") : "";
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		//tag.e() -> tag.hasKey()
		//tag.c() -> tag.get()
		//NBTBase.e_() -> NBTBase.asString()
		//item.t() -> getOrCreateTag()

		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		NBTTagCompound tag = ReflectionUtil.asNMSCopy(item).t();
		String name = tag.e("Potion") ? tag.c("Potion").e_().replace("minecraft:", "") : "";

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
		PotionRegistry potion = net.minecraft.world.item.alchemy.PotionUtil.d(nmsItem);
		return (potion != Potions.a) ? potion : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PotionRegistry registerInstantPotion(String name, int color) {
		//IRegistry.W -> MobEffectList
		//IRegistry.W.d() -> keySet()
		//MobEffectInfo.c -> MobEffectInfo.NEUTRAL
		//IRegistry.ab -> PotionRegistry

		int id = IRegistry.W.d().size()+1;
		InstantMobEffect instantMobEffect = new InstantMobEffect(MobEffectInfo.c, color);
		MobEffectList mobEffectList = (MobEffectList) IRegistry.a(IRegistry.W, id, name, instantMobEffect);
		PotionRegistry potionRegistry = new PotionRegistry(new MobEffect[]{new MobEffect(mobEffectList, 1)});
		return (PotionRegistry) IRegistry.a((IRegistry) IRegistry.ab, name, (Object) potionRegistry);
	}

	public static boolean registerBrewRecipe(PotionRegistry base, Material ingredient, PotionRegistry result) {
		Method method = ReflectionUtil.getRegisterBrewMethod();
		if (method == null) { return false; }

		Item potionIngredient = ReflectionUtil.asNMSCopy(new ItemStack(ingredient)).c();
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
