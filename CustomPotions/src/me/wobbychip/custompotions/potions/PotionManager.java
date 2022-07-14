package me.wobbychip.custompotions.potions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
	protected Map<CustomPotion, String> waiting = new HashMap<CustomPotion, String>();
	protected Map<String, CustomPotion> potions = new HashMap<String, CustomPotion>();
	protected Map<String, PotionRegistry> registry = new HashMap<String, PotionRegistry>();

	public String getPotions() {
		return String.join(", ", this.potions.keySet());
	}

	public Set<String> getPotionSet() {
		return this.potions.keySet();
	}

	public void registerPotion(CustomPotion potion) {
		//If base of potion is custom and not yet registered, save it for later
		if ((potion.getBase() == null) && !registry.containsKey(potion.getBaseName())) {
			Utils.sendMessage("Potion '" + potion.getName() + "' is waiting for base '" + potion.getBaseName() + "'");
			waiting.put(potion, potion.getBaseName());
			return;
		} else if (potion.getBase() == null) {
			potion.setBase(registry.get(potion.getBaseName()));
		}

		potions.put(potion.getName(), potion);
		PotionRegistry result = registerInstantPotion(potion.getName(), potion.getIntColor());
		registry.put(potion.getName(), result);

		//Register potions that are based on current potion
		Iterator<Entry<CustomPotion, String>> iterator = waiting.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<CustomPotion, String> entry = iterator.next();
			if (entry.getValue().equals(potion.getName())) {
				entry.getKey().setBase(result);
				Utils.sendMessage("Registering '" + entry.getKey().getName() + "' with the base '" + potion.getName() + "'");
				registerPotion(entry.getKey());
				iterator.remove();
			}
		}

		//If potion disabled register it, but don't add brew recipe
		if (!potion.isEnabled()) { return; }
		registerBrewRecipe(potion.getBase(), potion.getMaterial(), result);
	}

	public PotionRegistry getPotionRegistry(CustomPotion potion) {
		return registry.containsKey(potion.getName()) ? registry.get(potion.getName()) : null;
	}

	public PotionRegistry getPotionRegistry(String name) {
		return registry.containsKey(name) ? registry.get(name) : null;
	}

	public CustomPotion getCustomPotion(String name) {
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(Entity entity) {
		if (entity.getType() == EntityType.DRAGON_FIREBALL) { return null; } //This shit has no method getHandle()
		if (potions.containsKey(entity.getCustomName())) { return potions.get(entity.getCustomName()); }
		String name = getPotionTag(entity);
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public CustomPotion getCustomPotion(ItemStack item) {
		if (!Utils.isPotion(item) && !Utils.isTippedArrow(item)) { return null; }
		String name = getPotionTag(item);

		if (potions.containsKey(name)) {
			return potions.get(name);
		}

		if (item.getItemMeta() == null) { return null; }
		name = item.getItemMeta().getLocalizedName();
		return potions.containsKey(name) ? potions.get(name) : null;
	}

	public static String getPotionTag(Object object) {
		//tag.e() -> tag.hasKey()
		//tag.c() -> tag.get()
		//NBTBase.e_() -> NBTBase.asString()
		//item.t() -> getOrCreateTag()

		if (object instanceof Entity) {
			NBTTagCompound tag = new NBTTagCompound();
			ReflectionUtil.getEntity((Entity) object).e(tag);
			return ((tag != null) && tag.e("Potion")) ? tag.c("Potion").e_().replace("minecraft:", "") : "";
		}

		if (object instanceof ItemStack) {
			NBTTagCompound tag = ReflectionUtil.asNMSCopy((ItemStack) object).t();
			return ((tag != null) && tag.e("Potion")) ? tag.c("Potion").e_().replace("minecraft:", "") : "";
		}

		return "";
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
