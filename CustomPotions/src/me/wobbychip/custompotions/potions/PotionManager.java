package me.wobbychip.custompotions.potions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import me.wobbychip.custompotions.utils.ReflectionUtil;
import me.wobbychip.custompotions.utils.Utils;
import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistryMaterials;
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

	public Set<String> getPotionSet() {
		return this.potions.keySet();
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
		String name = ((tag != null) && tag.e("Potion")) ? tag.c("Potion").e_().replace("minecraft:", "") : "";
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		//tag.e() -> tag.hasKey()
		//tag.c() -> tag.get()
		//NBTBase.e_() -> NBTBase.asString()
		//item.t() -> getOrCreateTag()

		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		NBTTagCompound tag = ReflectionUtil.asNMSCopy(item).t();
		String name = ((tag != null) && tag.e("Potion")) ? tag.c("Potion").e_().replace("minecraft:", "") : "";

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
		//MobEffectInfo.c -> MobEffectInfo.NEUTRAL
		//IRegistry.W.d() -> keySet()
		//IRegistry.Y -> PotionRegistry
		//IRegistry.T -> MobEffectList
		//IRegistry.a -> registerMapping(IRegistry<V> iregistry, int i, String s, T t0)
		//j() -> freeze()

		setRegistryFrozen(IRegistry.T, false);
		setRegistryFrozen(IRegistry.Y, false);

		int id = IRegistry.T.d().size()+1;
		InstantMobEffect instantMobEffect = new InstantMobEffect(MobEffectInfo.c, color);
		MobEffectList mobEffectList = (MobEffectList) IRegistry.a(IRegistry.T, id, name, instantMobEffect);
		PotionRegistry potionRegistry = new PotionRegistry(new MobEffect[]{new MobEffect(mobEffectList, 1)});
		potionRegistry =  (PotionRegistry) IRegistry.a((IRegistry) IRegistry.Y, name, (Object) potionRegistry);

		setRegistryFrozen(IRegistry.T, true);
		setRegistryFrozen(IRegistry.Y, true);
		return potionRegistry;
	}

	public static void setRegistryFrozen(Object registry, boolean frozen) {
		//RegistryMaterials.bN (private) -> intrusiveHolderCache
		//RegistryMaterials.bL (private) -> frozen

        try {
            Field field = RegistryMaterials.class.getDeclaredField("bL");
            field.setAccessible(true);
            field.set(registry, frozen);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
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
